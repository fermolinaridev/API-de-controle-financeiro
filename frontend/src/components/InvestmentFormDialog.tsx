import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select } from "@/components/ui/select"
import { AssetSearchInput } from "@/components/AssetSearchInput"
import { InvestimentosApi, type InvestimentoInput } from "@/lib/api"
import type { AtivoBusca, Investimento, TipoAtivo } from "@/lib/types"

interface Props {
  onSaved: () => void
  trigger: React.ReactNode
  editing?: Investimento | null
  open?: boolean
  onOpenChange?: (o: boolean) => void
}

const classes: { value: TipoAtivo; label: string }[] = [
  { value: "ACAO", label: "Ação" },
  { value: "FII", label: "Fundo Imobiliário" },
  { value: "BDR", label: "BDR" },
  { value: "CRIPTO", label: "Cripto" },
  { value: "OUTRO", label: "Outro" },
]

function inferirClasse(tipo: string | null): TipoAtivo {
  switch (tipo) {
    case "fund": return "FII"
    case "bdr": return "BDR"
    case "stock": return "ACAO"
    default: return "ACAO"
  }
}

export function InvestmentFormDialog({ onSaved, trigger, editing, open, onOpenChange }: Props) {
  const [ticker, setTicker] = useState("")
  const [classe, setClasse] = useState<TipoAtivo>("ACAO")
  const [quantidade, setQuantidade] = useState("")
  const [precoMedio, setPrecoMedio] = useState("")
  const [erro, setErro] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (editing) {
      setTicker(editing.ticker)
      setClasse(editing.classe)
      setQuantidade(String(editing.quantidade))
      setPrecoMedio(String(editing.precoMedio))
    } else {
      setTicker(""); setClasse("ACAO"); setQuantidade(""); setPrecoMedio("")
    }
    setErro(null)
  }, [editing, open])

  function aoSelecionar(a: AtivoBusca) {
    setTicker(a.ticker)
    setClasse(inferirClasse(a.tipo))
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setErro(null)
    const t = ticker.trim().toUpperCase()
    if (!t) { setErro("Informe o ativo"); return }
    const body: InvestimentoInput = {
      ticker: t,
      classe,
      quantidade: Number(quantidade),
      precoMedio: Number(precoMedio),
    }
    setSaving(true)
    try {
      if (editing) await InvestimentosApi.update(editing.id, body)
      else await InvestimentosApi.create(body)
      onSaved()
      onOpenChange?.(false)
    } catch (e: any) {
      setErro(e.response?.data?.erro ?? "Erro ao salvar")
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{editing ? "Editar ativo" : "Adicionar ativo"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={submit} className="space-y-4">
          <div className="space-y-2">
            <Label>Ativo (ticker)</Label>
            <AssetSearchInput
              key={`${editing?.id ?? "novo"}-${open}`}
              initialValue={editing?.ticker ?? ""}
              onSelect={aoSelecionar}
              onQueryChange={setTicker}
              placeholder="Buscar ação ou digitar (ex.: PETR4, BTC)…"
            />
            <p className="text-xs text-muted-foreground">
              Busque uma ação da B3 ou digite o código manualmente (ex.: BTC para cripto).
            </p>
          </div>
          <div className="space-y-2">
            <Label>Tipo</Label>
            <Select value={classe} onChange={e => setClasse(e.target.value as TipoAtivo)}>
              {classes.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
            </Select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label>Quantidade</Label>
              <Input type="number" step="any" min="0" value={quantidade}
                     onChange={e => setQuantidade(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Preço médio (R$)</Label>
              <Input type="number" step="0.01" min="0" value={precoMedio}
                     onChange={e => setPrecoMedio(e.target.value)} required />
            </div>
          </div>
          {erro && <p className="text-sm text-destructive">{erro}</p>}
          <DialogFooter>
            <Button type="submit" disabled={saving}>{saving ? "Salvando…" : "Salvar"}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
