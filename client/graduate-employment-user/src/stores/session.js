import
{
  reactive
}
from 'vue'
import
{
  safeProfile
}
from '../api/contracts'
const key = 'graduate.student.session.v2'

function restore()
{
  try
  {
    const value = JSON.parse(sessionStorage.getItem(key))
    if (value?.demo && import.meta.env.DEV)
      return {
        ...value,
        user: safeProfile(value.user)
      }
    if (value?.token && value?.user)

      return {
        token: value.token,
        user: safeProfile(value.user),
        demo: false
      }
  }
  catch
  {
    /* Invalid browser state is treated as signed out. */
  }
  return {
    token: '',
    user: null,
    demo: false
  }
}
export const session = reactive(restore())
export function setSession(token, user, demo = false)
{
  Object.assign(session,
  {
    token,
    user: safeProfile(user),
    demo: Boolean(demo && import.meta.env.DEV),
  })
  persist()
}

function persist()
{
  try
  {
    sessionStorage.setItem(key, JSON.stringify(session))
  }
  catch
  {
    /* Session remains usable in memory. */
  }
}
export function updateProfile(values)
{
  session.user = safeProfile(
  {
    ...session.user,
    ...values
  })
  persist()
}
export function clearSession()
{
  Object.assign(session,
  {
    token: '',
    user: null,
    demo: false
  })
  try
  {
    sessionStorage.removeItem(key)
  }
  catch
  {
    /* Storage can be unavailable. */
  }
}
export function startPreview()
{
  if (!import.meta.env.DEV)
    return
  setSession(
    '',
    {
      name: '林同学',
      studentNo: '2023001001',
      college: '计算机与信息工程学院',
      major: '软件工程',
      className: '软件工程2301班',
      educationLevel: '本科',
      enrollmentYear: 2023,
      graduationYear: 2027,
      phone: '',
      email: '',
    },
    true,
  )
}
