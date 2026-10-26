<script setup>
import
{
  ref,
  onMounted,
  onBeforeUnmount
}
from 'vue'
import
{
  session
}
from '../stores/session'
import
{
  unwrapResponse
}
from '../api/contracts'
const services = ref([])
const loading = ref(false)
const error = ref('')
let controller
const safeAddress = value => typeof value === 'string' && (/^\/(?!\/)[A-Za-z0-9/_?#=&.%~-]*$/.test(
  value) || /^https?:\/\/[A-Za-z0-9][^\s\\]*$/.test(value))
async function load()
{
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  error.value = ''
  const timeout = setTimeout(() => controller.abort(), 15000)
  try
  {
    const response = await fetch('/api/service-entry/enabled',
    {
      headers:
      {
        Authorization: `Bearer ${session.token}`
      },
      signal: controller.signal
    })
    if (!response.ok)
      throw new Error(response.status === 401 ? '请重新登录后查看服务' : '服务加载失败，请重试')
    const data = unwrapResponse(await response.json())
    if (!Array.isArray(data) || data.some(item => !item || !safeAddress(item.url) || !safeAddress(
        item.iconUrl)))
      throw new Error('服务数据格式异常')
    services.value = data
  }
  catch (e)
  {
    error.value = e.name === 'AbortError' ? '请求超时，请重试' : e.message
  }
  finally
  {
    clearTimeout(timeout);
    loading.value = false
  }
}
onMounted(load)
onBeforeUnmount(() => controller?.abort())
</script>
<template>
<section
  class="service-launcher"
  aria-labelledby="all-services-title"
>
  <h2 id="all-services-title">全部服务</h2>
  <p
    v-if="loading"
    role="status"
  >正在加载服务…</p>
  <div
    v-else-if="error"
    role="alert"
  >{{ error }} <button
      type="button"
      @click="load"
    >重新加载</button></div>
  <p v-else-if="!services.length">暂无可用服务</p>
  <div
    v-else
    class="launcher-grid"
  >
    <component
      :is="service.url.startsWith('/') ? 'RouterLink' : 'a'"
      v-for="service in services"
      :key="service.id"
      :to="service.url.startsWith('/') ? service.url : undefined"
      :href="service.url.startsWith('/') ? undefined : service.url"
      :target="service.url.startsWith('/') ? undefined : '_blank'"
      rel="noopener noreferrer"
      class="launcher-item"
    >
      <img
        :src="service.iconUrl"
        alt=""
        width="64"
        height="64"
      />
      <span>{{ service.name }}</span>
    </component>
  </div>
  <svg
    class="launcher-campus"
    viewBox="0 0 420 180"
    fill="none"
    aria-hidden="true"
  >
    <path
      d="M5 175h410M65 175V65l150-50 150 50v110M55 65h320M80 82h270M95 97v60m40-60v60m40-60v60m80-60v60m40-60v60m40-60v60M200 175v-63h30v63M33 175v-45m-15 15 15-15 15 15M390 175v-45m-15 15 15-15 15 15M215 15V2h42v15"
      stroke="currentColor"
      stroke-width="2"
    />
  </svg>
</section>
</template>
<style scoped>
.service-launcher
{
  position: relative;
  isolation: isolate;
  background: white;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 28px 30px 30px
}

h2
{
  font-size: 20px;
  color: var(--primary);
  margin: 0 0 24px;
  font-weight: 600
}

.launcher-grid
{
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px
}

.launcher-item
{
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  min-height: 135px;
  padding: 14px 8px;
  border-radius: 8px;
  color: var(--ink);
  font-size: 14px;
  text-align: center;
  line-height: 1.6
}

.launcher-item:hover
{
  background: #f0f4fa;
  color: var(--primary)
}

.launcher-item:focus-visible
{
  outline: 2px solid var(--primary);
  outline-offset: 3px
}

.launcher-item img
{
  object-fit: contain
}

.launcher-campus
{
  position: absolute;
  z-index: -1;
  right: 0;
  bottom: 0;
  width: min(36%, 380px);
  color: #e8edf5;
  pointer-events: none;
  border-radius: 0 0 14px 0
}

@media(max-width:600px)
{
  .service-launcher
  {
    padding: 20px 16px
  }

  .launcher-grid
  {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px
  }

  .launcher-item
  {
    font-size: 12px;
    gap: 12px;
    min-height: 120px;
    padding: 10px 4px
  }

  .launcher-item img
  {
    width: 56px;
    height: 56px
  }

  .launcher-campus
  {
    width: 65%
  }
}
</style>
