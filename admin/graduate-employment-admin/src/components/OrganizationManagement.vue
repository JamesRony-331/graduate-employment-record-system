<script setup>
import
{
  computed,
  nextTick,
  onMounted,
  onBeforeUnmount,
  reactive,
  ref
}
from 'vue';
import
{
  onBeforeRouteLeave
}
from 'vue-router';
import
{
  ElTable,
  ElTableColumn,
  ElEmpty,
  ElDialog,
  ElSelect,
  ElOption,
  ElInputNumber,
  ElMessage,
  ElMessageBox
}
from 'element-plus';
import
{
  Plus,
  Refresh,
  Search
}
from '@element-plus/icons-vue';
import
{
  definitions,
  createModel,
  formErrors,
  organizationPayload
}
from '../api/organizationContracts';
import
{
  listOrganizations,
  organizationDetail,
  saveOrganization,
  deleteOrganization,
  collegeOptions,
  majorOptions
}
from '../api/organization';
import
{
  session
}
from '../stores/session';
const props = defineProps(
{
  kind:
  {
    type: String,
    required: true
  }
});
const config = definitions[props.kind];
const rows = ref([]),
  total = ref(0),
  pages = ref(0),
  page = ref(1),
  keyword = ref('');
const loading = ref(false),
  error = ref(''),
  deleting = ref(null);
const colleges = ref([]),
  collegesLoading = ref(false),
  collegesError = ref('');
const majors = ref([]);
const availableMajors = computed(() => majors.value.filter(m => String(m.collegeId) === String(model
  .collegeId)));

function changeCollege()
{
  if (props.kind === 'class')
    model.majorId = null;
}
const mode = ref(''),
  record = ref(null),
  model = reactive(createModel(props.kind));
const opened = ref(false),
  detailLoading = ref(false),
  saving = ref(false),
  dialogError = ref(''),
  errors = ref(
  {}),
  summary = ref(null);
const baseline = ref('');
let detailSequence = 0;
const dirty = computed(() => opened.value && ['add', 'edit', 'bind'].includes(mode.value) && JSON
  .stringify(model) !== baseline.value);
const collegeName = id => colleges.value.find(c => String(c.id) === String(id))?.collegeName || (
  id != null ? `学院 ID：${id}` : '未设置');

function display(row, key)
{
  const value = row[key];
  if (['notice', 'basic-knowledge', 'service-entry'].includes(props.kind))
  {
    const field = config.fields.find(f => f.key === key);
    if (field?.options)
      return field.options.find(o => o[0] === value)?.[1] || '未知';
  }
  if (key === 'status')
    return value === 1 ? '正常' : value === 0 ? '禁用' : '未知';
  if (key === 'majorId')
    return row.majorName || majors.value.find(m => String(m.id) === String(
      value))?.majorName || (value == null ? '未设置' : `专业 ID：${value}`);
  if (key === 'collegeId')
    return row.collegeName || collegeName(value);
  if (key === 'createdAt' || key === 'updatedAt')
    return value ? String(value).replace('T', ' ')
      .slice(0, 19) : '未记录';
  return value === '' || value == null ? '未填写' : value;
}
const filtered = computed(() => rows.value.filter(row => config.fields.some(f => String(display(row,
  f.key)).toLowerCase().includes(keyword.value.trim().toLowerCase()))));
const columns = [...config.fields.filter(f => !['description', 'content', 'answer'].includes(f
  .key) && !f.createOnly), ...(props.kind === 'department-admin' ? [
{
  key: 'status',
  label: '账号状态'
}] : [])];
const title = computed(() => mode.value === 'detail' ? `${config.label}详情` : mode.value === 'bind' ?
  '绑定学院' : `${mode.value === 'edit' ? '编辑' : '新增'}${config.label}`);
