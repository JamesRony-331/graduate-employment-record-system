export function counselorRows(data)
{
  if (!Array.isArray(data))
  {
    throw new Error('查询接口尚未返回辅导员名单，请联系管理员完善后重试。');
  }
  if (data.some(row => !row || row.id == null || typeof row.name !== 'string'))
  {
    throw new Error('辅导员名单格式异常，请联系管理员。');
  }
  return data;
}
export function counselorPayload(model, editing)
{
  const common = {
    gender: model.gender,
    phone: model.phone.trim(),
    email: model.email.trim()
  };
  return editing ?
  {
    id: model.id,
    ...common,
    status: model.status
  } :
  {
    counselorNo: model.counselorNo.trim(),
    name: model.name.trim(),
    ...common,
    hireDate: model.hireDate || null
  };
}
export function counselorErrors(model, editing)
{
  const errors = {};
  if (!editing && !model.counselorNo.trim())
    errors.counselorNo = '请输入工号';
  if (!editing && !model.name.trim())
    errors.name = '请输入姓名';
  if (model.phone.trim().length > 20)
    errors.phone = '联系电话不能超过20个字符';
  if (model.email.trim() && (model.email.trim().length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
      model.email.trim())))
    errors.email = '请输入有效的电子邮箱（最多100个字符）';
  return errors;
}
export function pagePayload(pageNum, pageSize)
{
  return {
    pageNum,
    pageSize,
    start: (pageNum - 1) * pageSize,
    total: 0,
    totalCount: 0
  };
}
