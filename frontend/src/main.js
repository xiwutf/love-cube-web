import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router/index.js'
import './assets/styles/index.css'
import { trackCurrentVisit } from '@/utils/visitorTracker.js'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vant)

router.afterEach((to) => {
  trackCurrentVisit(to.fullPath)
})

app.mount('#app')
