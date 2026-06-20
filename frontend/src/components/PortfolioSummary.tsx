import { Wallet, LineChart, TrendingUp, TrendingDown } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { brl } from "@/lib/utils"
import type { CarteiraResumo } from "@/lib/types"

export function PortfolioSummary({ resumo }: { resumo: CarteiraResumo | null }) {
  const rendimento = resumo?.rendimentoTotal ?? 0
  const percentual = resumo?.rendimentoPercentualTotal ?? 0
  const positivo = rendimento >= 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <Card>
        <CardContent className="flex items-center justify-between p-6">
          <div>
            <p className="text-sm text-muted-foreground">Total investido</p>
            <p className="text-2xl font-bold mt-1">{brl(resumo?.totalInvestido ?? 0)}</p>
          </div>
          <div className="p-3 rounded-full bg-primary/10"><Wallet className="h-6 w-6 text-primary" /></div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="flex items-center justify-between p-6">
          <div>
            <p className="text-sm text-muted-foreground">Valor atual</p>
            <p className="text-2xl font-bold mt-1">{brl(resumo?.valorAtualTotal ?? 0)}</p>
          </div>
          <div className="p-3 rounded-full bg-primary/10"><LineChart className="h-6 w-6 text-primary" /></div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="flex items-center justify-between p-6">
          <div>
            <p className="text-sm text-muted-foreground">Rendimento</p>
            <p className={`text-2xl font-bold mt-1 ${positivo ? "text-emerald-500" : "text-rose-500"}`}>
              {positivo ? "+" : "−"} {brl(Math.abs(rendimento))}
            </p>
            <p className={`text-sm ${positivo ? "text-emerald-500" : "text-rose-500"}`}>
              {positivo ? "+" : ""}{percentual.toFixed(2)}%
            </p>
          </div>
          <div className={`p-3 rounded-full ${positivo ? "bg-emerald-500/10" : "bg-rose-500/10"}`}>
            {positivo
              ? <TrendingUp className="h-6 w-6 text-emerald-500" />
              : <TrendingDown className="h-6 w-6 text-rose-500" />}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
