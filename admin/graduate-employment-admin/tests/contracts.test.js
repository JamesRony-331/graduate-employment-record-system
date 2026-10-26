import test from "node:test";
import assert from "node:assert/strict";
import
{
  safeUser,
  profileErrors,
  updatePayload,
  unwrap,
}
from "../src/api/contracts.js";
test("only whitelisted account fields are retained", () =>
{
  const user = safeUser(
  {
    username: "teacher",
    password: "secret",
    token: "secret",
    role: "admin",
  });
  assert.equal(user.username, "teacher");
  assert.equal("password" in user, false);
  assert.equal("role" in user, false);
});
test("HTTP-success business failures must throw", () =>
{
  assert.throws(
    () => unwrap(
    {
      code: 500,
      info: "账号或密码错误"
    }),
    /账号或密码错误/,
  );
  assert.deepEqual(unwrap(
  {
    code: 200,
    data:
    {
      ok: true
    }
  }),
  {
    ok: true
  });
});
test("password matches backend UTF-8 byte limit", () =>
{
  const value = {
    username: "teacher",
    realName: "老师",
    password: "",
    confirmPassword: "",
  };
  assert.deepEqual(profileErrors(value),
  {});
  assert.ok(
    profileErrors(
    {
      ...value,
      password: "中".repeat(25),
      confirmPassword: "中".repeat(25),
    }).password,
  );
  assert.ok(
    profileErrors(
    {
      ...value,
      password: "123456",
      confirmPassword: "123457"
    })
    .confirmPassword,
  );
});
test("update payload sends only changed editable fields", () =>
{
  assert.deepEqual(
    updatePayload(
    {
      username: "changed-account",
      realName: "新姓名",
      password: "",
      counselorId: 1,
    },
    {
      username: "teacher",
      realName: "旧姓名"
    }, ),
    {
      realName: "新姓名"
    },
  );
});

test("static counselor binding rejects duplicates and supports unbinding", async () =>
{
  const
  {
    bindDemoCounselor,
    accountFor
  } =
  await import("../src/data/accountDemo.js");
  assert.throws(() => bindDemoCounselor("demo-u2", "demo-c1"), /已绑定/);
  bindDemoCounselor("demo-u2", "demo-c2");
  assert.equal(accountFor("demo-c2").id, "demo-u2");
  bindDemoCounselor("demo-u2", null);
  assert.equal(accountFor("demo-c2"), undefined);
  assert.throws(() => bindDemoCounselor("demo-u2", "invalid"), /有效/);
});
test("login counselor name is retained in the session whitelist", () =>
{
  assert.equal(safeUser(
  {
    counselorName: "王老师"
  }).counselorName, "王老师");
  assert.equal(safeUser(
  {}).counselorName, null);
});
