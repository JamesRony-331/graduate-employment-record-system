<script setup>
import
{
  computed
}
from 'vue'
import
{
  EditPen
}
from '@element-plus/icons-vue'
import ProfileFrame from '../components/ProfileFrame.vue'
import
{
  session
}
from '../stores/session'
const user = computed(() => session.user ||
{})
const fields = [
  ['姓名', 'name'],
  ['学号', 'studentNo'],
  ['学院', 'college'],
  ['专业', 'major'],
  ['班级', 'className'],
  ['学历层次', 'educationLevel'],
  ['入学年份', 'enrollmentYear'],
  ['毕业年份', 'graduationYear'],
]
</script>
<template>
<ProfileFrame>
  <div class="section-title">
    <div>
      <h2>个人资料</h2>
      <p>你的学籍信息与联系方式</p>
    </div>
    <RouterLink
      to="/profile/edit"
      class="button-link"
    >
      <ElIcon>
        <EditPen />
      </ElIcon>修改个人信息
    </RouterLink>
  </div>
  <section class="detail-section">
    <div class="subheading">
      <h3>学籍信息</h3>
      <span>由学校维护</span>
    </div>
    <dl class="detail-grid">
      <div
        v-for="[label, key] in fields"
        :key="key"
      >
        <dt>{{ label }}</dt>
        <dd>{{ user[key] || '暂未提供' }}</dd>
      </div>
    </dl>
  </section>
  <section class="detail-section">
    <div class="subheading">
      <h3>联系方式</h3>
      <span>仅用于学校联系与就业服务</span>
    </div>
    <dl class="detail-grid">
      <div>
        <dt>手机号码</dt>
        <dd :class="{ muted: !user.phone }">{{ user.phone || '尚未填写' }}</dd>
      </div>
      <div>
        <dt>电子邮箱</dt>
        <dd :class="{ muted: !user.email }">{{ user.email || '尚未填写' }}</dd>
      </div>
    </dl>
  </section>
  <div class="profile-footnote">请保持联系方式准确，以便接收学校通知。</div>
</ProfileFrame>
</template>
