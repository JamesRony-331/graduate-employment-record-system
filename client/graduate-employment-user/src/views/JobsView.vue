<script setup>
import
{
  ref,
  onMounted,
  onBeforeUnmount
}
from 'vue'
import
{
  ElAlert,
  ElEmpty,
  ElSelect,
  ElOption,
  ElTag,
  ElDialog,
  ElDescriptions,
  ElDescriptionsItem
}
from 'element-plus'
import
{
  jobsPage,
  jobDetail
}
from '../api/jobs'
const rows = ref([]),
  page = ref(1),
  size = ref(10),
  total = ref(0),
  pages = ref(0),
  loading = ref(false),
  error = ref('')
const opened = ref(false),
  selected = ref(null),
  detailLoading = ref(false),
  detailError = ref(''),
  contact = ref(null),
  contactOpen = ref(false)
let sequence = 0,
  detailSequence = 0
const controllers = new Set()
async function timed(task)
{
  const c = new AbortController();
  controllers.add(c);
  const timer = setTimeout(() => c.abort(), 15000);
  try
  {
    return await task(c.signal)
  }
  finally
  {
    clearTimeout(timer);
    controllers.delete(c)
  }
}
const message = e => e.name === 'AbortError' ? '请求超时，请重试' : e instanceof TypeError ?
  '无法连接服务，请检查网络' : e.message
async function load(target = page.value)
{
  const seq = ++sequence;
  loading.value = true;
  error.value = '';
  try
  {
    let data = await timed(signal => jobsPage(target, size.value, signal));
    if (seq !== sequence)
      return;
    if (target > Math.max(1, data.total))
    {
      target = Math.max(1, data.total);
      data = await timed(signal => jobsPage(target, size.value, signal))
    }
    if (seq !== sequence)
      return;
    rows.value = data.records;
    total.value = data.totalCount;
    pages.value = data.total;
    page.value = target
  }
  catch (e)
  {
    if (seq === sequence)
      error.value = message(e)
  }
  finally
  {
    if (seq === sequence)
      loading.value = false
  }
}
async function showDetail(row)
{
  const seq = ++detailSequence;
  opened.value = true;
  selected.value = null;
  detailError.value = '';
  detailLoading.value = true;
  try
  {
    const data = await timed(signal => jobDetail(row.id, signal));
    if (seq === detailSequence)
    {
      if (!data || data.id == null)
        throw new Error('该工作已不存在，请刷新列表');
      selected.value = data
    }
  }
  catch (e)
  {
    if (seq === detailSequence)
      detailError.value = message(e)
  }
  finally
  {
    if (seq === detailSequence)
      detailLoading.value = false
  }
}

