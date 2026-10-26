<script setup>
import
{
  useRoute
}
from 'vue-router'
import
{
  FolderOpened
}
from '@element-plus/icons-vue'
const route = useRoute()
</script>
<template>
<section class="placeholder-page">
  <ElIcon
    :size="44"
    aria-hidden="true"
  >
    <FolderOpened />
  </ElIcon>
  <h1 tabindex="-1">{{ route.meta.title }}</h1>
  <p>此功能正在准备中，稍后开放。</p>
  <RouterLink to="/">返回首页</RouterLink>
</section>
</template>
