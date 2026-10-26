import
{
  managementRequest as request
}
from './managementRequest';
import
{
  readPage
}
from './organizationContracts';
export const listOrganizations = async (kind, page = 1, size = 10) => readPage(await request(
  `/${kind}/page?pageNum=${page}&pageSize=${size}`, 'GET'));
export async function organizationDetail(kind, id)
{
  const data = await request(`/${kind}/${encodeURIComponent(id)}`, 'GET');
  if (!data || data.id == null)
    throw new Error('详情数据格式异常，请重试');
  return data;
}
export const saveOrganization = (kind, body, editing) => request(
  `/${kind}/${editing ? 'update' : 'add'}`, editing ? 'PUT' : 'POST', body);
export const deleteOrganization = (kind, id) => request(`/${kind}/${encodeURIComponent(id)}`,
  'DELETE');
export const bindCollege = (classId, collegeId) => request('/class/bind-college', 'PUT',
{
  classId,
  collegeId
});
export async function collegeOptions()
{
  const result = [];
  let page = 1;
  while (true)
  {
    const data = await listOrganizations('college', page, 100);
    result.push(...data.records);
    if (page >= data.total)
      return result;
    if (!data.records.length)
      throw new Error('学院列表发生变化，请重新加载');
    page++;
  }
}

export const majorOptions = () => request('/major/options', 'GET');
