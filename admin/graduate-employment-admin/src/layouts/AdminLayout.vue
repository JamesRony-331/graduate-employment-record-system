<script setup>
import
{
  ref,
  watch,
  nextTick,
  computed,
  onMounted,
  onBeforeUnmount
}
from "vue";
import
{
  useRoute,
  useRouter
}
from "vue-router";
import
{
  School,
  Collection,
  House,
  User,
  DocumentChecked,
  Bell,
  Grid,
  SwitchButton,
  Menu,
  Close,
}
from "@element-plus/icons-vue";
import
{
  ElMessageBox
}
from "element-plus";
import
{
  menus,
  loadMenus,
  resetMenus
}
from "../stores/menus";
import
{
  session,
  clearSession
}
from "../stores/session";
const route = useRoute(),
  router = useRouter(),
  menuOpen = ref(false);
const navigation = ref(null);
watch(menuOpen, async (open) =>
{
  await nextTick();
  if (open)
    navigation.value?.querySelector("a")?.focus();
  else
    document.querySelector(".mobile-menu")?.focus();
});

function keepMenuFocus(event)
{
  if (!menuOpen.value || event.key !== "Tab")
    return;
  const items = [...navigation.value.querySelectorAll("a, button")].filter(
    (el) => el.getClientRects().length,
  );
  const first = items[0],
    last = items[items.length - 1];
  if (event.shiftKey && document.activeElement === first)
  {
    event.preventDefault();
    last?.focus();
  }
  else if (!event.shiftKey && document.activeElement === last)
  {
    event.preventDefault();
    first?.focus();
  }
}
const fallbackIcons = [
{
  path: "/menus",
  title: "菜单管理",
  icon: Collection,
  available: true
},
{
  path: "/",
  title: "工作台",
  icon: House
},
{
  path: "/students",
  title: "学生管理",
  icon: User,
  available: true
},
{
  path: "/classes",
  title: "班级管理",
  icon: Collection,
  available: true
},
{
  path: "/works",
  title: "工作管理",
  icon: Collection,
  available: true
},
{
  path: "/ai-knowledge",
  title: "AI提示词管理",
  icon: Collection,
  available: true
},
{
  path: "/work-skills",
  title: "工作技能管理",
  icon: Collection,
  available: true
},
{
  path: "/majors",
  title: "专业管理",
  icon: Collection,
  available: true
},
{
  path: "/colleges",
  title: "学院管理",
  icon: School,
  available: true
},
{
  path: "/filings",
  title: "备案审核",
  icon: DocumentChecked,
  available: true
},
{
  path: "/notices",
  title: "通知公告",
  icon: Bell,
  available: true
},
{
  path: "/services",
  title: "服务管理",
  icon: Grid
},
{
  path: "/accounts",
  title: "账号管理",
  icon: User,
  available: true
},
{
  path: "/department-admins",
  title: "院系管理员",
  icon: School,
  available: true
},
{
  path: "/counselors",
  title: "辅导员管理",
  icon: School,
  available: true
},
{
  path: "/counselor-classes",
  title: "辅导员班级管理",
  icon: Collection,
  available: true
}, ];
const icons = {
  School,
  Collection,
  House,
  User,
  DocumentChecked,
  Bell,
  Grid
};
const links = computed(() => menus.records.filter(m => m.menuType === 'MENU' && m.status === 1 && m
  .path && m.path !== '/profile').map(m => (
{
  path: m.path,
  title: m.name,
  icon: icons[m.icon] || fallbackIcons.find(l => l.path === m.path)?.icon || Collection,
  available: true
})));

