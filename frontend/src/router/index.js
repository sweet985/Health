import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/Home.vue') },
      { path: 'treehole', component: () => import('../views/TreeHole.vue') },
      { path: 'treasure', component: () => import('../views/Treasure.vue') },
      { path: 'community', component: () => import('../views/Community.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
      { path: 'find', component: () => import('../views/FindFriend.vue') },
      { path: 'chat', component: () => import('../views/Chat.vue') },
      { path: 'ai-chat', component: () => import('../views/AiAssistant.vue') },
      { path: 'assessment', component: () => import('../views/Assessment.vue') },
      { path: 'diary', component: () => import('../views/Diary.vue') },
      { path: 'contact', component: () => import('../views/Contact.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  console.log('Router beforeEach:', { to: to.path, from: from.path })
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    console.log('Redirect to login due to missing token')
    next('/login')
  } else {
    console.log('Allow navigation')
    next()
  }
})

router.onError((error) => {
  console.error('Router Error:', error)
})

export default router
