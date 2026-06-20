import { Suspense, lazy, useState } from "react"
import { Route, Routes } from "react-router-dom"
import Dashboard from "@/pages/Dashboard"
import Login from "@/pages/Login"
import { Layout } from "@/components/Layout"
import { auth } from "@/lib/auth"
import { AuthApi } from "@/lib/api"

const Investimentos = lazy(() => import("@/pages/Investimentos"))

export default function App() {
  const [authed, setAuthed] = useState(auth.isAuthenticated())

  async function handleLogout() {
    const rt = auth.getRefreshToken()
    if (rt) await AuthApi.logout(rt)
    auth.clear()
    setAuthed(false)
  }

  if (!authed) return <Login onAuthenticated={() => setAuthed(true)} />

  return (
    <Layout onLogout={handleLogout}>
      <Suspense fallback={<p className="text-sm text-muted-foreground text-center">Carregando…</p>}>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/investimentos" element={<Investimentos />} />
        </Routes>
      </Suspense>
    </Layout>
  )
}
