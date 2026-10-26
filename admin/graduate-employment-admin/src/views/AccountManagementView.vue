<script setup>
import
{
  computed,
  onMounted,
  ref
}
from 'vue';
import
{
  ElDialog,
  ElSelect,
  ElOption,
  ElTable,
  ElTableColumn,
  ElEmpty
}
from 'element-plus';
import
{
  Search,
  Refresh
}
from '@element-plus/icons-vue';
import
{
  listAccounts
}
from '../api/accountManagement';
import
{
  filterAccounts,
  roleText
}
from '../api/accountManagementContracts';
const rows = ref([]),
  page = ref(1),
  size = ref(10),
  total = ref(0),
  pages = ref(0);
const keyword = ref(''),
  type = ref('all'),
  status = ref('all'),
  binding = ref('all');
const loading = ref(false),
  error = ref(''),
  selected = ref(null),
  detailsOpen = ref(false);
const visibleRows = computed(() => filterAccounts(rows.value, keyword.value, type.value, status
  .value, binding.value));
const hasFilters = computed(() => keyword.value || type.value !== 'all' || status.value !== 'all' ||
  binding.value !== 'all');
const typeText = value => (
{
  STUDENT: '学生',
  STAFF: '教职工'
})[value] || '未知类型';
const statusText = value => value === 1 ? '正常' : value === 0 ? '禁用' : '未知状态';
const dateText = value => value ? String(value).replace('T', ' ').slice(0, 19) : '未记录';

function clearFilters()
{
  keyword.value = '';
  type.value = 'all';
  status.value = 'all';
  binding.value = 'all';
}

