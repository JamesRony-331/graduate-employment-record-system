<script setup>
import WorkRecommendationResults from '../components/WorkRecommendationResults.vue'
import SkillPicker from '../components/SkillPicker.vue'
import
{
  computed,
  reactive,
  ref,
  watch,
  nextTick
}
from 'vue'
import
{
  ElSelect,
  ElOption,
  ElInputNumber,
  ElSwitch,
  ElDialog,
  ElDescriptions,
  ElDescriptionsItem,
  ElAlert
}
from 'element-plus'
import
{
  submitWorkSelect,
  workSelectPayload
}
from '../api/workSelect'
const submitting = ref(false)
const submitError = ref('')
const submitSuccess = ref('')
const recommendations = ref(null)
const submittedForm = ref(null)
const submittedConditions = ref('')
const resultsStale = computed(() => submittedConditions.value !== JSON.stringify(workSelectPayload(
  form, strict)))
const formRef = ref()
const previewOpen = ref(false)
const fields = [
{
  title: '个人能力',
  description: '填写你目前的能力与经历。应届生的工作年限填写 0。',
  items: [
  {
    key: 'education',
    label: '最高学历',
    required: true,
    options: ['高中及以下', '中专 / 职高', '大专', '本科', '硕士', '博士']
  },
  {
    key: 'major',
    label: '专业',
    text: true
  },
  {
    key: 'experience',
    label: '工作年限',
    required: true,
    number: true,
    unit: '年'
  },
  {
    key: 'skills',
    label: '掌握技能',
    required: true,
    multiple: true,
    create: true,
    options: ['Java', 'Spring Boot', 'MySQL', 'JavaScript', 'Vue', 'React', 'Python',
      'Excel', '数据分析', '沟通协作'
    ]
  }, ]
},
{
  title: '求职目标',
  description: '可以选择多个岗位类别和城市，也可以输入其他选项。',
  items: [
  {
    key: 'categories',
    label: '期望岗位类别',
    required: true,
    multiple: true,
    create: true,
    options: ['后端开发', '前端开发', '软件测试', '数据分析', '产品经理', '运营', '设计', '财务', '市场营销', '行政人事']
  },
  {
    key: 'cities',
    label: '期望城市',
    required: true,
    multiple: true,
    create: true,
    strict: true,
    options: ['不限', '北京', '上海', '广州', '深圳', '杭州', '温州', '宁波', '南京', '成都', '武汉', '西安']
  },
  {
    key: 'jobType',
    label: '求职类型',
    required: true,
    options: ['全职', '实习', '兼职']
  }, ]
},
{
  title: '薪资要求',
  description: '单位为元 / 月。最低可接受月薪填写后将作为底线，期望月薪用于推荐排序。',
  items: [
  {
    key: 'minimumSalary',
    label: '最低可接受月薪',
    number: true,
    unit: '元 / 月'
  },
  {
    key: 'expectedSalary',
    label: '期望月薪',
    number: true,
    unit: '元 / 月'
  }, ]
},
{
  title: '工作偏好',
  description: '开启“必须满足”表示硬性条件；关闭时仅作为偏好。未填写表示未表达偏好。',
  items: [
  {
    key: 'workMode',
    label: '工作方式',
    strict: true,
    options: ['现场办公', '远程', '混合办公']
  },
  {
    key: 'restDays',
    label: '休息制度',
    strict: true,
    options: ['双休', '单双休', '单休']
  },
  {
    key: 'travel',
    label: '是否接受出差',
    strict: true,
    options: ['不接受', '接受偶尔出差', '不限']
  },
  {
    key: 'outsourcing',
    label: '是否接受外包岗位',
    strict: true,
    options: ['不接受', '接受', '不限']
  }, ]
},
{
  title: '推荐偏好与补充说明',
  description: '选择一个最看重的因素，帮助明确推荐重点。',
  items: [
  {
    key: 'priority',
    label: '最看重的因素',
    required: true,
    options: ['薪资', '技能匹配', '地点', '休息制度', '专业相关性', '工作方式', '发展空间']
  },
  {
    key: 'other',
    label: '其他要求',
    text: true,
    textarea: true
  }, ]
}, ]
const allFields = fields.flatMap(section => section.items)
const initial = () => Object.fromEntries(allFields.map(field => [field.key, field.multiple ? [] :
  field.number ? undefined : ''
]))
const form = reactive(initial())
const strict = reactive(Object.fromEntries(allFields.filter(field => field.strict).map(field => [
  field.key, false
])))
const hasValue = value => Array.isArray(value) ? value.length > 0 : value !== undefined && value !==
  null && String(value).trim() !== ''