function showContact(row)
{
  contact.value = row;
  contactOpen.value = true
}
const display = v => Array.isArray(v) ? v.join('、') || '未提供' : v == null || v === '' ? '未提供' : v
const fields = [
  ['岗位类别', 'categories'],
  ['工作城市', 'cities'],
  ['求职类型', 'jobType'],
  ['学历', 'education'],
  ['专业', 'major'],
  ['工作年限', 'experience'],
  ['技能要求', 'skills'],
  ['最低月薪（元）', 'minimumSalary'],
  ['期望月薪（元）', 'expectedSalary'],
  ['工作方式', 'workMode'],
  ['休息制度', 'restDays'],
  ['出差', 'travel'],
  ['外包', 'outsourcing'],
  ['补充说明', 'other']
]
onMounted(() => load())
onBeforeUnmount(() =>
{
  sequence++;
  detailSequence++;
  controllers.forEach(c => c.abort())
})
</script>
<template>
<div class="jobs-page">
  <header class="jobs-heading">
    <div>
      <h1 tabindex="-1">校园招聘</h1>
      <p>浏览全部工作，了解企业、岗位要求与联系方式。</p>
    </div>
    <RouterLink
      to="/recommendations"
      class="recommend-link"
    >按我的条件推荐岗位</RouterLink>
  </header>
  <section
    class="jobs-list"
    aria-label="工作列表"
    :aria-busy="loading"
  >
    <div class="list-toolbar">
      <h2>全部工作 <span>{{total}} 个</span></h2>
      <ElButton
        :loading="loading"
        @click="load()"
      >刷新列表</ElButton>
    </div>
    <p
      v-if="loading"
      role="status"
      class="list-status"
    >正在加载工作信息…</p>
    <div
      v-else-if="error"
      role="alert"
    >
      <ElAlert
        :title="error"
        type="error"
        :closable="false"
      />
      <ElButton
        class="retry-button"
        @click="load()"
      >重新加载</ElButton>
    </div>
    <ElEmpty
      v-else-if="!rows.length"
      description="暂无工作信息，请稍后再来看看"
    />
    <ul v-else>
      <li
        v-for="row in rows"
        :key="row.id"
        class="job-row"
      >
        <div class="job-info">
          <p class="company">{{row.companyName || '公司名称未提供'}}</p>
          <h3>{{display(row.categories)}}</h3>
          <p class="job-meta">{{display(row.cities)}} · {{display(row.jobType)}} ·
            {{display(row.education)}} · {{row.experience==null?'年限未提供':row.experience+' 年经验'}}
          </p>
          <div class="skill-tags">
            <ElTag
              v-for="skill in row.skills || []"
              :key="skill"
              type="info"
            >{{skill}}</ElTag>
          </div>
        </div>
        <div class="job-actions">
          <p class="salary">最低月薪 <strong>{{display(row.minimumSalary)}}</strong><span
              v-if="row.minimumSalary!=null"
            > 元</span></p>
          <p class="expected-salary">期望月薪：{{display(row.expectedSalary)}}<span
              v-if="row.expectedSalary!=null"
            > 元</span></p>
          <div>
            <ElButton
              :aria-label="`查看工作 ${row.id} 详情`"
              @click="showDetail(row)"
            >查看详情</ElButton>
            <ElButton
              type="primary"
              :aria-label="`联系${row.companyName || '公司'}，工作 ${row.id}`"
              @click="showContact(row)"
            >联系公司</ElButton>
          </div>
        </div>
      </li>
    </ul>
    <nav
      class="jobs-pagination"
      aria-label="工作列表分页"
    >
      <div class="page-size"><label for="jobs-size">每页条数</label>
        <ElSelect
          id="jobs-size"
          v-model="size"
          :disabled="loading"
          aria-label="每页条数"
          @change="load(1)"
        >
          <ElOption
            v-for="n in [10,20,50]"
            :key="n"
            :label="n+' 条'"
            :value="n"
          />
        </ElSelect>
      </div>
      <div class="page-buttons">
        <ElButton
          :disabled="loading||!!error||page<=1"
          @click="load(page-1)"
        >上一页</ElButton><span>第 {{page}} / {{Math.max(1,pages)}} 页</span>
        <ElButton
          :disabled="loading||!!error||page>=pages"
          @click="load(page+1)"
        >下一页</ElButton>
      </div>
    </nav>
  </section>
  <ElDialog
    v-model="opened"
    title="工作详情"
    width="800px"
    class="job-dialog"
    @close="detailSequence++"
  >
    <p
      v-if="detailLoading"
      role="status"
    >正在加载详情…</p>
    <ElAlert
      v-else-if="detailError"
      :title="detailError"
      type="error"
      :closable="false"
    /><template v-else-if="selected">
      <h2>{{selected.companyName || '公司名称未提供'}}</h2>
      <ElDescriptions
        :column="1"
        border
      >
        <ElDescriptionsItem
          v-for="[label,key] in fields"
          :key="key"
          :label="label"
        ><span class="detail-text">{{display(selected[key])}}</span></ElDescriptionsItem>
      </ElDescriptions>
    </template><template #footer>
      <ElButton @click="opened=false">关闭</ElButton>
      <ElButton
        v-if="selected&&!detailError"
        type="primary"
        @click="showContact(selected)"
      >联系公司</ElButton>
    </template>
  </ElDialog>
  <ElDialog
    v-model="contactOpen"
    title="联系公司"
    width="480px"
    class="job-dialog"
  >
    <ElDescriptions
      v-if="contact"
      :column="1"
      border
    >
      <ElDescriptionsItem label="公司名称">{{contact.companyName || '未提供'}}</ElDescriptionsItem>
      <ElDescriptionsItem label="联系方式"><span
          class="detail-text">{{contact.companyContact || '暂无联系方式，请联系学校管理员核实'}}</span>
      </ElDescriptionsItem>
    </ElDescriptions><template #footer>
      <ElButton @click="contactOpen=false">关闭</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.jobs-page
{
  max-width: 1140px;
  margin: 0 auto
}

