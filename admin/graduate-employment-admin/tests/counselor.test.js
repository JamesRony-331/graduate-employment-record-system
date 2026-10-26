import test from 'node:test';
import assert from 'node:assert/strict';
import
{
  counselorRows,
  counselorPayload,
  counselorErrors,
  pagePayload
}
from '../src/api/counselorContracts.js';
const model = {
  id: 7,
  counselorNo: ' T001 ',
  name: ' 张老师 ',
  gender: 1,
  phone: ' 123 ',
  email: '',
  hireDate: '',
  status: 1
};
test('query success message must not be mistaken for an empty list', () =>
{
  assert.throws(() => counselorRows('查询辅导员成功'), /尚未返回/);
  assert.throws(() => counselorRows(null));
  assert.deepEqual(counselorRows([]), []);
  assert.deepEqual(counselorRows([
  {
    id: 7,
    name: '张老师'
  }]), [
  {
    id: 7,
    name: '张老师'
  }]);
});
test('update only sends fields supported by update DTO', () =>
{
  assert.deepEqual(counselorPayload(model, true),
  {
    id: 7,
    gender: 1,
    phone: '123',
    email: '',
    status: 1
  });
  assert.deepEqual(counselorPayload(model, false),
  {
    counselorNo: 'T001',
    name: '张老师',
    gender: 1,
    phone: '123',
    email: '',
    hireDate: null
  });
});
test('validation catches required data and invalid contact fields', () =>
{
  assert.deepEqual(counselorErrors(model, false),
  {});
  assert.deepEqual(Object.keys(counselorErrors(
  {
    ...model,
    counselorNo: '',
    name: ' ',
    phone: '1'.repeat(21),
    email: 'invalid'
  }, false)), ['counselorNo', 'name', 'phone', 'email']);
});
test('page offset matches backend contract', () =>
{
  assert.deepEqual(pagePayload(3, 10),
  {
    pageNum: 3,
    pageSize: 10,
    start: 20,
    total: 0,
    totalCount: 0
  });
});
