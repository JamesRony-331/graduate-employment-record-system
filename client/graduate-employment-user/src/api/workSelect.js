import
{
  session
}
from '../stores/session'
import
{
  unwrapResponse
}
from './contracts'

export function workSelectPayload(form, strict)
{
  const payload = Object.fromEntries(Object.entries(form).map(([key, value]) => [
    key,
    Array.isArray(value) ? [...new Set(value.map(item => item.trim()).filter(Boolean))] :
    typeof value === 'string' ? value.trim() || null : value ?? null,
  ]))
  payload.strict = Object.fromEntries(Object.keys(strict).map(key =>
  {
    const value = payload[key]
    const specific = Array.isArray(value) ? value.length > 0 && !value.includes('不限') :
      value != null && value !== '不限'
    return [key, specific && strict[key] === true]
  }))
  return payload
}

export async function submitWorkSelect(payload)
{
  if (session.demo || !session.token)
    throw new Error('请使用学生账号登录后提交，演示模式不能提交')
  let response
  try
  {
    response = await fetch('/api/work-select',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${session.token}`
      },
      body: JSON.stringify(payload),
      signal: AbortSignal.timeout(15000),
    })
  }
  catch
  {
    throw new Error('未收到服务器响应，请检查网络或稍后重试')
  }
  if (response.status === 401)
    throw new Error('登录已失效，请重新登录')
  if (response.status === 403)
    throw new Error('当前账号无权提交，请使用学生账号')
  if (!response.ok)
    throw new Error('提交失败，请确认后端服务已启动并稍后重试')
  const data = unwrapResponse(await response.json())
  if (!Array.isArray(data) || data.some(row => !row || row.id == null || typeof row.similarity !==
      'number' || !Number.isFinite(row.similarity) || row.similarity < 0 || row.similarity > 100))
  {
    throw new Error('匹配结果格式异常，请确认后端已更新')
  }
  return data
}

export async function skillOptions(keyword = '')
{
  const response = await fetch('/api/work-skill/options?keyword=' + encodeURIComponent(keyword),
  {
    headers:
    {
      Authorization: `Bearer ${session.token}`
    },
    signal: AbortSignal.timeout(15000),
  })
  if (!response.ok)
    throw new Error('技能加载失败，请确认登录状态及后端服务')
  const data = unwrapResponse(await response.json())
  if (!Array.isArray(data))
    throw new Error('技能数据格式异常')
  return data
}
