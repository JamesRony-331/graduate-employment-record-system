import test from 'node:test'
import assert from 'node:assert/strict'
import
{
  filingPayload,
  filingErrors,
  filingState,
  normalizeFiling
}
from '../src/api/filing.js'
test('payload contains only accepted fields and preserves optional numeric null', () =>
{
  const data = filingPayload(
  {
    companyName: ' 单位 ',
    monthlySalary: '',
    studentUuid: 999,
    status: 30,
  })
  assert.equal(data.companyName, '单位')
  assert.equal(data.monthlySalary, null)
  assert.equal(Object.hasOwn(data, 'studentUuid'), false)
  assert.equal(Object.hasOwn(data, 'status'), false)
  assert.equal(filingPayload(
  {
    monthlySalary: '0'
  }).monthlySalary, 0)
})
test('validation catches required fields, invalid amounts and contacts', () =>
{
  const valid = {
    companyName: '单位',
    jobTitle: '开发',
    workProvince: '浙江',
    workCity: '温州',
    contactPhone: '13800138000',
  }
  assert.deepEqual(filingErrors(valid),
  {})
  for (const monthlySalary of ['-1', 'NaN', '12.345', '10000000000'])

    assert.ok(filingErrors(
    {
      ...valid,
      monthlySalary
    }).monthlySalary)
  assert.ok(filingErrors(
  {
    ...valid,
    contactEmail: 'bad'
  }).contactEmail)
  assert.ok(filingErrors(
  {}).companyName)
})
test('returned and unknown status never imply successful review', () =>
{
  assert.equal(filingState(10).active, 1)
  assert.equal(filingState(20).active, 2)
  assert.equal(filingState(30).active, 4)
  assert.equal(filingState(40).active, null)
  assert.equal(filingState(99).title, '状态待确认')
})
test('only explicit null means no filing', () =>
{
  assert.equal(normalizeFiling(null), null)
  for (const value of [undefined,
      {},
      [], '', false
    ])
    assert.throws(() => normalizeFiling(value))
  assert.equal(normalizeFiling(
  {
    status: 10
  }).status, 10)
})
