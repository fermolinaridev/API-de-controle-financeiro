import { useState } from "react"
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

  async function add(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      await CategoriasApi.create({ nome, tipo })
      setNome("")
      onChanged()
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
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

        <div className="max-h-64 overflow-auto space-y-1">
          {(["RECEITA", "DESPESA"] as TipoTransacao[]).map(t => (
            <div key={t}>
              <p className="text-xs font-semibold text-muted-foreground mt-2">{t}</p>
              {categorias.filter(c => c.tipo === t).map(c => (
                <div key={c.id} className="px-3 py-2 rounded bg-muted/50 text-sm">{c.nome}</div>
              ))}
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  )
}

export { Select }
