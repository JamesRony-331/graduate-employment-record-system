<script setup>
import
{
  useRouter
}
from 'vue-router'
import
{
  ElMessageBox
}
from 'element-plus'
import
{
  User,
  SwitchButton
}
from '@element-plus/icons-vue'
import BrandMark from '../components/BrandMark.vue'
import
{
  session,
  clearSession
}
from '../stores/session'
const router = useRouter()
async function requestLogout()
{
  try
  {
    await ElMessageBox.confirm('确定退出当前账户？未保存的修改将丢失。', '退出登录',
    {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
    })
  }
  catch
  {
    return
  }
  clearSession()
  await router.replace('/login')
}
</script>
<template>
<div class="student-app">
  <a
    href="#main-content"
    class="skip-link"
  >跳到主要内容</a>
  <div
    v-if="session.demo"
    class="preview-notice"
    role="status"
  >
    页面演示模式：当前为示例资料，修改仅保存在本次浏览器会话中。
  </div>
  <header class="app-header">
    <div class="header-inner">
      <RouterLink
        to="/"
        class="brand-link"
        aria-label="毕业生服务首页"
      >
        <BrandMark />
      </RouterLink>
      <nav
        class="primary-nav"
        aria-label="主导航"
      >
        <RouterLink
          to="/"
          exact-active-class="active"
        >首页</RouterLink>
        <RouterLink
          to="/filing"
          active-class="active"
        >就业备案</RouterLink>
        <RouterLink
          to="/jobs"
          active-class="active"
        >校园招聘</RouterLink>
        <RouterLink
          to="/recommendations"
          active-class="active"
        >岗位推荐</RouterLink>
        <RouterLink
          to="/notices"
          active-class="active"
        >通知公告</RouterLink>
      </nav>
      <div class="account-nav">
        <RouterLink
          to="/profile"
          :class="{ active: $route.path.startsWith('/profile') }"
        >
          <ElIcon>
            <User />
          </ElIcon><span>个人中心</span>
        </RouterLink><button
          class="icon-button"
          aria-label="退出登录"
          @click="requestLogout"
        >
          <ElIcon>
            <SwitchButton />
          </ElIcon>
        </button>
      </div>
    </div>
  </header>
  <main
    id="main-content"
    class="workspace"
    tabindex="-1"
  >
    <RouterView />
  </main>
  <footer class="app-footer">
    <span>高校毕业生就业信息备案系统</span><span>学生服务端</span>
  </footer>
</div>
</template>
