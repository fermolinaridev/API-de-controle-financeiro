import { useEffect, useRef, useState } from "react"
import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"
import { MercadoApi } from "@/lib/api"
import type { AtivoBusca } from "@/lib/types"
import { useDebounce } from "@/lib/useDebounce"

interface Props {
  onSelect: (ativo: AtivoBusca) => void
  onQueryChange?: (valor: string) => void
  placeholder?: string
  initialValue?: string
}

/** Campo de busca de ativos da B3 com autocomplete (usa /mercado/buscar). */
export function AssetSearchInput({ onSelect, onQueryChange, placeholder = "Buscar ação (ex.: PETR4, vale)…", initialValue = "" }: Props) {
  const [query, setQuery] = useState(initialValue)
  const [results, setResults] = useState<AtivoBusca[]>([])
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const termo = useDebounce(query, 350)
  const boxRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let ativo = true
    const t = termo.trim()
    if (t.length < 2) { setResults([]); return }
    setLoading(true)
    MercadoApi.buscar(t)
      .then(r => { if (ativo) { setResults(r); setOpen(true) } })
      .catch(() => { if (ativo) setResults([]) })
      .finally(() => { if (ativo) setLoading(false) })
    return () => { ativo = false }
  }, [termo])

  useEffect(() => {
    function onClickFora(e: MouseEvent) {
      if (boxRef.current && !boxRef.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener("mousedown", onClickFora)
    return () => document.removeEventListener("mousedown", onClickFora)
  }, [])

  function aoDigitar(valor: string) {
    setQuery(valor)
    onQueryChange?.(valor)
  }

  function selecionar(a: AtivoBusca) {
    setQuery(a.ticker)
    onQueryChange?.(a.ticker)
    setOpen(false)
    onSelect(a)
  }

  return (
    <div className="relative" ref={boxRef}>
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          value={query}
          onChange={e => aoDigitar(e.target.value)}
          onFocus={() => { if (results.length) setOpen(true) }}
          placeholder={placeholder}
          className="pl-9"
        />
      </div>
      {open && (results.length > 0 || loading) && (
        <div className="absolute z-20 mt-1 w-full rounded-md border bg-card shadow-lg max-h-72 overflow-auto">
          {loading && results.length === 0 && (
            <p className="px-3 py-2 text-sm text-muted-foreground">Buscando…</p>
          )}
          {results.map(a => (
            <button
              key={a.ticker}
              type="button"
              onClick={() => selecionar(a)}
              className="flex w-full items-center gap-3 px-3 py-2 text-left text-sm hover:bg-muted"
            >
              {a.logo
                ? <img src={a.logo} alt="" className="h-6 w-6 rounded-full bg-white object-contain" />
                : <span className="h-6 w-6 rounded-full bg-muted shrink-0" />}
              <span className="font-medium">{a.ticker}</span>
              <span className="text-muted-foreground truncate">{a.nome}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
