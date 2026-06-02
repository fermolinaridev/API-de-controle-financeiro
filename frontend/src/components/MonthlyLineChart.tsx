import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from "recharts"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import type { Transacao } from "@/lib/types"
import { brl } from "@/lib/utils"

export function MonthlyLineChart({ transacoes }: { transacoes: Transacao[] }) {
  const byDay = new Map<string, { dia: string; receitas: number; despesas: number }>()
  transacoes.forEach(t => {
    const dia = t.data.slice(8, 10)
    const entry = byDay.get(dia) ?? { dia, receitas: 0, despesas: 0 }
    if (t.tipo === "RECEITA") entry.receitas += Number(t.valor)
    else entry.despesas += Number(t.valor)
    byDay.set(dia, entry)
  })
  const data = Array.from(byDay.values()).sort((a, b) => a.dia.localeCompare(b.dia))

  return (
    <Card>
      <CardHeader>
        <CardTitle>Evolução no período</CardTitle>
      </CardHeader>
      <CardContent className="h-72">
        {data.length === 0 ? (
          <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
            Sem transações no período
          </div>
        ) : (
          <ResponsiveContainer>
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="dia" stroke="hsl(var(--muted-foreground))" fontSize={12} />
              <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} tickFormatter={v => brl(Number(v)).replace("R$", "")} />
              <Tooltip formatter={(v) => brl(Number(v))} />
              <Legend />
              <Line type="monotone" dataKey="receitas" stroke="#22c55e" strokeWidth={2} />
              <Line type="monotone" dataKey="despesas" stroke="#ef4444" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  )
}
