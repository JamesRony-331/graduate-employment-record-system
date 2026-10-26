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
export const noticeType = value => (
{
  1: '系统公告',
  2: '就业政策',
  3: '备案通知',
  4: '其他'
})[value] || '公告'
async function get(path)
{
  const response = await fetch('/api/notice/published/' + path,
  {
    headers:
    {
      Authorization: 'Bearer ' + session.token
    },
    signal: AbortSignal.timeout(15000)
  })
  if (response.status === 401)
    throw new Error('登录已失效，请重新登录')
  if (response.status === 403 || response.status === 404)
    throw new Error('学生公告服务暂未开放')
  if (!response.ok)
    throw new Error('公告加载失败，请稍后重试')
  return unwrapResponse(await response.json())
}
export async function noticePage(page = 1, size = 10)
{
  const data = await get(`page?pageNum=${page}&pageSize=${size}`)
  if (!data || !Array.isArray(data.records) || !Number.isInteger(data.total) || data.records.some(
      r => r.status !== 1))
    throw new Error('公告数据格式异常')
  return data
}
export async function noticeDetail(id)
{
  const data = await get(encodeURIComponent(id))
  if (!data || data.status !== 1)
    throw new Error('该公告已撤回或不可查看')
  return data
}
