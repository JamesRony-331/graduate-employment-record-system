import test from 'node:test';
import assert from 'node:assert/strict';
import
{
  readPage,
  createModel,
  formErrors,
  organizationPayload
}
from '../src/api/organizationContracts.js';
test('pagination reads total count separately from total pages and rejects non-list success', () =>
{
  assert.throws(() => readPage('成功'));
  const page = {
    records: [
    {
      id: 1
    }],
    totalCount: 21,
    total: 3
  };
  assert.equal(readPage(page).total, 3);
});
test('college payload trims input and excludes unrelated fields', () =>
{
  const model = createModel('college',
  {
    collegeCode: ' C01 ',
    collegeName: ' 学院 ',
    deanName: ' 李 ',
    password: 'ignored'
  });
  assert.deepEqual(organizationPayload('college', model, 1),
  {
    collegeCode: 'C01',
    collegeName: '学院',
    deanName: '李',
    phone: '',
    description: '',
    id: 1
  });
  assert.deepEqual(formErrors('college', model),
  {});
  assert.ok(formErrors('college',
  {
    ...model,
    collegeCode: ' ',
    description: '字'.repeat(501)
  }).description);
});
test('class validates required college and integer limits', () =>
{
  const model = createModel('class',
  {
    classCode: 'C1',
    className: '计算机一班',
    collegeId: 1,
    majorId: 1,
    enrollmentYear: 2026
  });
  assert.deepEqual(formErrors('class', model),
  {});
  const bad = formErrors('class',
  {
    ...model,
    collegeId: null,
    enrollmentYear: 1899,
    studentCount: -1
  });
  assert.deepEqual(Object.keys(bad), ['collegeId', 'enrollmentYear', 'studentCount']);
});
test('existing year cannot silently be cleared when SQL skips null', () =>
{
  const model = createModel('class',
  {
    classCode: 'C1',
    className: '一班',
    collegeId: 1
  });
  assert.ok(formErrors('class', model,
  {
    enrollmentYear: 2020
  }).enrollmentYear);
  assert.equal(formErrors('class', model).enrollmentYear, undefined);
});
test('department administrator create omits blank password and edit excludes credentials', () =>
{
  const model = createModel('department-admin',
  {
    username: ' admin ',
    name: '王老师',
    collegeId: 1
  });
  assert.deepEqual(organizationPayload('department-admin', model),
  {
    username: 'admin',
    name: '王老师',
    collegeId: 1
  });
  assert.deepEqual(organizationPayload('department-admin',
  {
    ...model,
    password: 'abcdef'
  }, 8),
  {
    id: 8,
    name: '王老师',
    collegeId: 1
  });
  assert.ok(formErrors('department-admin',
  {
    ...model,
    password: '123'
  }).password);
});

test('class submits major ID instead of a duplicate name', () =>
{
  const model = createModel('class',
  {
    classCode: 'C1',
    className: '一班',
    collegeId: 1,
    majorId: 3,
    majorName: '旧名称'
  });
  const payload = organizationPayload('class', model);
  assert.equal(payload.majorId, 3);
  assert.equal('majorName' in payload, false);
  assert.ok(formErrors('class',
  {
    ...model,
    majorId: null
  }).majorId);
});
test('major form requires name and college, and sends only API fields', () =>
{
  const model = createModel('major',
  {
    majorName: ' 软件工程 ',
    collegeId: 1,
    majorId: 99
  });
  assert.deepEqual(formErrors('major', model),
  {});
  assert.deepEqual(organizationPayload('major', model),
  {
    majorName: '软件工程',
    collegeId: 1
  });
  assert.deepEqual(organizationPayload('major', model, 3),
  {
    majorName: '软件工程',
    collegeId: 1,
    id: 3
  });
  assert.ok(formErrors('major',
  {
    ...model,
    majorName: ' '
  }).majorName);
  assert.ok(formErrors('major',
  {
    ...model,
    majorName: '字'.repeat(101)
  }).majorName);
  assert.ok(formErrors('major',
  {
    ...model,
    collegeId: null
  }).collegeId);
});
test('notice defaults to draft and excludes publisher supplied by client', () =>
{
  const model = createModel('notice',
  {
    title: ' 通知 ',
    content: ' 正文 ',
    publisherId: 99
  });
  assert.deepEqual(organizationPayload('notice', model),
  {
    title: '通知',
    content: '正文',
    noticeType: 1,
    status: 0,
    isTop: 0
  });
  assert.deepEqual(formErrors('notice', model),
  {});
  assert.ok(formErrors('notice',
  {
    ...model,
    title: ' ',
    content: ' '
  }).content);
});