const canRequire = key => hasValue(form[key]) && form[key] !== '不限' && !(Array.isArray(form[key]) &&
  form[key].includes('不限'))
watch(() => [...form.cities], (next, previous) =>
{
  if (next.includes('不限') && next.length > 1)
    form.cities = previous.includes('不限') ? next
    .filter(city => city !== '不限') : ['不限']
})
watch(form, () =>
{
  for (const key of Object.keys(strict))
    if (!canRequire(key))
      strict[key] = false
})
const rules = Object.fromEntries(allFields.filter(field => field.required).map(field => [field.key,
  [
  {
    validator: (_, value, done) => done(hasValue(value) ? undefined : new Error(
      `请填写${field.label}`)),
    trigger: 'change'
  }]
]))
rules.expectedSalary = [
{
  validator: (_, value, done) => done(hasValue(value) && hasValue(form.minimumSalary) && value <
    form.minimumSalary ? new Error('期望月薪不能低于最低可接受月薪') : undefined),
  trigger: 'change'
}]
const completed = computed(() => allFields.filter(field => field.required && hasValue(form[field
  .key])).length)
const requiredCount = allFields.filter(field => field.required).length
async function preview()
{
  submitError.value = ''
  submitSuccess.value = ''
  if (await formRef.value.validate().catch(() => false))
    previewOpen.value = true
}
async function submit()
{
  if (submitting.value || submitSuccess.value)
    return
  if (!await formRef.value.validate().catch(() => false))
    return
  submitting.value = true
  submitError.value = ''
  try
  {
    const payload = workSelectPayload(form, strict)
    const data = await submitWorkSelect(payload)
    submittedForm.value = structuredClone(payload)
    recommendations.value = data
    submittedConditions.value = JSON.stringify(payload)
    submitSuccess.value = '已获取匹配结果'
    previewOpen.value = false
    await nextTick()
    document.getElementById('result-heading')?.focus()
    document.getElementById('result-heading')?.scrollIntoView(
    {
      behavior: 'auto',
      block: 'start'
    })
  }
  catch (error)
  {
    submitError.value = error.message || '提交失败，请稍后重试'
  }
  finally
  {
    submitting.value = false
  }
}

function reset()
{
  Object.assign(form, initial())
  Object.keys(strict).forEach(key =>
  {
    strict[key] = false
  })
  formRef.value.clearValidate()
}

function display(field)
{
  const value = form[field.key]
  if (!hasValue(value))
    return '未填写（未表达偏好）'
  return (Array.isArray(value) ? value.join('、') : String(value).trim()) + (field.unit ?
    ` ${field.unit}` : '')
}
</script>

