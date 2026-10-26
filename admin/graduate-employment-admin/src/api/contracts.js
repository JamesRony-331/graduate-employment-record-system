export function safeUser(user = {})
{
  return Object.fromEntries(
    [
      "id",
      "username",
      "realName",
      "counselorId",
      "counselorName",
      "status",
      "createdAt",
      "updatedAt",
    ].map((key) => [key, user[key] ?? null]),
  );
}
export function unwrap(body)
{
  if (Number(body?.code) !== 200)
  {
    const error = new Error(body?.info || "操作未完成，请稍后重试");
    error.code = Number(body?.code);
    throw error;
  }
  return body.data;
}
export function profileErrors(model)
{
  const errors = {};
  if (model.realName.trim().length > 50)

    errors.realName = "姓名不能超过50个字符";
  if (
    model.password &&
    (!model.password.trim() ||
      model.password.length < 6 ||
      new TextEncoder().encode(model.password).length > 72)
  )

    errors.password = "密码至少6个字符，UTF-8编码不超过72字节";
  if (model.password !== model.confirmPassword)

    errors.confirmPassword = "两次输入的密码不一致";
  return errors;
}
export function updatePayload(model, previous)
{
  const body = {};
  if (model.realName.trim() !== (previous.realName || ""))

    body.realName = model.realName.trim();
  if (model.password)
    body.password = model.password;
  return body;
}
