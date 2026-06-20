import { useCallback, useEffect, useState } from "react"
import { DollarSign, Bitcoin, RefreshCw, TrendingUp, TrendingDown } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { MercadoApi } from "@/lib/api"
import type { Cotacao } from "@/lib/types"
import { brl } from "@/lib/utils"

interface Estado {
  cotacao: Cotacao | null
  erro: string | null
}

const inicial: Estado = { cotacao: null, erro: null }

export function MarketQuoteCards() {
  const [dolar, setDolar] = useState<Estado>(inicial)
  const [btc, setBtc] = useState<Estado>(inicial)
  const [loading, setLoading] = useState(false)

  const carregar = useCallback(async () => {
    setLoading(true)
    const [d, b] = await Promise.allSettled([MercadoApi.dolar(), MercadoApi.bitcoin()])
    setDolar(d.status === "fulfilled"
      ? { cotacao: d.value, erro: null }
      : { cotacao: null, erro: mensagemErro(d.reason) })
    setBtc(b.status === "fulfilled"
      ? { cotacao: b.value, erro: null }
      : { cotacao: null, erro: mensagemErro(b.reason) })
    setLoading(false)
  }, [])

  useEffect(() => { carregar() }, [carregar])

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-medium text-muted-foreground">Cotações ao vivo</h2>
        <Button variant="ghost" size="sm" onClick={carregar} disabled={loading}>
          <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} /> Atualizar
        </Button>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <QuoteCard titulo="Dólar (USD)" icon={DollarSign} estado={dolar} />
        <QuoteCard titulo="Bitcoin (BTC)" icon={Bitcoin} estado={btc} />
      </div>
    </div>
  )
}

function QuoteCard({ titulo, icon: Icon, estado }: {
  titulo: string
  icon: typeof DollarSign
  estado: Estado
}) {
  const { cotacao, erro } = estado
  const variacao = cotacao?.variacaoPercentual ?? null
  const subindo = (variacao ?? 0) >= 0

  return (
    <Card>
      <CardContent className="flex items-center justify-between p-6">
        <div className="min-w-0">
          <p className="text-sm text-muted-foreground">{titulo}</p>
          {cotacao ? (
            <>
              <p className="text-2xl font-bold mt-1">{brl(cotacao.preco)}</p>
              {variacao !== null && (
                <p className={`text-sm mt-0.5 flex items-center gap-1 ${subindo ? "text-emerald-500" : "text-rose-500"}`}>
                  {subindo ? <TrendingUp className="h-3.5 w-3.5" /> : <TrendingDown className="h-3.5 w-3.5" />}
                  {subindo ? "+" : ""}{variacao.toFixed(2)}%
                </p>
              )}
            </>
          ) : (
            <p className="text-sm mt-2 text-muted-foreground max-w-[14rem]">{erro ?? "—"}</p>
          )}
        </div>
        <div className={`p-3 rounded-full ${cotacao ? "bg-primary/10" : "bg-muted"}`}>
          <Icon className={`h-6 w-6 ${cotacao ? "text-primary" : "text-muted-foreground"}`} />
        </div>
      </CardContent>
    </Card>
  )
}

function mensagemErro(reason: unknown): string {
  const err = reason as { response?: { data?: { erro?: string } } }
  return err?.response?.data?.erro ?? "Cotação indisponível no momento"
}
