import test from 'node:test';
import assert from 'node:assert/strict';
import
{
  studentModel,
  studentErrors,
  studentPayload,
  readStudentPage
}
from '../src/api/studentContracts.js';
const classes = [
{
  id: 1,
  collegeId: 2,
  majorId: 3
}];
const valid = () => studentModel(
{
  studentNo: 'S01',
  name: '李同学',
  idCard: '330302200001011234',
  classId: 1
});
test('create sends only accepted fields, excludes derived identities and contacts', () =>
{
  const m = {
    ...valid(),
    collegeId: 999,
    majorId: 999,
    phone: '13800000000'
  };
  assert.deepEqual(studentErrors(m, null, classes),
  {});
  assert.deepEqual(studentPayload(m),
  {
    studentNo: 'S01',
    name: '李同学',
    idCard: '330302200001011234',
    classId: 1
  });
});
test('rejects inaccessible classes and backwards years', () =>
{
  const e = studentErrors(
  {
    ...valid(),
    classId: 9,
    enrollmentYear: 2026,
    graduationYear: 2020,
    password: '中'.repeat(25)
  }, null, classes);
  assert.ok(e.classId);
  assert.ok(e.graduationYear);
});
test('edit sends changed fields without account/password or silent clears', () =>
{
  const original = {
    ...valid(),
    uuid: 5,
    phone: '13800000000'
  };
  const m = {
    ...studentModel(original),
    name: '新姓名',
    password: 'ignored',
    studentNo: 'changed'
  };
  assert.deepEqual(studentPayload(m, original),
  {
    uuid: 5,
    name: '新姓名'
  });
  assert.ok(studentErrors(
  {
    ...m,
    phone: ''
  }, original, classes).phone);
});
test('student pages use uuid and reject malformed responses', () =>
{
  assert.equal(readStudentPage(
  {
    records: [
    {
      uuid: 1
    }],
    totalCount: 1,
    total: 1
  }).records.length, 1);
  assert.throws(() => readStudentPage(
  {
    records: [
    {
      id: 1
    }],
    totalCount: 1,
    total: 1
  }));
});

test('creation never sends a client-supplied password', () =>
{
  assert.equal('password' in studentModel(), false);
  assert.equal('password' in studentPayload(
  {
    ...valid(),
    password: 'override'
  }), false);
});