const fields = computed(() => mode.value === 'bind' ? config.fields.filter(f => ['collegeId',
  'majorId'
].includes(f.key)) : config.fields.filter(f => mode.value === 'add' || !f.createOnly));
async function loadColleges()
{
  if (collegesLoading.value)
    return;
  collegesLoading.value = true;
  collegesError.value = '';
  try
  {
    const [collegeData, majorData] = await Promise.all([collegeOptions(), props.kind === 'class' ?
      majorOptions() : Promise.resolve([])
    ]);
    colleges.value = collegeData;
    majors.value = majorData;
  }
  catch (e)
  {
    collegesError.value = e.message;
  }
  finally
  {
    collegesLoading.value = false;
  }
}
async function load(target = page.value)
{
  if (loading.value)
    return;
  loading.value = true;
  error.value = '';
  try
  {
    let data = await listOrganizations(props.kind, target);
    if (target > Math.max(1, data.total))
    {
      target = Math.max(1, data.total);
      data = await listOrganizations(props.kind, target);
    }
    rows.value = data.records;
    total.value = data.totalCount;
    pages.value = data.total;
    page.value = target;
  }
  catch (e)
  {
    error.value = e.message;
    rows.value = [];
  }
  finally
  {
    loading.value = false;
  }
}
async function fetchRecord(id)
{
  const sequence = ++detailSequence;
  detailLoading.value = true;
  dialogError.value = '';
  try
  {
    const data = await organizationDetail(props.kind, id);
    if (sequence !== detailSequence || !opened.value)
      return;
    record.value = data;
    Object.assign(model, createModel(props.kind, data));
    baseline.value = JSON.stringify(model);
  }
  catch (e)
  {
    if (sequence === detailSequence)
      dialogError.value = e.message;
  }
  finally
  {
    if (sequence === detailSequence)
      detailLoading.value = false;
  }
}

function open(nextMode, row)
{
  mode.value = nextMode;
  record.value = row ?
  {
    id: row.id
  } : null;
  Object.assign(model, createModel(props.kind));
  errors.value = {};
  dialogError.value = '';
  baseline.value = JSON.stringify(model);
  opened.value = true;
  if (row)
    fetchRecord(row.id);
  if (['class', 'department-admin', 'major'].includes(props.kind))
    loadColleges();
}
async function canLeave()
{
  if (!session.token)
    return true;
  if (saving.value || deleting.value !== null)
    return false;
  if (!dirty.value)
    return true;
  try
  {
    await ElMessageBox.confirm('尚未保存的修改将丢失。', '放弃修改？',
    {
      confirmButtonText: '放弃修改',
      cancelButtonText: '继续编辑',
      type: 'warning'
    });
    return true;
  }
  catch
  {
    return false;
  }
}
async function close(done)
{
  if (await canLeave())
  {
    opened.value = false;
    detailSequence++;
    detailLoading.value = false;
    if (typeof done === 'function')
      done();
  }
}
const owned = row => props.kind !== 'notice' || String(row.publisherId) === String(session.user
  ?.id);