<template>
<div class="recommendation-page">
  <header class="recommendation-heading">
    <div>
      <p class="service-caption">学生就业服务</p>
      <h1 tabindex="-1">岗位推荐</h1>
      <p>从你的能力和期望出发，描述一份适合自己的工作。</p>
    </div>
    <span class="preview-label">求职条件采集</span>
  </header>
  <aside
    class="ai-guide"
    aria-label="AI 就业问答入口"
  >
    <div>
      <h2>推荐给你方向，AI 帮你理清下一步</h2>
      <p>聊聊岗位选择、技能提升和面试准备。向就业 AI 助手提问，获取针对性的建议。</p>
    </div>
    <RouterLink to="/ai-chat">进入 AI 问答 →</RouterLink>
  </aside>
  <ElAlert
    title="填写并提交条件，查看数据库中的工作匹配度。已按最低薪资及“必须满足”条件筛选，其余条件用于匹配排序。"
    type="info"
    :closable="false"
    show-icon
  />
  <ElForm
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="preference-form"
    scroll-to-error
    @submit.prevent="preview"
  >
    <section
      v-for="section in fields"
      :key="section.title"
      class="preference-section"
    >
      <div class="section-intro">
        <h2>{{ section.title }}</h2>
        <p>{{ section.description }}</p>
      </div>
      <div class="field-grid">
        <ElFormItem
          v-for="field in section.items"
          :key="field.key"
          :label="field.label"
          :prop="field.key"
          :class="{ 'wide-field': field.textarea }"
        >
          <SkillPicker
            v-if="field.key === 'skills'"
            v-model="form.skills"
          />
          <ElInput
            v-else-if="field.text"
            v-model="form[field.key]"
            :type="field.textarea ? 'textarea' : 'text'"
            :rows="4"
            :maxlength="field.textarea ? 1000 : 100"
            :show-word-limit="field.textarea"
            :placeholder="field.textarea ? '例如：希望有系统的入职培训（当前仅展示）' : '请输入专业名称（选填）'"
            :aria-label="field.label"
          />
          <div
            v-else-if="field.number"
            class="number-field"
          >
            <ElInputNumber
              v-model="form[field.key]"
              :min="0"
              :max="field.key === 'experience' ? 60 : 10000000"
              :precision="0"
              :controls="false"
              :placeholder="field.required ? '应届生填 0' : '选填'"
              :aria-label="field.label"
            /><span>{{ field.unit }}</span>
          </div>
          <ElSelect
            v-else
            v-model="form[field.key]"
            :multiple="field.multiple"
            :allow-create="field.create"
            filterable
            clearable
            default-first-option
            :placeholder="field.create ? '请选择或输入后按回车添加' : '请选择'"
            :aria-label="field.label"
          >
            <ElOption
              v-for="option in field.options"
              :key="option"
              :label="option"
              :value="option"
            />
          </ElSelect>
          <div
            v-if="field.strict"
            class="strict-setting"
          >
            <ElSwitch
              v-model="strict[field.key]"
              :disabled="!canRequire(field.key)"
              :aria-label="`${field.label}必须满足`"
            /><span>必须满足</span>
          </div>
        </ElFormItem>
      </div>
    </section>
    <footer class="form-actions">
      <p>必填项 {{ completed }} / {{ requiredCount }}<span>带 * 的项目为必填项</span></p>
      <div>
        <ElButton @click="reset">清空表单</ElButton>
        <ElButton
          type="primary"
          native-type="submit"
        >预览求职条件</ElButton>
      </div>
    </footer>
  </ElForm>
  <WorkRecommendationResults
    v-if="recommendations !== null"
    :records="recommendations"
    :submitted-form="submittedForm"
    :stale="resultsStale"
  />
  <ElDialog
    v-model="previewOpen"
    title="求职条件预览"
    width="720px"
    class="recommendation-preview"
    :close-on-click-modal="!submitting"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
  >
    <p class="preview-note">请核对后提交。未填写的可选项以空值发送，不推断其他偏好。</p>
    <ElAlert
      v-if="submitError"
      :title="submitError"
      type="error"
      :closable="false"
      role="alert"
    />
    <ElAlert
      v-if="submitSuccess"
      :title="submitSuccess"
      type="success"
      :closable="false"
      role="status"
    />
    <ElDescriptions
      :column="1"
      border
    >
      <ElDescriptionsItem
        v-for="field in allFields"
        :key="field.key"
        :label="field.label"
      >{{ display(field) }}<strong
          v-if="strict[field.key]"
          class="strict-badge"
        >必须满足</strong></ElDescriptionsItem>
    </ElDescriptions>
    <template #footer>
      <ElButton
        :disabled="submitting"
        @click="previewOpen = false"
      >{{ submitSuccess ? '关闭' : '返回修改' }}</ElButton>
      <ElButton
        type="primary"
        :loading="submitting"
        :disabled="!!submitSuccess"
        @click="submit"
      >{{ submitSuccess ? '已提交' : '获取匹配结果' }}</ElButton>
    </template>
  </ElDialog>
