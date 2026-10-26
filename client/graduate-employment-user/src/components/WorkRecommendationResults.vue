<script setup>
import
{
  computed,
  ref,
  watch
}
from 'vue'
import
{
  ElEmpty,
  ElTag,
  ElDialog,
  ElDescriptions,
  ElDescriptionsItem
}
from 'element-plus'
const props = defineProps(
{
  records:
  {
    type: Array,
    required: true
  },
  stale: Boolean,
  submittedForm: Object
})
const page = ref(1),
  selected = ref(null),
  opened = ref(false)
const contactCompany = ref(null),
  contactOpen = ref(false)
const pages = computed(() => Math.max(1, Math.ceil(props.records.length / 10)))
const visible = computed(() => props.records.slice((page.value - 1) * 10, page.value * 10))
watch(() => props.records, () =>
{
  page.value = 1;
  opened.value = false
})
const display = value => Array.isArray(value) ? value.join('、') || '未提供' : value === null ||
  value === undefined || value === '' ? '未提供' : value
const fields = [
  ['学历', 'education'],
  ['专业', 'major'],
  ['工作年限', 'experience'],
  ['技能', 'skills'],
  ['岗位类别', 'categories'],
  ['城市', 'cities'],
  ['求职类型', 'jobType'],
  ['最低月薪（元）', 'minimumSalary'],
  ['期望月薪（元）', 'expectedSalary'],
  ['工作方式', 'workMode'],
  ['休息制度', 'restDays'],
  ['出差', 'travel'],
  ['外包', 'outsourcing'],
  ['最看重的因素', 'priority'],
  ['补充说明', 'other']
]

function showContact(row)
{
  contactCompany.value = row
  contactOpen.value = true
}

function detail(row)
{
  selected.value = row;
  opened.value = true
}
</script>
<template>
<section
  class="recommendation-results"
  aria-labelledby="result-heading"
