import
{
  createApp
}
from "vue";
import
{
  ElConfigProvider,
  ElForm,
  ElFormItem,
  ElInput,
  ElButton,
  ElIcon,
  ElAlert,
  ElTag,
}
from "element-plus";
import "element-plus/dist/index.css";
import router from "./router";
import "./style.css";
import App from "./App.vue";
const app = createApp(App);
for (const component of [
    ElConfigProvider,
    ElForm,
    ElFormItem,
    ElInput,
    ElButton,
    ElIcon,
    ElAlert,
    ElTag,
  ])

  app.component(component.name, component);
app.use(router).mount("#app");
