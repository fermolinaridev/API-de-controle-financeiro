import { useCallback, useEffect, useState } from "react"
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts"
import { TrendingUp, TrendingDown } from "lucide-react"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { AssetSearchInput } from "@/components/AssetSearchInput"
import { MercadoApi } from "@/lib/api"
import type { AtivoBusca, Cotacao, Historico } from "@/lib/types"
import { brl } from "@/lib/utils"

const ranges = [
  { value: "1mo", label: "1M" },
  { value: "3mo", label: "3M" },
  { value: "6mo", label: "6M" },
  { value: "1y", label: "1A" },
]

export function AssetSearchChart() {
  const [ticker, setTicker] = useState<string | null>(null)
  const [range, setRange] = useState("3mo")
  const [historico, setHistorico] = useState<Historico | null>(null)
  const [cotacao, setCotacao] = useState<Cotacao | null>(null)
  const [loading, setLoading] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const carregar = useCallback(async (tk: string, rng: string) => {
    setLoading(true)
    setErro(null)
    try {
      const [h, c] = await Promise.all([
        MercadoApi.historico(tk, rng),
        MercadoApi.cotacao(tk).catch(() => null),
      ])
      setHistorico(h)
      setCotacao(c)
    } catch (e: any) {
      setHistorico(null)
      setCotacao(null)
      setErro(e.response?.data?.erro ?? "Não foi possível carregar o gráfico")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (ticker) carregar(ticker, range)
  }, [ticker, range, carregar])

  function aoSelecionar(a: AtivoBusca) {
    setTicker(a.ticker)
  }

  const dados = (historico?.pontos ?? []).map(p => ({
    data: `${p.data.slice(8, 10)}/${p.data.slice(5, 7)}`,
    fechamento: p.fechamento,
  }))
  const subindo = (cotacao?.variacaoPercentual ?? 0) >= 0

  return (
    <Card>
      <CardHeader>
        <CardTitle>Buscar ação e ver o gráfico</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <AssetSearchInput onSelect={aoSelecionar} />

        {!ticker && (
          <div className="h-72 flex items-center justify-center text-sm text-muted-foreground text-center">
            Busque uma ação da B3 acima para ver o gráfico de preço.
          </div>
        )}

        {ticker && (
          <>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="font-semibold">{ticker}</p>
                {historico?.nome && <p className="text-xs text-muted-foreground">{historico.nome}</p>}
              </div>
              <div className="flex items-center gap-3">
                {cotacao && (
                  <div className="text-right">
                    <p className="font-bold">{brl(cotacao.preco)}</p>
                    {cotacao.variacaoPercentual !== null && (
                      <p className={`text-xs flex items-center gap-1 justify-end ${subindo ? "text-emerald-500" : "text-rose-500"}`}>
                        {subindo ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                        {subindo ? "+" : ""}{cotacao.variacaoPercentual.toFixed(2)}%
                      </p>
                    )}
                  </div>
                )}
                <div className="flex rounded-md border overflow-hidden">
                  {ranges.map(r => (
                    <button
                      key={r.value}
                      type="button"
                      onClick={() => setRange(r.value)}
                      className={`px-3 py-1 text-xs font-medium transition-colors ${
                        range === r.value ? "bg-primary text-primary-foreground" : "hover:bg-muted"
                      }`}
                    >
                      {r.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="h-72">
              {loading ? (
                <div className="h-full flex items-center justify-center text-sm text-muted-foreground">Carregando…</div>
              ) : erro ? (
                <div className="h-full flex items-center justify-center text-sm text-muted-foreground">{erro}</div>
              ) : dados.length === 0 ? (
                <div className="h-full flex items-center justify-center text-sm text-muted-foreground">Sem dados no período</div>
              ) : (
                <ResponsiveContainer>
                  <LineChart data={dados}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                    <XAxis dataKey="data" stroke="hsl(var(--muted-foreground))" fontSize={12}
                           interval="preserveStartEnd" minTickGap={32} />
                    <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} domain={["auto", "auto"]}
                           tickFormatter={v => brl(Number(v)).replace("R$", "")} width={70} />
                    <Tooltip
                      formatter={(v) => brl(Number(v))}
                      contentStyle={{
                        background: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: 8,
                        color: "hsl(var(--foreground))",
                      }}
                    />
                    <Line type="monotone" dataKey="fechamento" stroke={subindo ? "#22c55e" : "#ef4444"}
                          strokeWidth={2} dot={false} name="Fechamento" />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}
