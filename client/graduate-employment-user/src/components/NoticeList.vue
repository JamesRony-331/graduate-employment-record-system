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
  ElDialog,
  ElEmpty,
  ElAlert,
  ElTag
}
from 'element-plus'
import
{
  noticePage,
  noticeDetail,
  noticeType
}
from '../api/notice'
const props = defineProps(
{
  compact: Boolean
})
const rows = ref([]),
  page = ref(1),
  pages = ref(0),
  loading = ref(false),
  error = ref(''),
  opened = ref(false),
  selected = ref(null),
  detailError = ref(''),
  detailLoading = ref(false)
let alive = true,
  seq = 0
const date = v => v ? String(v).replace('T', ' ').slice(0, 16) : '未记录'
async function load(target = page.value)
{
  loading.value = true;
  error.value = '';
  try
  {
    const data = await noticePage(target, props.compact ? 5 : 10);
    if (alive)
    {
      rows.value = data.records;
      pages.value = data.total;
      page.value = target
    }
  }
  catch (e)
  {
    if (alive)
    {
      error.value = e.message;
      rows.value = []
    }
  }
  finally
  {
    if (alive)
      loading.value = false
  }
}
async function detail(id)
{
  const current = ++seq;
  opened.value = true;
  selected.value = null;
  detailLoading.value = true;
  detailError.value = '';
  try
  {
    const data = await noticeDetail(id);
    if (alive && current === seq)
      selected.value = data
  }
  catch (e)
  {
    if (alive && current === seq)
      detailError.value = e.message
  }
  finally
  {
    if (alive && current === seq)
      detailLoading.value = false
  }
}
onMounted(() => load());
onBeforeUnmount(() =>
{
  alive = false;
  seq++
})
</script>
<template>
<div
  class="notice-list"
  :aria-busy="loading"
>
  <p
    v-if="loading"
    role="status"
  >正在加载公告…</p>
  <div
    v-else-if="error"
    role="alert"
  >
    <ElAlert
      :title="error"
      type="warning"
      :closable="false"
    />
    <ElButton @click="load()">重试</ElButton>
  </div>
  <ElEmpty
    v-else-if="!rows.length"
    description="暂无已发布公告"
    :image-size="compact?48:90"
  />
  <ul v-else>
    <li
      v-for="row in rows"
      :key="row.id"
    >
      <button
        class="notice-item"
        type="button"
        @click="detail(row.id)"
      >
        <span class="notice-copy">
          <span class="notice-title">{{ row.title }}</span>
          <small>{{ noticeType(row.noticeType) }} · {{ date(row.publishTime) }}</small>
        </span>
        <ElTag
          v-if="row.isTop === 1"
          class="notice-pin"
          size="small"
        >置顶</ElTag>
      </button>
    </li>
  </ul>
  <nav
    v-if="!compact"
    aria-label="公告分页"
  >
    <ElButton
      :disabled="loading||!!error||page<=1"
      @click="load(page-1)"
    >上一页</ElButton><span>第 {{page}} / {{Math.max(1,pages)}} 页</span>
    <ElButton
      :disabled="loading||!!error||page>=pages"
      @click="load(page+1)"
    >下一页</ElButton>
  </nav>
  <ElDialog
    v-model="opened"
    title="公告详情"
    width="760px"
    class="student-notice-dialog"
    @close="seq++"
    destroy-on-close
  >
    <p
      v-if="detailLoading"
      role="status"
    >正在读取公告…</p>
    <ElAlert
      v-else-if="detailError"
      :title="detailError"
      type="warning"
      :closable="false"
    />
    <article v-else-if="selected">
      <h2>{{selected.title}}</h2>
      <p class="notice-meta">{{noticeType(selected.noticeType)}} ·
        {{selected.publisherName || '学校'}} · {{date(selected.publishTime)}}
      </p>
      <div class="notice-body">{{selected.content}}</div>
    </article>
    <template #footer>
      <ElButton @click="opened=false">关闭</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.notice-list ul
{
  list-style: none;
  padding: 0;
  margin: 0
}

.notice-list li
{
  border-bottom: 1px solid #e2e8f0
}

.notice-item
{
  display: flex;
  align-items: center;
  gap: 16px;
  border: 0;
  background: none;
  width: 100%;
  text-align: left;
  padding: 18px 0;
  cursor: pointer;
  color: inherit;
  font: inherit
}

.notice-item:hover
{
  color: #28518c
}

.notice-item:focus-visible
{
  outline: 2px solid #28518c;
  outline-offset: 3px
}

.notice-copy
{
  flex: 1;
  min-width: 0
}

.notice-title
{
  display: block;
  overflow-wrap: anywhere;
  line-height: 1.7
}

.notice-pin
{
  flex-shrink: 0
}

.notice-list small
{
  display: block;
  margin-top: 8px;
  color: #64748b
}

.notice-list nav
{
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 24px
}

.notice-body
{
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.9
}

.notice-meta
{
  color: #64748b;
  font-size: 13px;
  margin: 18px 0 28px
}

h2
{
  overflow-wrap: anywhere
}
</style>
<style>
.student-notice-dialog
{
  max-width: calc(100vw - 32px)
}
</style>
