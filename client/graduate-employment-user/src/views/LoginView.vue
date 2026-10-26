<script setup>
import
{
  reactive,
  ref,
  nextTick
}
from 'vue'
import
{
  useRouter,
  useRoute
}
from 'vue-router'
import
{
  ElMessageBox
}
from 'element-plus'
import
{
  ArrowRight,
  Lock,
  User
}
from '@element-plus/icons-vue'
import BrandMark from '../components/BrandMark.vue'
import
{
  login
}
from '../api/student'
import
{
  setSession,
  startPreview
}
from '../stores/session'
const router = useRouter(),
  route = useRoute()
const model = reactive(
{
  studentNo: '',
  password: ''
})
const errors = reactive(
  {}),
  busy = ref(false),
  error = ref(''),
  summary = ref(null)
const preview = import.meta.env.DEV
async function submit()
{
  if (busy.value)
    return
  delete errors.studentNo
  delete errors.password
  if (!model.studentNo.trim())
    errors.studentNo = '请输入学号'
  if (!model.password)
    errors.password = '请输入密码'
  error.value = ''
  if (Object.keys(errors).length)
  {
    await nextTick()
    summary.value?.focus()
    return
  }
  busy.value = true
  try
  {
    const data = await login(
    {
      studentNo: model.studentNo.trim(),
      password: model.password
    })
    if (!data?.token || !data?.studentMsgVO || data?.userMsgVO?.accountType !== 'STUDENT')

      throw new Error('登录返回信息不完整，请联系管理员')
    setSession(data.token, data.studentMsgVO)
    model.password = ''
    await router.replace('/')
  }
  catch (e)
  {
    error.value = e.message
    await nextTick()
    summary.value?.focus()
  }
  finally
  {
    busy.value = false
  }
}

function help()
{
  ElMessageBox.alert(
    '学生账号由学校维护。忘记密码或无法登录时，请联系本班辅导员重置密码。',
    '登录帮助',
    {
      confirmButtonText: '知道了'
    },
  ).catch(() =>
  {})
}

function demo()
{
  startPreview()
  router.push('/')
}
</script>
<template>
<main class="login-page">
  <section class="login-story">
    <BrandMark />
    <div class="story-copy">
      <p class="story-intro">从校园出发</p>
      <h1 tabindex="-1">记录成长，<br />开启下一程。</h1>
      <p class="story-description">
        高校毕业生就业信息备案系统<br />让每一份去向，都有清晰的记录。
      </p>
    </div>
    <svg
      class="campus-art"
      viewBox="0 0 560 300"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M40 278h480M100 278V110h360v168M90 110l190-80 190 80M125 110l155-65 155 65M225 278V175a55 55 0 0 1 110 0v103M242 278V175a38 38 0 0 1 76 0v103M140 145h35v50h-35zM385 145h35v50h-35zM140 215h35v40h-35zM385 215h35v40h-35zM280 30V6h65v23h-65M60 278v-66m-16 30 16-30 16 30m408 36v-66m-16 30 16-30 16 30"
        stroke="currentColor"
        stroke-width="2"
      />
    </svg>
    <p class="story-footer">高校毕业生服务平台</p>
  </section>
  <section class="login-panel">
    <div class="login-form-wrap">
      <span class="login-edition">学生端</span>
      <h2>欢迎回来</h2>
      <p class="login-subtitle">使用学号登录，管理你的个人信息。</p>
      <div
        v-if="route.query.expired"
        class="form-notice"
        role="status"
      >
        登录已失效，请重新登录。
      </div>
      <div
        v-if="error || Object.keys(errors).length"
        ref="summary"
        class="error-summary"
        role="alert"
        tabindex="-1"
      >
        <strong>{{ error || '请检查以下信息' }}</strong><a
          v-for="(message, field) in errors"
          :key="field"
          :href="'#login-' + field"
        >{{
            message
          }}</a>
      </div>
      <ElForm
        :model="model"
        label-position="top"
        @submit.prevent="submit"
      >
        <ElFormItem
          label="学号"
          for="login-studentNo"
          :error="errors.studentNo"
        >
          <ElInput
            id="login-studentNo"
            v-model="model.studentNo"
            :prefix-icon="User"
            placeholder="请输入学号"
            autocomplete="username"
            maxlength="32"
            :disabled="busy"
            :aria-invalid="Boolean(errors.studentNo)"
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
            :prefix-icon="Lock"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="current-password"
            :disabled="busy"
            :aria-invalid="Boolean(errors.password)"
          />
        </ElFormItem>
        <div class="login-help">
          <span>请使用学校提供的学生账号</span><button
            type="button"
            class="text-button"
            @click="help"
          >忘记密码？</button>
        </div>
        <ElButton
          class="login-submit"
          type="primary"
          native-type="submit"
          :loading="busy"
        >{{ busy ? '正在登录' : '登 录' }}
          <ElIcon v-if="!busy">
            <ArrowRight />
          </ElIcon>
        </ElButton>
      </ElForm>
      <p class="login-support">账号异常？请联系本班辅导员。</p>
      <button
        v-if="preview"
        class="preview-button"
        :disabled="busy"
        @click="demo"
      >
        预览页面（示例资料）
      </button>
    </div>
    <p class="login-footer">高校毕业生就业信息备案系统</p>
  </section>
</main>
</template>
