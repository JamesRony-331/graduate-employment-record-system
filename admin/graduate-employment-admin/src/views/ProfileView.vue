<script setup>
import
{
  computed,
  reactive,
  ref,
  nextTick,
  onMounted,
  onBeforeUnmount,
}
from "vue";
import
{
  onBeforeRouteLeave,
  useRouter
}
from "vue-router";
import
{
  ElMessage,
  ElMessageBox
}
from "element-plus";
import
{
  session,
  saveSession,
  clearSession
}
from "../stores/session";
import
{
  updateInfo,
  getProfile
}
from "../api/account";
import
{
  profileErrors,
  updatePayload
}
from "../api/contracts";
const router = useRouter();
const profile = ref(null),
  profileLoading = ref(false),
  profileError = ref('');
let alive = true;
const user = computed(() => (
{
  username: session.user?.username,
  id: profile.value?.userId,
  realName: profile.value?.name
}));
async function loadProfile()
{
  profileLoading.value = true;
  profileError.value = '';
  try
  {
    const data = await getProfile();
    if (!data || data.userId == null)
      throw new Error('个人资料返回格式异常');
    if (alive)
    {
      profile.value = data;
      saveSession(session.token,
      {
        ...session.user,
        id: data.userId,
        realName: data.name
      });
    }
  }
  catch (e)
  {
    if (alive)
      profileError.value = e.message;
  }
  finally
  {
    if (alive)
      profileLoading.value = false;
  }
}
const editing = ref(false),
  busy = ref(false),
  errors = ref(
  {}),
  error = ref(""),
  summary = ref(null);
const model = reactive(
{
  realName: "",
  password: "",
  confirmPassword: "",
});
const baseline = ref("");
const dirty = computed(
  () => editing.value && JSON.stringify(model) !== baseline.value,
);
const dateText = (value) =>
  value ? String(value).replace("T", " ").slice(0, 19) : "—";

