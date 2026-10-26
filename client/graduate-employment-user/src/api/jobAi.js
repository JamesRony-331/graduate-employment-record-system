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

export async function sendJobPrompt(prompt, signal)
{
  if (!session.token || session.demo)
    throw new Error('请使用学生账号登录后发送问题')
  const response = await fetch('/api/job-ai/prompt',
  {
    method: 'POST',
    headers:
    {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${session.token}`
    },
    body: JSON.stringify(
    {
      prompt
    }),
    signal,
  })
  if (response.status === 401)
    throw new Error('登录已失效，请重新登录')
  if (response.status === 403)
    throw new Error('当前账号无权使用，请使用学生账号')
  if (!response.ok)
    throw new Error('发送失败，请确认后端服务已启动')
  const data = unwrapResponse(await response.json())
  if (typeof data !== 'string' || !data.trim())
    throw new Error('服务返回内容为空或格式异常')
  return data
}
