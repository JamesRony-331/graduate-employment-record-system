export function readFilings(data)
{
  if (!Array.isArray(data) || data.some(r => !r || r.id == null || r.status == null))

    throw new Error('备案列表数据格式异常，请联系管理员');
  return data;
}
export const filingStatus = value => (
{
  10: '待辅导员初审',
  20: '待院系复审',
  30: '已归档',
  40: '已打回'
})[value] || `未知状态（${value ?? '未返回'}）`;
export const applicationType = value => (
{
  1: '首次备案',
  2: '变更申请',
  3: '打回重提'
})[value] || '未记录';
export function parseFilingJson(value, array = false)
{
  if (value == null || value === '')
    return {
      value: array ? [] :
      {},
      error: false
    };
  try
  {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value;
    if (array ? !Array.isArray(parsed) : !parsed || typeof parsed !== 'object' || Array.isArray(
        parsed))
      throw new Error();
    return {
      value: parsed,
      error: false
    };
  }
  catch
  {
    return {
      value: array ? [] :
      {},
      error: true
    };
  }
}
export function filterFilings(rows, keyword, classId)
{
  const q = keyword.trim().toLowerCase();
  return rows.filter(r => (classId == null || String(r.classId) === String(classId)) && [r
    .applicationNo, r.studentNo, r.studentName, r.companyName
  ].some(v => String(v || '').toLowerCase().includes(q)));
}
export function reviewPayload(record, result, opinion = '')
{
  if (!record?.id || !Number.isInteger(record.version) || record.version < 0)
    throw new Error(
      '缺少有效备案版本，请刷新列表');
  const status = Number(record.status);
  if (![10, 20].includes(status))
    throw new Error('当前备案不能审核，请刷新列表');
  if (![1, 2].includes(result) || (status === 20 && result !== 1))
    throw new Error('院系复审仅支持通过');
  const text = opinion.trim();
  if (text.length > 1000)
    throw new Error('审核意见不能超过1000字');
  if (result === 2 && !text)
    throw new Error('请填写打回原因');
  return {
    filingId: record.id,
    version: record.version,
    fromStatus: status,
    auditResult: result,
    auditOpinion: text
  };
}
