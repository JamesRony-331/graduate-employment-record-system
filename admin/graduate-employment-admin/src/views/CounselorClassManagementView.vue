<script setup>
import
{
  computed,
  ref,
  onMounted
}
from 'vue';
import
{
  onBeforeRouteLeave
}
from 'vue-router';
import
{
  session
}
from '../stores/session';
import
{
  ElTable,
  ElTableColumn,
  ElDialog,
  ElEmpty,
  ElMessage,
  ElMessageBox,
  ElCheckbox,
  ElRadioGroup,
  ElRadioButton
}
from 'element-plus';
import
{
  Search
}
from '@element-plus/icons-vue';
import
{
  listCounselors
}
from '../api/counselor';
import
{
  listOrganizations,
  collegeOptions
}
from '../api/organization';
import
{
  managementRequest as request
}
from '../api/managementRequest';
const counselors = ref([]),
  classes = ref([]),
  relations = ref([]),
  colleges = ref([]);
const loading = ref(false),
  loadError = ref(''),
  busy = ref(false),
  operationError = ref('');
const keyword = ref(''),
  selected = ref(null),
  opened = ref(false),
  classKeyword = ref(''),
  chosen = ref([]),
  tab = ref('unbound');
const rows = computed(() => counselors.value.filter(row => `${row.name} ${row.counselorNo}`
  .includes(keyword.value.trim())));
const assignedCount = id => new Set(relations.value.filter(r => r.counselorId === id).map(r => r
  .classId)).size;
const collegeName = id => colleges.value.find(c => c.id === id)?.collegeName || `学院 ID：${id}`;
const shownClasses = computed(() => classes.value.filter(row => (tab.value === 'unbound' ? !
    relations.value.some(r => r.classId === row.id) : relations.value.some(r => r.classId ===
      row.id && r.counselorId === selected.value?.id)) &&
  `${row.className} ${row.classCode} ${collegeName(row.collegeId)}`.includes(classKeyword.value
    .trim())));
async function allPages(kind)
{
  const result = [];
  let page = 1;
  while (true)
  {
    const data = await listOrganizations(kind, page, 100);
    result.push(...data.records);
    if (page >= data.total)
      return result;
    if (!data.records.length)
      throw new Error('分页数据发生变化，请刷新重试');
    page++;
  }
}
async function allCounselors()
{
  const result = [];
  let page = 1;
  while (true)
  {
    const data = await listCounselors(page++, 100);
    result.push(...data);
    if (data.length < 100)
      return result;
  }
}
async function load()
{
  if (loading.value)
    return;
  loading.value = true;
  loadError.value = '';
  chosen.value = [];
  try
  {
    const data = await Promise.all([allCounselors(), allPages('class'), allPages(
      'counselor-class'), collegeOptions()]);
    [counselors.value, classes.value, relations.value, colleges.value] = data;
    if (selected.value)
    {
      selected.value = counselors.value.find(c => c.id === selected.value.id) || null;
      if (!selected.value)
        opened.value = false;
    }
  }
  catch (e)
  {
    loadError.value = e.message;
  }
  finally
  {
    loading.value = false;
  }
}

