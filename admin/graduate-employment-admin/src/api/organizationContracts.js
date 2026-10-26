export const definitions = {
  'service-entry':
  {
    label: '服务',
    name: 'name',
    description: '仅最高管理员可维护首页服务。停用后不再展示，排序值越小越靠前。地址支持站内路径或 HTTP/HTTPS 链接。',
    fields: [
    {
      key: 'name',
      label: '服务名称',
      required: true,
      max: 50
    },
    {
      key: 'iconUrl',
      label: '图标地址',
      required: true,
      max: 500
    },
    {
      key: 'url',
      label: '跳转地址',
      required: true,
      max: 500
    },
    {
      key: 'sortOrder',
      label: '排序值',
      required: true,
      type: 'number',
      min: 0,
      max: 99999
    },
    {
      key: 'status',
      label: '启用状态',
      required: true,
      type: 'select',
      options: [
        [0, '停用'],
        [1, '启用']
      ]
    }, ]
  },
  'basic-knowledge':
  {
    label: 'AI提示词',
    name: 'title',
    description: '维护基础知识的标题和回答，只有已生效内容供 AI 知识查询使用。',
    fields: [
    {
      key: 'title',
      label: '标题',
      required: true,
      max: 200
    },
    {
      key: 'answer',
      label: '回答',
      required: true,
      max: 10000,
      type: 'textarea'
    },
    {
      key: 'status',
      label: '是否生效',
      required: true,
      type: 'select',
      options: [
        [0, '未生效'],
        [1, '已生效']
      ]
    }, ]
  },
  'work-skill':
  {
    label: '工作技能',
    name: 'name',
    description: '维护可选技能。被工作引用的技能不能改名或删除。',
    fields: [
    {
      key: 'name',
      label: '技能名称',
      required: true,
      max: 100
    }]
  },
  notice:
  {
    label: '公告',
    name: 'title',
    description: '维护学校通知与就业政策，仅可编辑、删除本人公告。',
    fields: [
    {
      key: 'title',
      label: '标题',
      required: true,
      max: 200
    },
    {
      key: 'content',
      label: '正文',
      required: true,
      type: 'textarea'
    },
    {
      key: 'noticeType',
      label: '公告类型',
      required: true,
      type: 'select',
      options: [
        [1, '系统公告'],
        [2, '就业政策'],
        [3, '备案通知'],
        [4, '其他']
      ]
    },
    {
      key: 'status',
      label: '发布状态',
      required: true,
      type: 'select',
      options: [
        [0, '草稿'],
        [1, '已发布'],
        [2, '已撤回']
      ]
    },
    {
      key: 'isTop',
      label: '置顶',
      required: true,
      type: 'select',
      options: [
        [0, '不置顶'],
        [1, '置顶']
      ]
    }, ],
  },
  major:
  {
    label: '专业',
    name: 'majorName',
    description: '维护专业名称与所属学院，供班级和学生关联使用。',
    fields: [
    {
      key: 'majorName',
      label: '专业名称',
      required: true,
      max: 100
    },
    {
      key: 'collegeId',
      label: '所属学院',
      required: true,
      type: 'college'
    }, ],
  },
  'department-admin':
  {
    label: '院系管理员',
    code: 'username',
    name: 'name',
    description: '维护院系管理员账号与所属学院。',
    fields: [
    {
      key: 'username',
      label: '登录账号',
      required: true,
      max: 50,
      readonlyOnEdit: true
    },
    {
      key: 'name',
      label: '姓名',
      required: true,
      max: 50
    },
    {
      key: 'collegeId',
      label: '所属学院',
      required: true,
      type: 'college'
    },
    {
      key: 'password',
      label: '初始密码',
      type: 'password',
      createOnly: true
    }, ],
  },
  college:
  {
    label: '学院',
    code: 'collegeCode',
    name: 'collegeName',
    description: '维护学院资料、负责人及联系方式。',
    fields: [
    {
      key: 'collegeCode',
      label: '学院编号',
      required: true,
      max: 20
    },
    {
      key: 'collegeName',
      label: '学院名称',
      required: true,
      max: 100
    },
    {
      key: 'deanName',
      label: '院长姓名',
      max: 50
    },
    {
      key: 'phone',
      label: '联系电话',
      max: 20
    },
    {
      key: 'description',
      label: '学院简介',
      max: 500,
      type: 'textarea'
    }, ],
  },
  class:
  {
    label: '班级',
    code: 'classCode',
    name: 'className',
    description: '维护班级资料、所属学院及入学信息。',
    fields: [
    {
      key: 'classCode',
      label: '班级编号',
      required: true,
      max: 30
    },
    {
      key: 'className',
      label: '班级名称',
      required: true,
      max: 100
    },
    {
      key: 'collegeId',
      label: '所属学院',
      required: true,
      type: 'college'
    },
    {
      key: 'majorId',
      label: '专业',
      required: true,
      type: 'major'
    },
    {
      key: 'enrollmentYear',
      label: '入学年份',
      type: 'number',
      min: 1900,
      max: 9999
    },
    {
      key: 'studentCount',
      label: '学生人数',
      required: true,
      type: 'number',
      min: 0,
      max: 2147483647
    }, ],
  },
};
export function readPage(data)
{
  if (!data || !Array.isArray(data.records) || !Number.isInteger(data.totalCount) || data
    .totalCount < 0 || !Number.isInteger(data.total) || data.total < 0 || data.records.some(row => !
      row || row.id == null))
    throw new Error('列表数据格式异常，请联系管理员');
  return data;
}
export function createModel(kind, record = {})
{
  return Object.fromEntries(definitions[kind].fields.map(field => [field.key, record[field.key] ?? (
    ['studentCount', 'status', 'isTop'].includes(field.key) ? 0 : field.key ===
    'noticeType' ? 1 : field.type === 'number' || ['college', 'major'].includes(field
      .type) ? null : '')]));
}
export function formErrors(kind, model, original)
{
  const errors = {};
  for (const f of definitions[kind].fields)
  {
    if (original && (f.createOnly || f.readonlyOnEdit))
      continue;
    const value = typeof model[f.key] === 'string' ? model[f.key].trim() : model[f.key];
    const empty = value === '' || value == null;
    if (f.type === 'password' && !empty && (!value.trim() || value.length < 6 || new TextEncoder()
        .encode(value).length > 72))
      errors[f.key] = '密码至少6个字符，UTF-8编码不超过72字节';
    if (f.required && empty)
      errors[f.key] =
      `请${['college', 'major'].includes(f.type) ? '选择' : '填写'}${f.label}`;
    else if (f.type === 'number' && !empty && (!Number.isInteger(value) || value < f.min || value >
        f.max))
      errors[f.key] = `${f.label}须为${f.min}至${f.max}之间的整数`;
    else if (!f.type && !empty && value.length > f.max || f.type === 'textarea' && value.length > f
      .max)
      errors[f.key] = `${f.label}不能超过${f.max}个字符`;
    // Existing update SQL skips null values, so clearing a saved year would silently do nothing.
    if (f.key === 'enrollmentYear' && original?.enrollmentYear != null && empty)
      errors[f.key] =
      '已有入学年份暂不支持清空，请填写年份';
  }
  return errors;
}
export function organizationPayload(kind, model, id)
{
  const payload = {};
  for (const f of definitions[kind].fields)
  {
    if (id != null && (f.createOnly || f.readonlyOnEdit))
      continue;
    const value = model[f.key];
    if (f.type === 'password')
    {
      if (value)
        payload[f.key] = value;
      continue;
    }
    payload[f.key] = typeof value === 'string' ? value.trim() : value ?? null;
  }
  if (id != null)
    payload.id = id;
  return payload;
}
