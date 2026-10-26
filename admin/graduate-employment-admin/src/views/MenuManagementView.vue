<script setup>
import
{
  ref,
  reactive,
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
  ElCheckbox,
  ElCheckboxGroup,
  ElMessage,
  ElMessageBox
}
from 'element-plus'
import
{
  managementRequest as request
}
from '../api/managementRequest'
const rows = ref([]),
  roles = ref([]),
  error = ref(''),
  loading = ref(false),
  opened = ref(false),
  saving = ref(false),
  id = ref(null),
  role = ref(null),
  grants = ref([]),
  grantOpen = ref(false)
const initial = () => (
{
  name: '',
  path: '',
  permission: '',
  icon: 'Collection',
  parentId: 0,
  menuType: 'MENU',
  clientType: 'ADMIN',
  status: 1,
  sortOrder: 0
})
const form = reactive(initial()),
  formRef = ref()
async function load()
{
  loading.value = true;
  error.value = '';
  try
  {
    [rows.value, roles.value] = await Promise.all([request('/menu/all', 'GET'), request(
      '/menu/roles', 'GET')])
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

function edit(row)
{
  id.value = row?.id;
  Object.assign(form, initial(), row ||
  {});
  opened.value = true
}
async function save()
{
  if (saving.value || !await formRef.value.validate().catch(() => false))
    return;
  saving.value = true;
  try
  {
    await request('/menu' + (id.value ? '/' + id.value : ''), id.value ? 'PUT' : 'POST', form);
    opened.value = false;
    await load();
    window.dispatchEvent(new Event('menus-updated'));
    ElMessage.success('保存成功')
  }
  catch (e)
  {
    ElMessage.error(e.message)
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
    await ElMessageBox.confirm('删除此菜单及其角色授权？', '删除菜单')
  }
  catch
  {
    return
  }
  try
  {
    await request('/menu/' + row.id, 'DELETE');
    await load();
    window.dispatchEvent(new Event('menus-updated'))
  }
  catch (e)
  {
    ElMessage.error(e.message)
  }
}
async function openGrant()
{
  if (!role.value)
    return;
  try
  {
    grants.value = await request('/menu/role/' + role.value, 'GET');
    grantOpen.value = true
  }
  catch (e)
  {
    ElMessage.error(e.message)
  }
}
async function saveGrant()
{
  saving.value = true;
  try
  {
    await request('/menu/role/' + role.value, 'PUT', grants.value);
    grantOpen.value = false;
    window.dispatchEvent(new Event('menus-updated'));
    ElMessage.success('授权成功')
  }
  catch (e)
  {
    ElMessage.error(e.message)
  }
  finally
  {
    saving.value = false
  }
}
onMounted(load)
</script>
<template>
<div>
  <h1 tabindex="-1">菜单管理</h1>
  <p>维护菜单入口，并为角色配置可见菜单。</p>
  <ElAlert
    v-if="error"
    :title="error"
    type="error"
    :closable="false"
  />
  <div class="menu-toolbar">
    <ElButton
      type="primary"
      @click="edit()"
    >新增菜单</ElButton>
    <ElButton
      :loading="loading"
      @click="load"
    >刷新</ElButton>
    <ElSelect
      v-model="role"
      placeholder="选择角色"
      aria-label="授权角色"
    >
      <ElOption
        v-for="r in roles"
        :key="r.id"
        :label="r.roleName"
        :value="r.id"
      />
    </ElSelect>
    <ElButton
      :disabled="!role"
      @click="openGrant"
    >配置角色权限</ElButton>
  </div>
  <ElTable :data="rows">
    <ElTableColumn
      prop="name"
      label="菜单"
    />
    <ElTableColumn
      prop="clientType"
      label="所属端"
    />
    <ElTableColumn
      prop="path"
      label="路由"
    />
    <ElTableColumn
      prop="permission"
      label="权限标识"
      min-width="200"
    />
    <ElTableColumn
      prop="menuType"
      label="类型"
    />
    <ElTableColumn label="状态"><template #default="{row}">{{row.status===1?'启用':'禁用'}}</template>
    </ElTableColumn>
    <ElTableColumn
      label="操作"
      width="160"
    ><template #default="{row}">
        <ElButton
          link
          @click="edit(row)"
        >编辑</ElButton>
        <ElButton
          link
          type="danger"
          @click="remove(row)"
        >删除</ElButton>
      </template></ElTableColumn>
  </ElTable>
  <ElDialog
    v-model="opened"
    title="菜单资料"
    width="600px"
    class="menu-editor"
    :close-on-click-modal="false"
  >
    <ElForm
      ref="formRef"
      :model="form"
      label-position="top"
    >
      <ElFormItem
        v-for="[key,label] in [['name','菜单名称'],['path','页面路由'],['permission','权限标识'],['icon','图标名称']]"
        :key="key"
        :label="label"
        :prop="key"
        :rules="['name','permission'].includes(key)?[{required:true,message:'请填写'+label,whitespace:true}]:[]"
      >
        <ElInput v-model="form[key]" />
      </ElFormItem>
      <ElFormItem label="所属端">
        <ElSelect v-model="form.clientType">
          <ElOption
            label="管理端"
            value="ADMIN"
          />
          <ElOption
            label="学生端"
            value="STUDENT"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="类型">
        <ElSelect v-model="form.menuType">
          <ElOption
            label="菜单"
            value="MENU"
          />
          <ElOption
            label="目录"
            value="DIRECTORY"
          />
          <ElOption
            label="按钮权限"
            value="BUTTON"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="父菜单">
        <ElSelect v-model="form.parentId">
          <ElOption
            label="顶级"
            :value="0"
          />
          <ElOption
            v-for="m in rows.filter(m=>m.id!==id&&m.clientType===form.clientType&&m.menuType!=='BUTTON')"
            :key="m.id"
            :label="m.name"
            :value="m.id"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="排序">
        <ElInputNumber
          v-model="form.sortOrder"
          :min="0"
        />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElSelect v-model="form.status">
          <ElOption
            label="启用"
            :value="1"
          />
          <ElOption
            label="禁用"
            :value="0"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm><template #footer>
      <ElButton
        :loading="saving"
        type="primary"
        @click="save"
      >保存</ElButton>
    </template>
  </ElDialog>
  <ElDialog
    v-model="grantOpen"
    title="角色菜单授权"
    width="700px"
    class="menu-editor"
  >
    <ElCheckboxGroup
      v-model="grants"
      class="grant-list"
    >
      <ElCheckbox
        v-for="m in rows"
        :key="m.id"
        :value="m.id"
      >{{m.clientType==='ADMIN'?'管理端':'学生端'}} · {{m.name}}（{{m.menuType}}）</ElCheckbox>
    </ElCheckboxGroup><template #footer>
      <ElButton
        :loading="saving"
        type="primary"
        @click="saveGrant"
      >保存授权</ElButton>
    </template>
  </ElDialog>
</div>
</template>
<style scoped>
.menu-toolbar
{
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin: 24px 0
}

.menu-toolbar .el-select
{
  width: 200px
}

.grant-list
{
  display: flex;
  flex-direction: column;
  align-items: flex-start
}

:global(.menu-editor)
{
  max-width: calc(100vw - 32px)
}

h1
{
  margin-bottom: 12px
}

p
{
  color: var(--muted)
}
</style>
