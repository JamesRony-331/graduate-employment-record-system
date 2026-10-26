import vue from '@vitejs/plugin-vue'
import
{
  defineConfig
}
from 'vite'

// https://vite.dev/config/
export default defineConfig(
{
  plugins: [vue()],
  server:
  {
    host: '127.0.0.1',
    proxy:
    {
      '/api':
      {
        target: 'http://127.0.0.1:5050',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
