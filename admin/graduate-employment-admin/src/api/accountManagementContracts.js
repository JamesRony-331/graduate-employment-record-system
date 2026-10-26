const fields = ['id', 'username', 'realName', 'accountType', 'status', 'studentUuid', 'counselorId',
  'createdAt', 'updatedAt', 'roles'
];
export function accountPage(page)
{
  if (page.records.some(row => typeof row.username !== 'string' || !row.username))
    throw new Error(
      '账号列表格式异常，请重试');
  return {
    ...page,
    records: page.records.map(row => Object.fromEntries(fields.map(key => [key, row[key] ?? null])))
  };
}
export function filterAccounts(rows, keyword, type, status, binding)
{
  const term = keyword.trim().toLowerCase();
  return rows.filter(row => `${row.username} ${row.realName || ''}`.toLowerCase().includes(term) &&
    (type === 'all' || row.accountType === type) &&
    (status === 'all' || row.status === status) &&
    (binding === 'all' || (row.accountType === 'STAFF' && (binding === 'bound' ? row
      .counselorId != null : row.counselorId == null))));
}

export function roleText(account)
{
  if (!Array.isArray(account.roles))
    return '角色信息未返回';
  if (!account.roles.length)
    return '未分配角色';
  return account.roles.map(role =>
    `${role.roleName || role.roleCode || '未知角色'}${role.status === 0 ? '（已停用）' : ''}`).join('、');
}
