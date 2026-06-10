const ACCESS_KEY = "financas.accessToken"
const REFRESH_KEY = "financas.refreshToken"
const USER_KEY = "financas.user"

export interface StoredUser { nome: string; email: string }

export const auth = {
  getAccessToken: () => localStorage.getItem(ACCESS_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_KEY),
  getUser: (): StoredUser | null => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  },
  save: (accessToken: string, refreshToken: string, user: StoredUser) => {
    localStorage.setItem(ACCESS_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  updateTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(USER_KEY)
  },
  isAuthenticated: () => !!localStorage.getItem(ACCESS_KEY),
}