async function submit()
{
  if (saving.value || detailLoading.value)
    return;
  errors.value = formErrors(props.kind, model, mode.value !== 'add' ? record.value : null);
  dialogError.value = '';
  if (Object.keys(errors.value).length)
  {
    await nextTick();
    summary.value?.focus();
    return;
  }
  if (props.kind === 'notice' && model.status === 1)
  {
    try
    {
      await ElMessageBox.confirm('保存后公告将对学生可见，请核对内容。', '确认发布公告？',
      {
        confirmButtonText: '发布',
        cancelButtonText: '继续编辑'
      });
    }
    catch
    {
      return;
    }
  }
  saving.value = true;
  try
  {
    if (mode.value === 'bind')
      await saveOrganization('class',
      {
        id: record.value.id,
        collegeId: model.collegeId,
        majorId: model.majorId
      }, true);
    else
      await saveOrganization(props.kind, organizationPayload(props.kind, model, mode.value ===
        'edit' ? record.value.id : null), mode.value === 'edit');
    opened.value = false;
    ElMessage.success(mode.value === 'bind' ? '学院绑定已保存' :
      `${config.label}${mode.value === 'add' ? '已新增' : '已保存'}`);
    await load(mode.value === 'add' ? 1 : page.value);
  }
  catch (e)
  {
    dialogError.value = e.message;
    await nextTick();
    summary.value?.focus();
  }
  finally
  {
    saving.value = false;
  }
}
async function remove(row)
{
  if (deleting.value !== null)
    return;
  deleting.value = row.id;
  try
  {
    await ElMessageBox.confirm(props.kind === 'department-admin' ?
      `删除“${row[config.name]}”将禁用其登录账号、移除全部角色并删除管理员资料，无法恢复。` :
      `删除“${row[config.name]}”后无法恢复。存在关联数据时不能删除。`, `删除${config.label}？`,
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      });
  }
  catch
  {
    deleting.value = null;
    return;
  }
  try
  {
    await deleteOrganization(props.kind, row.id);
    ElMessage.success(`${config.label}已删除`);
    await load();
  }
  catch (e)
  {
    ElMessage.error(e.message);
  }
  finally
  {
    deleting.value = null;
  }
}

