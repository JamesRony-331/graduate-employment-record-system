# 后台账号接口

sys_user.counselor_id 仅为辅导员关联，不决定账号角色；角色及权限后续接入 RBAC。
当前注册接口开放，注册不创建角色或权限；修改和删除仅操作 JWT 对应的当前账号。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | /sys-user/register | 注册账号 |
| POST | /sys-user/login | 登录 |
| PUT | /sys-user/updateInfo | 修改当前账号 |
| DELETE | /sys-user/delete | 软删除当前账号，无请求体 |

注册示例：

```json
{"username":"teacher01","password":"password123","realName":"张老师","counselorId":1}
```

counselorId、realName 可省略。登录只传 username、password。
登录成功返回 data.token 和 data.userMsgVO，账号信息不包含密码。
修改和删除须传请求头 `Authorization: Bearer <token>`，不接受目标账号 ID。
修改请求支持 username、password、realName、counselorId；未传或 null 的字段保持原值。
密码通过 BCrypt 哈希存储，不能将学生表的 MD5 密码直接迁入本表。

软删除将 status 置为 0，保留数据和用户名；该账号无法再次登录，已有 Token 也不能执行本模块的修改和删除。
当前表没有独立删除标记，因此软删除与禁用使用相同状态。

数据库前提：id 为自增主键；username 必须有单列唯一索引。
若缺少唯一索引，先处理重复数据，再执行 sql/sys_user_username_unique.sql 中的 ALTER。
本次未直接修改数据库。
