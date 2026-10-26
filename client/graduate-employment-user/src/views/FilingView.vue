<script setup>
import
{
  computed,
  reactive,
  ref,
  onMounted,
  nextTick,
  onBeforeUnmount
}
from 'vue'
import
{
  onBeforeRouteLeave
}
from 'vue-router'
import
{
  ElMessage,
  ElMessageBox,
  ElAlert,
  ElSkeleton,
  ElSteps,
  ElStep,
  ElTag,
  ElDatePicker,
  ElDialog,
  ElEmpty,
}
from 'element-plus'
import
{
  DocumentChecked
}
from '@element-plus/icons-vue'
import
{
  session
}
from '../stores/session'
import
{
  getLastFiling,
  getFilings,
  addEmploymentFiling
}
from '../api/student'
import
{
  filingSections,
  filingPayload,
  filingErrors,
  filingState,
  normalizeFiling,
}
from '../api/filing'
const latest = ref(null),
  loading = ref(true),
  loadError = ref(''),
  busy = ref(false)
const submitted = ref(false),
  uncertain = ref(false),
  errors = ref(
  {}),
  submitError = ref(''),
  summary = ref(null)
const history = ref([]),
  historyOpen = ref(false),
  historyBusy = ref(false),
  historyError = ref('')
const newApplication = ref(false),
  changeReason = ref('')
const selected = ref(null)
const model = reactive(
  Object.fromEntries(filingSections.flatMap((s) => s.fields).map((f) => [f.key, ''])),
)
model.contactPhone = session.user?.phone || ''
model.contactEmail = session.user?.email || ''
const baseline = ref(JSON.stringify(model))
const dirty = computed(
  () => (!latest.value || newApplication.value) && !submitted.value && (JSON.stringify(model) !==
    baseline.value || !!changeReason.value),
)
const state = computed(() => filingState(latest.value?.status))
const detail = computed(() => selected.value || latest.value)
const dateText = (value) => (value ? String(value).replace('T', ' ').slice(0, 19) : '—')
const display = (value) => (value === null || value === undefined || value === '' ? '—' : value)
async function load()
{
  loading.value = true
  loadError.value = ''
  try
  {
    if (session.demo)
      latest.value = null
    else
      latest.value = normalizeFiling(await getLastFiling())
    uncertain.value = false
    if (latest.value && Number(latest.value.status) !== 30)
      newApplication.value = false
    if (latest.value)
      baseline.value = JSON.stringify(model)
  }
  catch (e)
  {
    loadError.value = e.message
  }
  finally
  {
    loading.value = false
  }
}

function startNew()
{
  for (const f of filingSections.flatMap(s => s.fields))
    model[f.key] = latest.value?.[f.key] ?? ''
  baseline.value = JSON.stringify(model)
  changeReason.value = '';
  submitted.value = false;
  submitError.value = '';
  errors.value = {};
  newApplication.value = true
}
async function submit()
{
  if (busy.value || (latest.value && !newApplication.value) || submitted.value || uncertain
    .value || session.demo)
    return
  errors.value = filingErrors(model)
  if (newApplication.value && (!changeReason.value.trim() || changeReason.value.trim().length >
      1000))
    errors.value.changeReason = '请填写变更原因，最多1000字'
  submitError.value = ''
  if (Object.keys(errors.value).length)
  {
    await nextTick()
    summary.value?.focus()
    return
  }
  busy.value = true
  try
  {
    await ElMessageBox.confirm(
      '提交后只能查看，不能直接修改。请确认所填信息准确。',
      '确认提交就业备案',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '继续核对',
        type: 'warning'
      },
    )
  }
  catch
  {
    busy.value = false
    return
  }
  try
  {
    // Recheck before sending: another tab may already have submitted a filing.
    const current = normalizeFiling(await getLastFiling())
    if (current && !(newApplication.value && Number(current.status) === 30))
    {
      latest.value = current
      newApplication.value = false
      ElMessage.info('已存在备案记录，已为你更新进度')
      return
    }
    uncertain.value = true
    await addEmploymentFiling(
    {
      ...filingPayload(model),
      ...(newApplication.value ?
      {
        changeReason: changeReason.value.trim()
      } :
      {})
    })
    newApplication.value = false
    submitted.value = true
    baseline.value = JSON.stringify(model)
    ElMessage.success('就业备案已提交')
    await load()
  }
  catch (e)
  {
    submitError.value = e.message + (uncertain.value ? '。请先刷新核实记录，避免重复提交。' : '')
    await nextTick()
    summary.value?.focus()
  }
  finally
  {
    busy.value = false
  }
}
async function openHistory()
{
  historyOpen.value = true
  historyBusy.value = true
  historyError.value = ''
  try
  {
    const result = await getFilings()
    if (result !== null && !Array.isArray(result))
      throw new Error('历史记录数据格式异常')
    history.value = (result || []).map(normalizeFiling).filter(Boolean)
  }
  catch (e)
  {
    historyError.value = e.message
  }
  finally
  {
    historyBusy.value = false
  }
}
onBeforeRouteLeave(async () =>
{
  if (!session.user)
    return true
  if (busy.value)
    return false
  if (!dirty.value)
    return true
  try
  {
    await ElMessageBox.confirm('离开后，尚未提交的内容将丢失。', '离开备案页面？',
    {
      confirmButtonText: '离开',
      cancelButtonText: '继续填写',
    })
    return true
  }
  catch
  {
    return false
  }
})

