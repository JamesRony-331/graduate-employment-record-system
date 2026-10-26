import
{
  createRouter,
  createWebHashHistory
}
from 'vue-router'
import
{
  session
}
from '../stores/session'
import StudentLayout from '../layouts/StudentLayout.vue'
const router = createRouter(
{
  history: createWebHashHistory(),
  routes: [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta:
    {
      title: '学生登录'
    },
  },
  {
    path: '/',
    component: StudentLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta:
        {
          title: '首页'
        },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
        meta:
        {
          title: '个人中心'
        },
      },
      {
        path: 'profile/edit',
        name: 'profile-edit',
        component: () => import('../views/EditProfileView.vue'),
        meta:
        {
          title: '修改个人信息'
        },
      },
      {
        path: 'filing',
        name: 'filing',
        component: () => import('../views/FilingView.vue'),
        meta:
        {
          title: '就业备案'
        },
      },
      {
        path: 'jobs',
        name: 'jobs',
        component: () => import('../views/JobsView.vue'),
        meta:
        {
          title: '校园招聘'
        }
      },
      {
        path: 'assessment',
        redirect: '/recommendations'
      },
      {
        path: 'ai-chat',
        name: 'ai-chat',
        component: () => import('../views/AiChatView.vue'),
        meta:
        {
          title: 'AI 就业问答'
        }
      },
      {
        path: 'recommendations',
        name: 'recommendations',
        component: () => import('../views/JobRecommendationView.vue'),
        meta:
        {
          title: '岗位推荐'
        }
      },
      {
        path: 'notices',
        component: () => import('../views/NoticesView.vue'),
        meta:
        {
          title: '通知公告'
        }
      },
      ...[

        ['security', '账号安全'],

      ].map(([path, title]) => (
      {
        path,
        name: path,
        component: () => import('../views/PlaceholderView.vue'),
        meta:
        {
          title
        },
      })),
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }, ],
  scrollBehavior: () => (
  {
    top: 0
  }),
})
router.beforeEach((to) =>
{
  if (to.name !== 'login' && !session.token && !session.demo)
    return {
      name: 'login'
    }
  if (to.name === 'login' && (session.token || session.demo))
    return {
      name: 'home'
    }
})
router.afterEach((to) =>
{
  document.title = to.meta.title + ' · 毕业生服务'
  requestAnimationFrame(() => document.querySelector('main h1')?.focus(
  {
    preventScroll: true
  }))
})
window.addEventListener('student-session-expired', () =>
  router.replace(
  {
    name: 'login',
    query:
    {
      expired: '1'
    }
  }),
)
export default router
