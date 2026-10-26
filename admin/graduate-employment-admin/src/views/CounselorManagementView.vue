<script setup>
import
{
  computed,
  nextTick,
  onMounted,
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
  ElDatePicker,
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
  listCounselors,
  addCounselor,
  updateCounselor,
  removeCounselor
}
from '../api/counselor';
import
{
  counselorErrors,
  counselorPayload
}
from '../api/counselorContracts';

const rows = ref([]),
  loading = ref(false),
  loadError = ref('');
const page = ref(1),
  size = 10,
  keyword = ref('');
const dialog = ref(false),
  editing = ref(false),
  saving = ref(false),
  removing = ref(null);
const detailsOpen = ref(false),
  selectedCounselor = ref(null);

function openDetails(row)
{
  selectedCounselor.value = {
    ...row
  };
  detailsOpen.value = true;
}
const errors = ref(
  {}),
  saveError = ref(''),
  summary = ref(null);
const model = reactive(
{});
const baseline = ref('');
const dirty = computed(() => dialog.value && JSON.stringify(model) !== baseline.value);
const visibleRows = computed(() => rows.value.filter(row =>
  `${row.name} ${row.counselorNo} ${row.phone || ''}`.toLowerCase().includes(keyword.value
    .trim().toLowerCase())));
const genderText = value => (
{
  0: '未设置',
  1: '男',
  2: '女'
})[value] || '未设置';
async function load(target = page.value)
{
  if (loading.value)
    return;
  loading.value = true;
  loadError.value = '';
  try
  {
    const data = await listCounselors(target, size);
    if (!data.length && target > 1)
    {
      const previous = await listCounselors(target - 1, size);
      rows.value = previous;
      page.value = target - 1;
      ElMessage.info('已到最后一页');
    }
    else
    {
      rows.value = data;
      page.value = target;
    }
  }
  catch (error)
  {
    rows.value = [];
    loadError.value = error.message;
  }
  finally
  {
    loading.value = false;
  }
}

