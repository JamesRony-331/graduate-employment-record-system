import
{
  reactive
}
from 'vue'
import
{
  session
}
from './session'
import
{
  managementRequest
}
from '../api/managementRequest'
export const menus = reactive(
{
  records: [],
  token: '',
  loading: false,
  error: ''
})
let pending = null
export function resetMenus()
{
  menus.records = [];
  menus.token = '';
  menus.error = '';
  pending = null
}
export async function loadMenus(force = false)
{
  const token = session.token
  if (!token)
  {
    resetMenus();
    return
  }
  if (!force && menus.token === token)
    return
  if (pending && menus.loading)
    return pending
  menus.loading = true;
  menus.error = '';
  menus.records = []
  pending = (async () =>
  {
    try
    {
      const data = await managementRequest('/menu/mine?clientType=ADMIN', 'GET')
      if (!Array.isArray(data))
        throw new Error('菜单返回格式异常')
      if (session.token === token)
      {
        menus.records = data;
        menus.token = token
      }
    }
    catch (e)
    {
      if (session.token === token)
      {
        menus.error = e.message;
        menus.token = ''
      };
      throw e
    }
    finally
    {
      menus.loading = false;
      pending = null
    }
  })()
  return pending
}
