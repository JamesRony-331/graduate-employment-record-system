<script setup>
import
{
  ref,
  reactive,
  watch,
  onMounted
}
from 'vue'
import
{
  ElTable,
  ElTableColumn,
  ElDialog,
  ElSelect,
  ElOption,
  ElInputNumber,
  ElSwitch,
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElMessageBox
}
from 'element-plus'
import
{
  managementRequest as request
}
from '../api/managementRequest'
import SkillPicker from '../components/SkillPicker.vue'
import
{
  fields
}
from '../data/workFields'
const all = fields.flatMap(section => section.items)
const initial = () => Object.fromEntries(all.map(f => [f.key, f.multiple ? [] : f.number ?
  undefined : ''
]))
const form = reactive(initial()),
  strict = reactive(
  {
    cities: false,
    workMode: false,
    restDays: false,
    travel: false,
    outsourcing: false
  })
const rows = ref([]),
  page = ref(1),
  pages = ref(0),
  total = ref(0),
  loading = ref(false),
  error = ref(''),
  opened = ref(false),
  mode = ref(''),
  id = ref(null),
  saving = ref(false),
  dialogError = ref(''),
  formRef = ref(),
  deleting = ref(null)
const hasValue = value => Array.isArray(value) ? value.length > 0 : value !== undefined && value !==
  null && String(value).trim() !== ''
const canRequire = key => hasValue(form[key]) && form[key] !== '不限' && !(Array.isArray(form[key]) &&
  form[key].includes('不限'))
watch(() => [...form.cities], (next, previous) =>
{
  if (next.includes('不限') && next.length > 1)
    form.cities = previous.includes('不限') ? next
    .filter(v => v !== '不限') : ['不限']
})
watch(form, () =>
{
  for (const key of Object.keys(strict))
    if (!canRequire(key))
      strict[key] = false
})
const rules = Object.fromEntries(all.filter(f => f.required).map(f => [f.key, [
{
  validator: (_, v, done) => done(hasValue(v) ? undefined : new Error(`请填写${f.label}`)),
  trigger: 'change'
}]]))
rules.expectedSalary = [
{
  validator: (_, v, done) => done(hasValue(v) && hasValue(form.minimumSalary) && v < form
    .minimumSalary ? new Error('期望月薪不能低于最低月薪') : undefined),
  trigger: 'change'
}]
async function load(target = page.value)
{
  loading.value = true;
  error.value = '';
  try
  {
    let data = await request(`/work-compare/page?pageNum=${target}&pageSize=10`, 'GET');
    if (target > Math.max(1, data.total))
    {
      target = Math.max(1, data.total);
      data = await request(`/work-compare/page?pageNum=${target}&pageSize=10`, 'GET')
    }
    rows.value = data.records;
    page.value = target;
    pages.value = data.total;
    total.value = data.totalCount
  }
  catch (e)
  {
    error.value = e.message
  }
  finally
  {
    loading.value = false
  }
}
async function open(next, row)
{
  mode.value = next;
  id.value = row?.id;
  dialogError.value = '';
  Object.assign(form, initial());
  Object.keys(strict).forEach(k => strict[k] = false);
  if (row)
  {
    try
    {
      const data = await request('/work-compare/' + row.id, 'GET');
      for (const f of all)
        form[f.key] = data[f.key] ?? (f.multiple ? [] : f.number ? undefined :
          '');
      Object.keys(strict).forEach(k => strict[k] = data.strict?.[k] === true)
    }
    catch (e)
    {
      ElMessage.error(e.message);
      return
    }
  }
  opened.value = true
}
async function save()
{
  if (saving.value || !await formRef.value.validate().catch(() => false))
    return;
  saving.value = true;
  dialogError.value = '';
  try
  {
    const body = Object.fromEntries(all.map(f => [f.key, typeof form[f.key] === 'string' ? form[f
      .key].trim() || null : form[f.key] ?? null]));
    body.strict = {
      ...strict
    };
    await request(id.value ? '/work-compare/' + id.value : '/work-compare/add', id.value ? 'PUT' :
      'POST', body);
    ElMessage.success('保存成功');
    opened.value = false;
    await load()
  }
  catch (e)
  {
    dialogError.value = e.message
  }
  finally
  {
    saving.value = false
  }
}
async function remove(row)
{
  try
  {
    await ElMessageBox.confirm(`确定删除工作记录 #${row.id}？删除后无法恢复。`, '删除工作',
    {
      type: 'warning'
    })
  }
  catch
  {
    return
  }
  deleting.value = row.id;
  try
  {
    await request('/work-compare/' + row.id, 'DELETE');
    ElMessage.success('删除成功');
    await load()
  }
  catch (e)
  {
    ElMessage.error(e.message)
  }
  finally
  {
    deleting.value = null
  }
}
const display = value => Array.isArray(value) ? value.join('、') : value == null || value === '' ?
  '未填写' : value