function refreshMenus()
{
  loadMenus(true).catch(() =>
  {})
}
onMounted(() =>
{
  loadMenus().catch(() =>
  {});
  window.addEventListener('menus-updated', refreshMenus)
});
onBeforeUnmount(() => window.removeEventListener('menus-updated', refreshMenus));
// Navigate through the existing route guard before clearing a session.
async function exit()
{
  try
  {
    await ElMessageBox.confirm(
      "退出后需要重新登录才能访问管理端。",
      "退出登录？",
      {
        confirmButtonText: "退出登录",
        cancelButtonText: "取消"
      },
    );
    const failure = await router.push("/?signout=1");
    if (failure)
      return;
    clearSession();
    resetMenus();
    await router.replace("/login");
  }
  catch
  {
    /* Cancel keeps the session. */
  }
}
</script>
<template>
<div class="admin-shell">
  <a
    class="skip-link"
    href="#main-content"
  >跳到主要内容</a>
  <button
    v-if="menuOpen"
    class="nav-scrim"
    aria-label="关闭导航菜单"
    @click="menuOpen = false"
  ></button>
  <aside
    ref="navigation"
    class="sidebar"
    @keydown="keepMenuFocus"
    :class="{ open: menuOpen }"
    @keydown.esc="menuOpen = false"
  >
    <RouterLink
      to="/"
      class="brand"
      @click="menuOpen = false"
    >
      <ElIcon :size="30">
        <School />
      </ElIcon>
      <div>
        <strong>毕业生就业管理</strong><small>学校管理工作空间</small>
      </div>
    </RouterLink>
    <button
      class="mobile-close"
      aria-label="关闭导航菜单"
      @click="menuOpen = false"
    >
      <ElIcon>
        <Close />
      </ElIcon>
    </button>
    <p class="nav-label">业务工作台</p>
    <p
      v-if="menus.loading"
      role="status"
      class="menu-status"
    >正在加载菜单…</p>
    <p
      v-else-if="menus.error"
      role="alert"
      class="menu-status"
    >{{ menus.error }} <button @click="refreshMenus">重试</button></p>
    <p
      v-else-if="!links.length"
      class="menu-status"
    >暂无可用菜单，请联系管理员授权。</p>
    <nav aria-label="主要导航">
      <RouterLink
        v-for="item in links"
        :key="item.path"
        :to="item.path"
        :class="{ active: route.path === item.path }"
        :aria-current="route.path === item.path ? 'page' : undefined"
        @click="menuOpen = false"
      >
        <ElIcon>
          <component :is="item.icon" />
        </ElIcon>{{ item.title
          }}<small v-if="item.path !== '/' && !item.available">{{
            item.preview ? "预览" : "待开放"
          }}</small>
      </RouterLink>
    </nav>
    <div class="sidebar-bottom">
      <RouterLink
        to="/profile"
        :class="{ active: route.path === '/profile' }"
        @click="menuOpen = false"
      >
        <ElIcon>
          <User />
        </ElIcon>个人中心
      </RouterLink>
      <p>高校毕业生就业信息备案系统</p>
    </div>
  </aside>
  <div class="shell-body">
    <header class="topbar">
      <button
        class="mobile-menu"
        aria-label="打开导航菜单"
        :aria-expanded="menuOpen"
        @click="menuOpen = !menuOpen"
      >
        <ElIcon>
          <Menu />
        </ElIcon>
      </button><span>{{ route.meta.title }}</span>
      <div class="topbar-account">
        <RouterLink to="/profile"><span class="avatar small">{{
              (session.user?.realName || session.user?.username || "用").slice(
                0,
                1,
              )
            }}</span><span>{{
              session.user?.realName || session.user?.username
            }}</span></RouterLink>
        <ElButton
          text
          aria-label="退出登录"
          @click="exit"
        >
          <ElIcon :size="19">
            <SwitchButton />
          </ElIcon>
        </ElButton>
      </div>
    </header>
    <main
      id="main-content"
      class="workspace"
      tabindex="-1"
    >
      <RouterView />
    </main>
    <footer class="workspace-footer">
      高校毕业生就业信息备案系统 <span>学校管理端</span>
    </footer>
  </div>
</div>
</template>