>
  <header>
    <div>
      <h2
        id="result-heading"
        tabindex="-1"
      >岗位匹配结果</h2>
      <p>共 {{ records.length }} 条，按后端返回的匹配度排序。</p>
    </div>
    <ElTag
      v-if="stale"
      type="warning"
    >条件已修改，请重新提交</ElTag>
  </header>
  <p class="result-note">按本次提交条件计算匹配度，最低薪资和已开启的“必须满足”条件用于过滤。</p>
  <ElEmpty
    v-if="!records.length"
    description="暂无匹配结果，可调整条件后重新提交"
  />
  <ol
    v-else
    class="result-list"
    :start="(page-1)*10+1"
  >
    <li
      v-for="row in visible"
      :key="row.id"
    >
      <div class="result-main">
        <p class="company-name">{{ row.companyName || '公司名称未提供' }}</p>
        <h3>{{ display(row.categories) }} <small>#{{ row.id }}</small></h3>
        <p>{{ display(row.cities) }} · {{ display(row.jobType) }} · {{ display(row.education) }} ·
          {{ display(row.experience) }} 年
        </p>
        <div class="result-skills">
          <ElTag
            v-for="skill in row.skills || []"
            :key="skill"
            type="info"
          >{{ skill }}</ElTag>
        </div>
        <p>最低月薪：{{ display(row.minimumSalary) }}　期望月薪：{{ display(row.expectedSalary) }}<span
            v-if="row.minimumSalary != null || row.expectedSalary != null"
          > 元 / 月</span></p>
      </div>
      <div class="result-score">
        <strong>{{ row.similarity.toFixed(2) }}<small>%</small></strong><span>匹配度</span>
        <ElButton @click="showContact(row)">联系公司</ElButton>
        <ElButton
          :aria-label="`查看工作 ${row.id} 详情`"
          @click="detail(row)"
        >查看详情</ElButton>
      </div>
    </li>
  </ol>
  <nav
    v-if="records.length > 10"
    aria-label="匹配结果分页"
  >
    <ElButton
      :disabled="page<=1"
      @click="page--"
    >上一页</ElButton><span>{{page}} / {{pages}}</span>
    <ElButton
      :disabled="page>=pages"
      @click="page++"
    >下一页</ElButton>
  </nav>
  <ElDialog
    v-model="contactOpen"
    title="联系公司"
    width="480px"
    class="recommendation-detail"
  >
    <ElDescriptions
      v-if="contactCompany"
      :column="1"
      border
    >
      <ElDescriptionsItem label="公司名称">{{ contactCompany.companyName || '未提供' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="联系方式"><span
          class="contact-value">{{ display(contactCompany.companyContact) }}</span>
      </ElDescriptionsItem>
    </ElDescriptions>
    <template #footer>
      <ElButton @click="contactOpen=false">关闭</ElButton>
    </template>
  </ElDialog>
  <ElDialog
    v-model="opened"
    title="工作匹配详情"
    width="920px"
    class="recommendation-detail"
  >
    <template v-if="selected">
      <h3>{{ selected.companyName || '公司名称未提供' }}</h3>
      <p>联系方式：<span>{{ display(selected.companyContact) }}</span></p>
      <p class="detail-score">匹配度 {{ selected.similarity.toFixed(2) }}%</p>
      <p
        v-if="stale"
        class="result-note"
      >以下为生成本次结果时提交的条件，未使用当前修改中的表单。</p>
      <div class="comparison-scroll">
        <table class="comparison-table">
          <thead>
            <tr>
              <th scope="col">对比项目</th>
              <th scope="col">我的条件</th>
              <th scope="col">工作要求</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="[label,key] in fields"
              :key="key"
            >
              <th scope="row">{{ label }}</th>
              <td>{{ display(submittedForm?.[key]) }}
                <ElTag
                  v-if="submittedForm?.strict?.[key]"
                  size="small"
                >必须满足</ElTag>
              </td>
              <td>{{ display(selected[key]) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <template #footer>
      <ElButton @click="opened=false">关闭</ElButton>
    </template>
  </ElDialog>
</section>
</template>
<style scoped>
.comparison-scroll
{
  overflow-x: auto
}

.comparison-table
{
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  min-width: 480px
}

.comparison-table th,
.comparison-table td
{
  border: 1px solid var(--line);
  padding: 14px;
  text-align: left;
  vertical-align: top;
  overflow-wrap: anywhere
}

.comparison-table th
{
  background: #f1f5fa
}

.comparison-table th:first-child
{
  width: 24%
}

.contact-value
{
  user-select: text;
  overflow-wrap: anywhere
}

.company-name
{
  font-weight: 600
}

.recommendation-results
{
  margin-top: 28px;
  padding: 28px 32px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: white
}

.recommendation-results header
{
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center
}

.recommendation-results h2
{
  font-size: 23px;
  margin: 0 0 10px
}

.recommendation-results p
{
  color: var(--muted);
  font-size: 13px;
  line-height: 1.8
}

.result-note
{
  padding: 12px 16px;
  background: #f1f5fa;
  border-radius: 6px
}

.result-list
{
  list-style: none;
  padding: 0;
  margin: 0
}

.result-list li
{
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 0;
  border-bottom: 1px solid var(--line)
}

.result-main
{
  min-width: 0;
  overflow-wrap: anywhere
}

.result-main h3
{
  margin: 0 0 12px;
  font-size: 18px
}

.result-main h3 small
{
  font-weight: 400;
  color: var(--muted)
}

.result-skills
{
  display: flex;
  gap: 8px;
  flex-wrap: wrap
}

.result-score
{
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0
}

.result-score>.el-button
{
  width: 112px;
  margin-left: 0
}

.result-score strong
{
  font-size: 30px;
  color: var(--primary)
}

.result-score small
{
  font-size: 15px
}

.result-score>span
{
  font-size: 12px;
  color: var(--muted)
}

nav
{
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px
}

.detail-score
{
  color: var(--primary);
  font-size: 20px
}

:global(.recommendation-detail)
{
  max-width: calc(100vw - 32px)
}

@media(max-width:550px)
{
  .recommendation-results
  {
    padding: 20px 16px
  }

  .recommendation-results header
  {
    align-items: flex-start;
    flex-direction: column
  }

  .result-list li
  {
    gap: 14px
  }

  .result-score strong
  {
    font-size: 24px
  }
}
</style>