function open(row)
{
  selected.value = row;
  opened.value = true;
  classKeyword.value = '';
  chosen.value = [];
  tab.value = 'unbound';
  operationError.value = '';
}
async function bind(ids)
{
  if (busy.value || loading.value || loadError.value || !selected.value || selected.value
    .status !== 1)
    return;
  const candidates = classes.value.filter(row => ids.includes(row.id) && !relations.value.some(
    r => r.classId === row.id));
  if (!candidates.length)
    return;
  busy.value = true;
  operationError.value = '';
  let success = 0;
  try
  {
    for (const row of candidates)
    {
      try
      {
        await request('/counselor-class/add', 'POST',
        {
          counselorId: selected.value.id,
          classId: row.id,
          roleType: 1
        });
        success++;
      }
      catch (e)
      {
        operationError.value =
          `已完成 ${success} 个，绑定“${row.className}”失败：${e.message}。后续班级未提交，请核实后重试。`;
        break;
      }
    }
    if (success)
      ElMessage.success(`已绑定 ${success} 个班级`);
    await load();
  }
  finally
  {
    busy.value = false;
  }
}
async function unbind(row)
{
  if (busy.value || loading.value || loadError.value)
    return;
  const relation = relations.value.find(r => r.classId === row.id && r.counselorId === selected
    .value.id);
  if (!relation)
    return;
  busy.value = true;
  try
  {
    try
    {
      await ElMessageBox.confirm(`解除“${row.className}”与${selected.value.name}的关联？`, '解除关联',
      {
        confirmButtonText: '确认解除',
        cancelButtonText: '取消',
        type: 'warning'
      });
    }
    catch
    {
      return;
    }
    operationError.value = '';
    try
    {
      await request(`/counselor-class/${relation.id}`, 'DELETE');
      ElMessage.success('关联已解除');
      await load();
    }
    catch (e)
    {
      operationError.value = e.message;
    }
  }
  finally
  {
    busy.value = false;
  }
}
onBeforeRouteLeave(() => !session.token || !busy.value);
onMounted(load);
</script>
<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">辅导员班级管理</h1>
      <p class="subtext">选择辅导员，集中查看和分配负责的班级。</p>
    </div>
  </header>
  <ElAlert
    v-if="loadError"
    title="数据加载失败"
    :description="loadError"
    type="error"
    :closable="false"
    show-icon
  />
  <ElButton
    :loading="loading"
    :disabled="busy"
    @click="load"
  >刷新数据</ElButton>
  <section
    class="surface"
    :aria-busy="loading"
  >
    <p
      v-if="loading"
      role="status"
    >正在加载辅导员及班级关联…</p>
    <div class="section-heading">
      <h2>辅导员列表</h2><span class="quiet-tag">{{ counselors.length }} 位辅导员</span>
    </div>
    <ElInput
      v-model="keyword"
      class="relation-search"
      :prefix-icon="Search"
      clearable
      aria-label="筛选辅导员"
      placeholder="搜索辅导员姓名或工号"
    />
    <ElEmpty
      v-if="!loading && !loadError && !rows.length"
      description="没有符合条件的辅导员"
    />
    <template v-if="!loading && !loadError && rows.length">
      <ElTable
        :data="rows"
        row-key="id"
        class="relation-table"
      >
        <ElTableColumn
          prop="name"
          label="辅导员"
          min-width="120"
        />
        <ElTableColumn
          prop="counselorNo"
          label="工号"
          min-width="130"
        />
        <ElTableColumn
          label="已绑定班级"
          width="130"
        ><template #default="{ row }">{{ assignedCount(row.id) }} 个</template></ElTableColumn>
        <ElTableColumn
          label="操作"
          width="120"
        ><template #default="{ row }">
            <ElButton
              link
              type="primary"
              :aria-label="`查看${row.name}详情`"
              @click="open(row)"
            >查看详情</ElButton>
          </template></ElTableColumn>
      </ElTable>
      <div class="relation-mobile">
        <article
          v-for="row in rows"
          :key="row.id"
        >
          <h3>{{ row.name }} · {{ row.counselorNo }}</h3>
          <p>已绑定 {{ assignedCount(row.id) }} 个班级</p>
          <ElButton
            :aria-label="`查看${row.name}详情`"
            @click="open(row)"
          >查看详情</ElButton>
        </article>
      </div>
    </template>
  </section>
  <ElDialog
    v-model="opened"
    :title="selected ? selected.name + ' · 班级管理' : '班级管理'"
    width="860px"
    class="relation-dialog"
    :show-close="!busy"
    :close-on-click-modal="false"
    :close-on-press-escape="!busy"
    destroy-on-close
  >
    <template v-if="selected">
      <p class="relation-context">{{ selected.counselorNo }} ·
        {{ selected.status === 1 ? '在职' : '离职（不可新增绑定）' }}
      </p>
      <ElAlert
        v-if="operationError || loadError"
        :title="operationError || loadError"
        type="error"
        :closable="false"
        show-icon
      />
      <ElButton
        v-if="loadError"
        :disabled="busy"
        :loading="loading"
        @click="load"
      >重试加载</ElButton>
      <ElRadioGroup
        :disabled="busy || loading"
        v-model="tab"
        aria-label="班级绑定状态"
        @change="chosen = []; classKeyword = ''"
      >
        <ElRadioButton value="unbound">未绑定班级</ElRadioButton>
        <ElRadioButton value="bound">已绑定班级（{{ assignedCount(selected.id) }}）</ElRadioButton>
      </ElRadioGroup>
      <ElInput
        v-model="classKeyword"
        class="class-search"
        :prefix-icon="Search"
        clearable
        aria-label="筛选班级"
        placeholder="搜索班级名称、编号或学院"
        @input="chosen = []"
        @clear="chosen = []"
      />
      <p class="muted">{{ tab === 'unbound' ? '仅显示尚未分配辅导员的班级，可直接绑定或勾选后批量绑定。' : '以下班级由当前辅导员负责。' }}
      </p>
      <p
        v-if="loading"
        role="status"
      >正在刷新班级关联…</p>
      <ElEmpty
        v-else-if="!loadError && !shownClasses.length"
        :description="classKeyword ? '没有符合条件的班级' : tab === 'unbound' ? '所有班级均已分配辅导员' : '暂未绑定班级'"
      />
      <div
        v-else-if="!loading && !loadError"
        class="class-list"
      >
        <article
          v-for="row in shownClasses"
          :key="row.id"
        >
          <ElCheckbox
            :disabled="busy || loading"
            v-if="tab === 'unbound'"
            :model-value="chosen.includes(row.id)"
            :aria-label="`选择${row.className}`"
            @change="value => chosen = value ? [...chosen, row.id] : chosen.filter(id => id !== row.id)"
          />
          <div class="class-info">
            <h3>{{ row.className }}</h3>
            <p>{{ row.classCode }} · {{ collegeName(row.collegeId) }}</p>
          </div>
          <ElButton
            v-if="tab === 'unbound'"
            type="primary"
            plain
            :disabled="busy || loading || selected.status !== 1"
            :aria-label="`绑定${row.className}`"
            @click="bind([row.id])"
          >一键绑定</ElButton>
          <ElButton
            v-else
            type="danger"
            plain
            :disabled="busy || loading"
            @click="unbind(row)"
          >解除关联</ElButton>
        </article>
      </div>
    </template>
    <template #footer>
      <div class="relation-footer"><span
          class="muted"
          aria-live="polite"
        >{{ tab === 'unbound' ? `已选择 ${chosen.length} 个班级` : '关联变更将保存到数据库' }}</span>
        <div>
          <ElButton
            :disabled="busy"
            @click="opened = false"
          >关闭</ElButton>
          <ElButton
            v-if="tab === 'unbound'"
            type="primary"
            :disabled="!chosen.length || busy || loading || !!loadError || selected?.status !== 1"
            @click="bind(chosen)"
          >绑定所选班级</ElButton>
        </div>
      </div>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.relation-search
{
  max-width: 420px;
  margin-bottom: 24px;
}

.relation-mobile
{
  display: none;
}

.relation-mobile article
{
  border-top: 1px solid var(--line);
  padding: 24px 0;
}

.relation-mobile p,
.class-info p
{
  color: var(--muted);
  font-size: 13px;
  margin: 10px 0;
  line-height: 1.7;
}

.relation-context
{
  margin-bottom: 20px;
  color: var(--muted);
}

.class-search
{
  margin: 20px 0 12px;
}

.class-list
{
  max-height: 380px;
  overflow-y: auto;
  margin-top: 16px;
}

.class-list article
{
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 4px;
  border-bottom: 1px solid var(--line);
}

.class-info
{
  flex: 1;
  min-width: 0;
  overflow-wrap: anywhere;
}

.class-list .el-button
{
  flex-shrink: 0;
}

.relation-footer
{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

@media(max-width:700px)
{
  .relation-table
  {
    display: none;
  }

  .relation-mobile
  {
    display: block;
  }

  .class-list article
  {
    gap: 8px;
    flex-wrap: wrap;
  }

  .class-info
  {
    flex-basis: 70%;
  }

  .class-list .el-button
  {
    margin-left: auto;
  }
}
</style>
<style>
.relation-dialog
{
  max-width: calc(100vw - 32px);
}
</style>