function beforeUnload(event)
{
  if (dirty.value)
  {
    event.preventDefault();
    event.returnValue = '';
  }
}
onBeforeRouteLeave(canLeave);
onMounted(() =>
{
  load();
  if (['class', 'department-admin', 'major'].includes(props.kind))
    loadColleges();
  window.addEventListener('beforeunload', beforeUnload);
});
onBeforeUnmount(() =>
{
  detailSequence++;
  window.removeEventListener('beforeunload', beforeUnload);
});
</script>
<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">{{ config.label }}管理</h1>
      <p class="subtext">{{ config.description }}</p>
    </div>
    <ElButton
      type="primary"
      :icon="Plus"
      :disabled="loading || deleting !== null"
      @click="open('add')"
    >新增{{ config.label }}</ElButton>
  </header>
  <section
    class="surface"
    :aria-busy="loading"
  >
    <div class="section-heading">
      <h2>{{ config.label }}列表</h2>
      <ElButton
        :icon="Refresh"
        :loading="loading"
        :disabled="deleting !== null"
        @click="load()"
      >刷新列表</ElButton>
    </div>
    <div
      v-if="collegesError"
      class="college-warning"
    >
      <ElAlert
        :title="'学院名称加载失败：' + collegesError"
        type="warning"
        :closable="false"
        show-icon
      />
      <ElButton
        :loading="collegesLoading"
        @click="loadColleges"
      >重试学院列表</ElButton>
    </div>
    <div class="org-toolbar">
      <ElInput
        v-model="keyword"
        :prefix-icon="Search"
        :aria-label="'搜索本页' + config.label"
        :placeholder="'搜索本页' + config.label + '信息'"
        clearable
      /><span
        class="muted"
        aria-live="polite"
      >{{ error ? '数据暂不可用' : `共 ${total} 个${config.label}` }}</span>
    </div>
    <div
      v-if="error"
      role="alert"
    >
      <ElAlert
        title="列表加载失败"
        :description="error"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton
        class="retry"
        @click="load()"
      >重新加载</ElButton>
    </div>
    <p
      v-else-if="loading"
      class="org-loading"
      role="status"
    >正在加载{{ config.label }}列表…</p>
    <template v-else>
      <ElEmpty
        v-if="!filtered.length"
        :description="keyword ? '本页没有符合条件的记录' : `暂无${config.label}`"
      >
        <ElButton
          v-if="keyword"
          @click="keyword = ''"
        >清除筛选</ElButton>
        <ElButton
          v-else
          type="primary"
          @click="open('add')"
        >新增{{ config.label }}</ElButton>
      </ElEmpty>
      <template v-else>
        <ElTable
          :data="filtered"
          row-key="id"
          class="org-table"
        >
          <ElTableColumn
            v-for="f in columns"
            :key="f.key"
            :label="f.label"
            :min-width="f.key === 'collegeId' || f.key.endsWith('Name') ? 160 : 125"
          ><template #default="{ row }">{{ display(row, f.key) }}</template></ElTableColumn>
          <ElTableColumn
            label="操作"
            :width="kind === 'class' ? 300 : 230"
            fixed="right"
          ><template #default="{ row }">
              <ElButton
                link
                type="primary"
                :aria-label="`查看${row[config.name]}详情`"
                @click="open('detail', row)"
              >查看详情</ElButton>
              <ElButton
                link
                type="primary"
                :disabled="deleting !== null || !owned(row)"
                :aria-label="`编辑${row[config.name]}`"
                @click="open('edit', row)"
              >编辑</ElButton>
              <ElButton
                v-if="kind === 'class'"
                link
                type="primary"
                :disabled="deleting !== null"
                :aria-label="`为${row[config.name]}绑定学院`"
                @click="open('bind', row)"
              >绑定学院</ElButton>
              <ElButton
                link
                type="danger"
                :disabled="deleting !== null || !owned(row)"
                :loading="deleting === row.id"
                :aria-label="`删除${row[config.name]}`"
                @click="remove(row)"
              >删除</ElButton>
            </template></ElTableColumn>
        </ElTable>
        <div class="org-mobile">
          <article
            v-for="row in filtered"
            :key="row.id"
          >
            <h3>{{ row[config.name] }}</h3>
            <dl>
              <div
                v-for="f in columns.filter(f => f.key !== config.name)"
                :key="f.key"
              >
                <dt>{{ f.label }}</dt>
                <dd>{{ display(row, f.key) }}</dd>
              </div>
            </dl>
            <div class="org-actions">
              <ElButton
                :aria-label="`查看${row[config.name]}详情`"
                @click="open('detail', row)"
              >查看详情</ElButton>
              <ElButton
                :disabled="deleting !== null || !owned(row)"
                :aria-label="`编辑${row[config.name]}`"
                @click="open('edit', row)"
              >编辑</ElButton>
              <ElButton
                v-if="kind === 'class'"
                :disabled="deleting !== null"
                :aria-label="`为${row[config.name]}绑定学院`"
                @click="open('bind', row)"
              >绑定学院</ElButton>
              <ElButton
                type="danger"
                plain
                :disabled="deleting !== null || !owned(row)"
                :loading="deleting === row.id"
                :aria-label="`删除${row[config.name]}`"
                @click="remove(row)"
              >删除</ElButton>
            </div>
          </article>
        </div>
      </template>
    </template>
    <footer class="org-footer">
      <p class="muted">每页 10 条，搜索仅筛选当前页。</p>
      <nav :aria-label="config.label + '分页'">
        <ElButton
          :disabled="page <= 1 || loading || !!error || deleting !== null"
          @click="load(page - 1)"
        >上一页</ElButton><span>第 {{ page }} / {{ Math.max(1, pages) }} 页</span>
        <ElButton
          :disabled="page >= pages || loading || !!error || deleting !== null"
          @click="load(page + 1)"
        >下一页</ElButton>
      </nav>
    </footer>
  </section>
  <ElDialog
    v-model="opened"
    :title="title"
    width="660px"
    class="org-dialog"
    @closed="model.password !== undefined && (model.password = '')"
    :before-close="close"
    :show-close="!saving"
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    destroy-on-close
  >
    <p
      v-if="detailLoading"
      class="org-loading"
      role="status"
    >正在读取最新资料…</p>
    <template v-else-if="mode !== 'add' && !record?.[config.name]">
      <ElAlert
        :title="dialogError || '详情暂不可用'"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton
        class="retry"
        @click="fetchRecord(record.id)"
      >重试</ElButton>
    </template>
    <dl
      v-else-if="mode === 'detail'"
      class="org-details"
    >
      <div
        v-for="f in [...config.fields.filter(f => !f.createOnly), ...(kind === 'department-admin' ? [{ key: 'userId', label: '账号 ID' }, { key: 'status', label: '账号状态' }] : []), ...(kind === 'major' ? [{ key: 'id', label: '专业 ID' }] : [{ key: 'createdAt', label: '创建时间' }, { key: 'updatedAt', label: '更新时间' }])]"
        :key="f.key"
        :class="{ wide: f.type === 'textarea' }"
      >
        <dt>{{ f.label }}</dt>
        <dd>{{ display(record, f.key) }}</dd>
      </div>
    </dl>
    <ElForm
      v-else
      label-position="top"
      :disabled="saving"
      @submit.prevent="submit"
    >
      <div
        v-if="dialogError || Object.keys(errors).length"
        ref="summary"
        class="error-summary"
        role="alert"
        tabindex="-1"
      ><strong>{{ dialogError || '请核对以下信息' }}</strong><a
          v-for="(message, key) in errors"
          :key="key"
          :href="'#org-' + key"
        >{{ message }}</a></div>
      <p
        v-if="mode === 'bind'"
        class="org-note"
      >为 {{ record.className }} 选择所属学院。</p>
      <p
        v-if="kind === 'major' && mode === 'edit'"
        class="org-note"
      >已关联班级或学生的专业可以修改名称，不能变更所属学院或删除。</p>
      <div class="form-grid">
        <ElFormItem
          v-for="f in fields"
          :key="f.key"
          :label="f.label"
          :for="'org-' + f.key"
          :required="f.required"
          :error="errors[f.key]"
          :class="{ wide: f.type === 'textarea' }"
        >
          <template v-if="f.type === 'college'">
            <ElSelect
              :id="'org-' + f.key"
              v-model="model[f.key]"
              filterable
              @change="changeCollege"
              :loading="collegesLoading"
              :disabled="!!collegesError || collegesLoading"
              placeholder="请选择学院"
              no-data-text="暂无学院，请先新增学院"
            >
              <ElOption
                v-for="college in colleges"
                :key="college.id"
                :label="`${college.collegeName}（${college.collegeCode}）`"
                :value="college.id"
              />
            </ElSelect>
            <p
              v-if="collegesError"
              class="field-note"
              role="alert"
            >{{ collegesError }}
              <ElButton
                link
                type="primary"
                @click="loadColleges"
              >重新加载</ElButton>
            </p>
            <p
              v-else-if="!collegesLoading && !colleges.length"
              class="field-note"
            >请先到学院管理新增学院，再返回添加{{ config.label }}。</p>
          </template>
          <ElSelect
            v-else-if="f.type === 'major'"
            :id="'org-' + f.key"
            v-model="model.majorId"
            filterable
            :loading="collegesLoading"
            :disabled="!model.collegeId || collegesLoading || !!collegesError"
            placeholder="请选择专业"
            no-data-text="该学院暂无专业"
          >
            <ElOption
              v-for="major in availableMajors"
              :key="major.id"
              :label="major.majorName"
              :value="major.id"
            />
          </ElSelect>
          <ElSelect
            v-else-if="f.type === 'select'"
            :id="'org-' + f.key"
            v-model="model[f.key]"
          >
            <ElOption
              v-for="[value,label] in f.options"
              :key="value"
              :value="value"
              :label="label"
            />
          </ElSelect>
          <ElInputNumber
            v-else-if="f.type === 'number'"
            :id="'org-' + f.key"
            v-model="model[f.key]"
            :min="f.min"
            :max="f.max"
            :step="1"
            :precision="0"
            controls-position="right"
            :placeholder="'请输入' + f.label"
          />
          <ElInput
            v-else-if="f.type === 'password'"
            :id="'org-' + f.key"
            v-model="model[f.key]"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="留空使用默认密码"
          />
          <ElInput
            v-else
            :readonly="mode === 'edit' && f.readonlyOnEdit"
            :id="'org-' + f.key"
            v-model="model[f.key]"
            :type="f.type === 'textarea' ? 'textarea' : 'text'"
            :maxlength="f.max"
            :rows="4"
            :show-word-limit="f.type === 'textarea'"
            :placeholder="'请输入' + f.label"
          />
          <p
            v-if="f.type === 'password'"
            class="field-note"
          >留空时初始密码为 123456，请通知用户首次登录后修改。</p>
          <p
            v-if="f.readonlyOnEdit && mode === 'edit'"
            class="field-note"
          >登录账号不可修改。</p>
          <p
            v-if="f.key === 'enrollmentYear' && mode === 'edit' && record.enrollmentYear != null"
            class="field-note"
          >已有入学年份可修改，暂不支持清空。</p>
        </ElFormItem>
      </div>
      <div class="form-actions">
        <ElButton
          :disabled="saving"
          @click="close()"
        >取消</ElButton>
        <ElButton
          type="primary"
          native-type="submit"
          :loading="saving"
          :disabled="(mode !== 'add' && !dirty) || (['class', 'department-admin', 'major'].includes(kind) && (!!collegesError || collegesLoading || !colleges.length))"
        >{{ mode === 'add' ? '确认新增' : '保存修改' }}</ElButton>
      </div>
    </ElForm>
    <template
      v-if="mode === 'detail'"
      #footer
    >
      <ElButton @click="close()">关闭</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.org-toolbar,
