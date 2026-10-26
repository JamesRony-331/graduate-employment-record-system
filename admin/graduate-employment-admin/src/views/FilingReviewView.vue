<script setup>
import
{
  onBeforeRouteLeave
}
from 'vue-router';
import
{
  computed,
  onMounted,
  onBeforeUnmount,
  ref,
  watch
}
from 'vue';
import
{
  ElTabs,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElDialog,
  ElEmpty,
  ElSelect,
  ElOption,
  ElMessage,
  ElMessageBox
}
from 'element-plus';
import
{
  Refresh,
  Search
}
from '@element-plus/icons-vue';
import
{
  reviewFilings,
  submitReview
}
from '../api/filingReview';
import
{
  filingStatus,
  applicationType,
  parseFilingJson,
  filterFilings,
  reviewPayload
}
from '../api/filingReviewContracts';
const tab = ref('pending-review'),
  rows = ref([]),
  loading = ref(false),
  error = ref('');
const keyword = ref(''),
  classId = ref(null),
  page = ref(1),
  size = ref(10),
  selected = ref(null),
  opened = ref(false);
const opinion = ref(''),
  reviewError = ref(''),
  submitting = ref(false),
  stale = ref(false);
const canReview = computed(() => tab.value === 'pending-review' && [10, 20].includes(Number(selected
  .value?.status)));

function closeDialog(done)
{
  if (!submitting.value)
    done();
}
async function audit(result)
{
  if (submitting.value || stale.value || !canReview.value)
    return;
  reviewError.value = '';
  let body;
  try
  {
    body = reviewPayload(selected.value, result, opinion.value);
  }
  catch (e)
  {
    reviewError.value = e.message;
    return;
  }
  const message = result === 2 ? '打回后学生需要补充资料并重新提交申请。' : body.fromStatus === 10 ? '通过后将转交院系复审。' :
    '通过后该备案将自动归档。';
  submitting.value = true;
  try
  {
    try
    {
      await ElMessageBox.confirm(
        `${selected.value.studentName} · ${selected.value.applicationNo}。${message}`, result ===
        2 ? '确认打回？' : '确认审核通过？',
        {
          type: 'warning',
          confirmButtonText: result === 2 ? '确认打回' : '确认通过',
          cancelButtonText: '取消'
        });
    }
    catch
    {
      return;
    }
    await submitReview(body);
    opened.value = false;
    ElMessage.success(result === 2 ? '备案已打回' : body.fromStatus === 10 ? '初审通过，已转交院系复审' :
      '复审通过，备案已归档');
    await load();
  }
  catch (e)
  {
    reviewError.value = e.message;
    // Conflict or uncertain transport outcome must be reconciled before retrying.
    stale.value = true;
  }
  finally
  {
    submitting.value = false;
  }
}
async function reconcile()
{
  opened.value = false;
  await load();
}
let sequence = 0,
  alive = true;
const classes = computed(() => [...new Map(rows.value.filter(r => r.classId != null).map(r => [r
  .classId,
  {
    id: r.classId,
    name: r.className || `班级 ID：${r.classId}`
  }
])).values()]);
const filtered = computed(() => filterFilings(rows.value, keyword.value, classId.value));
const pages = computed(() => Math.max(1, Math.ceil(filtered.value.length / size.value)));
const visible = computed(() => filtered.value.slice((page.value - 1) * size.value, page.value * size
  .value));
const materials = computed(() => parseFilingJson(selected.value?.materials, true));
const destination = computed(() => parseFilingJson(selected.value?.destinationDetails));
const sections = [
{
  title: '学生与申请',
  fields: [
    ['申请编号', 'applicationNo'],
    ['申请类型', 'applicationType'],
    ['姓名', 'studentName'],
    ['学号', 'studentNo'],
    ['学院', 'college'],
    ['专业', 'major'],
    ['班级', 'className'],
    ['毕业年份', 'graduationYear'],
    ['提交时间', 'submittedAt'],
    ['归档时间', 'archivedAt'],
    ['前次备案 ID', 'previousFilingId'],
    ['变更原因', 'changeReason']
  ]
},
{
  title: '就业资料',
  fields: [
    ['毕业去向编码', 'destinationType'],
    ['就业单位', 'companyName'],
    ['统一社会信用代码', 'companyCreditCode'],
    ['单位行业', 'companyIndustry'],
    ['单位性质', 'companyNature'],
    ['就业岗位', 'jobTitle'],
    ['工作省份', 'workProvince'],
    ['工作城市', 'workCity'],
    ['工作地址', 'workAddress'],
    ['月薪（元）', 'monthlySalary'],
    ['签约日期', 'signedDate']
  ]
},
{
  title: '联系方式与备注',
  fields: [
    ['联系电话', 'contactPhone'],
    ['联系邮箱', 'contactEmail'],
    ['学生备注', 'remark']
  ]
}, ];