.jobs-heading
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px
}

.jobs-heading h1
{
  font-size: 32px;
  margin: 0 0 12px
}

.jobs-heading p
{
  color: var(--muted);
  line-height: 1.8
}

.recommend-link
{
  color: var(--primary);
  white-space: nowrap;
  border-bottom: 1px solid currentColor;
  padding: 10px 0
}

.jobs-list
{
  padding: 0
}

.list-toolbar
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px
}

.list-toolbar h2
{
  font-size: 20px;
  margin: 0
}

.list-toolbar h2 span
{
  font-size: 13px;
  color: var(--muted);
  font-weight: 400;
  margin-left: 10px
}

.jobs-list ul
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  padding: 0;
  margin: 0;
  list-style: none
}

.job-row
{
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: white;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 26px;
  transition: border-color .15s ease
}

.job-row:hover,
.job-row:focus-within
{
  border-color: #9cb2d3
}

.job-info
{
  min-width: 0;
  overflow-wrap: anywhere;
  flex: 1
}

.company
{
  color: var(--muted);
  font-size: 13px;
  margin: 0 0 12px;
  line-height: 1.7
}

.job-info h3
{
  font-size: 21px;
  margin: 0 0 12px
}

.job-meta
{
  color: var(--muted);
  font-size: 13px;
  line-height: 1.8
}

.skill-tags
{
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 16px 0 24px
}

.skill-tags .el-tag
{
  height: auto;
  min-height: 24px;
  white-space: normal;
  overflow-wrap: anywhere;
  max-width: 100%
}

.job-actions
{
  border-top: 1px solid var(--line);
  padding-top: 20px;
  text-align: left
}

.salary
{
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 8px
}

.salary strong
{
  font-size: 23px;
  color: var(--primary);
  font-weight: 600
}

.expected-salary
{
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 20px
}

.job-actions>div
{
  display: flex;
  gap: 12px
}

.job-actions .el-button
{
  flex: 1;
  min-height: 42px;
  margin-left: 0
}

.jobs-pagination
{
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  margin-top: 28px
}

.page-size,
.page-buttons
{
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px
}

.page-size .el-select
{
  width: 100px
}

.list-status
{
  padding: 50px 0;
  color: var(--muted)
}

.retry-button
{
  margin: 16px 0
}

.detail-text
{
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  user-select: text
}

:global(.job-dialog)
{
  max-width: calc(100vw - 32px)
}

.job-dialog h2
{
  margin: 0 0 20px;
  font-size: 21px
}

.recommend-link:focus-visible
{
  outline: 2px solid var(--primary);
  outline-offset: 4px
}

@media(max-width:700px)
{
  .jobs-list ul
  {
    grid-template-columns: 1fr;
    gap: 16px
  }

  .jobs-heading,
  .jobs-pagination
  {
    flex-direction: column;
    align-items: flex-start
  }

  .job-row
  {
    padding: 22px 20px
  }

  .jobs-heading h1
  {
    font-size: 28px
  }

  .jobs-pagination
  {
    gap: 18px
  }

  .page-buttons
  {
    width: 100%;
    justify-content: space-between
  }
}

@media(prefers-reduced-motion:reduce)
{
  .job-row
  {
    transition: none
  }
}
</style>
