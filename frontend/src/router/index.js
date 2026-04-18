import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// Eager load ALL components to prevent navigation delay and "no reaction" issues caused by Vercel/GFW network latency
import Layout from '../views/Layout.vue'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import TreeHole from '../views/TreeHole.vue'
import Treasure from '../views/Treasure.vue'
import Community from '../views/Community.vue'
import Profile from '../views/Profile.vue'
import FindFriend from '../views/FindFriend.vue'
import Chat from '../views/Chat.vue'
import AiAssistant from '../views/AiAssistant.vue'
import Assessment from '../views/Assessment.vue'
import Diary from '../views/Diary.vue'
import Contact from '../views/Contact.vue'

const routes = [
  {
    path: '/login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: 'home', component: Home },
      { path: 'treehole', component: TreeHole },
      { path: 'treasure', component: Treasure },
      { path: 'community', component: Community },
      { path: 'profile', component: Profile },
      { path: 'find', component: FindFriend },
      { path: 'chat', component: Chat },
      { path: 'ai-chat', component: AiAssistant },
      { path: 'assessment', component: Assessment },
      { path: 'diary', component: Diary },
      { path: 'contact', component: Contact }
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
