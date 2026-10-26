# 学生管理接口

所有请求携带 `Authorization: Bearer <token>`，返回统一 `ResponseVO`。
账号必须具备有效的辅导员或院系管理员角色。辅导员仅操作其负责或协管班级，院系管理员仅操作所属学院；学生拒绝访问。

| 方法 | 路径 | 功能 |
|---|---|---|
| POST | `/student/add` | 同一事务创建 student、sys_user 和学生角色绑定 |
| GET | `/student/{uuid}` | 查询学生资料，uuid 为 student 主键 |
| GET | `/student/page` | 分页查询权限范围内的学生 |
| PUT | `/student/update` | 修改资料，姓名同步 sys_user.real_name |

本次不提供删除接口。

## 新增

```json
{
  "studentNo": "20260001",
  "name": "张同学",
  "password": "Password@123",
  "idCard": "330302200001011234",
  "classId": 1,
  "enrollmentYear": 2022,
  "graduationYear": 2026,
  "educationLevel": "本科"
}
```

学号同时作为登录账号；密码必填，6 至 72 个字符，并遵循现有密码加密限制。
学院、专业由班级确定；传入 collegeId、majorId 时必须与班级一致。
需预先配置启用的 STUDENT 角色。成功返回学生 UserMsgVO，不返回密码。

## 分页

- 全部权限范围：`/student/page?pageNum=1&pageSize=10`
- 按学院：`/student/page?collegeId=1&pageNum=1&pageSize=10`
- 按班级：`/student/page?classId=1&pageNum=1&pageSize=10`

两个筛选条件可同时传入，取交集。每页 1 至 100 条，按学生 uuid 倒序。
返回 data 中包含 records、pageNum、pageSize、totalCount、total。
无匹配或筛选范围不在权限范围内时返回空分页结果。

## 修改

```json
{
  "uuid": 1,
  "name": "张同学",
  "phone": "13800138000",
  "email": "student@example.com",
  "classId": 2
}
```

uuid 必填，其他字段按需传入。支持姓名及原有管理员学生资料字段：idCard、phone、email、collegeId、majorId、enrollmentYear、graduationYear、educationLevel、studentStatus、address、classId。
转班时原班级和目标班级均须在管理范围内，学院和专业随目标班级同步。
此接口不修改学号、登录名或密码；姓名和账号姓名在同一事务更新。

## 按账号角色查询管理范围内全部学生

`GET /student/by-user/{userId}`，userId 为 `sys_user.id`，必须与 Token 当前账号一致。
返回 `data: UserMsgVO[]`，不分页，按学生 uuid 倒序；无匹配学生返回空列表。
复用有效 RBAC 角色及班级范围：DEPARTMENT_HEAD 查询所属学院班级的学生，COUNSELOR 查询负责或协管班级的学生。
学生角色优先拒绝；未配置有效角色、角色停用或传入他人用户 ID，返回业务码 403。
