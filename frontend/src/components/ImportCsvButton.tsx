import { useRef, useState } from "react"
import { Upload } from "lucide-react"
import { Button } from "@/components/ui/button"
import { TransacoesApi } from "@/lib/api"

export function ImportCsvButton({ onImported }: { onImported: () => void }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)

  async function handle(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const r = await TransacoesApi.importarCsv(file)
      const msg = `${r.importadas} transação(ões) importada(s). Falhas: ${r.falhas}` +
                  (r.falhas > 0 ? `\n\nErros:\n${r.erros.join("\n")}` : "")
      alert(msg)
      onImported()
    } catch (e: any) {
      alert(e.response?.data?.erro ?? "Falha ao importar")
    } finally {
      setUploading(false)
      if (inputRef.current) inputRef.current.value = ""
    }
  }

  return (
    <>
      <input ref={inputRef} type="file" accept=".csv,text/csv" className="hidden" onChange={handle} />
      <Button variant="outline" onClick={() => inputRef.current?.click()} disabled={uploading}>
        <Upload className="h-4 w-4" /> {uploading ? "Importando…" : "Importar CSV"}
      </Button>
    </>
  )
}