function startEdit()
{
  Object.assign(model,
  {
    realName: user.value.realName || "",
    password: "",
    confirmPassword: "",
  });
  baseline.value = JSON.stringify(model);
  errors.value = {};
  error.value = "";
  editing.value = true;
}
async function discard()
{
  if (dirty.value)
  {
    try
    {
      await ElMessageBox.confirm("尚未保存的修改将丢失。", "放弃修改？",
      {
        confirmButtonText: "放弃修改",
        cancelButtonText: "继续编辑",
      });
    }
    catch
    {
      return false;
    }
  }
  return true;
}
async function cancel()
{
  if (await discard())
  {
    editing.value = false;
    model.password = "";
    model.confirmPassword = "";
  }
}
async function submit()
{
  if (busy.value)
    return;
  errors.value = profileErrors(model);
  error.value = "";
  if (Object.keys(errors.value).length)
  {
    await nextTick();
    summary.value?.focus();
    return;
  }
  const payload = updatePayload(model, user.value);
  if (!Object.keys(payload).length)
  {
    editing.value = false;
    return;
  }
  busy.value = true;
  try
  {
    await updateInfo(payload);
    const reLogin = Boolean(payload.password);
    if (!reLogin)
      await loadProfile();
    saveSession(session.token,
    {
      ...session.user,
      ...(payload.realName !== undefined ?
      {
        realName: payload.realName
      } :
      {}),
    });
    editing.value = false;
    model.password = "";
    model.confirmPassword = "";
    ElMessage.success(reLogin ? "密码已修改，请重新登录" : "个人资料已保存");
    if (reLogin)
    {
      clearSession();
      await router.replace("/login");
    }
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
onBeforeRouteLeave(async () =>
{
  if (!session.token)
    return true;
  if (busy.value)
    return false;
  return discard();
});

function beforeUnload(e)
{
  if (dirty.value)
  {
    e.preventDefault();
    e.returnValue = "";
  }
}
onMounted(() =>
{
  window.addEventListener("beforeunload", beforeUnload);
  loadProfile();
});
onBeforeUnmount(() =>
{
  alive = false;
  window.removeEventListener("beforeunload", beforeUnload);
});
</script>
<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <h1 tabindex="-1">个人中心</h1>
      <p class="subtext">查看账号资料，维护个人信息与登录密码。</p>
    </div>
  </header>
  <p
    v-if="profileLoading"
    role="status"
  >正在加载个人资料…</p>
  <div
    v-if="profileError"
    role="alert"
  >
    <ElAlert
      :title="profileError"
      type="error"
      :closable="false"
    />
    <ElButton @click="loadProfile">重新加载</ElButton>
  </div>
  <section class="profile-identity">
    <span class="avatar">{{
        (user.realName || user.username || "用").slice(0, 1)
      }}</span>
    <div>
      <h2>{{ user.realName || user.username }}</h2>
      <p>{{ user.username }} <span class="quiet-tag">管理端账号</span></p>
    </div>
  </section>
  <section
    v-if="!editing"
    class="surface"
  >
    <div class="section-heading">
      <h2>账号资料</h2>
      <ElButton
        type="primary"
        :disabled="profileLoading || !!profileError || !profile"
        @click="startEdit"
      >修改资料</ElButton>
    </div>
    <dl class="profile-details">
      <div
        v-for="(label, key) in {
            realName: '姓名',
            username: '登录账号（登录信息）',
            id: '账号 ID',
          }"
        :key="key"
      >
        <dt>{{ label }}</dt>
        <dd>{{ user[key] ?? "未设置" }}</dd>
      </div>
      <div>
        <dt>角色</dt>
        <dd>{{ profile?.roles?.map(role => role.roleName).join('、') || '未绑定角色' }}</dd>
      </div>
    </dl>
    <p class="profile-note">姓名、账号 ID 和角色由个人资料接口实时获取。登录账号不可修改。</p>
  </section>
  <section
    v-else
    class="surface"
  >
    <div class="section-heading">
      <h2>修改资料</h2>
      <span class="muted">登录账号不可修改</span>
    </div>
    <ElForm
      label-position="top"
      @submit.prevent="submit"
    >
      <div
        v-if="error || Object.keys(errors).length"
        ref="summary"
        role="alert"
        tabindex="-1"
        class="error-summary"
      >
        <strong>{{ error || "请核对以下信息" }}</strong><a
          v-for="(msg, key) in errors"
          :key="key"
          :href="'#profile-' + key"
        >{{ msg }}</a>
      </div>
      <div class="form-grid">
        <ElFormItem
          label="姓名"
          for="profile-realName"
          :error="errors.realName"
        >
          <ElInput
            id="profile-realName"
            v-model="model.realName"
            maxlength="50"
            :disabled="busy"
            autocomplete="name"
          />
        </ElFormItem>
        <ElFormItem
          label="登录账号"
          for="profile-username"
        >
          <ElInput
            id="profile-username"
            :model-value="user.username"
            readonly
            aria-readonly="true"
            autocomplete="username"
          />
        </ElFormItem>
      </div>
      <h3 class="form-divider">修改密码 <span>不修改请留空</span></h3>
      <div class="form-grid">
        <ElFormItem
          label="新密码"
          for="profile-password"
          :error="errors.password"
        >
          <ElInput
            id="profile-password"
            v-model="model.password"
            type="password"
            show-password
            autocomplete="new-password"
            :disabled="busy"
            placeholder="至少6个字符"
          />
        </ElFormItem>
        <ElFormItem
          label="确认新密码"
          for="profile-confirmPassword"
          :error="errors.confirmPassword"
        >
          <ElInput
            id="profile-confirmPassword"
            v-model="model.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
            :disabled="busy"
            placeholder="再次输入新密码"
          />
        </ElFormItem>
      </div>
      <p class="profile-note">修改密码后，需要重新登录。</p>
      <div class="form-actions">
        <ElButton
          native-type="submit"
          type="primary"
          :loading="busy"
          :disabled="!dirty"
        >保存修改</ElButton>
        <ElButton
          :disabled="busy"
          @click="cancel"
        >取消</ElButton>
      </div>
    </ElForm>
  </section>
</div>
</template>
