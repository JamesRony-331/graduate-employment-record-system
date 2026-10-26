const profileKeys = [
  'uuid',
  'studentNo',
  'name',
  'gender',
  'phone',
  'email',
  'collegeId',
  'college',
  'majorId',
  'major',
  'className',
  'enrollmentYear',
  'graduationYear',
  'educationLevel',
  'address',
]
export function safeProfile(data = {})
{
  return Object.fromEntries(profileKeys.map((key) => [key, data[key] ?? '']))
}
export function unwrapResponse(body)
{
  if (!body || Number(body.code) !== 200)
  {
    const error = new Error(body?.info || '操作未完成，请稍后重试')
    error.code = Number(body?.code)
    throw error
  }
  return body.data
}
export function contactErrors(
{
  phone = '',
  email = ''
}, previous = {})
{
  const errors = {}
  if (previous.phone && !phone.trim())
    errors.phone = '已有手机号码不能清空，请填写新的号码'
  if (previous.email && !email.trim())
    errors.email = '已有邮箱不能清空，请填写新的邮箱'
  if (phone && !/^1[3-9]\d{9}$/.test(phone.trim()))
    errors.phone = '请输入正确的11位手机号码'
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()))

    errors.email = '请输入正确的邮箱地址'
  return errors
}
