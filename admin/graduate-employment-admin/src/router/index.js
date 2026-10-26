import
{
  createRouter,
  createWebHashHistory
}
from "vue-router";
import
{
  session
}
from "../stores/session";
import AdminLayout from "../layouts/AdminLayout.vue";
const router = createRouter(
{
  history: createWebHashHistory(),
  routes: [
  {
    path: "/login",
    component: () => import("../views/LoginView.vue"),
    meta:
    {
      title: "管理端登录"
    },
  },
  {
    path: "/",
    component: AdminLayout,
    children: [
    {
      path: "",
      component: () => import("../views/HomeView.vue"),
      meta:
      {
        title: "工作台"
      },
    },
    {
      path: "profile",
      component: () => import("../views/ProfileView.vue"),
      meta:
      {
        title: "个人中心"
      },
    },
    {
      path: "accounts",
      component: () => import("../views/AccountManagementView.vue"),
      meta:
      {
        title: "账号管理"
      },
    },
    {
      path: "counselors",
      component: () => import("../views/CounselorManagementView.vue"),
      meta:
      {
        title: "辅导员管理"
      },
    },
    {
      path: "classes",
      component: () => import("../views/ClassManagementView.vue"),
      meta:
      {
        title: "班级管理"
      }
    },
    {
      path: "works",
      component: () => import("../views/WorkManagementView.vue"),
      meta:
      {
        title: "工作管理"
      }
    },
    {
      path: "menus",
      component: () => import("../views/MenuManagementView.vue"),
      meta:
      {
        title: "菜单管理"
      }
    },
    {
      path: "ai-knowledge",
      component: () => import("../views/AiKnowledgeManagementView.vue"),
      meta:
      {
        title: "AI提示词管理"
      }
    },
    {
      path: "work-skills",
      component: () => import("../views/WorkSkillManagementView.vue"),
      meta:
      {
        title: "工作技能管理"
      }
    },
    {
      path: "majors",
      component: () => import("../views/MajorManagementView.vue"),
      meta:
      {
        title: "专业管理"
      }
    },
    {
      path: "colleges",
      component: () => import("../views/CollegeManagementView.vue"),
      meta:
      {
        title: "学院管理"
      }
    },
    {
      path: "department-admins",
      component: () => import("../views/DepartmentAdminManagementView.vue"),
      meta:
      {
        title: "院系管理员"
      }
    },
    {
      path: "counselor-classes",
      component: () => import("../views/CounselorClassManagementView.vue"),
      meta:
      {
        title: "辅导员班级管理"
      }
    },
    {
      path: "students",
      component: () => import("../views/StudentManagementView.vue"),
      meta:
      {
        title: "学生管理"
      }
    },
    {
      path: "filings",
      component: () => import("../views/FilingReviewView.vue"),
      meta:
      {
        title: "备案审核"
      }
    },
    {
      path: "notices",
      component: () => import("../views/NoticeManagementView.vue"),
      meta:
      {
        title: "通知公告"
      }
    },
    {
      path: "services",
      component: () => import("../views/ServiceManagementView.vue"),
      meta:
      {
        title: "服务管理"
      }
    }, ],
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/"
  }, ],
  scrollBehavior: () => (
  {
    top: 0
  }),
});
router.beforeEach((to) =>
{
  if (to.path !== "/login" && !session.token)
    return "/login";
  if (to.path === "/login" && session.token)
    return "/";
});
router.afterEach((to) =>
{
  document.title = to.meta.title + " · 就业管理";
  requestAnimationFrame(() => document.querySelector("main h1")?.focus());
});
window.addEventListener("admin-session-expired", () =>
  router.replace("/login?expired=1"),
);
export default router;
