import
{
  managementRequest
}
from './managementRequest';
import
{
  readPage
}
from './organizationContracts';
import
{
  accountPage
}
from './accountManagementContracts';
export async function listAccounts(page, size)
{
  return accountPage(readPage(await managementRequest(
    `/sys-user/page?pageNum=${page}&pageSize=${size}`, 'GET')));
}
