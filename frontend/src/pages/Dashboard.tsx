import { useEffect, useMemo, useState } from "react"
import { Plus, Tags } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Select } from "@/components/ui/select"
import { Layout } from "@/components/Layout"
import { SummaryCards } from "@/components/SummaryCards"
import { TransactionsTable } from "@/components/TransactionsTable"
import { TransactionFormDialog } from "@/components/TransactionFormDialog"
import { CategoriesDialog } from "@/components/CategoriesDialog"
import { CategoryPieChart } from "@/components/CategoryPieChart"
import { MonthlyLineChart } from "@/components/MonthlyLineChart"
import { CategoriasApi, TransacoesApi } from "@/lib/api"
import type { Categoria, Resumo, Transacao } from "@/lib/types"

const MESES = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"]

export default function Dashboard({ onLogout }: { onLogout: () => void }) {
  const now = new Date()
  const [mes, setMes] = useState(now.getMonth() + 1)
  const [ano, setAno] = useState(now.getFullYear())
  const [categoriaId, setCategoriaId] = useState<number | "">("")

  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [resumo, setResumo] = useState<Resumo | null>(null)
  const [loading, setLoading] = useState(false)

  const [openForm, setOpenForm] = useState(false)
  const [editing, setEditing] = useState<Transacao | null>(null)

  async function carregar() {
    setLoading(true)
    try {
      const [cats, page, res] = await Promise.all([
        CategoriasApi.list(),
        TransacoesApi.list({ mes, ano, categoriaId: categoriaId || undefined, size: 100, sort: "data,desc" }),
        TransacoesApi.resumo(),
      ])
      setCategorias(cats)
      setTransacoes(page.content)
      setResumo(res)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { carregar() }, [mes, ano, categoriaId])

  async function handleDelete(t: Transacao) {
    if (!confirm(`Excluir "${t.descricao}"?`)) return
    await TransacoesApi.remove(t.id)
    carregar()
  }

  const anos = useMemo(() => {
    const atual = now.getFullYear()
    return [atual - 2, atual - 1, atual, atual + 1]
  }, [])

  return (
    <Layout onLogout={onLogout}>
      <div className="max-w-7xl mx-auto space-y-6">
        <header className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
            <p className="text-muted-foreground text-sm">Acompanhe suas receitas e despesas</p>
          </div>
          <div className="flex gap-2">
            <CategoriesDialog
              categorias={categorias}
              onChanged={carregar}
              trigger={<Button variant="outline"><Tags className="h-4 w-4" /> Categorias</Button>}
            />
            <TransactionFormDialog
              categorias={categorias}
              onSaved={carregar}
              editing={editing}
              open={openForm}
              onOpenChange={(o) => { setOpenForm(o); if (!o) setEditing(null) }}
              trigger={<Button onClick={() => { setEditing(null); setOpenForm(true) }}><Plus className="h-4 w-4" /> Nova transação</Button>}
            />
          </div>
        </header>

        <SummaryCards resumo={resumo} />

        <div className="flex flex-wrap gap-3 items-end">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Mês</label>
            <Select value={mes} onChange={e => setMes(Number(e.target.value))}>
              {MESES.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
            </Select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Ano</label>
            <Select value={ano} onChange={e => setAno(Number(e.target.value))}>
              {anos.map(a => <option key={a} value={a}>{a}</option>)}
            </Select>
          </div>
          <div className="space-y-1 flex-1 min-w-48">
            <label className="text-xs text-muted-foreground">Categoria</label>
            <Select value={categoriaId} onChange={e => setCategoriaId(e.target.value ? Number(e.target.value) : "")}>
              <option value="">Todas</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nome} · {c.tipo}</option>)}
            </Select>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <CategoryPieChart transacoes={transacoes} />
          <MonthlyLineChart transacoes={transacoes} />
        </div>

        <TransactionsTable
          transacoes={transacoes}
          onEdit={(t) => { setEditing(t); setOpenForm(true) }}
          onDelete={handleDelete}
        />

        {loading && <p className="text-sm text-muted-foreground text-center">Carregando…</p>}
      </div>
    </Layout>
  )
}
