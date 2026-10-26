import vue from "@vitejs/plugin-vue";
import
{
  defineConfig
}
from "vite";
export default defineConfig(
{
  plugins: [vue()],
  server:
  {
    host: "127.0.0.1",
    port: 5176,
    proxy:
    {
      "/api":
      {
        target: "http://127.0.0.1:5050",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
});
