import { useState } from "react"
import { Wallet } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { AuthApi } from "@/lib/api"
import { auth } from "@/lib/auth"

export default function Login({ onAuthenticated }: { onAuthenticated: () => void }) {
  const [mode, setMode] = useState<"login" | "register">("login")
  const [nome, setNome] = useState("")
  const [email, setEmail] = useState("admin@financas.local")
  const [senha, setSenha] = useState("admin123")
  const [erro, setErro] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setErro(null); setLoading(true)
    try {
      const data = mode === "login"
        ? await AuthApi.login({ email, senha })
        : await AuthApi.register({ nome, email, senha })
      auth.save(data.token, { nome: data.nome, email: data.email })
      onAuthenticated()
    } catch (e: any) {
      setErro(e.response?.data?.erro ?? "Falha na autenticação")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-3">
          <div className="mx-auto p-3 rounded-full bg-primary/10 w-fit">
            <Wallet className="h-8 w-8 text-primary" />
          </div>
          <CardTitle className="text-2xl">Finanças</CardTitle>
          <CardDescription>
            {mode === "login" ? "Entre com sua conta" : "Crie sua conta"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="space-y-4">
            {mode === "register" && (
              <div className="space-y-2">
                <Label>Nome</Label>
                <Input value={nome} onChange={e => setNome(e.target.value)} required maxLength={120} />
              </div>
            )}
            <div className="space-y-2">
              <Label>E-mail</Label>
              <Input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Senha</Label>
              <Input type="password" value={senha} onChange={e => setSenha(e.target.value)} required minLength={6} />
            </div>
            {erro && <p className="text-sm text-destructive">{erro}</p>}
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Aguarde…" : mode === "login" ? "Entrar" : "Criar conta"}
            </Button>
            <button
              type="button"
              onClick={() => { setMode(mode === "login" ? "register" : "login"); setErro(null) }}
              className="w-full text-sm text-muted-foreground hover:text-foreground"
            >
              {mode === "login" ? "Não tem conta? Criar conta" : "Já tem conta? Entrar"}
            </button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
