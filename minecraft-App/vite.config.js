import { defineConfig, loadEnv } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

// 开发环境代理：浏览器请求 /api、/upload 时转发到后端，规避跨域限制
// 代理目标统一从 VITE_API_BASE_URL 的 origin（协议+主机+端口）推导，
// 保证与 utils/config.js 中的接口基地址始终一致，避免出现“请求网址错误”。
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiBase = env.VITE_API_BASE_URL || env.VITE_APP_BASE_URL || 'http://system.61j8or.top:8080/api';

  // 取接口基地址的 origin 作为代理目标；解析失败时回退到默认远程后端
  let proxyTarget = 'http://system.61j8or.top:8080';
  try {
    proxyTarget = new URL(apiBase).origin;
  } catch (e) {
    // 非法地址时使用默认值
  }

  return {
    plugins: [uni()],
    server: {
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true
        },
        // 后端上传目录（Spring Boot 在 8080 提供 /upload 静态资源），
        // H5 下图片走同源代理，否则 /upload/xxx 会被当作前端静态资源返回 404
        '/upload': {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    }
  };
});
