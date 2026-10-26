// Only fields accepted by AddEmployDTO are sent to the server.
export const filingSections = [
{
  title: '单位与岗位',
  fields: [
  {
    key: 'companyName',
    label: '单位名称',
    max: 200,
    required: true
  },
  {
    key: 'companyCreditCode',
    label: '统一社会信用代码',
    max: 18
  },
  {
    key: 'jobTitle',
    label: '就业岗位',
    max: 128,
    required: true
  },
  {
    key: 'signedDate',
    label: '签约日期',
    type: 'date'
  },
  {
    key: 'monthlySalary',
    label: '月薪（元）',
    type: 'number'
  }, ],
},
{
  title: '工作地点',
  fields: [
  {
    key: 'workProvince',
    label: '工作省份',
    max: 64,
    required: true
  },
  {
    key: 'workCity',
    label: '工作城市',
    max: 64,
    required: true
  },
  {
    key: 'workAddress',
    label: '详细地址',
    max: 255
  }, ],
},
{
  title: '联系方式',
  fields: [
  {
    key: 'contactPhone',
    label: '联系电话',
    max: 32,
    type: 'tel',
    required: true
  },
  {
    key: 'contactEmail',
    label: '联系邮箱',
    max: 128,
    type: 'email'
  },
  {
    key: 'remark',
    label: '备注',
    max: 1000,
    type: 'textarea'
  }, ],
}, ]
export function filingPayload(model)
{
  return Object.fromEntries(
    filingSections
    .flatMap((s) => s.fields)
    .map((f) =>
    {
      const raw = model[f.key]
      const value = String(raw ?? '').trim()
      return [f.key, f.type === 'number' ? (value === '' ? null : Number(value)) : value ||
        null
      ]
    }),
  )
}
export function filingErrors(model)
{
  const errors = {}
  for (const field of filingSections.flatMap((s) => s.fields))
  {
    const value = String(model[field.key] ?? '').trim()
    if (field.required && !value)
      errors[field.key] = '请填写' + field.label
    else if (field.max && value.length > field.max)

      errors[field.key] = '最多填写' + field.max + '个字符'
  }
  if (model.companyCreditCode && !/^[0-9A-Z]{18}$/.test(model.companyCreditCode.trim()))

    errors.companyCreditCode = '请输入18位大写字母或数字'
  if (model.contactPhone && !/^[+\d][\d\s()-]{5,31}$/.test(model.contactPhone.trim()))

    errors.contactPhone = '请输入有效的联系电话'
  if (model.contactEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(model.contactEmail.trim()))

    errors.contactEmail = '请输入正确的邮箱地址'
  const salary = String(model.monthlySalary ?? '').trim()
  if (salary && (!/^\d+(\.\d{1,2})?$/.test(salary) || Number(salary) > 9999999999.99))

    errors.monthlySalary = '请填写非负金额，最多两位小数'
  return errors
}
export function filingState(status)
{
  return (
  {
    10:
    {
      title: '待辅导员初审',
      active: 1,
      type: 'primary',
      note: '备案已提交，请等待辅导员审核。审核期间不能提交新的申请。',
    },
    20:
    {
      title: '待院系复审',
      active: 2,
      type: 'primary',
      note: '辅导员初审已通过，请等待院系复审。审核期间不能提交新的申请。',
    },
    30:
    {
      title: '已归档',
      active: 4,
      type: 'success',
      note: '备案已完成审核并归档。如需更新就业信息，可创建新的变更申请，原记录将保留。',
    },
    40:
    {
      title: '已打回',
      active: null,
      type: 'warning',
      note: '请联系辅导员了解打回原因。修改重提功能暂未开放。',
    },
  } [status] ||
  {
    title: '状态待确认',
    active: null,
    type: 'info',
    note: '暂时无法识别备案状态，请刷新或联系辅导员。',
  })
}
export function normalizeFiling(value)
{
  if (value === null)
    return null
  if (
    !value ||
    Array.isArray(value) ||
    typeof value !== 'object' ||
    !Object.hasOwn(value, 'status')
  )

    throw new Error('备案数据格式异常，请稍后重试')
  return value
}
