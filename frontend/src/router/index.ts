import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/student'
  },
  // ===== 学员端（方雨菲负责） =====
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    children: [
      {
        path: '',
        name: 'StudentHome',
        component: () => import('@/pages/student/Home.vue'),
        meta: { title: '学员首页' }
      },
      // 4.16 在线作答
      {
        path: 'exam-taking',
        name: 'StudentExamTaking',
        component: () => import('@/pages/student/exam-taking/index.vue'),
        meta: { title: '在线作答' }
      },
      {
        path: 'exam-taking/:examId',
        name: 'StudentExamRoom',
        component: () => import('@/pages/student/exam-taking/Room.vue'),
        meta: { title: '考试作答中' }
      },
      // 4.17 个人测评
      {
        path: 'self-test',
        name: 'StudentSelfTest',
        component: () => import('@/pages/student/self-test/index.vue'),
        meta: { title: '个人测评' }
      },
      // 4.19 知识管理
      {
        path: 'knowledge',
        name: 'StudentKnowledge',
        component: () => import('@/pages/student/knowledge/index.vue'),
        meta: { title: '知识管理' }
      }
    ]
  },
  // ===== 管理端（占位，王茗瑾负责） =====
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('@/pages/admin/Home.vue'),
        meta: { title: '管理端首页（占位）' }
      }
    ]
  },
  // ===== 错误页 =====
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/pages/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - GAC-LMS`
  }
})

export default router
