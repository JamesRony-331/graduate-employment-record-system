<script setup>
import
{
  computed
}
from 'vue'
import
{
  session
}
from '../stores/session'
import
{
  User,
  Lock
}
from '@element-plus/icons-vue'
const user = computed(() => session.user ||
{})
</script>
<template>
<div class="page-heading">
  <div>
    <p class="breadcrumb">学生服务 / 个人中心</p>
    <h1 tabindex="-1">个人中心</h1>
    <p>查看学籍资料，维护你的联系方式。</p>
  </div>
  <span class="quiet-label">学生账户</span>
</div>
<div class="profile-layout">
  <aside
    class="profile-sidebar"
    aria-label="个人中心导航"
  >
    <div class="identity">
      <div
        class="avatar"
        aria-hidden="true"
      >{{ user.name?.slice(-2) || '同学' }}</div>
      <h2>{{ user.name || '未提供姓名' }}</h2>
      <p>{{ user.studentNo || '未提供学号' }}</p>
      <span class="student-badge">学生账户</span>
    </div>
    <nav class="profile-nav">
      <RouterLink
        to="/profile"
        :class="{ selected: $route.path.startsWith('/profile') }"
      >
        <ElIcon>
          <User />
        </ElIcon>个人资料
      </RouterLink>
      <RouterLink to="/security">
        <ElIcon>
          <Lock />
        </ElIcon>账号安全<span class="soon">待开放</span>
      </RouterLink>
    </nav>
    <p class="sidebar-note">学籍信息由学校统一维护。<br />如需更正，请联系辅导员。</p>
  </aside>
  <section class="profile-content">
    <slot />
  </section>
</div>
</template>
