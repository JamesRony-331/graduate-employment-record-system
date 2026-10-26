<script setup>
import
{
  computed
}
from 'vue'
import
{
  ArrowRight,
  Document,
  Bell,
  Suitcase,
  InfoFilled
}
from '@element-plus/icons-vue'
import NoticeList from '../components/NoticeList.vue'
import ServiceLauncher from '../components/ServiceLauncher.vue'
import
{
  session
}
from '../stores/session'

const user = computed(() => session.user ||
{})
const missingContacts = computed(() => [!user.value.phone && '手机号', !user.value.email && '邮箱']
  .filter(Boolean),
)
const date = new Intl.DateTimeFormat('zh-CN',
{
  timeZone: 'Asia/Shanghai',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
}).format(new Date())
const steps = [
{
  title: '提交备案',
  description: '填写单位、岗位及联系方式'
},
{
  title: '辅导员初审',
  description: '核对填报内容及相关材料'
},
{
  title: '院系复审',
  description: '审核完成后更新备案状态'
}, ]
</script>

<template>
<div class="home-page">
  <section
    class="home-welcome"
    aria-labelledby="welcome-title"
  >
    <div>
      <p class="home-context">学生服务工作台</p>
      <h1
        id="welcome-title"
        tabindex="-1"
      >{{ user.name || '同学' }}，欢迎回来</h1>
      <p class="welcome-meta">
        {{
            [user.college, user.major].filter(Boolean).join(' / ') ||
            '从这里查看你的就业服务与个人信息。'
          }}
      </p>
    </div>
    <p class="home-date">{{ date }}</p>
  </section>

  <section
    class="contact-reminder"
    aria-label="个人资料提醒"
  >
    <ElIcon aria-hidden="true">
      <InfoFilled />
    </ElIcon>
    <p v-if="missingContacts.length">
      <strong>完善联系方式</strong><span>你的{{ missingContacts.join('和') }}尚未填写，请及时补充，方便学校与你联系。</span>
    </p>
    <p v-else>
      <strong>联系方式已填写</strong><span>如联系方式有变化，请及时更新个人资料。</span>
    </p>
    <RouterLink to="/profile/edit">{{ missingContacts.length ? '去完善' : '更新资料'
        }}
      <ElIcon aria-hidden="true">
        <ArrowRight />
      </ElIcon>
    </RouterLink>
  </section>

  <div class="home-main-grid">
    <section
      class="filing-panel"
      aria-labelledby="filing-title"
    >
      <div class="filing-intro">
        <div>
          <span class="filing-label">
            <ElIcon aria-hidden="true">
              <Document />
            </ElIcon>就业备案
          </span>
          <h2 id="filing-title">记录你的毕业去向</h2>
          <p>填报就业信息，查看备案记录，跟进审核进度。</p>
        </div>
        <span class="light-status">已开放</span>
      </div>
      <div class="filing-action">
        <RouterLink
          to="/filing"
          class="filing-link"
        >进入就业备案<ElIcon aria-hidden="true">
            <ArrowRight />
          </ElIcon>
        </RouterLink><span>首次填写备案，或查看最新审核进度。</span>
      </div>
      <div class="filing-guide">
        <h3>办理流程</h3>
        <ol>
          <li
            v-for="(step, index) in steps"
            :key="step.title"
          >
            <span
              class="step-number"
              aria-hidden="true"
            >{{ index + 1 }}</span>
            <div>
              <h4>{{ step.title }}</h4>
              <p>{{ step.description }}</p>
            </div>
          </li>
        </ol>
        <details class="filing-rules">
          <summary>填报前，请了解这些规则</summary>
          <ul>
            <li>提交后只能查看；辅导员打回后可修改并重新提交。</li>
            <li>已有审核中的备案时，不能提交新的备案或变更申请。</li>
            <li>备案变更需另行申请；允许多次申请，保留历史，以最新一次为准。</li>
            <li>归档是备案状态，无需单独执行归档操作。</li>
          </ul>
        </details>
      </div>
    </section>

    <section
      class="notice-panel"
      aria-labelledby="notice-title"
    >
      <div class="home-section-heading">
        <h2 id="notice-title">通知公告</h2>
        <RouterLink to="/notices">查看全部<ElIcon aria-hidden="true">
            <ArrowRight />
          </ElIcon>
        </RouterLink>
      </div>
      <NoticeList compact />
      <div class="notice-footnote">办理时间与材料要求，请以学校发布的通知为准。</div>
    </section>
  </div>

  <ServiceLauncher />

  <section
    class="home-recruitment"
    aria-labelledby="recruitment-title"
  >
    <div class="recruitment-title">
      <ElIcon
        :size="25"
        aria-hidden="true"
      >
        <Suitcase />
      </ElIcon>
      <div>
        <h2 id="recruitment-title">校园招聘</h2>
        <p>认识校招企业，寻找适合自己的岗位。</p>
      </div>
    </div>
    <div class="recruitment-copy">
      <span class="neutral-status">已开放</span>
      <p>查看企业与岗位信息，了解工作要求及联系方式。</p>
    </div>
    <RouterLink
      to="/jobs"
      class="button-link"
    >进入校园招聘<ElIcon aria-hidden="true">
        <ArrowRight />
      </ElIcon>
    </RouterLink>
  </section>
