import { useState } from "react"
import { Pencil, Trash2, Check, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select } from "@/components/ui/select"
import { CategoriasApi } from "@/lib/api"
import type { Categoria, TipoTransacao } from "@/lib/types"

interface Props {
  categorias: Categoria[]
  onChanged: () => void
  trigger: React.ReactNode
}

export function CategoriesDialog({ categorias, onChanged, trigger }: Props) {
  const [nome, setNome] = useState("")
  const [tipo, setTipo] = useState<TipoTransacao>("DESPESA")
  const [saving, setSaving] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editNome, setEditNome] = useState("")
  const [editTipo, setEditTipo] = useState<TipoTransacao>("DESPESA")

  async function add(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      await CategoriasApi.create({ nome, tipo })
      setNome("")
      onChanged()
    } catch (e: any) {
      alert(e.response?.data?.erro ?? "Erro ao criar")
    } finally {
      setSaving(false)
    }
  }

  function startEdit(c: Categoria) {
    setEditingId(c.id); setEditNome(c.nome); setEditTipo(c.tipo)
  }

  async function saveEdit(id: number) {
    try {
      await CategoriasApi.update(id, { nome: editNome, tipo: editTipo })
      setEditingId(null)
      onChanged()
    } catch (e: any) {
      alert(e.response?.data?.erro ?? "Erro ao salvar")
    }
  }

  async function remove(c: Categoria) {
    if (!confirm(`Excluir "${c.nome}"?`)) return
    try {
      await CategoriasApi.remove(c.id)
      onChanged()
    } catch (e: any) {
      alert(e.response?.data?.erro ?? "Erro ao excluir")
    }
  }

  return (
    <Dialog>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Categorias</DialogTitle>
        </DialogHeader>

        <form onSubmit={add} className="space-y-3 border-b pb-4">
          <div className="grid grid-cols-2 gap-2">
            <Button type="button" variant={tipo === "DESPESA" ? "default" : "outline"} onClick={() => setTipo("DESPESA")}>Despesa</Button>
            <Button type="button" variant={tipo === "RECEITA" ? "default" : "outline"} onClick={() => setTipo("RECEITA")}>Receita</Button>
          </div>
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input value={nome} onChange={e => setNome(e.target.value)} required maxLength={80} />
          </div>
          <Button type="submit" disabled={saving || !nome}>{saving ? "Adicionando…" : "Adicionar categoria"}</Button>
        </form>

        <div className="max-h-80 overflow-auto space-y-3">
          {(["RECEITA", "DESPESA"] as TipoTransacao[]).map(t => (
            <div key={t}>
              <p className="text-xs font-semibold text-muted-foreground mb-1">{t}</p>
              <div className="space-y-1">
                {categorias.filter(c => c.tipo === t).map(c => (
                  <div key={c.id} className="flex items-center gap-2 px-3 py-2 rounded bg-muted/50 text-sm">
                    {editingId === c.id ? (
                      <>
                        <Input className="h-8 flex-1" value={editNome} onChange={e => setEditNome(e.target.value)} />
                        <Select className="h-8 w-32" value={editTipo} onChange={e => setEditTipo(e.target.value as TipoTransacao)}>
                          <option value="RECEITA">Receita</option>
                          <option value="DESPESA">Despesa</option>
                        </Select>
                        <Button size="icon" variant="ghost" onClick={() => saveEdit(c.id)}><Check className="h-4 w-4 text-emerald-600" /></Button>
                        <Button size="icon" variant="ghost" onClick={() => setEditingId(null)}><X className="h-4 w-4" /></Button>
                      </>
                    ) : (
                      <>
                        <span className="flex-1">{c.nome}</span>
                        <Button size="icon" variant="ghost" onClick={() => startEdit(c)}><Pencil className="h-4 w-4" /></Button>
                        <Button size="icon" variant="ghost" onClick={() => remove(c)}><Trash2 className="h-4 w-4 text-destructive" /></Button>
                      </>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  )
}
