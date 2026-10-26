import
{
  session,
  clearSession
}
from "../stores/session";
import
{
  unwrap
}
from "./contracts";
async function request(path, method, body, auth = false)
{
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try
  {
    const response = await fetch("/api/sys-user" + path,
    {
      method,
      headers:
      {
        "Content-Type": "application/json",
        ...(auth ?
        {
          Authorization: "Bearer " + session.token
        } :
        {}),
      },
      body: JSON.stringify(body),
      signal: controller.signal,
    });
    if ([401, 403].includes(response.status))
    {
      const e = new Error("登录已失效，请重新登录");
      e.code = response.status;
      throw e;
    }
    if (!response.ok)
      throw new Error("服务暂时不可用，请稍后重试");
    return unwrap(await response.json());
  }
  catch (e)
  {
    if (auth && [401, 403, 901].includes(e.code))
    {
      clearSession();
      window.dispatchEvent(new Event("admin-session-expired"));
    }
    if (e.name === "AbortError")
      throw new Error("请求超时，请稍后核实后重试");
    if (e instanceof TypeError || e instanceof SyntaxError)

      throw new Error("无法连接服务，请确认后端已启动");
    throw e;
  }
  finally
  {
    clearTimeout(timeout);
  }
}
export const login = (body) =>
  request("/login", "POST",
  {
    ...body,
    accountType: "STAFF"
  });
export const updateInfo = (body) => request("/updateInfo", "PUT", body, true);

export const getProfile = () => request("/profile", "GET", undefined, true);
