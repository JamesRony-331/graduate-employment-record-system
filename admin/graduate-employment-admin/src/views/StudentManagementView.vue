<script setup>
import
{
  computed,
  ref,
  reactive,
  onMounted,
  onBeforeUnmount,
  nextTick
}
from 'vue';
import
{
  onBeforeRouteLeave
}
from 'vue-router';
import
{
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElSelect,
  ElOption,
  ElDialog,
  ElEmpty,
  ElInputNumber
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
  studentPage,
  managedClasses,
  studentDetail,
  saveStudent
}
from '../api/student';
import
{
  collegeOptions
}
from '../api/organization';
import
{
  studentModel,
  studentErrors,
  studentPayload,
  editableFields
}
from '../api/studentContracts';
import
{
  session
}
from '../stores/session';
const rows = ref([]),
  classes = ref([]),
  colleges = ref([]);
const page = ref(1),
  size = ref(10),
  total = ref(0),
  pages = ref(0);
const collegeId = ref(null),
  classId = ref(null),
  keyword = ref('');
const loading = ref(false),
  scopeLoading = ref(false),
  scopeError = ref(''),
  error = ref(''),
  collegeError = ref('');
const opened = ref(false),
  mode = ref('detail'),
  original = ref(null),
  detailLoading = ref(false),
  saving = ref(false);
const model = reactive(studentModel()),
  errors = ref(
  {}),
  dialogError = ref(''),
  baseline = ref(''),
  summary = ref(null);
let listSequence = 0,
  detailSequence = 0,
  alive = true;
const collegeName = id => colleges.value.find(c => String(c.id) === String(id))?.collegeName ||
  `学院 ID：${id}`;
const scopeColleges = computed(() => [...new Set(classes.value.map(c => c.collegeId).filter(id =>
  id != null))].map(id => (
{
  id,
  name: collegeName(id)
})));
const filterClasses = computed(() => classes.value.filter(c => collegeId.value == null || String(c
  .collegeId) === String(collegeId.value)));
const selectedClass = computed(() => classes.value.find(c => String(c.id) === String(model
  .classId)));
const filtered = computed(() => rows.value.filter(s => [s.studentNo, s.name].some(v => String(v ||
  '').toLowerCase().includes(keyword.value.trim().toLowerCase()))));
const dirty = computed(() => opened.value && mode.value !== 'detail' && JSON.stringify(model) !==
  baseline.value);
const fields = computed(() => editableFields(mode.value === 'edit'));
const title = computed(() => (
{
  add: '新增学生',
  edit: '编辑学生资料',
  detail: '学生详情'
})[mode.value]);
const detailFields = [
  ['学号', 'studentNo'],
  ['姓名', 'name'],
  ['学院', 'college'],
  ['专业', 'major'],
  ['班级', 'className'],
  ['身份证号', 'idCard'],
  ['性别', 'gender'],
  ['出生日期', 'birthDate'],
  ['入学年份', 'enrollmentYear'],
  ['毕业年份', 'graduationYear'],
  ['学历层次', 'educationLevel'],
  ['联系电话', 'phone'],
  ['电子邮箱', 'email'],
  ['联系地址', 'address']
];

