import { Wallet, LayoutDashboard } from "lucide-react"
import type { ReactNode } from "react"

export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="hidden md:flex w-60 flex-col border-r bg-card">
        <div className="flex items-center gap-2 px-6 py-5 border-b">
          <Wallet className="h-6 w-6 text-primary" />
          <span className="font-semibold text-lg">Finanças</span>
        </div>
        <nav className="flex-1 p-3 space-y-1">
          <a className="flex items-center gap-3 px-3 py-2 rounded-md bg-primary/10 text-primary font-medium">
            <LayoutDashboard className="h-4 w-4" />
            Dashboard
          </a>
        </nav>
        <div className="p-4 text-xs text-muted-foreground border-t">v0.1 · dev</div>
      </aside>
      <main className="flex-1 p-6 md:p-8 overflow-auto">{children}</main>
    </div>
  )
}
