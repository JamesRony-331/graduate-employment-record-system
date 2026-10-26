import
{
  reactive
}
from "vue";
// Isolated UI examples. Never sent to the backend or persisted in browser storage.
export const demoCounselors = [
{
  id: "demo-c1",
  name: "陈老师",
  college: "计算机与信息工程学院",
  staffNo: "示例-001",
  phone: "未填写",
},
{
  id: "demo-c2",
  name: "林老师",
  college: "经济管理学院",
  staffNo: "示例-002",
  phone: "未填写",
},
{
  id: "demo-c3",
  name: "周老师",
  college: "计算机与信息工程学院",
  staffNo: "示例-003",
  phone: "未填写",
}, ];
export const demoAccounts = reactive([
{
  id: "demo-u1",
  username: "demo_teacher01",
  name: "示例教师一",
  status: 1,
  counselorId: "demo-c1",
},
{
  id: "demo-u2",
  username: "demo_teacher02",
  name: "示例教师二",
  status: 1,
  counselorId: null,
},
{
  id: "demo-u3",
  username: "demo_admin01",
  name: "示例管理员",
  status: 1,
  counselorId: null,
}, ]);
export const counselorFor = (id) =>
  demoCounselors.find((item) => item.id === id);
export const accountFor = (id) =>
  demoAccounts.find((item) => item.counselorId === id);
export function bindDemoCounselor(accountId, counselorId)
{
  const account = demoAccounts.find((item) => item.id === accountId);
  if (!account)
    throw new Error("示例账号不存在");
  if (counselorId && !counselorFor(counselorId))

    throw new Error("请选择有效的辅导员");
  const bound = counselorId && accountFor(counselorId);
  if (bound && bound.id !== accountId)

    throw new Error("该辅导员已绑定其他账号");
  account.counselorId = counselorId || null;
}