function display(row, key)
{
  const value = row?.[key];
  if (value == null || value === '')
    return '未填写';
  if (key === 'gender')
    return Number(value) === 1 ? '男' : Number(value) === 0 ? '女' : '未记录';
  if (key === 'birthDate')
    return String(value).slice(0, 10);
  return value;
}
async function load(target = page.value)
{
  const sequence = ++listSequence;
  loading.value = true;
  error.value = '';
  try
  {
    let data = await studentPage(target, size.value, collegeId.value, classId.value);
    if (sequence !== listSequence || !alive)
      return;
    if (target > Math.max(1, data.total))
    {
      target = Math.max(1, data.total);
      data = await studentPage(target, size.value, collegeId.value, classId.value);
    }
    if (sequence !== listSequence || !alive)
      return;
    rows.value = data.records;
    total.value = data.totalCount;
    pages.value = data.total;
    page.value = target;
  }
  catch (e)
  {
    if (sequence === listSequence && alive)
    {
      error.value = e.message;
      rows.value = [];
    }
  }
  finally
  {
    if (sequence === listSequence && alive)
      loading.value = false;
  }
}
async function loadScope()
{
  scopeLoading.value = true;
  scopeError.value = '';
  try
  {
    const data = await managedClasses();
    if (alive)
      classes.value = data;
  }
  catch (e)
  {
    if (alive)
    {
      scopeError.value = e.message;
      classes.value = [];
    }
  }
  finally
  {
    if (alive)
      scopeLoading.value = false;
  }
}
async function loadCollegeNames()
{
  collegeError.value = '';
  try
  {
    const data = await collegeOptions();
    if (alive)
      colleges.value = data;
  }
  catch (e)
  {
    if (alive)
      collegeError.value = e.message;
  }
}

function changeCollege()
{
  classId.value = null;
  load(1);
}
async function fetchDetail(uuid)
{
  const sequence = ++detailSequence;
  detailLoading.value = true;
  dialogError.value = '';
  try
  {
    const data = await studentDetail(uuid);
    if (sequence !== detailSequence || !opened.value || !alive)
      return;
    original.value = data;
    Object.assign(model, studentModel(data));
    baseline.value = JSON.stringify(model);
  }
  catch (e)
  {
    if (sequence === detailSequence && alive)
      dialogError.value = e.message;
  }
  finally
  {
    if (sequence === detailSequence && alive)
      detailLoading.value = false;
  }
}

