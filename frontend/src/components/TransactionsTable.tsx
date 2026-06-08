import { Pencil, Trash2 } from "lucide-react"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import type { Transacao } from "@/lib/types"
import { brl, formatDate } from "@/lib/utils"

interface Props {
  transacoes: Transacao[]
  onEdit: (t: Transacao) => void
  onDelete: (t: Transacao) => void
}

export function TransactionsTable({ transacoes, onEdit, onDelete }: Props) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Transações</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="text-left text-muted-foreground border-b">
              <tr>
                <th className="px-4 md:px-6 py-3 font-medium">Descrição</th>
                <th className="hidden md:table-cell px-6 py-3 font-medium">Categoria</th>
                <th className="hidden sm:table-cell px-6 py-3 font-medium">Data</th>
                <th className="px-4 md:px-6 py-3 font-medium text-right">Valor</th>
                <th className="px-4 md:px-6 py-3 font-medium w-20"></th>
              </tr>
            </thead>
            <tbody>
              {transacoes.length === 0 && (
                <tr><td colSpan={5} className="px-6 py-12 text-center text-muted-foreground">
                  Nenhuma transação no período
                </td></tr>
              )}
              {transacoes.map(t => (
                <tr key={t.id} className="border-b last:border-b-0 hover:bg-muted/40">
                  <td className="px-4 md:px-6 py-3 font-medium">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span>{t.descricao}</span>
                      {t.agendada && (
                        <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400">
                          Agendada
                        </span>
                      )}
                    </div>
                    <span className="md:hidden text-xs text-muted-foreground">
                      {t.categoriaNome} · {formatDate(t.data)}
                    </span>
                  </td>
                  <td className="hidden md:table-cell px-6 py-3">
                    <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-2 py-1 rounded-full ${
                      t.tipo === "RECEITA"
                        ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                        : "bg-rose-500/10 text-rose-600 dark:text-rose-400"
                    }`}>
                      {t.categoriaNome}
                    </span>
                  </td>
                  <td className="hidden sm:table-cell px-6 py-3 text-muted-foreground">{formatDate(t.data)}</td>
                  <td className={`px-4 md:px-6 py-3 text-right font-semibold ${
                    t.tipo === "RECEITA" ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                  }`}>
                    {t.tipo === "RECEITA" ? "+" : "−"} {brl(t.valor)}
                  </td>
                  <td className="px-4 md:px-6 py-3">
                    <div className="flex gap-1 justify-end">
                      <Button variant="ghost" size="icon" onClick={() => onEdit(t)}><Pencil className="h-4 w-4" /></Button>
                      <Button variant="ghost" size="icon" onClick={() => onDelete(t)}><Trash2 className="h-4 w-4 text-destructive" /></Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
