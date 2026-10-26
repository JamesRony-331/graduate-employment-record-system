<script setup>
import
{
  ref,
  nextTick,
  onBeforeUnmount
}
from 'vue'
import MarkdownAnswer from '../components/MarkdownAnswer.vue'
import
{
  sendJobPrompt
}
from '../api/jobAi'
import
{
  ChatDotRound,
  ArrowRight
}
from '@element-plus/icons-vue'
const question = ref('')
const input = ref()
const latestReply = ref()
const pendingQuestion = ref('')
const sending = ref(false),
  error = ref(''),
  messages = ref([])
let controller, alive = true
onBeforeUnmount(() =>
{
  alive = false;
  controller?.abort()
})
async function send()
{
  const prompt = question.value.trim()
  if (!prompt || sending.value)
    return
  sending.value = true;
  error.value = '';
  pendingQuestion.value = prompt
  controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 120000)
  try
  {
    const reply = await sendJobPrompt(prompt, controller.signal)
    if (!alive)
      return
    messages.value.push(
    {
      prompt,
      reply
    })
    question.value = ''
    await nextTick()
    if (latestReply.value)
      latestReply.value.scrollTop = latestReply.value.scrollHeight
  }
  catch (e)
  {
    if (alive)
      error.value = e.name === 'AbortError' ? '回答等待超时，请稍后重新发送' : e instanceof TypeError ?
      '无法连接后端，请检查网络及服务状态' : e.message
  }
  finally
  {
    clearTimeout(timeout)
    if (alive)
      sending.value = false
  }
}
const suggestions = [
{
  title: '解读岗位匹配',
  question: '怎样理解岗位匹配度？选择工作时还应该关注哪些条件？'
},
{
  title: '准备面试',
  question: '我是应届毕业生，应该怎样准备第一场求职面试？'
},
{
  title: '完善简历',
  question: '缺少工作经验时，如何在简历中展示课程项目和个人技能？'
},
{
  title: '了解就业备案',
  question: '提交就业备案前，我需要准备哪些信息和材料？'
}, ]

function choose(value)
{
  if (sending.value)
    return;
  question.value = value;
  input.value?.focus()
}
</script>
<template>
<div class="ai-page">
  <header class="ai-heading">
    <div>
      <p class="ai-caption">毕业生就业助手</p>
      <h1 tabindex="-1">AI 就业问答</h1>
      <p>从岗位选择到面试准备，把你的求职问题说清楚。</p>
    </div>
    <RouterLink
      to="/recommendations"
      class="back-recommendations"
    >岗位推荐 <ElIcon>
        <ArrowRight />
      </ElIcon>
    </RouterLink>
  </header>
  <section
    class="chat-workspace"
    aria-label="就业问答"
  >
    <div
      ref="latestReply"
      class="chat-scroll"
      tabindex="0"
      role="region"
      aria-label="问答内容，可滚动查看"
    >
      <div
        v-if="!messages.length && !sending"
        class="chat-empty"
      ><span class="assistant-mark">
          <ElIcon :size="30">
            <ChatDotRound />
          </ElIcon>
        </span>
        <h2>你想先了解什么？</h2>
        <p>可以从下面的问题开始，也可以写下自己的情况。</p>
      </div>
      <div
        v-if="!messages.length && !sending"
        class="suggestion-grid"
      ><button
          v-for="item in suggestions"
          :key="item.title"
          type="button"
          @click="choose(item.question)"
        ><strong>{{ item.title }}</strong><span>{{ item.question }}</span>
          <ElIcon aria-hidden="true">
            <ArrowRight />
          </ElIcon>
        </button></div>
      <div
        class="chat-status"
        role="status"
      >可以咨询就业备案、岗位选择、简历和面试准备。请在每次提问中写明相关背景。</div>
      <div
        v-if="messages.length"
        class="message-list"
        role="log"
        aria-label="问答记录"
        aria-live="polite"
      >
        <article
          v-for="(message, index) in messages"
          :key="index"
        >
          <div class="user-message"><strong>我的问题</strong>
            <p>{{ message.prompt }}</p>
          </div>
          <div class="service-message"><strong>就业 AI 助手</strong>
            <MarkdownAnswer :content="message.reply" />
          </div>
        </article>
      </div>

      <p
        v-if="error"
        class="send-error"
        role="alert"
      >{{ error }}，输入内容已保留。</p>
      <div
        v-if="sending"
        class="pending-message"
        :aria-busy="sending"
      >
        <div class="user-message"><strong>我的问题</strong>
          <p>{{ pendingQuestion }}</p>
        </div>
        <p
          class="thinking"
          role="status"
        >就业 AI 助手正在整理回答，请稍候…</p>
      </div>
    </div>
    <p class="context-note">每次提问独立处理，不记忆之前的问答；多次询问可能推荐相同岗位。每次优先推荐 3–5 个相关岗位，不足时按实际数量返回，更多结果请前往岗位推荐。
    </p>
    <form
      class="composer"
      @submit.prevent="send"
    >
      <label for="career-question">你的问题</label>
      <ElInput
        id="career-question"
        ref="input"
        v-model="question"
        :disabled="sending"
        type="textarea"
        :rows="3"
        :maxlength="2000"
        show-word-limit
        resize="none"
        placeholder="例如：我熟悉 Java 和 MySQL，正在寻找杭州的后端实习，应该怎样准备？"
        aria-describedby="chat-availability"
      />
      <div class="composer-footer">
        <p id="chat-availability">每次提问独立回答，请补充必要背景。</p>
        <ElButton
          type="primary"
          native-type="submit"
          :loading="sending"
          :disabled="!question.trim()"
        >{{ sending ? '正在回答' : '发送问题' }}</ElButton>
      </div>
    </form>
  </section>
