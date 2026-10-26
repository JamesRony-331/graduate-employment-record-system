<script setup>
import
{
  useRoute
}
from "vue-router";
import
{
  Document
}
from "@element-plus/icons-vue";
const route = useRoute();
</script>
<template>
<header class="page-heading">
  <div>
    <h1 tabindex="-1">{{ route.meta.title }}</h1>
    <p class="subtext">学校就业管理服务</p>
  </div>
</header>
<section class="surface placeholder">
  <ElIcon :size="42">
    <Document />
  </ElIcon>
  <h2>{{ route.meta.title }}即将开放</h2>
  <p>功能上线后，可在这里办理相关业务。</p>
  <RouterLink
    to="/"
    class="button-link"
  >返回工作台</RouterLink>
</section>
</template>
