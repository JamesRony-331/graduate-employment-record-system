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
async function get(path, signal)
{
  const response = await fetch('/api/work-compare/' + path,
  {
    headers:
    {
      Authorization: `Bearer ${session.token}`
    },
    signal
  })
  if (response.status === 401)
    throw new Error('登录已失效，请重新登录')
  if (response.status === 403)
    throw new Error('暂时无权访问工作信息，请联系管理员')
  if (!response.ok)
    throw new Error('工作信息加载失败，请稍后重试')
  return unwrapResponse(await response.json())
}
export async function jobsPage(page, size, signal)
{
  const data = await get(`page?pageNum=${page}&pageSize=${size}`, signal)
  if (!data || !Array.isArray(data.records) || !Number.isInteger(data.totalCount) || !Number
    .isInteger(data.total) || data.records.some(r => !r || r.id == null))
    throw new Error(
      '工作列表格式异常')
  return data
}
export const jobDetail = (id, signal) => get(encodeURIComponent(id), signal)