function showDetails(row)
{
  selected.value = {
    ...row
  };
  detailsOpen.value = true;
}
async function load(target = page.value)
{
  if (loading.value)
    return;
  loading.value = true;
  error.value = '';
  try
  {
    let result = await listAccounts(target, size.value);
    if (target > Math.max(1, result.total))
    {
      target = Math.max(1, result.total);
      result = await listAccounts(target, size.value);
    }
    rows.value = result.records;
    total.value = result.totalCount;
    pages.value = result.total;
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
onMounted(() => load());
</script>
<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">账号管理</h1>
      <p class="subtext">查看学生与教职工账号、账号状态及角色信息。</p>
    </div>
    <RouterLink
      class="button-link"
      to="/counselors"
    >辅导员管理</RouterLink>
  </header>
  <section
    class="surface"
    :aria-busy="loading"
    aria-label="账号列表"
  >
    <div class="section-heading">
      <h2>账号列表</h2>
      <ElButton
        :icon="Refresh"
        :loading="loading"
        @click="load()"
      >刷新列表</ElButton>
    </div>
    <div class="account-filters">
      <ElInput
        v-model="keyword"
        aria-label="搜索本页账号或姓名"
        placeholder="搜索本页登录账号或姓名"
        clearable
        :prefix-icon="Search"
      />
      <ElSelect
        v-model="type"
        aria-label="本页账号类型"
      >
        <ElOption
          label="全部账号类型"
          value="all"
        />
        <ElOption
          label="学生"
          value="STUDENT"
        />
        <ElOption
          label="教职工"
          value="STAFF"
        />
      </ElSelect>
      <ElSelect
        v-model="status"
        aria-label="本页账号状态"
      >
        <ElOption
          label="全部账号状态"
          value="all"
        />
        <ElOption
          label="正常"
          :value="1"
        />
        <ElOption
          label="禁用"
          :value="0"
        />
      </ElSelect>

    </div>
    <div class="account-summary"><span
        class="muted"
        aria-live="polite"
      >{{ error ? '数据暂不可用' : `共 ${total} 个账号，本页显示 ${visibleRows.length} 个` }}</span>
      <ElButton
        v-if="hasFilters"
        link
        type="primary"
        @click="clearFilters"
      >清除筛选</ElButton>
    </div>
    <div
      v-if="error"
      role="alert"
    >
      <ElAlert
        title="账号加载失败"
        :description="error"
        type="error"
        show-icon
        :closable="false"
      />
      <ElButton
        class="retry"
        @click="load()"
      >重新加载</ElButton>
    </div>
    <p
      v-else-if="loading"
      class="account-loading"
      role="status"
    >正在加载账号…</p>
    <ElEmpty
      v-else-if="!visibleRows.length"
      :description="hasFilters ? '本页没有符合条件的账号' : '暂无账号'"
    />
    <template v-else>
      <ElTable
        :data="visibleRows"
        row-key="id"
        class="account-table"
      >
        <ElTableColumn
          prop="username"
          label="登录账号"
          min-width="180"
        />
        <ElTableColumn
          label="姓名"
          min-width="120"
        ><template #default="{ row }">{{ row.realName || '未填写' }}</template></ElTableColumn>
        <ElTableColumn
          label="账号类型"
          width="110"
        ><template #default="{ row }">{{ typeText(row.accountType) }}</template></ElTableColumn>
        <ElTableColumn
          label="账号状态"
          width="100"
        ><template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'info'">{{ statusText(row.status) }}
            </ElTag>
          </template></ElTableColumn>
        <ElTableColumn
          label="角色"
          min-width="180"
        ><template #default="{ row }">{{ roleText(row) }}</template></ElTableColumn>
        <ElTableColumn
          label="创建时间"
          min-width="175"
        ><template #default="{ row }">{{ dateText(row.createdAt) }}</template></ElTableColumn>
        <ElTableColumn
          label="操作"
          width="110"
          fixed="right"
        ><template #default="{ row }">
            <ElButton
              link
              type="primary"
              :aria-label="`查看账号${row.username}详情`"
              @click="showDetails(row)"
            >查看详情</ElButton>
          </template></ElTableColumn>
      </ElTable>
      <div class="account-mobile">
        <article
          v-for="row in visibleRows"
          :key="row.id"
        >
          <div class="mobile-heading">
            <h3>{{ row.username }}</h3>
            <ElTag :type="row.status === 1 ? 'success' : 'info'">{{ statusText(row.status) }}
            </ElTag>
          </div>
          <dl>
            <div>
              <dt>姓名</dt>
              <dd>{{ row.realName || '未填写' }}</dd>
            </div>
            <div>
              <dt>账号类型</dt>
              <dd>{{ typeText(row.accountType) }}</dd>
            </div>
            <div>
              <dt>角色</dt>
              <dd>{{ roleText(row) }}</dd>
            </div>
          </dl>
          <ElButton
            :aria-label="`查看账号${row.username}详情`"
            @click="showDetails(row)"
          >查看详情</ElButton>
        </article>
      </div>
    </template>
    <footer class="account-footer">
      <div class="page-size">
        <ElSelect
          v-model="size"
          :disabled="loading"
          aria-label="每页条数"
          @change="load(1)"
        >
          <ElOption
            v-for="n in [10, 20, 50]"
            :key="n"
            :label="`每页 ${n} 条`"
            :value="n"
          />
        </ElSelect><span class="muted">搜索与筛选仅作用于本页。</span>
      </div>
      <nav aria-label="账号分页">
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
    <p class="profile-note">登录账号不可修改。账号绑定与角色分配暂未开放。</p>
  </section>
  <ElDialog
    v-model="detailsOpen"
    title="账号详情"
    width="640px"
    class="account-dialog"
    destroy-on-close
  >
    <template v-if="selected">
      <p class="muted">以下资料来自当前列表，更新后请刷新列表。</p>
      <dl class="account-details">
        <div>
          <dt>账号 ID</dt>
          <dd>{{ selected.id }}</dd>
        </div>
        <div>
          <dt>登录账号</dt>
          <dd>{{ selected.username }}</dd>
        </div>
        <div>
          <dt>姓名</dt>
          <dd>{{ selected.realName || '未填写' }}</dd>
        </div>
        <div>
          <dt>账号类型</dt>
          <dd>{{ typeText(selected.accountType) }}</dd>
        </div>
        <div>
          <dt>账号状态</dt>
          <dd>{{ statusText(selected.status) }}</dd>
        </div>
        <div>
          <dt>角色</dt>
          <dd>{{ roleText(selected) }}</dd>
        </div>
        <div>
          <dt>创建时间</dt>
          <dd>{{ dateText(selected.createdAt) }}</dd>
        </div>
        <div>
          <dt>更新时间</dt>
          <dd>{{ dateText(selected.updatedAt) }}</dd>
        </div>
      </dl>
    </template><template #footer>
      <ElButton @click="detailsOpen = false">关闭</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.account-filters
{
  display: grid;
  grid-template-columns: minmax(220px, 2fr) 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.account-summary,
.account-footer,
.account-footer nav,
.page-size,
.mobile-heading
{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.account-summary
{
  margin-bottom: 20px;
}

.account-footer
{
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.account-footer nav
{
  font-size: 13px;
}

.account-footer .el-button
{
  margin: 0;
}

.page-size .el-select
{
  width: 125px;
}

.account-loading
{
  text-align: center;
  padding: 64px 0;
  color: var(--muted);
}

.retry
{
  margin-top: 16px;
}

.account-details
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin: 24px 0;
}

.account-details dt,
.account-mobile dt
{
  color: var(--muted);
  font-size: 13px;
}

.account-details dd
{
  margin: 8px 0 0;
  line-height: 1.7;
  overflow-wrap: anywhere;
}

.account-mobile
{
  display: none;
}

.account-mobile article
{
  border-top: 1px solid var(--line);
  padding: 24px 0;
}

.account-mobile dl>div
{
  display: flex;
  gap: 16px;
  margin: 12px 0;
}

.account-mobile dt
{
  width: 70px;
  flex-shrink: 0;
}

.account-mobile dd
{
  margin: 0;
  overflow-wrap: anywhere;
}

.account-mobile h3
{
  overflow-wrap: anywhere;
}

@media (max-width: 1100px)
{
  .account-filters
  {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px)
{
  .account-table
  {
    display: none;
  }

  .account-mobile
  {
    display: block;
  }

  .account-filters,
  .account-details
  {
    grid-template-columns: 1fr;
  }
}
</style>
<style>
.account-dialog
{
  max-width: calc(100vw - 32px);
}
</style>
