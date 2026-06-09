import { useState } from "react"
import Dashboard from "@/pages/Dashboard"
import Login from "@/pages/Login"
import { auth } from "@/lib/auth"
import { AuthApi } from "@/lib/api"

export default function App() {
  const [authed, setAuthed] = useState(auth.isAuthenticated())

  async function handleLogout() {
    const rt = auth.getRefreshToken()
    if (rt) await AuthApi.logout(rt)
    auth.clear()
    setAuthed(false)
  }

  if (!authed) return <Login onAuthenticated={() => setAuthed(true)} />
  return <Dashboard onLogout={handleLogout} />
}
