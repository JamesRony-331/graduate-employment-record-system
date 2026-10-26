export const fields = [
{
  title: '公司信息',
  items: [
  {
    key: 'companyName',
    label: '公司名称',
    text: true
  },
  {
    key: 'companyContact',
    label: '公司联系方式（电话或邮箱）',
    text: true
  }, ]
},
{
  title: '个人能力',
  description: '填写你目前的能力与经历。应届生的工作年限填写 0。',
  items: [
  {
    key: 'education',
    label: '最高学历',
    required: true,
    options: ['高中及以下', '中专 / 职高', '大专', '本科', '硕士', '博士']
  },
  {
    key: 'major',
    label: '专业',
    text: true
  },
  {
    key: 'experience',
    label: '工作年限',
    required: true,
    number: true,
    unit: '年'
  },
  {
    key: 'skills',
    label: '掌握技能',
    required: true,
    multiple: true,
    create: true,
    options: ['Java', 'Spring Boot', 'MySQL', 'JavaScript', 'Vue', 'React', 'Python',
      'Excel', '数据分析', '沟通协作'
    ]
  }, ]
},
{
  title: '求职目标',
  description: '可以选择多个岗位类别和城市，也可以输入其他选项。',
  items: [
  {
    key: 'categories',
    label: '期望岗位类别',
    required: true,
    multiple: true,
    create: true,
    options: ['后端开发', '前端开发', '软件测试', '数据分析', '产品经理', '运营', '设计', '财务', '市场营销', '行政人事']
  },
  {
    key: 'cities',
    label: '期望城市',
    required: true,
    multiple: true,
    create: true,
    strict: true,
    options: ['不限', '北京', '上海', '广州', '深圳', '杭州', '温州', '宁波', '南京', '成都', '武汉', '西安']
  },
  {
    key: 'jobType',
    label: '求职类型',
    required: true,
    options: ['全职', '实习', '兼职']
  }, ]
},
{
  title: '薪资要求',
  description: '单位为元 / 月。最低可接受月薪填写后将作为底线，期望月薪用于推荐排序。',
  items: [
  {
    key: 'minimumSalary',
    label: '最低可接受月薪',
    number: true,
    unit: '元 / 月'
  },
  {
    key: 'expectedSalary',
    label: '期望月薪',
    number: true,
    unit: '元 / 月'
  }, ]
},
{
  title: '工作偏好',
  description: '开启“必须满足”表示硬性条件；关闭时仅作为偏好。未填写表示未表达偏好。',
  items: [
  {
    key: 'workMode',
    label: '工作方式',
    strict: true,
    options: ['现场办公', '远程', '混合办公']
  },
  {
    key: 'restDays',
    label: '休息制度',
    strict: true,
    options: ['双休', '单双休', '单休']
  },
  {
    key: 'travel',
    label: '是否接受出差',
    strict: true,
    options: ['不接受', '接受偶尔出差', '不限']
  },
  {
    key: 'outsourcing',
    label: '是否接受外包岗位',
    strict: true,
    options: ['不接受', '接受', '不限']
  }, ]
},
{
  title: '推荐偏好与补充说明',
  description: '选择一个最看重的因素，帮助明确推荐重点。',
  items: [
  {
    key: 'priority',
    label: '最看重的因素',
    required: true,
    options: ['薪资', '技能匹配', '地点', '休息制度', '专业相关性', '工作方式', '发展空间']
  },
  {
    key: 'other',
    label: '其他要求',
    text: true,
    textarea: true
  }, ]
}, ]
