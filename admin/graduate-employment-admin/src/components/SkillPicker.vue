<script setup>
import
{
  ref,
  computed,
  onMounted,
  onBeforeUnmount
}
from 'vue'
import
{
  ElSelect,
  ElOption,
  ElAlert,
  ElButton
}
from 'element-plus'
import
{
  managementRequest
}
from '../api/managementRequest'
const requestOptions = keyword => managementRequest('/work-skill/options?keyword=' +
  encodeURIComponent(keyword), 'GET')
const props = defineProps(
{
  modelValue:
  {
    type: Array,
    default: () => []
  }
})
const emit = defineEmits(['update:modelValue'])
const rows = ref([]),
  loading = ref(false),
  error = ref('')
let sequence = 0,
  timer
const selectedOptions = computed(() => [...new Set([...props.modelValue, ...rows.value.map(row =>
  row.name)])])
async function load(keyword = '')
{
  const current = ++sequence
  loading.value = true;
  error.value = ''
  try
  {
    const data = await requestOptions(keyword);
    if (current === sequence)
      rows.value = data
  }
  catch (e)
  {
    if (current === sequence)
    {
      rows.value = [];
      error.value = e.message
    }
  }
  finally
  {
    if (current === sequence)
      loading.value = false
  }
}

function search(keyword)
{
  clearTimeout(timer);
  ++sequence;
  timer = setTimeout(() => load(keyword), 250)
}
onMounted(() => load())
onBeforeUnmount(() =>
{
  ++sequence;
  clearTimeout(timer)
})
</script>
<template>
<div class="skill-picker">
  <ElSelect
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    multiple
    filterable
    remote
    :remote-method="search"
    :loading="loading"
    clearable
    placeholder="搜索并选择已有技能"
    aria-label="掌握技能"
    @visible-change="open => { if(open) load() }"
    no-data-text="没有匹配技能，请联系管理员添加"
  >
    <ElOption
      v-for="name in selectedOptions"
      :key="name"
      :label="name"
      :value="name"
    />
  </ElSelect>
  <small>默认展示前 20 个技能，可输入关键词搜索；不支持自定义添加。</small>
  <div
    v-if="error"
    role="alert"
  >
    <ElAlert
      :title="error"
      type="error"
      :closable="false"
    />
    <ElButton
      size="small"
      @click="load()"
    >重试</ElButton>
  </div>
</div>
</template>
<style scoped>
.skill-picker
{
  width: 100%
}

.skill-picker small
{
  display: block;
  color: var(--muted);
  line-height: 1.6;
  font-size: 12px;
  margin-top: 6px
}
</style>