</div>
</template>

<style scoped>
.home-page
{
  display: grid;
  gap: 28px;
}

.home-welcome
{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 4px 0;
}

.home-context
{
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 8px;
}

.welcome-meta
{
  color: var(--muted);
  font-size: 13px;
  margin-top: 8px;
}

.home-date
{
  font-size: 13px;
  color: var(--muted);
  white-space: nowrap;
}

.contact-reminder
{
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #eaf0f9;
  border-radius: 8px;
  color: var(--primary);
}

.contact-reminder>.el-icon
{
  flex-shrink: 0;
  font-size: 18px;
}

.contact-reminder p
{
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  font-size: 12px;
}

.contact-reminder strong
{
  font-weight: 600;
  font-size: 13px;
}

.contact-reminder p span
{
  color: #475f81;
}

.contact-reminder a
{
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  min-height: 44px;
  white-space: nowrap;
  font-size: 13px;
}

.home-main-grid
{
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(280px, 1fr);
  gap: 24px;
}

.filing-panel
{
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  background: white;
}

.filing-intro
{
  background: #17345d;
  color: white;
  padding: 28px 28px 18px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.filing-label
{
  display: flex;
  align-items: center;
  gap: 8px;
  color: #d5e1f1;
  font-size: 12px;
  margin-bottom: 18px;
}

.filing-intro h2
{
  font-size: 25px;
  font-weight: 500;
  letter-spacing: 0.4px;
}

.filing-intro p
{
  color: #d5e1f1;
  font-size: 13px;
  margin-top: 10px;
}

.light-status
{
  height: fit-content;
  border: 1px solid #7289a8;
  padding: 4px 9px;
  border-radius: 4px;
  color: #e3ebf7;
  font-size: 11px;
  white-space: nowrap;
}

.filing-action
{
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  background: #17345d;
  padding: 8px 28px 28px;
}

.filing-link
{
  display: inline-flex;
  align-items: center;
  gap: 22px;
  min-height: 44px;
  padding: 12px 18px;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.filing-link:hover
{
  background: #eaf0f9;
}

.filing-action>span
{
  font-size: 11px;
  color: #d5e1f1;
}

.filing-guide
{
  padding: 24px 28px 20px;
}

.filing-guide>h3
{
  font-size: 13px;
  color: var(--muted);
  font-weight: 400;
}

.filing-guide ol
{
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin: 22px 0 24px;
  padding: 0;
  list-style: none;
  gap: 16px;
}

.filing-guide li
{
  min-width: 0;
}

.filing-guide ol li
{
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.step-number
{
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid #b5c4d8;
  width: 25px;
  height: 25px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--primary);
}

.filing-guide h4
{
  margin: 2px 0 7px;
  font-size: 13px;
  font-weight: 500;
}

.filing-guide ol p
{
  font-size: 11px;
  color: var(--muted);
  line-height: 1.7;
}

.filing-rules
{
  border-top: 1px solid var(--line);
  font-size: 12px;
  color: var(--muted);
}

.filing-rules summary
{
  cursor: pointer;
  padding: 16px 0 0;
  min-height: 44px;
  color: var(--primary);
}

.filing-rules summary:focus-visible
{
  outline: 3px solid #648bc4;
  outline-offset: 3px;
}

.filing-rules ul
{
  padding-left: 18px;
  margin-bottom: 0;
  line-height: 1.9;
}

.filing-rules ul li+li
{
  margin-top: 8px;
}

.notice-panel
{
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.home-section-heading
{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.home-section-heading h2
{
  font-size: 17px;
}

.home-section-heading>a
{
  display: inline-flex;
  align-items: center;
  gap: 7px;
  min-height: 44px;
  font-size: 12px;
}

.home-section-heading>p
{
  color: var(--muted);
  font-size: 12px;
}

.notice-empty
{
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-align: center;
  gap: 14px;
  padding: 32px 0;
}

.empty-symbol
{
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f0f4fa;
  color: #6a80a0;
}

.notice-empty h3
{
  font-size: 14px;
  font-weight: 500;
}

.notice-empty p
{
  font-size: 12px;
  color: var(--muted);
}

.neutral-status
{
  background: #f1f3f7;
  color: var(--muted);
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  display: inline-block;
}

.notice-footnote
{
  font-size: 11px;
  color: var(--muted);
  border-top: 1px solid var(--line);
  padding-top: 16px;
  line-height: 1.8;
}

.home-services
{
  padding: 4px 0;
}

.service-links
{
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 18px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
}

.service-link
{
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 25px 22px;
  min-width: 0;
  color: var(--ink);
}

.service-link+.service-link
{
  border-left: 1px solid var(--line);
}

.service-link:hover
{
  background: #eef2f8;
}

.service-icon
{
  color: var(--primary);
  display: flex;
}

.service-link h3
{
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
}

.service-link p
{
  font-size: 11px;
  color: var(--muted);
  margin-top: 8px;
}

.service-status
{
  font-size: 10px;
  font-weight: 400;
  color: var(--muted);
}

.service-arrow
{
  margin-left: auto;
  color: var(--muted);
  flex-shrink: 0;
}

.home-recruitment
{
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 26px 28px;
}

.recruitment-title
{
  display: flex;
  align-items: center;
  gap: 16px;
}

.recruitment-title>.el-icon
{
  color: var(--primary);
}

.recruitment-title h2
{
  font-size: 17px;
}

.recruitment-title p,
.recruitment-copy p
{
  font-size: 12px;
  color: var(--muted);
  margin-top: 8px;
}

.recruitment-copy
{
  margin-left: auto;
}

.home-recruitment>a
{
  gap: 12px;
}

@media (max-width: 1100px)
{
  .service-link
  {
    padding: 22px 16px;
    gap: 12px;
  }

  .service-link h3
  {
    font-size: 13px;
  }

  .home-recruitment
  {
    flex-wrap: wrap;
  }

  .recruitment-copy
  {
    margin-left: 0;
  }
}

@media (max-width: 800px)
{
  .home-main-grid
  {
    grid-template-columns: 1fr;
  }

  .service-links
  {
    grid-template-columns: 1fr;
  }

  .service-link+.service-link
  {
    border-left: 0;
    border-top: 1px solid var(--line);
  }

  .notice-empty
  {
    padding: 28px 0;
  }

  .home-recruitment>a
  {
    margin-left: auto;
  }
}

@media (max-width: 600px)
{
  .home-page
  {
    gap: 20px;
  }

  .home-welcome
  {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .home-welcome h1
  {
    font-size: 25px;
  }

  .home-date
  {
    font-size: 12px;
  }

  .contact-reminder
  {
    padding: 12px 16px;
    gap: 10px;
    align-items: flex-start;
  }

  .contact-reminder>.el-icon
  {
    margin-top: 5px;
  }

  .contact-reminder a
  {
    align-self: center;
  }

  .filing-intro
  {
    padding: 24px 20px 16px;
  }

  .filing-intro h2
  {
    font-size: 22px;
  }

  .filing-action
  {
    padding: 4px 20px 24px;
  }

  .filing-guide
  {
    padding: 22px 20px;
  }

  .filing-guide ol
  {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .filing-guide ol p
  {
    font-size: 12px;
  }

  .filing-guide ol li
  {
    gap: 14px;
  }

  .notice-panel
  {
    padding: 20px;
  }

  .home-recruitment
  {
    padding: 24px 20px;
    gap: 20px;
  }

  .home-recruitment>a
  {
    margin-left: 0;
  }

  .recruitment-copy
  {
    width: 100%;
  }
}
</style>
