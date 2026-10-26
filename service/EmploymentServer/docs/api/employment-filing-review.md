# 备案审核与归档列表查询

请求均需 `Authorization: Bearer <token>`，userId 是 `sys_user.id`，必须与当前登录账号一致。
返回统一 ResponseVO，data 为 EmploymentFilingManagementVO 数组；无记录返回空数组，不分页。

## 待审核列表

`GET /employment-filing/pending-review/{userId}`

- COUNSELOR：返回本人负责或协管班级学生的 status=10（待辅导员初审）备案。
- DEPARTMENT_HEAD：返回本人所属学院班级学生的 status=20（待院系复审）备案。
- 使用启用的 RBAC 角色，多个角色时院系管理员优先；学生角色优先拒绝。

## 已归档列表

`GET /employment-filing/archived/{userId}`

仅返回 status=30 的备案，使用与待审核列表相同的班级或学院范围。

## 数据范围与排序

按学生当前的 class_id 关联 class_info，再关联 counselor_class 或 college_admin、college；不会修改备案中提交时的班级等快照字段。
按 submitted_at 倒序、id 倒序返回；重复管理关系不会产生重复备案。
无有效角色、学生或传入其他用户 ID 均拒绝访问。

原有学生提交及本人备案查询接口保持不变。

## 提交审核

- `PUT /employment-filing/review/counselor`：COUNSELOR 角色，初审通过 10→20，打回 10→40。
- `PUT /employment-filing/review/department`：DEPARTMENT_HEAD 角色，复审通过 20→30，并写入归档时间。

请求示例（辅导员通过）：

```json
{"filingId": 123, "version": 0, "fromStatus": 10, "auditResult": 1, "auditOpinion": "材料齐全"}
```

院系复审使用数据库当前 version 和 fromStatus=20。auditResult=2 表示打回，auditOpinion 必填。
现有数据库约束仅允许辅导员打回，院系接口不接受打回。

审核人 ID 和姓名由当前登录账号读取，角色、阶段及目标状态由后端确定，客户端无需传入。
服务读取并锁定备案记录，比较数据库状态与版本；不一致或不在对应审核阶段返回业务码 409，需刷新后重试。
同时验证有效 RBAC 角色和学生当前班级对应的审核范围。校验通过后通过状态及版本条件更新，版本加 1。
状态更新和 EmploymentAuditRecordService.add 处于同一事务，任一步失败整体回滚。
客户端必须使用管理列表返回的 id、version、status，不要自行推算版本。
