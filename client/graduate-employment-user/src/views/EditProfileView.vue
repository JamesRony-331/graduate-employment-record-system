<script setup>
import
{
  computed,
  reactive,
  ref,
  nextTick,
  onMounted,
  onBeforeUnmount
}
from 'vue'
import
{
  useRouter,
  onBeforeRouteLeave
}
from 'vue-router'
import
{
  ElMessage,
  ElMessageBox
}
from 'element-plus'
import ProfileFrame from '../components/ProfileFrame.vue'
import
{
  session,
  updateProfile
}
from '../stores/session'
import
{
  saveContact
}
from '../api/student'
import
{
  contactErrors
}
from '../api/contracts'
const router = useRouter()
const model = reactive(
{
  phone: session.user?.phone || '',
  email: session.user?.email || ''
})
const errors = ref(
  {}),
  error = ref(''),
  busy = ref(false),
  summary = ref(null)
const baseline = ref(JSON.stringify(model))
const dirty = computed(() => JSON.stringify(model) !== baseline.value)
async function submit()
{
  if (busy.value)
    return
  errors.value = contactErrors(model, JSON.parse(baseline.value))
  error.value = ''
  if (Object.keys(errors.value).length)
  {
    await nextTick()
    summary.value?.focus()
    return
  }
  busy.value = true
  try
  {
    const values = {
      phone: model.phone.trim(),
      email: model.email.trim()
    }
    if (!session.demo)
      await saveContact(values)
    updateProfile(values)
    Object.assign(model, values)
    baseline.value = JSON.stringify(model)
    ElMessage.success(session.demo ? '示例资料已保存到本次会话' : '个人信息已保存')
    await router.push('/profile')
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
onBeforeRouteLeave(async () =>
{
  if (!session.user)
    return true
  if (!dirty.value)
    return true
  if (busy.value)
    return false
  try
  {
    await ElMessageBox.confirm('离开后，本次未保存的修改将丢失。', '放弃修改？',
    {
      confirmButtonText: '放弃修改',
      cancelButtonText: '继续编辑',
      type: 'warning',
    })
    return true
  }
  catch
  {
    return false
  }
})

function beforeUnload(event)
{
  if (dirty.value)
  {
    event.preventDefault()
    event.returnValue = ''
  }
}
onMounted(() => window.addEventListener('beforeunload', beforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
</script>
<template>
<ProfileFrame>
  <div class="section-title">
    <div>
      <h2>修改个人信息</h2>
      <p>更新联系方式，学籍信息不受影响。</p>
    </div>
    <span class="quiet-label">联系方式</span>
  </div>
  <div class="editing-context">
    <span>{{ session.user?.name || '学生' }}</span><span>学号
      {{ session.user?.studentNo || '—' }}</span><span>姓名与学籍资料由学校维护</span>
  </div>
  <ElForm
    class="edit-form"
    :model="model"
    label-position="top"
    @submit.prevent="submit"
  >
    <div
      v-if="error || Object.keys(errors).length"
      ref="summary"
      class="error-summary"
      tabindex="-1"
      role="alert"
    >
      <strong>{{ error || '请检查以下信息' }}</strong><a
        v-for="(message, field) in errors"
        :key="field"
        :href="'#edit-' + field"
      >{{
          message
        }}</a>
    </div>
    <h3>联系方式</h3>
    <div class="form-grid">
      <ElFormItem
        label="手机号码"
        for="edit-phone"
        :error="errors.phone"
      >
        <ElInput
          id="edit-phone"
          v-model="model.phone"
          type="tel"
          autocomplete="tel"
          inputmode="tel"
          placeholder="请输入手机号码"
          maxlength="11"
          :disabled="busy"
          :aria-invalid="Boolean(errors.phone)"
        />
        <p class="field-hint">用于学校联系，请填写本人常用号码。</p>
      </ElFormItem>
      <ElFormItem
        label="电子邮箱"
        for="edit-email"
        :error="errors.email"
      >
        <ElInput
          id="edit-email"
          v-model="model.email"
          autocomplete="email"
          inputmode="email"
          placeholder="请输入电子邮箱"
          maxlength="100"
          :disabled="busy"
          :aria-invalid="Boolean(errors.email)"
        />
        <p class="field-hint">用于接收就业相关资料。</p>
      </ElFormItem>
    </div>
    <p class="edit-note">已有联系方式可更新，暂不支持清空。学籍信息如需更正，请联系辅导员。</p>
    <div class="form-actions">
      <ElButton
        type="primary"
        native-type="submit"
        :loading="busy"
        :disabled="!dirty"
      >{{
          busy ? '正在保存' : '保存修改'
        }}</ElButton>
      <ElButton
        :disabled="busy"
        @click="router.push('/profile')"
      >取消</ElButton><span
        v-if="dirty"
        role="status"
      >有未保存的修改</span>
    </div>
  </ElForm>
</ProfileFrame>
</template>
