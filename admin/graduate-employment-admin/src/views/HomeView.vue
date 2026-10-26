<script setup>
import
{
  DocumentChecked,
  User,
  Bell,
  Grid,
  ArrowRight,
}
from "@element-plus/icons-vue";
import
{
  session
}
from "../stores/session";
const date = new Intl.DateTimeFormat("zh-CN",
{
  timeZone: "Asia/Shanghai",
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "long",
}).format(new Date());
const entries = [
{
  title: "账号管理",
  description: "查看账号状态与身份绑定信息",
  icon: User,
  path: "/accounts",
  available: true,
},
{
  title: "辅导员管理",
  description: "维护辅导员资料与在职状态",
  icon: User,
  path: "/counselors",
  available: true,
},
{
  title: "学生管理",
  description: "维护学生资料与班级信息",
  icon: User,
  path: "/students",
  available: true,
},
{
  title: "备案审核",
  description: "核对就业信息，跟进审核流程",
  icon: DocumentChecked,
  path: "/filings",
  available: true,
},
{
  title: "通知公告",
  description: "发布填报安排与学校通知",
  icon: Bell,
  path: "/notices",
  available: true,
},
{
  title: "服务管理",
  description: "维护学生端服务入口",
  icon: Grid,
  path: "/services",
}, ];
</script>
<template>
<div class="page-stack">
  <header class="page-heading">
    <div>
      <p class="context-label">{{ date }}</p>
      <h1 tabindex="-1">
        {{ session.user?.realName || session.user?.username }}，欢迎回来
      </h1>
      <p class="subtext">从这里进入就业管理工作，查看和维护你的账号资料。</p>
    </div>
    <RouterLink
      class="text-link"
      to="/profile"
    >个人中心 <ElIcon>
        <ArrowRight />
      </ElIcon>
    </RouterLink>
  </header>
  <section class="work-banner">
    <div>
      <p>就业备案工作</p>
      <h2>从学生填报，到审核归档</h2>
      <p class="banner-note">保持信息准确，跟进每一次备案申请。</p>
    </div>
    <ol>
      <li><span>01</span>学生提交</li>
      <li><span>02</span>辅导员初审</li>
      <li><span>03</span>院系复审</li>
      <li><span>04</span>已归档</li>
    </ol>
  </section>
  <section class="surface">
    <div class="section-heading">
      <h2>业务入口</h2>
      <span class="muted">功能将逐步开放</span>
    </div>
    <div class="entry-grid">
      <RouterLink
        v-for="entry in entries"
        :key="entry.path"
        :to="entry.path"
        class="entry"
      >
        <ElIcon :size="25">
          <component :is="entry.icon" />
        </ElIcon>
        <div>
          <h3>
            {{ entry.title }}
            <span
              v-if="!entry.available"
              class="quiet-tag"
            >{{
                entry.preview ? "静态预览" : "待开放"
              }}</span>
          </h3>
          <p>{{ entry.description }}</p>
        </div>
        <ElIcon class="entry-arrow">
          <ArrowRight />
        </ElIcon>
      </RouterLink>
    </div>
  </section>
  <div class="home-bottom">
    <section class="surface">
      <div class="section-heading">
        <h2>待办事项</h2>
        <span class="quiet-tag">待开放</span>
      </div>
      <div class="todo-empty">
        <ElIcon :size="30">
          <DocumentChecked />
        </ElIcon>
        <h3>审核待办将在这里集中展示</h3>
        <p>后续可查看待审核申请，按办理阶段跟进。</p>
      </div>
    </section>
    <section class="surface rules">
      <h2>备案办理规则</h2>
      <ul>
        <li>学生提交后只读，辅导员打回后方可修改重提。</li>
        <li>审核期间不允许提交新的备案或变更申请。</li>
        <li>历史记录保留，以最新一次申请为准。</li>
        <li>归档是审核完成后的状态，无需单独操作。</li>
      </ul>
      <p class="muted">统计概览待统计口径确定后开放。</p>
    </section>
  </div>
</div>
</template>
