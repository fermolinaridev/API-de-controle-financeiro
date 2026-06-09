import { Search, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select } from "@/components/ui/select"
import type { Categoria, TipoTransacao } from "@/lib/types"

const MESES = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"]

export type FilterMode = "mes" | "intervalo"

export interface FiltersState {
  mode: FilterMode
  mes: number
  ano: number
  dataInicio: string
  dataFim: string
  categoriaId: number | ""
  tipo: TipoTransacao | ""
  busca: string
}

export function Filters({
  state, setState, categorias, anos, onReset,
}: {
  state: FiltersState
  setState: (s: FiltersState) => void
  categorias: Categoria[]
  anos: number[]
  onReset: () => void
}) {
  const update = (patch: Partial<FiltersState>) => setState({ ...state, ...patch })
  const hasCustom =
    state.categoriaId !== "" || state.tipo !== "" || state.busca !== "" ||
    state.mode === "intervalo"

  return (
    <div className="space-y-3 rounded-xl border bg-card p-4">
      {/* linha 1: busca + reset */}
      <div className="flex gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Buscar por descrição…"
            value={state.busca}
            onChange={e => update({ busca: e.target.value })}
          />
        </div>
        {hasCustom && (
          <Button variant="outline" onClick={onReset}>
            <X className="h-4 w-4" /> Limpar
          </Button>
        )}
      </div>

      {/* linha 2: toggle mes/intervalo */}
      <div className="flex gap-2">
        <Button
          size="sm"
          variant={state.mode === "mes" ? "default" : "outline"}
          onClick={() => update({ mode: "mes", dataInicio: "", dataFim: "" })}
        >
          Por mês
        </Button>
        <Button
          size="sm"
          variant={state.mode === "intervalo" ? "default" : "outline"}
          onClick={() => update({ mode: "intervalo" })}
        >
          Intervalo personalizado
        </Button>
      </div>

      {/* linha 3: campos de período */}
      {state.mode === "mes" ? (
        <div className="grid grid-cols-2 md:flex gap-3">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Mês</label>
            <Select value={state.mes} onChange={e => update({ mes: Number(e.target.value) })}>
              {MESES.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
            </Select>
          </div>
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Ano</label>
            <Select value={state.ano} onChange={e => update({ ano: Number(e.target.value) })}>
              {anos.map(a => <option key={a} value={a}>{a}</option>)}
            </Select>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-2 md:flex gap-3">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">De</label>
            <Input type="date" value={state.dataInicio} onChange={e => update({ dataInicio: e.target.value })} />
          </div>
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Até</label>
            <Input type="date" value={state.dataFim} onChange={e => update({ dataFim: e.target.value })} />
          </div>
        </div>
      )}

      {/* linha 4: tipo + categoria */}
      <div className="grid grid-cols-2 md:flex gap-3">
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">Tipo</label>
          <Select value={state.tipo} onChange={e => update({ tipo: e.target.value as TipoTransacao | "" })}>
            <option value="">Todos</option>
            <option value="RECEITA">Receita</option>
            <option value="DESPESA">Despesa</option>
          </Select>
        </div>
        <div className="space-y-1 col-span-2 md:flex-1 md:min-w-48">
          <label className="text-xs text-muted-foreground">Categoria</label>
          <Select
            value={state.categoriaId}
            onChange={e => update({ categoriaId: e.target.value ? Number(e.target.value) : "" })}
          >
            <option value="">Todas</option>
            {categorias.map(c => <option key={c.id} value={c.id}>{c.nome} · {c.tipo}</option>)}
          </Select>
        </div>
      </div>
    </div>
  )
}