</div>
</template>
<style scoped>
.chat-scroll
{
  height: clamp(300px, 48vh, 580px);
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  padding-right: 10px
}

.chat-scroll:focus-visible
{
  outline: 2px solid var(--primary);
  outline-offset: 4px
}

.context-note
{
  font-size: 12px;
  color: var(--muted);
  line-height: 1.8;
  margin: 16px 0
}

.pending-message p
{
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.8
}

.thinking
{
  color: var(--primary);
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 10px
}

.message-list article
{
  margin-bottom: 24px
}

.message-list p
{
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.8;
  margin: 10px 0 0
}

.user-message,
.service-message
{
  padding: 18px 20px;
  border-radius: 10px;
  margin-bottom: 12px
}

.user-message
{
  background: #edf3fc
}

.service-message
{
  background: #f5f7fa
}

.message-list strong
{
  font-size: 13px;
  color: var(--primary)
}

.send-error
{
  color: #b42318;
  line-height: 1.8
}

.ai-page
{
  max-width: 1050px;
  margin: 0 auto
}

.ai-heading
{
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  margin-bottom: 28px
}

.ai-caption
{
  font-size: 13px;
  color: var(--primary);
  margin: 0 0 10px
}

.ai-heading h1
{
  font-size: 32px;
  margin: 0 0 12px
}

.ai-heading p:not(.ai-caption)
{
  color: var(--muted);
  line-height: 1.8
}

.back-recommendations
{
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  color: var(--primary)
}

.chat-workspace
{
  background: white;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 36px
}

.chat-empty
{
  text-align: center;
  padding: 12px 0 28px
}

.assistant-mark
{
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  border-radius: 18px;
  background: #edf3fc;
  color: var(--primary)
}

.chat-empty h2
{
  font-size: 24px;
  margin: 18px 0 10px
}

.chat-empty p
{
  color: var(--muted);
  line-height: 1.8
}

.suggestion-grid
{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px
}

.suggestion-grid button
{
  position: relative;
  background: #fafbfd;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 20px 38px 20px 20px;
  text-align: left;
  font: inherit;
  color: var(--ink);
  cursor: pointer
}

.suggestion-grid button:hover
{
  border-color: var(--primary);
  background: #f2f6fc
}

.suggestion-grid button:focus-visible
{
  outline: 2px solid var(--primary);
  outline-offset: 3px
}

.suggestion-grid strong
{
  display: block;
  font-size: 15px;
  margin-bottom: 8px
}

.suggestion-grid span
{
  font-size: 13px;
  color: var(--muted);
  line-height: 1.8
}

.suggestion-grid .el-icon
{
  position: absolute;
  right: 16px;
  top: 24px;
  color: var(--primary)
}

.chat-status
{
  padding: 18px 0;
  margin-top: 16px;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.8
}

.composer
{
  border-top: 1px solid var(--line);
  padding-top: 22px
}

.composer label
{
  display: block;
  font-weight: 600;
  margin-bottom: 12px
}

.composer-footer
{
  display: flex;
  gap: 16px;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px
}

.composer-footer p
{
  font-size: 12px;
  color: var(--muted)
}

@media(max-width:600px)
{
  .ai-heading
  {
    align-items: flex-start;
    flex-direction: column
  }

  .chat-workspace
  {
    padding: 20px 16px
  }

  .suggestion-grid
  {
    grid-template-columns: 1fr
  }

  .composer-footer
  {
    align-items: flex-start;
    flex-direction: column
  }

  .ai-heading h1
  {
    font-size: 28px
  }
}
</style>