function beforeUnload(e)
{
  if (dirty.value || busy.value)
  {
    e.preventDefault()
    e.returnValue = ''
  }
}
onMounted(() =>
{
  load()
  window.addEventListener('beforeunload', beforeUnload)
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
</script>

<template>
<div class="filing-page">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">就业备案</h1>
      <p>填写毕业去向，查看备案审核进度。</p>
    </div>
    <ElButton
      :loading="loading"
      :disabled="busy"
      @click="load"
    >刷新记录</ElButton>
  </header>
  <ElAlert
    v-if="session.demo"
    title="当前为页面预览，可以体验填写，正式提交请使用学生账号登录。"
    type="info"
    :closable="false"
    show-icon
  />
  <section
    v-if="loading"
    class="filing-surface"
    aria-busy="true"
    aria-label="正在查询备案"
  >
    <ElSkeleton
      :rows="7"
      animated
    />
  </section>
  <section
    v-else-if="loadError"
    class="filing-surface"
    role="alert"
  >
    <h2>暂时无法查询备案</h2>
    <p>{{ loadError }}</p>
    <ElButton
      type="primary"
      @click="load"
    >重新查询</ElButton>
  </section>
  <template v-else-if="latest && !newApplication">
    <section
      class="filing-surface progress-surface"
      aria-labelledby="progress-title"
    >
      <div class="progress-heading">
        <div>
          <p class="eyebrow">最新备案进度</p>
          <h2 id="progress-title">{{ state.title }}</h2>
        </div>
        <ElIcon
          :size="38"
          aria-hidden="true"
        >
          <DocumentChecked />
        </ElIcon>
      </div>
      <p class="status-note">{{ state.note }}</p>
      <ElButton
        v-if="Number(latest.status) === 30"
        type="primary"
        @click="startNew"
      >创建新提交</ElButton>
      <ElSteps
        v-if="state.active !== null"
        :active="state.active"
        finish-status="success"
        align-center
        class="progress-steps"
      >
        <ElStep title="提交备案" />
        <ElStep title="辅导员初审" />
        <ElStep title="院系复审" />
        <ElStep title="已归档" />
      </ElSteps>
      <ElAlert
        v-else
        :title="state.title === '已打回' ? '审核已中止，等待处理' : '暂无法展示审核阶段'"
        :type="state.type"
        :closable="false"
        show-icon
      />
      <p class="record-time">
        记录创建时间：{{ dateText(latest.createdAt) }}<span>详细审核意见暂未开放查询</span>
      </p>
    </section>
    <section class="filing-surface">
      <div class="detail-heading">
        <h2>备案详情</h2>
        <div>
          <ElTag type="info">提交后只读</ElTag>
          <ElButton @click="openHistory">历史记录</ElButton>
        </div>
      </div>
      <dl class="detail-grid">
        <div
          v-for="(label, key) in {
              studentName: '姓名',
              studentNo: '学号',
              college: '学院',
              major: '专业',
              className: '班级',
              graduationYear: '毕业年份',
            }"
          :key="key"
        >
          <dt>{{ label }}</dt>
          <dd>{{ display(latest[key]) }}</dd>
        </div>
      </dl>
      <section
        v-for="section in filingSections"
        :key="section.title"
        class="detail-section"
      >
        <h3>{{ section.title }}</h3>
        <dl class="detail-grid">
          <div
            v-for="field in section.fields"
            :key="field.key"
          >
            <dt>{{ field.label }}</dt>
            <dd>{{ display(latest[field.key]) }}</dd>
          </div>
        </dl>
      </section>
    </section>
  </template>
  <section
    v-else-if="submitted"
    class="filing-surface"
    role="status"
  >
    <h2>提交已成功，正在等待记录更新</h2>
    <p>请稍后刷新查看审核进度，无需重复提交。</p>
    <ElButton
      type="primary"
      @click="load"
    >查询进度</ElButton>
  </section>
  <div
    v-else
    class="filing-workspace"
  >
    <section class="filing-surface">
      <div class="detail-heading">
        <div>
          <p class="eyebrow">{{ newApplication ? '原归档记录保留' : '尚无备案记录' }}</p>
          <h2>{{ newApplication ? '提交变更申请' : '添加就业备案' }}</h2>
        </div>
        <ElTag effect="plain">{{ newApplication ? '变更申请' : '首次填报' }}</ElTag>
      </div>
      <div class="student-strip">
        <strong>{{ session.user?.name || '学生' }}</strong><span>{{ session.user?.studentNo }}</span><span>{{ session.user?.major }}</span>
        <RouterLink to="/profile">查看个人资料</RouterLink>
      </div>
      <ElForm
        label-position="top"
        :model="model"
        @submit.prevent="submit"
      >
        <ElFormItem
          v-if="newApplication"
          label="变更原因"
          for="changeReason"
          required
          :error="errors.changeReason"
        >
          <ElInput
            id="changeReason"
            v-model="changeReason"
            type="textarea"
            :rows="3"
            :maxlength="1000"
            show-word-limit
            :disabled="busy || uncertain"
          />
        </ElFormItem>
        <div
          v-if="submitError || Object.keys(errors).length"
          ref="summary"
          tabindex="-1"
          role="alert"
          class="error-summary"
        >
          <strong>{{ submitError || '请核对以下信息' }}</strong><a
            v-for="(message, key) in errors"
            :key="key"
            :href="'#filing-' + key"
          >{{
              message
            }}</a>
        </div>
        <section
          v-for="section in filingSections"
          :key="section.title"
          class="form-section"
        >
          <h3>{{ section.title }}</h3>
          <div class="filing-fields">
            <ElFormItem
              v-for="field in section.fields"
              :key="field.key"
              :label="field.label"
              :for="'filing-' + field.key"
              :required="field.required"
              :error="errors[field.key]"
              :class="{ wide: field.type === 'textarea' || field.key === 'workAddress' }"
            >
              <ElDatePicker
                v-if="field.type === 'date'"
                :id="'filing-' + field.key"
                v-model="model[field.key]"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择签约日期（选填）"
                :disabled="busy"
              />
              <ElInput
                v-else
                :id="'filing-' + field.key"
                v-model="model[field.key]"
                :type="field.type === 'number' ? 'text' : field.type || 'text'"
                :inputmode="field.type === 'number' ? 'decimal' : undefined"
                :maxlength="field.max"
                :rows="3"
                :disabled="busy"
                :aria-invalid="Boolean(errors[field.key])"
                :placeholder="field.required ? '请输入' + field.label : '选填'"
              />
            </ElFormItem>
          </div>
        </section>
        <div class="submit-bar">
          <p>提交后信息将进入审核流程，请认真核对。</p>
          <ElButton
            v-if="uncertain"
            type="primary"
            @click="load"
          >刷新核实提交结果</ElButton>
          <ElButton
            v-else
            type="primary"
            native-type="submit"
            :loading="busy"
            :disabled="session.demo"
          >{{ busy ? '正在处理' : '提交就业备案' }}</ElButton>
        </div>
      </ElForm>
    </section>
    <aside class="filing-help">
      <h2>填报须知</h2>
      <ol>
        <li>
          <strong>核对信息</strong>
          <p>姓名、学号及院系信息以学校维护的资料为准。</p>
        </li>
        <li>
          <strong>提交审核</strong>
          <p>提交后仅可查看。审核期间不能重复申请。</p>
        </li>
        <li>
          <strong>跟进进度</strong>
          <p>辅导员初审、院系复审通过后，备案自动进入已归档状态。</p>
        </li>
      </ol>
      <div class="availability">
        <h3>后续开放</h3>
        <p>佐证材料上传、变更申请和打回重提暂未开放。</p>
        <p>当前填写单位就业信息，其他毕业去向暂未开放。</p>
      </div>
    </aside>
  </div>
  <ElDialog
    v-model="historyOpen"
    title="历史备案记录"
    width="760px"
    class="filing-dialog"
  >
    <div
      v-if="historyBusy"
      role="status"
    >正在查询历史记录…</div>
    <div
      v-else-if="historyError"
      role="alert"
    >
      <p>{{ historyError }}</p>
      <ElButton @click="openHistory">重试</ElButton>
    </div>
    <ElEmpty
      v-else-if="!history.length"
      description="暂无历史备案记录"
    />
    <div
      v-else
      class="history-list"
    >
      <p>按服务器返回顺序展示，最新一次备案为准。</p>
      <button
        v-for="(record, index) in history"
        :key="index"
        class="history-row"
        @click="selected = record"
      >
        <span><strong>{{ record.companyName || '就业备案' }}</strong><small>{{ dateText(record.createdAt) }}</small></span><span>{{ filingState(record.status).title }}
          · 查看</span>
      </button>
    </div>
  </ElDialog>
  <ElDialog
    :model-value="Boolean(selected)"
    title="历史备案详情"
    width="680px"
    class="filing-dialog"
    @close="selected = null"
  ><template v-if="selected">
      <p>{{ filingState(detail.status).title }} · {{ dateText(detail.createdAt) }}</p>
      <dl class="detail-grid">
        <div
          v-for="field in filingSections.flatMap((s) => s.fields)"
          :key="field.key"
        >
          <dt>{{ field.label }}</dt>
          <dd>{{ display(detail[field.key]) }}</dd>
        </div>
      </dl>
    </template></ElDialog>
</div>
</template>

<style scoped>
.filing-page
{
  display: grid;
  gap: 24px;
}

.page-heading,
.detail-heading,
.progress-heading
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-heading p,
.eyebrow
{
  color: var(--muted);
  margin-top: 6px;
  font-size: 13px;
}

.filing-surface
{
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 32px;
  min-width: 0;
}

.filing-surface h2
{
  font-size: 21px;
  font-weight: 600;
}

.filing-surface>p
{
  margin: 16px 0;
}

.filing-workspace
{
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 32px;
  align-items: start;
}

.eyebrow
{
  margin: 0 0 8px;
}

.student-strip
{
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
  background: #f4f6fa;
  padding: 16px;
  margin: 24px 0 8px;
  font-size: 13px;
}

.student-strip a
{
  margin-left: auto;
  min-height: 44px;
  display: flex;
  align-items: center;
}

.form-section
{
  padding-top: 24px;
}

.form-section h3,
.detail-section h3
{
  font-size: 16px;
  margin-bottom: 20px;
}

.filing-fields
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 24px;
}

