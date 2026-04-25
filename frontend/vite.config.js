import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@':  fileURLToPath(new URL('./src', import.meta.url)),
        '@f': fileURLToPath(new URL('./src/modules/fellowship', import.meta.url))
      }
    },
    server: {
      port: 5173,
      // 开发代理：将 /admin 请求转发到后端，规避本地浏览器跨域限制
      proxy: {
        '/admin': {
          target: env.VITE_BACKEND_ORIGIN || 'http://xifg.com.cn:8090',
          changeOrigin: true
        }
      }
    }
  }
})
