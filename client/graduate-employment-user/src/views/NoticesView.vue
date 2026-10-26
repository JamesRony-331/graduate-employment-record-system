<script setup>
import NoticeList from '../components/NoticeList.vue'
</script>
<template>
<div>
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">通知公告</h1>
      <p>查看学校通知、就业政策与备案安排。</p>
    </div>
  </header>
  <section class="notice-surface">
    <NoticeList />
  </section>
</div>
</template>
<style scoped>
.notice-surface
{
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 28px
}

@media(max-width:700px)
{
  .notice-surface
  {
    padding: 20px
  }
}
</style>
