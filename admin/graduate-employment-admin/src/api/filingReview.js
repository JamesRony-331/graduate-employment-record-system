import
{
  managementRequest
}
from './managementRequest';
import
{
  readFilings
}
from './filingReviewContracts';
import
{
  session
}
from '../stores/session';
export async function reviewFilings(kind)
{
  if (!['pending-review', 'archived'].includes(kind))
    throw new Error('无效的备案列表类型');
  if (session.user?.id == null)
    throw new Error('登录信息缺少账号 ID，请重新登录');
  return readFilings(await managementRequest(
    `/employment-filing/${kind}/${encodeURIComponent(session.user.id)}`, 'GET'));
}
export const submitReview = body => managementRequest(
  `/employment-filing/review/${body.fromStatus === 10 ? 'counselor' : 'department'}`, 'PUT', body);
