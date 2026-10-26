import
{
  session,
  clearSession
}
from '../stores/session';
import
{
  unwrap
}
from './contracts';
import
{
  counselorRows,
  pagePayload
}
from './counselorContracts';

async function request(path, method, body)
{
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try
  {
    const response = await fetch('/api/counselor' + path,
    {
      method,
      headers:
      {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + session.token
      },
      ...(body === undefined ?
      {} :
      {
        body: JSON.stringify(body)
      }),
      signal: controller.signal,
    });
    if (response.status === 401 || response.status === 403)
    {
      const error = new Error(response.status === 401 ? '登录已失效，请重新登录' : '当前账号没有操作权限');
      error.code = response.status;
      throw error;
    }
    if (!response.ok)
      throw new Error('服务暂时不可用，请稍后重试');
    return unwrap(await response.json());
  }
  catch (error)
  {
    if ([401, 901].includes(error.code))
    {
      clearSession();
      window.dispatchEvent(new Event('admin-session-expired'));
    }
    if (error.name === 'AbortError')
      throw new Error('请求超时，请刷新名单核实结果后再操作');
    if (error instanceof TypeError)
      throw new Error('无法连接服务，请确认后端已启动');
    if (error instanceof SyntaxError)
      throw new Error('服务返回格式异常，请稍后重试');
    throw error;
  }
  finally
  {
    clearTimeout(timeout);
  }
}
export const listCounselors = async (page, size) => counselorRows(await request(
  '/SelectCounselorByPage', 'POST', pagePayload(page, size)));
export const addCounselor = body => request('/add-counselor', 'POST', body);
export const updateCounselor = body => request('/UpdateCounselot', 'PUT', body);
export const removeCounselor = id => request('/DeleteCounselor/' + encodeURIComponent(id),
  'DELETE');
