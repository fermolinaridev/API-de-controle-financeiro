import { useEffect, useMemo, useState } from "react"
import { Plus, Tags } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Layout } from "@/components/Layout"
import { SummaryCards } from "@/components/SummaryCards"
import { TransactionsTable } from "@/components/TransactionsTable"
import { TransactionFormDialog } from "@/components/TransactionFormDialog"
import { CategoriesDialog } from "@/components/CategoriesDialog"
import { ImportCsvButton } from "@/components/ImportCsvButton"
import { CategoryPieChart } from "@/components/CategoryPieChart"
import { MonthlyLineChart } from "@/components/MonthlyLineChart"
import { Filters, type FiltersState } from "@/components/Filters"
import { CategoriasApi, TransacoesApi, type TransacaoListParams } from "@/lib/api"
import type { Categoria, Resumo, Transacao } from "@/lib/types"
import { useDebounce } from "@/lib/useDebounce"

export default function Dashboard({ onLogout }: { onLogout: () => void }) {
  const now = new Date()

  const [filters, setFilters] = useState<FiltersState>({
    mode: "mes",
    mes: now.getMonth() + 1,
    ano: now.getFullYear(),
    dataInicio: "",
    dataFim: "",
    categoriaId: "",
    tipo: "",
    busca: "",
  })
  const buscaDebounced = useDebounce(filters.busca, 350)

  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [resumo, setResumo] = useState<Resumo | null>(null)
  const [loading, setLoading] = useState(false)

  const [openForm, setOpenForm] = useState(false)
  const [editing, setEditing] = useState<Transacao | null>(null)

  function montarParams(): TransacaoListParams {
    const p: TransacaoListParams = { size: 100, sort: "data,desc" }
    if (filters.mode === "mes") {
      p.mes = filters.mes; p.ano = filters.ano
    } else {
      if (filters.dataInicio) p.dataInicio = filters.dataInicio
      if (filters.dataFim) p.dataFim = filters.dataFim
    }
    if (filters.categoriaId) p.categoriaId = Number(filters.categoriaId)
    if (filters.tipo) p.tipo = filters.tipo
    if (buscaDebounced.trim()) p.q = buscaDebounced.trim()
    return p
  }

  async function carregarCategorias() {
    setCategorias(await CategoriasApi.list())
  }

  async function carregar() {
    setLoading(true)
    try {
      const [page, res] = await Promise.all([
        TransacoesApi.list(montarParams()),
        TransacoesApi.resumo(),
      ])
      setTransacoes(page.content)
      setResumo(res)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { carregarCategorias() }, [])
  useEffect(() => {
    carregar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    filters.mode, filters.mes, filters.ano, filters.dataInicio, filters.dataFim,
    filters.categoriaId, filters.tipo, buscaDebounced,
  ])

  async function handleDelete(t: Transacao) {
    if (!confirm(`Excluir "${t.descricao}"?`)) return
    await TransacoesApi.remove(t.id)
    carregar()
  }

  function reset() {
    setFilters({
      mode: "mes",
      mes: now.getMonth() + 1,
      ano: now.getFullYear(),
      dataInicio: "",
      dataFim: "",
      categoriaId: "",
      tipo: "",
      busca: "",
    })
  }

  const anos = useMemo(() => {
    const atual = now.getFullYear()
    return [atual - 2, atual - 1, atual, atual + 1]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Layout onLogout={onLogout}>
      <div className="max-w-7xl mx-auto space-y-6">
        <header className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Dashboard</h1>
            <p className="text-muted-foreground text-sm">Acompanhe suas receitas e despesas</p>
          </div>
          <div className="flex gap-2 flex-wrap">
            <ImportCsvButton onImported={carregar} />
            <CategoriesDialog
              categorias={categorias}
              onChanged={() => { carregarCategorias(); carregar() }}
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

        <Filters
          state={filters}
          setState={setFilters}
          categorias={categorias}
          anos={anos}
          onReset={reset}
        />

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
