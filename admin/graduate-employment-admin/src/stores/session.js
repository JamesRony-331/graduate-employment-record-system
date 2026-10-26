import
{
  reactive
}
from "vue";
import
{
  safeUser
}
from "../api/contracts";
const key = "graduate.admin.session.v2";

function restore()
{
  try
  {
    const data = JSON.parse(sessionStorage.getItem(key));
    if (typeof data?.token === "string" && data.token && data.user?.username)

      return {
        token: data.token,
        user: safeUser(data.user)
      };
  }
  catch
  {
    /* Invalid session is signed out. */
  }
  return {
    token: "",
    user: null
  };
}
export const session = reactive(restore());
export function saveSession(token, user)
{
  Object.assign(session,
  {
    token,
    user: safeUser(user)
  });
  try
  {
    sessionStorage.setItem(key, JSON.stringify(session));
  }
  catch
  {
    /* Keep in-memory session. */
  }
}
export function clearSession()
{
  Object.assign(session,
  {
    token: "",
    user: null
  });
  try
  {
    sessionStorage.removeItem(key);
  }
  catch
  {
    /* Storage may be disabled. */
  }
}