function openEditor(row)
{
  editing.value = Boolean(row);
  Object.assign(model,
  {
    id: row?.id,
    counselorNo: row?.counselorNo || '',
    name: row?.name || '',
    gender: row?.gender ?? 0,
    phone: row?.phone || '',
    email: row?.email || '',
    hireDate: row?.hireDate || '',
    status: row?.status ?? 1,
  });
  errors.value = {};
  saveError.value = '';
  baseline.value = JSON.stringify(model);
  dialog.value = true;
}
async function canDiscard()
{
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
      type: 'warning',
    });
    return true;
  }
  catch
  {
    return false;
  }
}
async function closeEditor(done)
{
  if (await canDiscard())
  {
    dialog.value = false;
    if (typeof done === 'function')
      done();
  }
}
async function submit()
{
  if (saving.value)
    return;
  errors.value = counselorErrors(model, editing.value);
  saveError.value = '';
  if (Object.keys(errors.value).length)
  {
    await nextTick();
    summary.value?.focus();
    return;
  }
  saving.value = true;
  try
  {
    const payload = counselorPayload(model, editing.value);
    await (editing.value ? updateCounselor(payload) : addCounselor(payload));
    ElMessage.success(editing.value ? '辅导员资料已保存' : '辅导员已新增');
    dialog.value = false;
    await load(editing.value ? page.value : 1);
  }
  catch (error)
  {
    saveError.value = error.message;
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
  if (removing.value !== null)
    return;
  try
  {
    await ElMessageBox.confirm(`将“${row.name}”标记为离职，资料会保留。`, '确认删除辅导员？',
    {
      confirmButtonText: '确认离职',
      cancelButtonText: '取消',
      type: 'warning',
    });
  }
  catch
  {
    return;
  }
  removing.value = row.id;
  try
  {
    await removeCounselor(row.id);
    ElMessage.success('辅导员已标记为离职');
    await load();
  }
  catch (error)
  {
    ElMessage.error(error.message);
  }
  finally
  {
    removing.value = null;
  }
}
onBeforeRouteLeave(() => canDiscard());
onMounted(() => load());
</script>

<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">辅导员管理</h1>
      <p class="subtext">维护辅导员资料、联系方式与在职状态。</p>
    </div>
    <ElButton
      type="primary"
      :icon="Plus"
      :disabled="loading || removing !== null"
      @click="openEditor()"
    >新增辅导员</ElButton>
  </header>
  <section
    class="surface"
    :aria-busy="loading"
    aria-label="辅导员名单"
  >
    <div class="section-heading">
      <h2>辅导员名单</h2>
      <ElButton
        :icon="Refresh"
        :loading="loading"
        :disabled="removing !== null"
        @click="load()"
      >刷新名单</ElButton>
    </div>
    <div class="list-toolbar">
      <ElInput
        v-model="keyword"
        aria-label="筛选本页辅导员"
        placeholder="搜索本页姓名、工号或电话"
        :prefix-icon="Search"
        clearable
      />
      <span class="muted">{{ loading ? '正在加载…' : `本页 ${rows.length} 位辅导员` }}</span>
    </div>
    <div
      v-if="loadError"
      class="load-error"
      role="alert"
    >
      <ElAlert
        title="名单加载失败"
        :description="loadError"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton @click="load()">重新加载</ElButton>
    </div>
    <div
      v-else-if="loading"
      class="loading-state"
      role="status"
    >正在加载辅导员名单…</div>
    <template v-else>
      <ElEmpty
        v-if="!visibleRows.length"
        :description="keyword ? '本页没有符合条件的辅导员' : '暂无辅导员'"
      >
        <ElButton
          v-if="keyword"
          @click="keyword = ''"
        >清除筛选</ElButton>
        <ElButton
          v-else
          type="primary"
          @click="openEditor()"
        >新增辅导员</ElButton>
      </ElEmpty>
      <template v-else>
        <ElTable
          :data="visibleRows"
          class="counselor-table"
          row-key="id"
        >
          <ElTableColumn
            prop="name"
            label="姓名"
            min-width="110"
          />
          <ElTableColumn
            prop="counselorNo"
            label="工号"
            min-width="130"
          />
          <ElTableColumn
            label="性别"
            width="80"
          ><template #default="{ row }">{{ genderText(row.gender) }}</template></ElTableColumn>
          <ElTableColumn
            label="联系方式"
            min-width="210"
          ><template #default="{ row }">
              <div>{{ row.phone || '未填写电话' }}</div>
              <div class="contact-email">{{ row.email || '未填写邮箱' }}</div>
            </template></ElTableColumn>
          <ElTableColumn
            label="入职日期"
            min-width="120"
          ><template #default="{ row }">{{ row.hireDate || '—' }}</template></ElTableColumn>
          <ElTableColumn
            label="状态"
            width="90"
          ><template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '在职' : '离职' }}
              </ElTag>
            </template></ElTableColumn>
          <ElTableColumn
            label="操作"
            width="240"
            fixed="right"
          ><template #default="{ row }">
              <ElButton
                link
                type="primary"
                :aria-label="`查看${row.name}详情`"
                @click="openDetails(row)"
              >查看详情</ElButton>
              <ElButton
                link
                type="primary"
                :disabled="removing !== null"
                :aria-label="`编辑${row.name}`"
                @click="openEditor(row)"
              >编辑</ElButton>
              <ElButton
                link
                type="danger"
                :disabled="row.status === 0 || removing !== null"
                :loading="removing === row.id"
                :aria-label="`删除${row.name}`"
                @click="remove(row)"
              >删除</ElButton>
            </template></ElTableColumn>
        </ElTable>
        <div class="counselor-mobile">
          <article
            v-for="row in visibleRows"
            :key="row.id"
          >
            <div class="mobile-heading">
              <h3>{{ row.name }}</h3>
              <ElTag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '在职' : '离职' }}
              </ElTag>
            </div>
            <dl>
              <div>
                <dt>工号</dt>
                <dd>{{ row.counselorNo }}</dd>
              </div>
              <div>
                <dt>性别</dt>
                <dd>{{ genderText(row.gender) }}</dd>
              </div>
              <div>
                <dt>电话</dt>
                <dd>{{ row.phone || '未填写' }}</dd>
              </div>
              <div>
                <dt>邮箱</dt>
                <dd>{{ row.email || '未填写' }}</dd>
              </div>
              <div>
                <dt>入职日期</dt>
                <dd>{{ row.hireDate || '未填写' }}</dd>
              </div>
            </dl>
            <ElButton
              :aria-label="`查看${row.name}详情`"
              @click="openDetails(row)"
            >查看详情</ElButton>
            <ElButton
              :disabled="removing !== null"
              :aria-label="`编辑${row.name}`"
              @click="openEditor(row)"
            >编辑</ElButton>
            <ElButton
              type="danger"
              plain
              :disabled="row.status === 0 || removing !== null"
              :loading="removing === row.id"
              :aria-label="`删除${row.name}`"
              @click="remove(row)"
            >删除</ElButton>
          </article>
        </div>
      </template>
    </template>
    <footer class="list-footer">
      <p class="muted">删除将标记为离职并保留资料，可通过编辑恢复在职。</p>
      <nav aria-label="辅导员分页">
        <ElButton
          :disabled="page <= 1 || loading || !!loadError || removing !== null"
          @click="load(page - 1)"
        >上一页</ElButton><span>第 {{ page }} 页</span>
        <ElButton
          :disabled="rows.length < size || loading || !!loadError || removing !== null"
          @click="load(page + 1)"
        >下一页</ElButton>
      </nav>
    </footer>
  </section>
  <ElDialog
    v-model="detailsOpen"
    title="辅导员详情"
    width="640px"
    class="counselor-dialog"
    destroy-on-close
  >
    <template v-if="selectedCounselor">
      <div class="detail-heading">
        <h3>{{ selectedCounselor.name }}</h3>
        <ElTag :type="selectedCounselor.status === 1 ? 'success' : 'info'">
          {{ selectedCounselor.status === 1 ? '在职' : selectedCounselor.status === 0 ? '离职' : '未设置' }}
        </ElTag>
      </div>
      <dl class="counselor-details">
        <div>
          <dt>工号</dt>
          <dd>{{ selectedCounselor.counselorNo || '未填写' }}</dd>
        </div>
        <div>
          <dt>性别</dt>
          <dd>{{ genderText(selectedCounselor.gender) }}</dd>
        </div>
        <div>
          <dt>联系电话</dt>
          <dd>{{ selectedCounselor.phone || '未填写' }}</dd>
        </div>
        <div>
          <dt>电子邮箱</dt>
          <dd>{{ selectedCounselor.email || '未填写' }}</dd>
        </div>
        <div>
          <dt>入职日期</dt>
          <dd>{{ selectedCounselor.hireDate || '未填写' }}</dd>
        </div>
      </dl>
    </template>
    <template #footer>
      <ElButton @click="detailsOpen = false">关闭</ElButton>
    </template>
  </ElDialog>
  <ElDialog
    v-model="dialog"
    :title="editing ? '编辑辅导员' : '新增辅导员'"
    width="640px"
    class="counselor-dialog"
    :before-close="closeEditor"
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    :show-close="!saving"
    destroy-on-close
  >
    <ElForm
      label-position="top"
      :disabled="saving"
      @submit.prevent="submit"
    >
      <div
        v-if="saveError || Object.keys(errors).length"
        ref="summary"
        class="error-summary"
        tabindex="-1"
        role="alert"
      >
        <strong>{{ saveError || '请核对以下信息' }}</strong><a
          v-for="(message, key) in errors"
          :key="key"
          :href="'#counselor-' + key"
        >{{ message }}</a>
      </div>
      <p
        v-if="editing"
        class="edit-note"
      >工号、姓名和入职日期不可修改。</p>
      <div class="form-grid">
        <ElFormItem
          label="工号"
          for="counselor-counselorNo"
          :required="!editing"
          :error="errors.counselorNo"
        >
          <ElInput
            id="counselor-counselorNo"
            v-model="model.counselorNo"
            :readonly="editing"
            placeholder="请输入辅导员工号"
          />
        </ElFormItem>
        <ElFormItem
          label="姓名"
          for="counselor-name"
          :required="!editing"
          :error="errors.name"
        >
          <ElInput
            id="counselor-name"
            v-model="model.name"
            :readonly="editing"
            placeholder="请输入姓名"
          />
        </ElFormItem>
        <ElFormItem
          label="性别"
          for="counselor-gender"
        >
          <ElSelect
            id="counselor-gender"
            v-model="model.gender"
          >
            <ElOption
              label="未设置"
              :value="0"
            />
            <ElOption
              label="男"
              :value="1"
            />
            <ElOption
              label="女"
              :value="2"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem
          label="入职日期"
          for="counselor-hireDate"
        >
          <ElDatePicker
            id="counselor-hireDate"
            v-model="model.hireDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="选择入职日期"
            :readonly="editing"
          />
        </ElFormItem>
        <ElFormItem
          label="联系电话"
          for="counselor-phone"
          :error="errors.phone"
        >
          <ElInput
            id="counselor-phone"
            v-model="model.phone"
            maxlength="20"
            type="tel"
            placeholder="请输入联系电话"
          />
        </ElFormItem>
        <ElFormItem
          label="电子邮箱"
          for="counselor-email"
          :error="errors.email"
        >
          <ElInput
            id="counselor-email"
            v-model="model.email"
            maxlength="100"
            placeholder="请输入电子邮箱"
          />
        </ElFormItem>
        <ElFormItem
          v-if="editing"
          label="在职状态"
          for="counselor-status"
        >
          <ElSelect
            id="counselor-status"
            v-model="model.status"
          >
            <ElOption
              label="在职"
              :value="1"
            />
            <ElOption
              label="离职"
              :value="0"
            />
          </ElSelect>
        </ElFormItem>
      </div>
      <div class="form-actions">
        <ElButton
          :disabled="saving"
          @click="closeEditor()"
        >取消</ElButton>
        <ElButton
          type="primary"
          native-type="submit"
          :loading="saving"
          :disabled="editing && !dirty"
        >{{ editing ? '保存修改' : '确认新增' }}</ElButton>
      </div>
    </ElForm>
  </ElDialog>