</div>
</template>

<style scoped>
.ai-guide
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  background: #eaf1fb;
  border: 1px solid #d5e1f2;
  border-radius: 12px;
  padding: 22px 24px;
  margin-bottom: 24px
}

.ai-guide h2
{
  font-size: 18px;
  margin: 0 0 8px;
  color: var(--primary)
}

.ai-guide p
{
  font-size: 13px;
  color: var(--muted);
  line-height: 1.8;
  margin: 0
}

.ai-guide a
{
  flex-shrink: 0;
  background: var(--primary);
  color: white;
  padding: 12px 18px;
  border-radius: 7px
}

.ai-guide a:focus-visible
{
  outline: 2px solid var(--primary);
  outline-offset: 4px
}

@media(max-width:600px)
{
  .ai-guide
  {
    align-items: flex-start;
    flex-direction: column;
    padding: 20px
  }
}

.recommendation-page
{
  max-width: 1100px;
  margin: 0 auto
}

.recommendation-heading
{
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  margin-bottom: 28px
}

.service-caption
{
  font-size: 13px;
  color: var(--primary);
  margin: 0 0 10px
}

.recommendation-heading h1
{
  font-size: 32px;
  margin: 0 0 12px
}

.recommendation-heading p:not(.service-caption)
{
  color: var(--muted);
  line-height: 1.8
}

.preview-label
{
  white-space: nowrap;
  color: var(--primary);
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 13px
}

.preference-form
{
  margin-top: 24px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 0 32px
}

.preference-section
{
  padding: 30px 0;
  border-bottom: 1px solid var(--line);
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 32px
}

.section-intro h2
{
  font-size: 19px;
  margin: 0 0 12px
}

.section-intro p
{
  color: var(--muted);
  font-size: 13px;
  line-height: 1.9;
  margin: 0;
  max-width: 220px
}

.field-grid
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 24px
}

.wide-field
{
  grid-column: 1/-1
}

.number-field
{
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%
}

.number-field .el-input-number
{
  flex: 1;
  min-width: 0;
  width: 100%
}

.number-field span
{
  white-space: nowrap;
  color: var(--muted);
  font-size: 12px
}

.strict-setting
{
  display: flex;
  gap: 8px;
  align-items: center;
  color: var(--muted);
  font-size: 12px;
  width: 100%;
  margin-top: 6px
}

.form-actions
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 24px 0
}

.form-actions p
{
  font-size: 14px;
  margin: 0
}

.form-actions p span
{
  display: block;
  color: var(--muted);
  font-size: 12px;
  margin-top: 6px
}

.preview-note
{
  color: var(--muted);
  line-height: 1.8;
  margin-bottom: 20px
}

.strict-badge
{
  display: inline-block;
  font-size: 12px;
  color: var(--primary);
  margin-left: 12px
}

.recommendation-page :deep(.el-form-item__label)
{
  color: var(--ink)
}

:global(.recommendation-preview)
{
  max-width: calc(100vw - 32px)
}

.recommendation-page :deep(.el-descriptions__content)
{
  overflow-wrap: anywhere
}

@media(max-width:800px)
{
  .preference-section
  {
    grid-template-columns: 1fr;
    gap: 20px
  }

  .section-intro p
  {
    max-width: none
  }

  .preference-form
  {
    padding: 0 20px
  }
}

@media(max-width:520px)
{
  .field-grid
  {
    grid-template-columns: 1fr
  }

  .recommendation-heading
  {
    align-items: flex-start
  }

  .recommendation-heading h1
  {
    font-size: 27px
  }

  .preview-label
  {
    font-size: 11px;
    padding: 6px
  }

  .form-actions
  {
    align-items: flex-start;
    flex-direction: column
  }

  .form-actions>div
  {
    display: flex;
    width: 100%
  }

  .form-actions .el-button
  {
    flex: 1
  }

  .preference-form
  {
    padding: 0 16px
  }
}
</style>