onMounted(() => load())
</script>
<template>
<div class="work-management">
  <header>
    <div>
      <h1 tabindex="-1">工作管理</h1>
      <p>维护用于岗位推荐对比的工作条件，字段与求职表单保持一致。</p>
    </div>
    <ElButton
      type="primary"
      @click="open('add')"
    >新增工作</ElButton>
  </header>
  <section class="work-panel">
    <div class="list-heading">
      <h2>工作列表</h2>
      <ElButton
        :loading="loading"
        @click="load()"
      >刷新列表</ElButton>
    </div>
    <ElAlert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
    />
    <p
      v-if="loading"
      role="status"
    >正在加载工作列表…</p>
    <ElTable
      :data="rows"
      :aria-busy="loading"
      empty-text="暂无工作记录"
    >
      <ElTableColumn
        prop="id"
        label="ID"
        width="75"
      />
      <ElTableColumn
        prop="companyName"
        label="公司名称"
        min-width="180"
      />
      <ElTableColumn
        label="岗位类别"
        min-width="180"
      ><template #default="{row}">{{display(row.categories)}}</template></ElTableColumn>
      <ElTableColumn
        label="城市 / 类型"
        min-width="160"
      ><template #default="{row}">{{display(row.cities)}}<small>{{row.jobType}}</small></template>
      </ElTableColumn>
      <ElTableColumn
        label="学历 / 年限"
        min-width="130"
      ><template #default="{row}">{{row.education}}<small>{{row.experience}} 年</small></template>
      </ElTableColumn>
      <ElTableColumn
        label="技能"
        min-width="200"
      ><template #default="{row}">{{display(row.skills)}}</template></ElTableColumn>
      <ElTableColumn
        label="月薪（最低 / 期望）"
        min-width="170"
      ><template #default="{row}">{{display(row.minimumSalary)}} /
          {{display(row.expectedSalary)}}</template></ElTableColumn>
      <ElTableColumn
        label="操作"
        fixed="right"
        width="210"
      ><template #default="{row}">
          <ElButton
            link
            type="primary"
            @click="open('detail',row)"
          >详情</ElButton>
          <ElButton
            link
            type="primary"
            @click="open('edit',row)"
          >编辑</ElButton>
          <ElButton
            link
            type="danger"
            :loading="deleting===row.id"
            :disabled="deleting!==null"
            @click="remove(row)"
          >删除</ElButton>
        </template></ElTableColumn>
    </ElTable>
    <footer><span>共 {{total}} 条</span>
      <div>
        <ElButton
          :disabled="loading||page<=1"
          @click="load(page-1)"
        >上一页</ElButton><span>第 {{page}} / {{Math.max(1,pages)}} 页</span>
        <ElButton
          :disabled="loading||page>=pages"
          @click="load(page+1)"
        >下一页</ElButton>
      </div>
    </footer>
  </section>
  <ElDialog
    v-model="opened"
    :title="mode==='detail'?'工作详情':mode==='edit'?'编辑工作':'新增工作'"
    width="900px"
    class="work-editor"
    destroy-on-close
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    :show-close="!saving"
  >
    <ElAlert
      v-if="dialogError"
      :title="dialogError"
      type="error"
      :closable="false"
    />
    <ElDescriptions
      v-if="mode==='detail'"
      :column="1"
      border
    >
      <ElDescriptionsItem
        v-for="f in all"
        :key="f.key"
        :label="f.label"
      >{{display(form[f.key])}} <b v-if="strict[f.key]">（必须满足）</b></ElDescriptionsItem>
    </ElDescriptions>
    <ElForm
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      :disabled="saving"
      scroll-to-error
    >
      <section
        v-for="section in fields"
        :key="section.title"
        class="form-section"
      >
        <h3>{{section.title}}</h3>
        <div class="form-grid">
          <ElFormItem
            v-for="f in section.items"
            :key="f.key"
            :label="f.label"
            :prop="f.key"
          >
            <SkillPicker
              v-if="f.key==='skills'"
              v-model="form.skills"
            />
            <ElInput
              v-else-if="f.text"
              v-model="form[f.key]"
              :type="f.textarea?'textarea':'text'"
              :maxlength="f.textarea?1000:f.key.startsWith('company')?200:100"
              :rows="3"
              :aria-label="f.label"
            />
            <ElInputNumber
              v-else-if="f.number"
              v-model="form[f.key]"
              :min="0"
              :max="f.key==='experience'?60:10000000"
              :precision="0"
              :controls="false"
              :aria-label="f.label"
            />
            <ElSelect
              v-else
              v-model="form[f.key]"
              :multiple="f.multiple"
              :allow-create="f.create"
              filterable
              clearable
              :aria-label="f.label"
            >
              <ElOption
                v-for="v in f.options"
                :key="v"
                :label="v"
                :value="v"
              />
            </ElSelect>
            <div
              v-if="f.strict"
              class="strict-option"
            >
              <ElSwitch
                v-model="strict[f.key]"
                :disabled="!canRequire(f.key)"
                :aria-label="f.label+'必须满足'"
              /><span>必须满足</span>
            </div>
          </ElFormItem>
        </div>
      </section>
    </ElForm>
    <template #footer>
      <ElButton
        :disabled="saving"
        @click="opened=false"
      >关闭</ElButton>
      <ElButton
        v-if="mode!=='detail'"
        type="primary"
        :loading="saving"
        @click="save"
      >保存</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
header,
.list-heading,
footer
{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px
}

header
{
  margin-bottom: 28px
}

h1
{
  margin: 0 0 12px
}

header p,
small
{
  color: var(--muted)
}

small
{
  display: block;
  margin-top: 6px
}

.work-panel
{
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 24px
}

.list-heading
{
  margin-bottom: 20px
}

h2
{
  font-size: 20px;
  margin: 0
}

footer
{
  margin-top: 24px
}

footer>div
{
  display: flex;
  gap: 14px;
  align-items: center
}

.form-section
{
  border-bottom: 1px solid var(--line);
  padding: 12px 0
}

.form-grid
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 24px
}

.strict-option
{
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  font-size: 12px;
  color: var(--muted)
}

.el-input-number
{
  width: 100%
}

:global(.work-editor)
{
  max-width: calc(100vw - 32px)
}

@media(max-width:650px)
{

  header,
  footer
  {
    align-items: flex-start;
    flex-direction: column
  }

  .form-grid
  {
    grid-template-columns: 1fr
  }

  .work-panel
  {
    padding: 16px
  }
}
</style>