</div>
</template>

<style scoped>
.detail-heading
{
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.detail-heading h3
{
  font-size: 20px;
  overflow-wrap: anywhere;
}

.counselor-details
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin: 24px 0;
}

.counselor-details dt
{
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 8px;
}

.counselor-details dd
{
  margin: 0;
  color: #202c40;
  overflow-wrap: anywhere;
  line-height: 1.6;
}

.list-toolbar,
.list-footer,
.list-footer nav,
.mobile-heading
{
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: space-between;
}

.list-toolbar
{
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.list-toolbar .el-input
{
  max-width: 380px;
}

.list-footer
{
  border-top: 1px solid var(--line);
  padding-top: 20px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.list-footer nav
{
  gap: 12px;
  font-size: 13px;
  white-space: nowrap;
}

.list-footer nav .el-button
{
  margin: 0;
}

.contact-email
{
  font-size: 12px;
  color: var(--muted);
  overflow-wrap: anywhere;
}

.loading-state
{
  padding: 64px 0;
  text-align: center;
  color: var(--muted);
}

.load-error .el-button
{
  margin-top: 16px;
}

.edit-note
{
  margin-bottom: 20px;
  color: var(--muted);
  font-size: 13px;
}

.counselor-mobile
{
  display: none;
}

.counselor-mobile article
{
  padding: 24px 0;
  border-top: 1px solid var(--line);
}

.counselor-mobile dl
{
  font-size: 13px;
}

.counselor-mobile dl>div
{
  display: flex;
  gap: 16px;
  margin: 12px 0;
}

.counselor-mobile dt
{
  color: var(--muted);
  width: 56px;
  flex-shrink: 0;
}

.counselor-mobile dd
{
  margin: 0;
  overflow-wrap: anywhere;
}

:deep(.el-date-editor.el-input),
:deep(.el-select)
{
  width: 100%;
}

@media (max-width: 700px)
{
  .counselor-details
  {
    grid-template-columns: 1fr;
  }

  .counselor-table
  {
    display: none;
  }

  .counselor-mobile
  {
    display: block;
  }

  .list-footer
  {
    align-items: flex-start;
  }

  .list-toolbar .el-input
  {
    max-width: none;
  }
}
</style>
<style>
.counselor-dialog
{
  max-width: calc(100vw - 32px);
}

@media (max-width: 700px)
{
  .counselor-dialog .form-grid
  {
    grid-template-columns: 1fr;
  }
}
</style>