.wide
{
  grid-column: 1 / -1;
}

.filing-fields :deep(.el-date-editor.el-input)
{
  width: 100%;
}

.filing-fields :deep(.el-input__wrapper)
{
  min-height: 44px;
}

.submit-bar
{
  border-top: 1px solid var(--line);
  padding-top: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.submit-bar p,
.filing-help p
{
  font-size: 13px;
  color: var(--muted);
  line-height: 1.9;
}

.filing-page :deep(.el-button)
{
  min-height: 44px;
}

.filing-help
{
  padding: 24px 0;
}

.filing-help h2
{
  font-size: 17px;
}

.filing-help ol
{
  padding-left: 22px;
  margin: 24px 0 32px;
}

.filing-help li
{
  padding-left: 6px;
  margin-bottom: 24px;
  color: var(--primary);
}

.filing-help strong
{
  font-size: 14px;
}

.filing-help p
{
  margin-top: 8px;
}

.availability
{
  border-top: 1px solid var(--line);
  padding-top: 24px;
}

.availability h3
{
  font-size: 14px;
}

.progress-surface
{
  border-top: 4px solid var(--primary);
}

.progress-heading
{
  color: var(--primary);
}

.status-note
{
  color: var(--muted);
  font-size: 14px;
}

.progress-steps
{
  margin: 32px 0;
}

.record-time
{
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  font-size: 12px;
  color: var(--muted);
}

.detail-heading>div:last-child
{
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-heading>div:first-child
{
  display: block;
}

.detail-grid
{
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  margin: 24px 0 0;
}

.detail-grid dt
{
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 8px;
}

.detail-grid dd
{
  margin: 0;
  font-size: 14px;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.detail-section
{
  border-top: 1px solid var(--line);
  margin-top: 28px;
  padding-top: 24px;
}

.history-list>p
{
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 16px;
}

.history-row
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  width: 100%;
  text-align: left;
  background: white;
  color: var(--ink);
  border: 0;
  border-bottom: 1px solid var(--line);
  padding: 20px 8px;
}

.history-row:hover
{
  background: #eef2f8;
}

.history-row small
{
  display: block;
  margin-top: 8px;
  color: var(--muted);
}

@media (max-width: 1000px)
{
  .filing-workspace
  {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .filing-help
  {
    padding: 24px 8px;
  }
}

@media (max-width: 600px)
{
  .filing-surface
  {
    padding: 22px 16px;
  }

  .filing-fields,
  .detail-grid
  {
    grid-template-columns: 1fr;
  }

  .detail-heading,
  .submit-bar
  {
    align-items: flex-start;
    flex-direction: column;
  }

  .progress-steps :deep(.el-step__title)
  {
    font-size: 12px;
    line-height: 1.7;
  }

  .history-row
  {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
<style>
.filing-dialog
{
  max-width: calc(100vw - 32px);
}
</style>
