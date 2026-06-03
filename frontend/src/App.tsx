import { useState } from "react"
import Dashboard from "@/pages/Dashboard"
import Login from "@/pages/Login"
import { auth } from "@/lib/auth"

export default function App() {
  const [authed, setAuthed] = useState(auth.isAuthenticated())
  if (!authed) return <Login onAuthenticated={() => setAuthed(true)} />
  return <Dashboard onLogout={() => { auth.clear(); setAuthed(false) }} />
}