.org-footer,
.org-footer nav,
.org-actions
{
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.org-toolbar,
.org-footer
{
  justify-content: space-between;
}

.org-toolbar
{
  margin-bottom: 24px;
}

.org-toolbar .el-input
{
  max-width: 380px;
}

.org-footer
{
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.org-footer nav
{
  font-size: 13px;
}

.org-footer .el-button,
.org-actions .el-button
{
  margin-left: 0;
}

.org-loading
{
  padding: 48px 0;
  text-align: center;
  color: var(--muted);
}

.retry
{
  margin-top: 16px;
}

.college-warning
{
  margin-bottom: 20px;
  display: grid;
  gap: 10px;
  justify-items: start;
}

.org-details
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin: 8px 0 24px;
}

.org-details dt,
.org-mobile dt
{
  font-size: 13px;
  color: var(--muted);
}

.org-details dd
{
  margin: 8px 0 0;
  color: #202c40;
  line-height: 1.7;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.wide
{
  grid-column: 1 / -1;
}

.org-note
{
  margin-bottom: 24px;
}

.field-note
{
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
  margin-top: 6px;
}

:deep(.el-input-number),
:deep(.el-select)
{
  width: 100%;
}

.org-mobile
{
  display: none;
}

.org-mobile article
{
  padding: 24px 0;
  border-top: 1px solid var(--line);
}

.org-mobile h3
{
  overflow-wrap: anywhere;
}

.org-mobile dl>div
{
  display: flex;
  gap: 16px;
  margin: 12px 0;
}

.org-mobile dt
{
  width: 70px;
  flex-shrink: 0;
}

.org-mobile dd
{
  margin: 0;
  overflow-wrap: anywhere;
  font-size: 14px;
}

@media (max-width: 700px)
{
  .org-table
  {
    display: none;
  }

  .org-mobile
  {
    display: block;
  }

  .org-details
  {
    grid-template-columns: 1fr;
  }

  .org-toolbar .el-input
  {
    max-width: none;
  }
}
</style>
<style>
.org-dialog
{
  max-width: calc(100vw - 32px);
}

@media (max-width: 700px)
{
  .org-dialog .form-grid
  {
    grid-template-columns: 1fr;
  }
}
</style>
