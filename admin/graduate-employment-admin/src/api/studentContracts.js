export const studentFields = [
{
  key: 'studentNo',
  label: '学号',
  required: true,
  max: 32,
  createOnly: true
},
{
  key: 'name',
  label: '姓名',
  required: true,
  max: 50
},
{
  key: 'idCard',
  label: '身份证号',
  required: true,
  max: 18
},
{
  key: 'classId',
  label: '所属班级',
  required: true,
  type: 'class'
},
{
  key: 'enrollmentYear',
  label: '入学年份',
  type: 'number'
},
{
  key: 'graduationYear',
  label: '毕业年份',
  type: 'number'
},
{
  key: 'educationLevel',
  label: '学历层次',
  max: 32
},
{
  key: 'phone',
  label: '联系电话',
  max: 20,
  editOnly: true
},
{
  key: 'email',
  label: '电子邮箱',
  max: 128,
  editOnly: true
},
{
  key: 'address',
  label: '联系地址',
  max: 255,
  editOnly: true
}, ];
export const editableFields = editing => studentFields.filter(f => editing ? !f.createOnly : !f
  .editOnly);
export function studentModel(row = {})
{
  return Object.fromEntries(studentFields.map(f => [f.key, row[f.key] ?? (['number', 'class']
    .includes(f.type) ? null : '')]));
}
export function studentErrors(model, original, classes)
{
  const errors = {};
  for (const f of editableFields(!!original))
  {
    const value = typeof model[f.key] === 'string' ? model[f.key].trim() : model[f.key];
    const empty = value === '' || value == null;
    if (f.required && empty)
      errors[f.key] = `请${f.type === 'class' ? '选择' : '填写'}${f.label}`;
    else if (f.max && value?.length > f.max)
      errors[f.key] = `${f.label}不能超过${f.max}个字符`;
    else if (f.type === 'number' && !empty && (!Number.isInteger(value) || value < 1900 || value >
        9999))
      errors[f.key] = '年份须为1900至9999之间的整数';
    if (original?.[f.key] != null && original[f.key] !== '' && empty)
      errors[f.key] =
      '已有信息暂不支持清空，请填写后保存';
  }
  if (model.idCard && !/^[0-9]{17}[0-9Xx]$/.test(model.idCard.trim()))
    errors.idCard = '身份证号须为18位';
  if (original && model.phone && !/^[0-9+() -]{6,20}$/.test(model.phone.trim()))
    errors.phone =
    '请输入有效联系电话';
  if (original && model.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(model.email.trim()))
    errors
    .email = '请输入有效邮箱';
  if (model.enrollmentYear != null && model.graduationYear != null && model.graduationYear < model
    .enrollmentYear)
    errors.graduationYear = '毕业年份不能早于入学年份';
  const selected = classes.find(c => String(c.id) === String(model.classId));
  if (!selected)
    errors.classId = '请选择管理范围内的班级';
  else if (!selected.majorId || !selected.collegeId)
    errors.classId = '该班级尚未设置学院或专业，请先完善班级资料';
  return errors;
}
export function studentPayload(model, original)
{
  const body = original ?
  {
    uuid: original.uuid
  } :
  {};
  for (const f of editableFields(!!original))
  {
    const value = typeof model[f.key] === 'string' ? model[f.key].trim() : model[f.key];
    if (value === '' || value == null)
      continue;
    if (!original || value !== original[f.key])
      body[f.key] = value;
  }
  return body;
}
export function readStudentPage(data)
{
  if (!data || !Array.isArray(data.records) || data.records.some(r => r?.uuid == null) || !Number
    .isInteger(data.totalCount) || data.totalCount < 0 || !Number.isInteger(data.total) || data
    .total < 0)
    throw new Error('学生列表数据格式异常');
  return data;
}
