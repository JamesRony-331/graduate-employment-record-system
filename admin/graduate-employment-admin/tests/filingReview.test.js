import test from 'node:test';
import assert from 'node:assert/strict';
import
{
  readFilings,
  filingStatus,
  parseFilingJson,
  filterFilings
}
from '../src/api/filingReviewContracts.js';
test('review list rejects malformed response instead of claiming empty', () =>
{
  assert.deepEqual(readFilings([]), []);
  for (const v of [null, '成功',
      {},
      [
      {
        status: 10
      }],
      [
      {
        id: 1
      }]
    ])
    assert.throws(() => readFilings(v));
});
test('statuses distinguish returned from archived and unknown', () =>
{
  assert.equal(filingStatus(40), '已打回');
  assert.equal(filingStatus(30), '已归档');
  assert.match(filingStatus(0), /未知/);
});
test('JSON strings and malformed materials are handled safely', () =>
{
  assert.deepEqual(parseFilingJson('[{"name":"证明.pdf"}]', true).value, [
  {
    name: '证明.pdf'
  }]);
  assert.equal(parseFilingJson('{}', true).error, true);
  assert.equal(parseFilingJson('bad').error, true);
  assert.equal(parseFilingJson('null').error, true);
  assert.deepEqual(parseFilingJson(null, true).value, []);
});
test('filters combine class and search without collapsing repeated student submissions', () =>
{
  const rows = [
  {
    id: 1,
    classId: 1,
    studentName: '张三'
  },
  {
    id: 2,
    classId: 1,
    studentName: '张三'
  },
  {
    id: 3,
    classId: 2,
    studentName: '李四'
  }];
  assert.equal(filterFilings(rows, '张三', 1).length, 2);
  assert.equal(filterFilings(rows, '张三', 2).length, 0);
});
import
{
  reviewPayload
}
from '../src/api/filingReviewContracts.js';
test('review sends original version and status, requires return reason', () =>
{
  const row = {
    id: 3,
    version: 0,
    status: 10
  };
  assert.deepEqual(reviewPayload(row, 1, ' 通过 '),
  {
    filingId: 3,
    version: 0,
    fromStatus: 10,
    auditResult: 1,
    auditOpinion: '通过'
  });
  assert.throws(() => reviewPayload(row, 2, ' '));
  assert.equal(reviewPayload(row, 2, '补充材料').auditResult, 2);
  assert.throws(() => reviewPayload(
  {
    ...row,
    status: 20
  }, 2, '打回'));
  assert.equal(reviewPayload(
  {
    ...row,
    status: 20
  }, 1).fromStatus, 20);
  assert.throws(() => reviewPayload(
  {
    ...row,
    status: 30
  }, 1));
  assert.throws(() => reviewPayload(
  {
    ...row,
    version: null
  }, 1));
  assert.throws(() => reviewPayload(row, 1, '字'.repeat(1001)));
});
