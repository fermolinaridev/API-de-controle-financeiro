import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import type { Transacao } from "@/lib/types"
import { brl } from "@/lib/utils"

const COLORS = ["#a855f7", "#22c55e", "#f97316", "#ef4444", "#3b82f6", "#eab308", "#06b6d4", "#ec4899"]

export function CategoryPieChart({ transacoes }: { transacoes: Transacao[] }) {
  const despesas = transacoes.filter(t => t.tipo === "DESPESA")
  const map = new Map<string, number>()
  despesas.forEach(t => map.set(t.categoriaNome, (map.get(t.categoriaNome) ?? 0) + Number(t.valor)))
  const data = Array.from(map.entries()).map(([name, value]) => ({ name, value }))

  return (
    <Card>
      <CardHeader>
        <CardTitle>Despesas por categoria</CardTitle>
      </CardHeader>
      <CardContent className="h-72">
        {data.length === 0 ? (
          <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
            Sem despesas no período
          </div>
        ) : (
          <ResponsiveContainer>
            <PieChart>
              <Pie data={data} dataKey="value" nameKey="name" innerRadius={50} outerRadius={90} paddingAngle={2}>
                {data.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(v) => brl(Number(v))} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  )
}