function display(row, key)
{
  const v = row?.[key];
  if (v == null || v === '')
    return '未填写';
  if (key === 'applicationType')
    return applicationType(v);
  if (key === 'submittedAt' || key === 'archivedAt')
    return String(v).replace('T', ' ').slice(0,
      19);
  if (key === 'monthlySalary')
    return Number.isFinite(Number(v)) ? Number(v).toLocaleString('zh-CN',
    {
      minimumFractionDigits: 2
    }) : String(v);
  return String(v);
}

function detail(row)
{
  selected.value = row;
  opinion.value = '';
  reviewError.value = '';
  stale.value = false;
  opened.value = true;
}
async function load()
{
  const current = ++sequence;
  loading.value = true;
  error.value = '';
  rows.value = [];
  try
  {
    const data = await reviewFilings(tab.value);
    if (alive && current === sequence)
      rows.value = data;
  }
  catch (e)
  {
    if (alive && current === sequence)
      error.value = e.message;
  }
  finally
  {
    if (alive && current === sequence)
      loading.value = false;
  }
}

function changeTab()
{
  keyword.value = '';
  classId.value = null;
  page.value = 1;
  selected.value = null;
  opened.value = false;
  load();
}
watch([keyword, classId, size], () =>
{
  page.value = 1;
});
watch(pages, n =>
{
  if (page.value > n)
    page.value = n;
});
onBeforeRouteLeave(() => !submitting.value);
onMounted(load);
onBeforeUnmount(() =>
{
  alive = false;
  sequence++;
});
</script>
<template>
<div class="page-stack filing-review">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">备案审核</h1>
      <p class="subtext">查看本人待审核申请与管理范围内的归档资料。</p>
    </div>
    <ElButton
      :icon="Refresh"
      :loading="loading"
      @click="load"
    >刷新列表</ElButton>
  </header>
  <section
    class="surface"
    :aria-busy="loading"
  >
    <ElTabs
      v-model="tab"
      @tab-change="changeTab"
    >
      <ElTabPane
        label="待我审核"
        name="pending-review"
      />
      <ElTabPane
        label="已归档"
        name="archived"
      />
    </ElTabs>
    <p class="review-note">
      {{ tab === 'pending-review' ? '辅导员查看待初审申请，院系管理员查看待复审申请；同时具有两种角色时按院系管理员范围显示。' : '显示管理范围内已归档的备案记录，可能包含同一学生的多次申请。' }}
    </p>
    <div class="review-filters"><label>搜索备案
        <ElInput
          v-model="keyword"
          :prefix-icon="Search"
          clearable
          placeholder="申请编号、姓名、学号或单位"
        />
      </label><label>提交时班级<ElSelect
          v-model="classId"
          clearable
          filterable
          placeholder="全部班级"
        >
          <ElOption
            v-for="c in classes"
            :key="c.id"
            :value="c.id"
            :label="c.name"
          />
        </ElSelect></label></div>
    <p
      class="review-count muted"
      aria-live="polite"
    >{{ loading ? '正在读取备案…' : error ? '数据暂不可用' : `共 ${rows.length} 条备案，筛选后 ${filtered.length} 条` }}
    </p>
    <div
      v-if="error"
      role="alert"
      class="review-error"
    >
      <ElAlert
        :title="error"
        description="请检查账号角色与管理范围；若接口刚更新，请确认后端已重启。"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton @click="load">重新加载</ElButton>
    </div>
    <p
      v-else-if="loading"
      class="review-loading"
      role="status"
    >正在加载备案资料…</p>
    <ElEmpty
      v-else-if="!filtered.length"
      :description="keyword || classId != null ? '没有符合筛选条件的备案' : tab === 'archived' ? '暂无已归档备案' : '暂无需要本人审核的备案'"
    >
      <ElButton
        v-if="keyword || classId != null"
        @click="keyword='';classId=null"
      >清除筛选</ElButton>
    </ElEmpty>
    <template v-else>
      <ElTable
        :data="visible"
        row-key="id"
        class="review-table"
      >
        <ElTableColumn
          label="申请编号 / 提交时间"
          min-width="215"
        ><template
            #default="{row}">{{ display(row,'applicationNo') }}<small>{{ display(row,'submittedAt') }}</small></template>
        </ElTableColumn>
        <ElTableColumn
          label="学生"
          min-width="145"
        ><template
            #default="{row}">{{ row.studentName }}<small>{{ row.studentNo }}</small></template>
        </ElTableColumn>
        <ElTableColumn
          label="班级 / 专业"
          min-width="190"
        ><template
            #default="{row}">{{ display(row,'className') }}<small>{{ display(row,'major') }}</small></template>
        </ElTableColumn>
        <ElTableColumn
          label="单位 / 岗位"
          min-width="200"
        ><template
            #default="{row}">{{ display(row,'companyName') }}<small>{{ display(row,'jobTitle') }}</small></template>
        </ElTableColumn>
        <ElTableColumn
          label="状态"
          min-width="155"
        ><template #default="{row}">
            <ElTag :type="Number(row.status)===30?'success':'warning'">
              {{ filingStatus(row.status) }}
            </ElTag>
          </template></ElTableColumn>
        <ElTableColumn
          label="操作"
          :width="tab === 'pending-review' ? 180 : 110"
          fixed="right"
        ><template #default="{row}">
            <ElButton
              link
              type="primary"
              :aria-label="`查看${row.studentName}的备案${row.applicationNo}`"
              @click="detail(row)"
            >查看资料</ElButton>
            <ElButton
              v-if="tab === 'pending-review' && [10,20].includes(Number(row.status))"
              type="primary"
              link
              :aria-label="`审核${row.studentName}的备案`"
              @click="detail(row)"
            >审核</ElButton>
          </template></ElTableColumn>
      </ElTable>
      <div class="review-mobile">
        <article
          v-for="row in visible"
          :key="row.id"
        >
          <div>
            <h3>{{ row.studentName }}</h3>
            <ElTag :type="Number(row.status)===30?'success':'warning'">
              {{ filingStatus(row.status) }}
            </ElTag>
          </div>
          <p>{{ row.studentNo }} · {{ row.className }}</p>
          <p>{{ display(row,'companyName') }}</p>
          <p class="muted">{{ display(row,'applicationNo') }}</p>
          <ElButton
            :aria-label="`查看${row.studentName}的备案${row.applicationNo}`"
            @click="detail(row)"
          >查看资料</ElButton>
          <ElButton
            v-if="tab === 'pending-review' && [10,20].includes(Number(row.status))"
            type="primary"
            link
            :aria-label="`审核${row.studentName}的备案`"
            @click="detail(row)"
          >审核</ElButton>
        </article>
      </div>
    </template>
    <footer class="review-footer"><label>每页条数<ElSelect
          v-model="size"
          aria-label="每页条数"
        >
          <ElOption
            v-for="n in [10,20,50]"
            :key="n"
            :value="n"
            :label="`${n} 条`"
          />
        </ElSelect></label>
      <nav aria-label="备案分页">
        <ElButton
          :disabled="loading || !!error || page<=1"
          @click="page--"
        >上一页</ElButton><span>第 {{page}} / {{pages}} 页</span>
        <ElButton
          :disabled="loading || !!error || page>=pages"
          @click="page++"
        >下一页</ElButton>
      </nav>
    </footer>
  </section>
  <ElDialog
    v-model="opened"
    title="备案资料"
    width="840px"
    class="review-dialog"
    :before-close="closeDialog"
    :show-close="!submitting"
    :close-on-click-modal="false"
    :close-on-press-escape="!submitting"
    destroy-on-close
    @closed="selected=null"
  >
    <template v-if="selected">
      <div class="review-detail-heading"><strong>{{selected.studentName}} ·
          {{selected.studentNo}}</strong>
        <ElTag :type="Number(selected.status)===30?'success':'warning'">
          {{filingStatus(selected.status)}}
        </ElTag>
      </div>
      <p class="review-note">
        以下为该次申请的提交资料。{{Number(selected.status)===30?'归档是备案状态。':'请核对资料后填写审核意见并提交结果。'}}</p>
      <section
        v-for="section in sections"
        :key="section.title"
        class="review-section"
      >
        <h3>{{section.title}}</h3>
        <dl>
          <div
            v-for="[label,key] in section.fields"
            :key="key"
          >
            <dt>{{label}}</dt>
            <dd>{{display(selected,key)}}</dd>
          </div>
        </dl>
      </section>
      <section class="review-section">
        <h3>其他去向信息</h3>
        <ElAlert
          v-if="destination.error"
          title="去向详情格式异常，无法解析"
          type="warning"
          :closable="false"
        />
        <pre v-else-if="Object.keys(destination.value).length">{{JSON.stringify(destination.value,null,2)}}
        </pre>
        <p
          v-else
          class="muted"
        >未填写其他去向信息</p>
      </section>
      <section class="review-section">
        <h3>证明材料</h3>
        <ElAlert
          v-if="materials.error"
          title="材料信息格式异常，无法解析"
          type="warning"
          :closable="false"
        /><template v-else-if="materials.value.length">
          <ul>
            <li
              v-for="(file,index) in materials.value"
              :key="index"
            >{{file?.name || `材料 ${index+1}`}}<span v-if="file?.type">（{{file.type}}）</span></li>
          </ul>
          <p class="muted">附件预览与下载暂未开放。</p>
        </template>
        <p
          v-else
          class="muted"
        >本次申请未提供证明材料</p>
      </section>
    </template>
    <section
      v-if="canReview"
      class="review-section"
    >
      <h3>{{ Number(selected.status) === 10 ? '辅导员初审' : '院系复审' }}</h3>
      <p class="review-note">
        {{ Number(selected.status) === 10 ? '通过后进入院系复审；打回必须填写原因。' : '复审通过后自动归档，当前规则不支持院系打回。' }}
      </p>
      <ElForm label-position="top">
        <ElFormItem
          label="审核意见"
          for="audit-opinion"
          :error="reviewError"
        >
          <ElInput
            id="audit-opinion"
            v-model="opinion"
            type="textarea"
            :rows="4"
            :maxlength="1000"
            show-word-limit
            :disabled="submitting || stale"
            placeholder="填写审核意见；打回时必填"
          />
        </ElFormItem>
      </ElForm>
      <ElAlert
        v-if="stale"
        title="请刷新列表核实最新状态后再操作"
        type="warning"
        :closable="false"
        show-icon
      />
    </section>
    <template #footer>
      <ElButton
        :disabled="submitting"
        @click="opened=false"
      >关闭</ElButton>
      <ElButton
        v-if="stale"
        type="primary"
        @click="reconcile"
      >刷新列表</ElButton><template v-else-if="canReview">
        <ElButton
          v-if="Number(selected.status)===10"
          type="danger"
          plain
          :disabled="submitting"
          @click="audit(2)"
        >打回</ElButton>
        <ElButton
          type="primary"
          :loading="submitting"
          @click="audit(1)"
        >{{ Number(selected.status)===20 ? '复审通过并归档' : '初审通过' }}</ElButton>
      </template>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.review-note
{
  color: var(--muted);
  line-height: 1.7;
  margin: 12px 0 24px;
  font-size: 14px;
}

