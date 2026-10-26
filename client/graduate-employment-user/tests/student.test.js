import test from 'node:test'
import assert from 'node:assert/strict'
import
{
  unwrapResponse,
  contactErrors,
  safeProfile
}
from '../src/api/contracts.js'

test('已有联系方式不能提交空值，避免与后端忽略空字符串行为不一致', () =>
{
  const errors = contactErrors(
  {
    phone: ' ',
    email: ''
  },
  {
    phone: '13800138000',
    email: 'a@example.edu'
  }, )
  assert.equal(Object.keys(errors).length, 2)
})

test('HTTP 200中的业务失败不能作为保存成功', () =>
{
  assert.throws(() => unwrapResponse(
  {
    code: 600,
    info: '密码错误'
  }), /密码错误/)
  assert.equal(unwrapResponse(
  {
    code: 200,
    data: '修改成功'
  }), '修改成功')
})
test('验证可选联系方式，拒绝不合法号码与邮箱', () =>
{
  assert.deepEqual(contactErrors(
  {
    phone: '',
    email: ''
  }),
  {})
  assert.deepEqual(contactErrors(
  {
    phone: '13800138000',
    email: 'student@example.edu'
  }),
  {})
  assert.equal(Object.keys(contactErrors(
  {
    phone: '123',
    email: 'broken'
  })).length, 2)
})
test('会话资料白名单不包含身份证、密码与额外字段', () =>
{
  const result = safeProfile(
  {
    name: '测试学生',
    studentNo: '20260001',
    idCard: 'secret',
    passwordHash: 'secret',
    role: 'admin',
  })
  assert.equal(result.name, '测试学生')
  assert.equal(result.studentNo, '20260001')
  assert.equal('idCard' in result, false)
  assert.equal('passwordHash' in result, false)
  assert.equal('role' in result, false)
})
