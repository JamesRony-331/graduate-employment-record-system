import test from 'node:test';
import assert from 'node:assert/strict';
import
{
  accountPage,
  filterAccounts
}
from '../src/api/accountManagementContracts.js';
const rows = [
{
  id: 1,
  username: 'student',
  realName: '张同学',
  accountType: 'STUDENT',
  studentUuid: 1,
  status: 1,
  counselorId: null
},
{
  id: 2,
  username: 'teacher',
  realName: '王老师',
  accountType: 'STAFF',
  status: 0,
  counselorId: 3
},
{
  id: 3,
  username: 'staff',
  accountType: 'STAFF',
  status: 1,
  counselorId: null
}, ];
test('account list drops credentials and preserves page metadata', () =>
{
  const result = accountPage(
  {
    records: [
    {
      ...rows[0],
      password: 'hash',
      passwordAlgorithm: 'BCRYPT'
    }],
    totalCount: 22,
    total: 3
  });
  assert.equal(result.totalCount, 22);
  assert.equal('password' in result.records[0], false);
  assert.equal('passwordAlgorithm' in result.records[0], false);
  assert.throws(() => accountPage(
  {
    records: [
    {
      id: 1
    }]
  }));
});
test('account filters distinguish disabled and unbound staff from students', () =>
{
  assert.deepEqual(filterAccounts(rows, '', 'all', 0, 'all').map(r => r.id), [2]);
  assert.deepEqual(filterAccounts(rows, '', 'all', 'all', 'unbound').map(r => r.id), [3]);
  assert.deepEqual(filterAccounts(rows, '张', 'STUDENT', 'all', 'all').map(r => r.id), [1]);
  assert.deepEqual(filterAccounts(rows, '', 'STUDENT', 'all', 'bound'), []);
});

test('role display uses returned RBAC data without inferring from personnel IDs', async () =>
{
  const
  {
    roleText
  } = await import('../src/api/accountManagementContracts.js');
  assert.equal(roleText(
  {
    counselorId: 1
  }), '角色信息未返回');
  assert.equal(roleText(
  {
    roles: []
  }), '未分配角色');
  assert.equal(roleText(
  {
    roles: [
    {
      roleCode: 'DEPARTMENT_HEAD',
      roleName: '院系负责人'
    }]
  }), '院系负责人');
});

test('disabled RBAC roles are labeled separately from active roles', async () =>
{
  const
  {
    roleText
  } = await import('../src/api/accountManagementContracts.js');
  assert.equal(roleText(
  {
    roles: [
    {
      roleName: '辅导员',
      status: 1
    },
    {
      roleName: '管理员',
      status: 0
    }]
  }), '辅导员、管理员（已停用）');
});
