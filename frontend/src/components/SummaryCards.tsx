import { ArrowDownCircle, ArrowUpCircle, Wallet } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { brl } from "@/lib/utils"
import type { Resumo } from "@/lib/types"

export function SummaryCards({ resumo }: { resumo: Resumo | null }) {
  const items = [
    {
      label: "Receitas do mês",
      value: resumo?.totalReceitas ?? 0,
      icon: ArrowUpCircle,
      color: "text-emerald-500",
      bg: "bg-emerald-500/10",
    },
    {
      label: "Despesas do mês",
      value: resumo?.totalDespesas ?? 0,
      icon: ArrowDownCircle,
      color: "text-rose-500",
      bg: "bg-rose-500/10",
    },
    {
      label: "Saldo",
      value: resumo?.saldo ?? 0,
      icon: Wallet,
      color: resumo?.saldoNegativo ? "text-rose-500" : "text-primary",
      bg: resumo?.saldoNegativo ? "bg-rose-500/10" : "bg-primary/10",
    },
  ]
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {items.map(it => (
        <Card key={it.label}>
          <CardContent className="flex items-center justify-between p-6">
            <div>
              <p className="text-sm text-muted-foreground">{it.label}</p>
              <p className={`text-2xl font-bold mt-1 ${it.color}`}>{brl(it.value)}</p>
            </div>
            <div className={`p-3 rounded-full ${it.bg}`}>
              <it.icon className={`h-6 w-6 ${it.color}`} />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
