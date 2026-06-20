import { useEffect, useState } from "react"
import { Plus } from "lucide-react"
import { Button } from "@/components/ui/button"
import { MarketQuoteCards } from "@/components/MarketQuoteCards"
import { PortfolioSummary } from "@/components/PortfolioSummary"
import { PortfolioTable } from "@/components/PortfolioTable"
import { InvestmentFormDialog } from "@/components/InvestmentFormDialog"
import { AssetSearchChart } from "@/components/AssetSearchChart"
import { InvestimentosApi } from "@/lib/api"
import type { Carteira, Investimento } from "@/lib/types"

export default function Investimentos() {
  const [carteira, setCarteira] = useState<Carteira | null>(null)
  const [loading, setLoading] = useState(false)
  const [openForm, setOpenForm] = useState(false)
  const [editing, setEditing] = useState<Investimento | null>(null)

  async function carregar() {
    setLoading(true)
    try {
      setCarteira(await InvestimentosApi.carteira())
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { carregar() }, [])

  async function handleDelete(i: Investimento) {
    if (!confirm(`Remover ${i.ticker} da carteira?`)) return
    await InvestimentosApi.remove(i.id)
    carregar()
  }

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold tracking-tight">Investimentos</h1>
          <p className="text-muted-foreground text-sm">Cotações ao vivo, sua carteira e gráficos da B3</p>
        </div>
        <InvestmentFormDialog
          onSaved={carregar}
          editing={editing}
          open={openForm}
          onOpenChange={(o) => { setOpenForm(o); if (!o) setEditing(null) }}
          trigger={<Button onClick={() => { setEditing(null); setOpenForm(true) }}><Plus className="h-4 w-4" /> Adicionar ativo</Button>}
        />
      </header>

      <MarketQuoteCards />

      <PortfolioSummary resumo={carteira?.resumo ?? null} />

      <PortfolioTable
        itens={carteira?.itens ?? []}
        onEdit={(i) => { setEditing(i); setOpenForm(true) }}
        onDelete={handleDelete}
      />

      <AssetSearchChart />

      {loading && <p className="text-sm text-muted-foreground text-center">Carregando…</p>}
    </div>
  )
}