function open(nextMode, row)
{
  mode.value = nextMode;
  original.value = row ?
  {
    uuid: row.uuid
  } : null;
  Object.assign(model, studentModel());
  errors.value = {};
  dialogError.value = '';
  if (nextMode === 'add' && classId.value != null)
    model.classId = classId.value;
  baseline.value = JSON.stringify(model);
  opened.value = true;
  if (row)
    fetchDetail(row.uuid);
}
async function canLeave()
{
  if (!session.token)
    return true;
  if (saving.value)
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
    if (typeof done === 'function')
      done();
  }
}
async function submit()
{
  if (saving.value || detailLoading.value || scopeLoading.value || scopeError.value)
    return;
  errors.value = studentErrors(model, mode.value === 'edit' ? original.value : null, classes
    .value);
  dialogError.value = '';
  if (Object.keys(errors.value).length)
  {
    await nextTick();
    summary.value?.focus();
    return;
  }
  const body = studentPayload(model, mode.value === 'edit' ? original.value : null);
  if (mode.value === 'edit' && Object.keys(body).length === 1)
  {
    ElMessage.info('没有需要保存的修改');
    return;
  }
  saving.value = true;
  try
  {
    await saveStudent(body, mode.value === 'edit');
    opened.value = false;
    ElMessage.success(mode.value === 'add' ? '学生与登录账号已创建' : '学生资料已保存');
    if (mode.value === 'add')
    {
      collegeId.value = null;
      classId.value = null;
      keyword.value = '';
    }
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

function beforeUnload(e)
{
  if (dirty.value)
  {
    e.preventDefault();
    e.returnValue = '';
  }
}
onBeforeRouteLeave(canLeave);
onMounted(() =>
{
  load();
  loadScope();
  loadCollegeNames();
  window.addEventListener('beforeunload', beforeUnload);
});
onBeforeUnmount(() =>
{
  alive = false;
  listSequence++;
  detailSequence++;
  window.removeEventListener('beforeunload', beforeUnload);
});
</script>
<template>
<div class="page-stack student-management">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">学生管理</h1>
      <p class="subtext">维护本人管理范围内的学生资料与班级归属。</p>
    </div>
    <ElButton
      type="primary"
      :icon="Plus"
      :disabled="scopeLoading || !!scopeError || !classes.length"
      @click="open('add')"
    >新增学生</ElButton>
  </header>
  <section
    class="surface"
    :aria-busy="loading"
  >
    <div class="section-heading">
      <h2>学生名册</h2>
      <ElButton
        :icon="Refresh"
        :loading="loading"
        @click="load()"
      >刷新名册</ElButton>
    </div>
    <div
      v-if="scopeError"
      class="student-feedback"
      role="alert"
    >
      <ElAlert
        :title="scopeError"
        description="仅有效辅导员和院系管理员可管理各自范围内的学生，请核对角色与班级关联。"
        type="warning"
        :closable="false"
        show-icon
      />
      <ElButton @click="loadScope(); load(1)">重新加载管理范围</ElButton>
    </div>
    <div
      v-if="collegeError"
      class="student-feedback"
    >
      <ElAlert
        title="学院名称暂不可用，暂以学院 ID 显示"
        type="warning"
        :closable="false"
      />
      <ElButton @click="loadCollegeNames">重试学院名称</ElButton>
    </div>
    <div class="student-filters">
      <label>学院<ElSelect
          v-model="collegeId"
          clearable
          filterable
          placeholder="全部管理学院"
          :disabled="scopeLoading || !!scopeError"
          @change="changeCollege"
        >
          <ElOption
            v-for="c in scopeColleges"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </ElSelect></label>
      <label>班级<ElSelect
          v-model="classId"
          clearable
          filterable
          placeholder="全部管理班级"
          :disabled="scopeLoading || !!scopeError"
          @change="load(1)"
        >
          <ElOption
            v-for="c in filterClasses"
            :key="c.id"
            :label="c.className"
            :value="c.id"
          />
        </ElSelect></label>
      <label>本页搜索
        <ElInput
          v-model="keyword"
          :prefix-icon="Search"
          clearable
          placeholder="姓名或学号"
        />
      </label>
    </div>
    <p
      class="muted student-count"
      aria-live="polite"
    >{{ error ? '学生数据暂不可用' : `共 ${total} 名学生，本页显示 ${filtered.length} 名` }}</p>
    <div
      v-if="error"
      role="alert"
      class="student-feedback"
    >
      <ElAlert
        :title="error"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton @click="load()">重新加载</ElButton>
    </div>
    <p
      v-else-if="loading"
      class="student-loading"
      role="status"
    >正在加载学生名册…</p>
    <ElEmpty
      v-else-if="!filtered.length"
      :description="keyword ? '本页没有符合条件的学生' : !classes.length && !scopeLoading ? '暂无可管理班级，请联系学校分配' : '当前范围内暂无学生'"
    >
      <ElButton
        v-if="keyword"
        @click="keyword = ''"
      >清除搜索</ElButton>
      <ElButton
        v-else-if="classes.length"
        type="primary"
        @click="open('add')"
      >新增学生</ElButton>
    </ElEmpty>
    <template v-else>
      <ElTable
        :data="filtered"
        row-key="uuid"
        class="student-table"
      >
        <ElTableColumn
          prop="studentNo"
          label="学号"
          min-width="150"
        />
        <ElTableColumn
          prop="name"
          label="姓名"
          min-width="110"
        />
        <ElTableColumn
          label="学院 / 专业"
          min-width="220"
        ><template
            #default="{ row }"><span>{{ display(row, 'college') }}</span><small>{{ display(row, 'major') }}</small></template>
        </ElTableColumn>
        <ElTableColumn
          prop="className"
          label="班级"
          min-width="170"
        />
        <ElTableColumn
          label="毕业年份"
          width="110"
        ><template #default="{ row }">{{ display(row, 'graduationYear') }}</template>
        </ElTableColumn>
        <ElTableColumn
          label="联系电话"
          min-width="150"
        ><template #default="{ row }">{{ display(row, 'phone') }}</template></ElTableColumn>
        <ElTableColumn
          label="操作"
          width="180"
          fixed="right"
        ><template #default="{ row }">
            <ElButton
              link
              type="primary"
              :aria-label="`查看${row.name}详情`"
              @click="open('detail', row)"
            >查看详情</ElButton>
            <ElButton
              link
              type="primary"
              :disabled="!!scopeError || scopeLoading"
              :aria-label="`编辑${row.name}`"
              @click="open('edit', row)"
            >编辑</ElButton>
          </template></ElTableColumn>
      </ElTable>
      <div class="student-mobile">
        <article
          v-for="row in filtered"
          :key="row.uuid"
        >
          <h3>{{ row.name }}</h3>
          <p>{{ row.studentNo }}</p>
          <p>{{ display(row, 'className') }}</p>
          <p class="muted">{{ display(row, 'college') }} / {{ display(row, 'major') }}</p>
          <ElButton
            :aria-label="`查看${row.name}详情`"
            @click="open('detail', row)"
          >查看详情</ElButton>
          <ElButton
            :disabled="!!scopeError || scopeLoading"
            :aria-label="`编辑${row.name}`"
            @click="open('edit', row)"
          >编辑</ElButton>
        </article>
      </div>
    </template>
    <footer class="student-footer">
      <div>
        <ElSelect
          v-model="size"
          aria-label="每页条数"
          :disabled="loading"
          @change="load(1)"
        >
          <ElOption
            v-for="n in [10,20,50]"
            :key="n"
            :value="n"
            :label="`每页 ${n} 条`"
          />
        </ElSelect><span class="muted">学院、班级筛选全部记录；姓名、学号搜索仅限本页。</span>
      </div>
      <nav aria-label="学生分页">
        <ElButton
          :disabled="loading || !!error || page <= 1"
          @click="load(page - 1)"
        >上一页</ElButton><span>第 {{ page }} / {{ Math.max(1, pages) }} 页</span>
        <ElButton
          :disabled="loading || !!error || page >= pages"
          @click="load(page + 1)"
        >下一页</ElButton>
      </nav>
    </footer>
  </section>
  <ElDialog
    v-model="opened"
    :title="title"
    width="720px"
    class="student-dialog"
    :before-close="close"
    :show-close="!saving"
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    destroy-on-close
  >
    <p
      v-if="detailLoading"
      class="student-loading"
      role="status"
    >正在读取最新资料…</p>
    <div
      v-else-if="mode !== 'add' && !original?.studentNo"
      class="student-feedback"
    >
      <ElAlert
        :title="dialogError || '学生详情暂不可用'"
        type="error"
        :closable="false"
      />
      <ElButton @click="fetchDetail(original.uuid)">重试</ElButton>
    </div>
    <dl
      v-else-if="mode === 'detail'"
      class="student-details"
    >
      <div
        v-for="[label,key] in detailFields"
        :key="key"
      >
        <dt>{{ label }}</dt>
        <dd>{{ display(original,key) }}</dd>
      </div>
    </dl>
    <ElForm
      v-else
      label-position="top"
      :disabled="saving"
      @submit.prevent="submit"
    >
      <p class="student-note">
        {{ mode === 'add' ? '学号将作为登录账号，创建后不可修改。初始密码为身份证号后6位（末位为X时与录入大小写一致）。学院、专业随所选班级确定。' : `学号：${original.studentNo}（不可修改）。调整班级后，学院与专业自动同步。` }}
      </p>
      <div
        v-if="dialogError || Object.keys(errors).length"
        ref="summary"
        class="error-summary"
        role="alert"
        tabindex="-1"
      ><strong>{{ dialogError || '请核对以下信息' }}</strong><a
          v-for="(message,key) in errors"
          :key="key"
          :href="'#student-' + key"
        >{{ message }}</a></div>
      <div class="form-grid">
        <ElFormItem
          v-for="f in fields"
          :key="f.key"
          :label="f.label"
          :for="'student-' + f.key"
          :required="f.required"
          :error="errors[f.key]"
        >
          <ElSelect
            v-if="f.type === 'class'"
            :id="'student-' + f.key"
            v-model="model.classId"
            filterable
            :loading="scopeLoading"
            :disabled="!!scopeError || scopeLoading"
            placeholder="请选择管理班级"
          >
            <ElOption
              v-for="c in classes"
              :key="c.id"
              :label="`${c.className}（${collegeName(c.collegeId)}）`"
              :value="c.id"
              :disabled="!c.majorId || !c.collegeId"
            />
          </ElSelect>
          <ElInputNumber
            v-else-if="f.type === 'number'"
            :id="'student-' + f.key"
            v-model="model[f.key]"
            :min="1900"
            :max="9999"
            :precision="0"
            controls-position="right"
          />
          <ElInput
            v-else
            :id="'student-' + f.key"
            v-model="model[f.key]"
            type="text"
            :maxlength="f.max"
            autocomplete="off"
          />
        </ElFormItem>
      </div>
      <p
        v-if="selectedClass"
        class="student-note"
      >所属学院：{{ collegeName(selectedClass.collegeId) }}　专业：{{ selectedClass.majorName || '未设置' }}</p>
      <p
        v-if="mode === 'edit'"
        class="muted"
      >已有信息暂不支持清空；密码请通过账号相关功能维护。</p>
      <div class="form-actions">
        <ElButton
          :disabled="saving"
          @click="close()"
        >取消</ElButton>
        <ElButton
          type="primary"
          native-type="submit"
          :loading="saving"
          :disabled="scopeLoading || !!scopeError || !classes.length || (mode === 'edit' && !dirty)"
        >{{ mode === 'add' ? '创建学生与账号' : '保存修改' }}</ElButton>
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
.student-filters
{
  display: grid;
  grid-template-columns: 1fr 1fr 1.2fr;
  gap: 20px;
}

.student-filters label
{
  display: grid;
  gap: 8px;
  font-size: 14px;
  color: var(--muted);
}

.student-count
{
  margin: 20px 0;
  font-size: 14px;
}

.student-table small
{
  display: block;
  color: var(--muted);
  margin-top: 4px;
}

.student-feedback
{
  display: grid;
  gap: 12px;
  justify-items: start;
  margin-bottom: 20px;
}

.student-footer,
.student-footer>div,
.student-footer nav
{
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.student-footer
{
  justify-content: space-between;
  border-top: 1px solid var(--line);
  margin-top: 24px;
  padding-top: 20px;
  font-size: 13px;
}

.student-footer .el-select
{
  width: 130px;
}

.student-footer .el-button
{
  margin: 0;
}

.student-loading
{
  padding: 48px 0;
  text-align: center;
  color: var(--muted);
}

.student-details
{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.student-details dt
{
  color: var(--muted);
  font-size: 13px;
}

.student-details dd
{
  margin: 8px 0 0;
  overflow-wrap: anywhere;
}

.student-note
{
  margin: 0 0 24px;
  line-height: 1.7;
  color: var(--muted);
}

.student-mobile
{
  display: none;
}

:deep(.el-input-number),
:deep(.el-select)
{
  width: 100%;
}

@media(max-width:700px)
{

  .student-filters,
  .student-details
  {
    grid-template-columns: 1fr;
  }

  .student-table
  {
    display: none;
  }

  .student-mobile
  {
    display: block;
  }

  .student-mobile article
  {
    border-top: 1px solid var(--line);
    padding: 20px 0;
  }

  .student-mobile p
  {
    margin: 8px 0;
    overflow-wrap: anywhere;
  }

  .student-mobile .el-button
  {
    min-height: 44px;
  }
}
</style>
<style>
.student-dialog
{
  max-width: calc(100vw - 32px);
}

@media(max-width:700px)
{
  .student-dialog .form-grid
  {
    grid-template-columns: 1fr;
  }
}
</style>
