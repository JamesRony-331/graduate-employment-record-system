import
{
  session,
  clearSession
}
from '../stores/session'
import
{
  unwrapResponse
}
from './contracts'
async function request(path, body, authenticated = false, method = 'POST')
{
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 15000)
  try
  {
    const response = await fetch('/api' + path,
    {
      method,
      headers:
      {
        'Content-Type': 'application/json',
        ...(authenticated && session.token ?
        {
          Authorization: 'Bearer ' + session.token
        } :
        {}),
      },
      body: method === 'GET' ? undefined : JSON.stringify(body),
      signal: controller.signal,
    })
    if (response.status === 401 || response.status === 403)
    {
      const error = new Error('登录已失效，请重新登录')
      error.code = response.status
      throw error
    }
    if (!response.ok)
      throw new Error('服务暂时不可用，请稍后重试')
    return unwrapResponse(await response.json())
  }
  catch (error)
  {
    if (authenticated && [401, 403, 901].includes(error.code))
    {
      clearSession()
      window.dispatchEvent(new Event('student-session-expired'))
    }
    if (error.name === 'AbortError')
      throw new Error('请求超时，请检查网络后重试')
    if (error instanceof TypeError || error instanceof SyntaxError)

      throw new Error('无法连接服务，请确认后端已启动后重试')
    throw error
  }
  finally
  {
    clearTimeout(timer)
  }
}
export const login = (values) =>
  request('/sys-user/login',
  {
    username: values.studentNo,
    password: values.password,
    accountType: 'STUDENT',
  })
export const getLastFiling = () =>
  request('/employment-filing/getLastFiling', undefined, true, 'GET')
export const getFilings = () => request('/employment-filing/getFilings', undefined, true, 'GET')
export const addEmploymentFiling = (values) =>
  request('/employment-filing/AddEmploymentFiling', values, true)
export const saveContact = (values) =>
  request('/sys-user/updateMsg',
  {
    phone: values.phone.trim(),
    email: values.email.trim()
  }, true)
