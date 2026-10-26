<script setup>
import
{
  reactive,
  ref,
  nextTick
}
from "vue";
import
{
  useRouter,
  useRoute
}
from "vue-router";
import
{
  School,
  Right
}
from "@element-plus/icons-vue";
import
{
  login
}
from "../api/account";
import
{
  loadMenus,
  resetMenus
}
from "../stores/menus";
import
{
  saveSession,
  clearSession
}
from "../stores/session";
const router = useRouter(),
  route = useRoute();
const model = reactive(
  {
    username: "",
    password: ""
  }),
  errors = ref(
  {}),
  error = ref(""),
  busy = ref(false),
  summary = ref(null);
async function submit()
{
  if (busy.value)
    return;
  errors.value = {};
  error.value = "";
  if (!model.username.trim() || model.username.trim().length > 50)

    errors.value.username = "请输入账号（最多50个字符）";
  if (!model.password.trim())
    errors.value.password = "请输入密码";
  if (Object.keys(errors.value).length)
  {
    await nextTick();
    summary.value?.focus();
    return;
  }
  busy.value = true;
  try
  {
    const data = await login(
    {
      username: model.username.trim(),
      password: model.password,
    });
    if (
      !data?.token ||
      !data?.userMsgVO?.username ||
      data?.userMsgVO?.accountType !== "STAFF"
    )

      throw new Error("登录返回信息不完整，请联系系统维护人员");
    saveSession(data.token, data.userMsgVO);
    resetMenus();
    try
    {
      await loadMenus();
    }
    catch (e)
    {
      clearSession();
      resetMenus();
      throw e;
    }
    model.password = "";
    await router.replace("/");
  }
  catch (e)
  {
    error.value = e.message;
    await nextTick();
    summary.value?.focus();
  }
  finally
  {
    busy.value = false;
  }
}
</script>
<template>
<main class="login-page">
  <section class="login-story">
    <div class="brand">
      <ElIcon :size="32">
        <School />
      </ElIcon>
      <div>
        <strong>毕业生就业管理</strong><small>高校毕业生就业信息备案系统</small>
      </div>
    </div>
    <div class="login-statement">
      <p class="context-label">学校管理工作空间</p>
      <h1>每一份去向，<br />都有清晰的记录。</h1>
      <p>
        连接学生填报与学校审核，<br />让毕业去向的核对、跟进与留存有序进行。
      </p>
    </div>
    <ol class="login-flow">
      <li>学生填报</li>
      <li>辅导员初审</li>
      <li>院系复审</li>
      <li>备案留存</li>
    </ol>
    <p class="login-footer">毕业生服务 · 管理端</p>
  </section>
  <section class="login-form-area">
    <div class="login-form-wrap">
      <p class="context-label">欢迎使用</p>
      <h2>登录管理端</h2>
      <p class="subtext">请使用学校提供的教职工或管理账号登录。</p>
      <ElAlert
        v-if="route.query.expired"
        title="登录已失效，请重新登录"
        type="warning"
        :closable="false"
        show-icon
      />
      <ElForm
        label-position="top"
        @submit.prevent="submit"
      >
        <div
          v-if="error || Object.keys(errors).length"
          ref="summary"
          class="error-summary"
          role="alert"
          tabindex="-1"
        >
          <strong>{{ error || "请检查登录信息" }}</strong><a
            v-for="(msg, key) in errors"
            :key="key"
            :href="'#login-' + key"
          >{{ msg }}</a>
        </div>
        <ElFormItem
          label="账号"
          for="login-username"
          :error="errors.username"
        >
          <ElInput
            id="login-username"
            v-model="model.username"
            autocomplete="username"
            maxlength="50"
            placeholder="请输入登录账号"
            :disabled="busy"
          />
        </ElFormItem>
        <ElFormItem
          label="密码"
          for="login-password"
          :error="errors.password"
        >
          <ElInput
            id="login-password"
            v-model="model.password"
            type="password"
            autocomplete="current-password"
            show-password
            placeholder="请输入密码"
            :disabled="busy"
          />
        </ElFormItem>
        <ElButton
          class="login-submit"
          native-type="submit"
          type="primary"
          :loading="busy"
        >{{ busy ? "正在登录" : "登录工作台"
            }}
          <ElIcon v-if="!busy">
            <Right />
          </ElIcon>
        </ElButton>
      </ElForm>
      <p class="login-assistance">忘记账号或密码？请联系学校系统维护人员。</p>
    </div>
    <p class="login-legal">请妥善保管账号，使用结束后及时退出登录。</p>
  </section>
</main>
</template>
