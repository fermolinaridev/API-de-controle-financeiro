import { Pencil, Trash2 } from "lucide-react"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import type { Investimento } from "@/lib/types"
import { brl } from "@/lib/utils"

interface Props {
  itens: Investimento[]
  onEdit: (i: Investimento) => void
  onDelete: (i: Investimento) => void
}

const numero = (v: number) => new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 8 }).format(v)

export function PortfolioTable({ itens, onEdit, onDelete }: Props) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Meus ativos</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="text-left text-muted-foreground border-b">
              <tr>
                <th className="px-4 md:px-6 py-3 font-medium">Ativo</th>
                <th className="hidden sm:table-cell px-6 py-3 font-medium text-right">Qtd</th>
                <th className="hidden md:table-cell px-6 py-3 font-medium text-right">Preço médio</th>
                <th className="hidden md:table-cell px-6 py-3 font-medium text-right">Preço atual</th>
                <th className="px-4 md:px-6 py-3 font-medium text-right">Valor atual</th>
                <th className="px-4 md:px-6 py-3 font-medium text-right">Rendimento</th>
                <th className="px-4 md:px-6 py-3 font-medium w-20"></th>
              </tr>
            </thead>
            <tbody>
              {itens.length === 0 && (
                <tr><td colSpan={7} className="px-6 py-12 text-center text-muted-foreground">
                  Nenhum ativo na carteira. Adicione um para acompanhar o rendimento.
                </td></tr>
              )}
              {itens.map(i => {
                const positivo = (i.rendimento ?? 0) >= 0
                return (
                  <tr key={i.id} className="border-b last:border-b-0 hover:bg-muted/40">
                    <td className="px-4 md:px-6 py-3">
                      <div className="font-medium">{i.ticker}</div>
                      <div className="text-xs text-muted-foreground truncate max-w-[12rem]">
                        {i.nome ?? i.classe}
                      </div>
                      <div className="sm:hidden text-xs text-muted-foreground mt-0.5">
                        {numero(i.quantidade)} × {brl(i.precoMedio)}
                      </div>
                    </td>
                    <td className="hidden sm:table-cell px-6 py-3 text-right">{numero(i.quantidade)}</td>
                    <td className="hidden md:table-cell px-6 py-3 text-right">{brl(i.precoMedio)}</td>
                    <td className="hidden md:table-cell px-6 py-3 text-right">
                      {i.precoAtual !== null ? brl(i.precoAtual) : <span className="text-muted-foreground">—</span>}
                    </td>
                    <td className="px-4 md:px-6 py-3 text-right font-medium">
                      {i.valorAtual !== null ? brl(i.valorAtual) : brl(i.valorInvestido)}
                    </td>
                    <td className="px-4 md:px-6 py-3 text-right">
                      {i.cotacaoIndisponivel || i.rendimento === null ? (
                        <span className="text-xs text-muted-foreground">cotação indisp.</span>
                      ) : (
                        <span className={`font-semibold ${positivo ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"}`}>
                          {positivo ? "+" : "−"} {brl(Math.abs(i.rendimento))}
                          <span className="block text-xs font-normal">
                            {positivo ? "+" : ""}{(i.rendimentoPercentual ?? 0).toFixed(2)}%
                          </span>
                        </span>
                      )}
                    </td>
                    <td className="px-4 md:px-6 py-3">
                      <div className="flex gap-1 justify-end">
                        <Button variant="ghost" size="icon" onClick={() => onEdit(i)}><Pencil className="h-4 w-4" /></Button>
                        <Button variant="ghost" size="icon" onClick={() => onDelete(i)}><Trash2 className="h-4 w-4 text-destructive" /></Button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
