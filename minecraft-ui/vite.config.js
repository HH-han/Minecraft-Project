import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { extraAppConfigPlugin } from './build/plugins/extra-app-config'

/**
 * 从模块 id 解析 npm 包名（兼容 npm / pnpm 的 node_modules 目录结构）
 * 例：.../node_modules/element-plus/es/index.mjs -> element-plus
 */
function getPackageName(id) {
  const normalized = id.replace(/\\/g, '/')
  const marker = '/node_modules/'
  const index = normalized.lastIndexOf(marker)
  if (index === -1) return ''
  const segments = normalized.slice(index + marker.length).split('/')
  return segments[0]?.startsWith('@') ? `${segments[0]}/${segments[1]}` : segments[0] || ''
}

// 需要独立拆包的第三方库，未命中的 node_modules 统一归入 vendor
const CHUNK_GROUPS = {
  element: ['element-plus'],
  globe: ['three', 'globe.gl', 'three-globe', 'topojson-client', '@amap/amap-jsapi-loader'],
  utils: ['axios', 'dayjs', 'lodash', 'lodash-es']
}

const resolveChunkName = (pkg) => {
  if (!pkg) return 'vendor'
  // Element Plus 产物中包含 .vue2.mjs，必须整体归入 element，避免与 vendor 循环引用
  if (pkg === 'element-plus' || pkg.startsWith('@element-plus/')) return 'element'
  // three 生态（如 three-render-objects）与 three 强耦合，必须同属 globe
  if (pkg === 'three' || pkg.startsWith('three-')) return 'globe'
  for (const [name, packages] of Object.entries(CHUNK_GROUPS)) {
    if (packages.includes(pkg)) return name
  }
  return 'vendor'
}

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const root = __dirname

  return {
    plugins: [vue(), extraAppConfigPlugin({ mode, root })],
    resolve: {
      alias: {
        '@': resolve(root, 'src')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
          additionalData: `@use "@/assets/styles/variables.scss" as *;`
        }
      }
    },
    server: {
      port: 8090,
      host: '0.0.0.0',
      open: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        },
        '/ws': {
          target: 'ws://localhost:8080',
          ws: true
        },
        // 后端上传目录（Spring Boot 在 8080 提供 /upload 静态资源，生产由 Nginx 转发）
        '/upload': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    preview: {
      port: 8091,
      host: '0.0.0.0'
    },
    build: {
      outDir: 'dist',
      // 构建前清空输出目录，避免历史产物残留
      emptyOutDir: true,
      // 生产包不输出 sourcemap，避免源码泄露
      sourcemap: false,
      // 移除调试器语句
      minify: 'esbuild',
      target: 'es2020',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          // 产物目录规范：入口 jse/、异步 chunk js/、样式 css/、静态资源 assets/
          entryFileNames: 'jse/index-[name]-[hash].js',
          chunkFileNames: 'js/[name]-[hash].js',
          // 产物目录规范：样式归入 css/，图片/字体等静态资源统一归入 assets/
          assetFileNames: (assetInfo) => {
            const name = assetInfo.names?.[0] || assetInfo.name || ''
            return name.endsWith('.css')
              ? 'css/[name]-[hash][extname]'
              : 'assets/[name]-[hash][extname]'
          },
          manualChunks: (id) => {
            if (!id.includes('node_modules')) return
            return resolveChunkName(getPackageName(id))
          }
        }
      }
    },
    // 构建期间移除 debugger；console 保留，避免误删业务日志
    esbuild: {
      drop: mode === 'production' ? ['debugger'] : []
    }
  }
})
