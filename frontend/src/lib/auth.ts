const TOKEN_KEY = "financas.token"
const USER_KEY = "financas.user"

export interface StoredUser { nome: string; email: string }

export const auth = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  getUser: (): StoredUser | null => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  },
  save: (token: string, user: StoredUser) => {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
  isAuthenticated: () => !!localStorage.getItem(TOKEN_KEY),
}
