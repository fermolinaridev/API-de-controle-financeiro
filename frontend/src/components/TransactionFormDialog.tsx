import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select } from "@/components/ui/select"
import { TransacoesApi, type TransacaoInput } from "@/lib/api"
import type { Categoria, TipoTransacao, Transacao } from "@/lib/types"

interface Props {
  categorias: Categoria[]
  onSaved: () => void
  trigger: React.ReactNode
  editing?: Transacao | null
  open?: boolean
  onOpenChange?: (o: boolean) => void
}

export function TransactionFormDialog({ categorias, onSaved, trigger, editing, open, onOpenChange }: Props) {
  const [tipo, setTipo] = useState<TipoTransacao>("DESPESA")
  const [descricao, setDescricao] = useState("")
  const [valor, setValor] = useState("")
  const [data, setData] = useState(new Date().toISOString().slice(0, 10))
  const [categoriaId, setCategoriaId] = useState<number | "">("")
  const [erro, setErro] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (editing) {
      setTipo(editing.tipo)
      setDescricao(editing.descricao)
      setValor(String(editing.valor))
      setData(editing.data)
      setCategoriaId(editing.categoriaId)
    } else {
      setTipo("DESPESA"); setDescricao(""); setValor(""); setData(new Date().toISOString().slice(0,10)); setCategoriaId("")
    }
    setErro(null)
  }, [editing, open])

  const cats = categorias.filter(c => c.tipo === tipo)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setErro(null)
    if (!categoriaId) { setErro("Selecione uma categoria"); return }
    const body: TransacaoInput = {
      descricao,
      valor: Number(valor),
      data,
      tipo,
      categoriaId: Number(categoriaId),
    }
    setSaving(true)
    try {
      if (editing) await TransacoesApi.update(editing.id, body)
      else await TransacoesApi.create(body)
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
          <DialogTitle>{editing ? "Editar transação" : "Nova transação"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={submit} className="space-y-4">
          <div className="grid grid-cols-2 gap-2">
            <Button
              type="button"
              variant={tipo === "DESPESA" ? "default" : "outline"}
              onClick={() => { setTipo("DESPESA"); setCategoriaId("") }}
            >Despesa</Button>
            <Button
              type="button"
              variant={tipo === "RECEITA" ? "default" : "outline"}
              onClick={() => { setTipo("RECEITA"); setCategoriaId("") }}
            >Receita</Button>
          </div>
          <div className="space-y-2">
            <Label>Descrição</Label>
            <Input value={descricao} onChange={e => setDescricao(e.target.value)} required maxLength={160} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label>Valor (R$)</Label>
              <Input type="number" step="0.01" min="0.01" value={valor} onChange={e => setValor(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Data</Label>
              <Input type="date" value={data} max={new Date().toISOString().slice(0,10)} onChange={e => setData(e.target.value)} required />
            </div>
          </div>
          <div className="space-y-2">
            <Label>Categoria</Label>
            <Select value={categoriaId} onChange={e => setCategoriaId(e.target.value ? Number(e.target.value) : "")} required>
              <option value="">Selecione…</option>
              {cats.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </Select>
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
