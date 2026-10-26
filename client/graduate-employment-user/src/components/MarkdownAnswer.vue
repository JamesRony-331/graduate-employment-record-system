<script setup>
import
{
  computed
}
from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
const props = defineProps(
{
  content:
  {
    type: String,
    default: ''
  }
})
const markdown = new MarkdownIt(
{
  html: false,
  breaks: true,
  linkify: false
})
// Do not load external images or render model-provided HTML.
markdown.disable('image')
const rendered = computed(() => DOMPurify.sanitize(markdown.render(props.content),
{
  USE_PROFILES:
  {
    html: true
  },
  FORBID_TAGS: ['img', 'iframe', 'style', 'form', 'input'],
}))
</script>
<template>
<div
  class="markdown-answer"
  v-html="rendered"
></div>
</template>
<style scoped>
.markdown-answer
{
  line-height: 1.85;
  overflow-wrap: anywhere;
  white-space: normal
}

.markdown-answer :deep(p)
{
  margin: 12px 0;
  white-space: normal
}

.markdown-answer :deep(h1),
.markdown-answer :deep(h2),
.markdown-answer :deep(h3)
{
  margin: 20px 0 10px;
  line-height: 1.5
}

.markdown-answer :deep(h1)
{
  font-size: 23px
}

.markdown-answer :deep(h2)
{
  font-size: 21px
}

.markdown-answer :deep(h3)
{
  font-size: 18px
}

.markdown-answer :deep(ul),
.markdown-answer :deep(ol)
{
  padding-left: 24px;
  margin: 12px 0
}

.markdown-answer :deep(li)
{
  margin: 6px 0
}

.markdown-answer :deep(strong)
{
  font-size: inherit;
  color: inherit
}

.markdown-answer :deep(pre)
{
  overflow-x: auto;
  background: #e9edf3;
  padding: 14px;
  border-radius: 6px;
  white-space: pre
}

.markdown-answer :deep(code)
{
  font-family: monospace
}

.markdown-answer :deep(table)
{
  display: block;
  max-width: 100%;
  overflow-x: auto;
  border-collapse: collapse
}

.markdown-answer :deep(th),
.markdown-answer :deep(td)
{
  border: 1px solid #ccd5e2;
  padding: 8px 12px
}

.markdown-answer :deep(blockquote)
{
  border-left: 3px solid #819bbd;
  padding-left: 14px;
  margin-left: 0;
  color: #53657e
}

.markdown-answer :deep(a)
{
  color: #28518c;
  text-decoration: underline
}
</style>