.review-filters
{
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.review-filters label
{
  display: grid;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
}

.review-count
{
  margin: 20px 0;
  font-size: 14px;
}

.review-table small
{
  display: block;
  margin-top: 5px;
  color: var(--muted);
}

.review-loading
{
  padding: 48px 0;
  text-align: center;
  color: var(--muted);
}

.review-error
{
  display: grid;
  gap: 16px;
  justify-items: start;
}

.review-footer,
.review-footer nav,
.review-footer label
{
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.review-footer
{
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
  font-size: 13px;
}

.review-footer .el-select
{
  width: 100px;
}

.review-footer .el-button
{
  margin: 0;
}

.review-detail-heading
{
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.review-section
{
  padding: 20px 0;
  border-top: 1px solid var(--line);
}

.review-section h3
{
  font-size: 16px;
  margin: 0 0 20px;
}

.review-section dl
{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.review-section dt
{
  font-size: 13px;
  color: var(--muted);
}

.review-section dd
{
  margin: 8px 0 0;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.review-section pre
{
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  font: inherit;
}

.review-section li
{
  overflow-wrap: anywhere;
  margin-bottom: 12px;
}

.review-mobile
{
  display: none;
}

@media(max-width:700px)
{

  .review-filters,
  .review-section dl
  {
    grid-template-columns: 1fr;
  }

  .review-table
  {
    display: none;
  }

  .review-mobile
  {
    display: block;
  }

  .review-mobile article
  {
    border-top: 1px solid var(--line);
    padding: 20px 0;
  }

  .review-mobile article>div
  {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .review-mobile p
  {
    overflow-wrap: anywhere;
    margin: 12px 0;
  }

  .review-mobile .el-button
  {
    min-height: 44px;
  }
}
</style>
<style>
.review-dialog
{
  max-width: calc(100vw - 32px);
}
</style>
