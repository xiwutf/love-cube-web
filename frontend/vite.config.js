import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue()
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '@f': fileURLToPath(new URL('./src/modules/fellowship', import.meta.url))
      }
    },
    server: {
      // 固定端口 + strictPort：避免占用时静默改 5174，换电脑/多项目时地址一致
      port: Number(env.VITE_DEV_PORT) || 5173,
      strictPort: true,
      proxy: {
        '/admin': {
          target: env.VITE_BACKEND_ORIGIN || 'http://xifg.com.cn:8090',
          changeOrigin: true,
          ws: true
        }
      }
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            'vendor-vue': ['vue', 'vue-router', 'pinia'],
            'vendor-vant': ['vant'],
            'vendor-axios': ['axios']
          }
        }
      }
    }
  }
})
