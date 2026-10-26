import
{
  managementRequest as request
}
from './managementRequest';
import
{
  readStudentPage
}
from './studentContracts';
export async function studentPage(pageNum, pageSize, collegeId, classId)
{
  const params = new URLSearchParams(
  {
    pageNum,
    pageSize
  });
  if (collegeId != null)
    params.set('collegeId', collegeId);
  if (classId != null)
    params.set('classId', classId);
  return readStudentPage(await request('/student/page?' + params, 'GET'));
}
export async function managedClasses()
{
  const data = await request('/class/managed', 'GET');
  if (!Array.isArray(data) || data.some(c => c?.id == null))
    throw new Error('管理班级数据格式异常');
  return data;
}
export async function studentDetail(uuid)
{
  const data = await request('/student/' + encodeURIComponent(uuid), 'GET');
  if (data?.uuid == null)
    throw new Error('学生详情数据格式异常');
  return data;
}
export const saveStudent = (body, editing) => request('/student/' + (editing ? 'update' : 'add'),
  editing ? 'PUT' : 'POST', body);
