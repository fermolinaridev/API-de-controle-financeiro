import { Wallet, LayoutDashboard, TrendingUp, LogOut, Menu, X } from "lucide-react"
import { useEffect, useState, type ReactNode } from "react"
import { NavLink } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ThemeToggle } from "@/components/ThemeToggle"
import { auth } from "@/lib/auth"
import { cn } from "@/lib/utils"

const navItems = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/investimentos", label: "Investimentos", icon: TrendingUp },
]

export function Layout({ children, onLogout }: { children: ReactNode; onLogout?: () => void }) {
  const user = auth.getUser()
  const [open, setOpen] = useState(false)

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : ""
    return () => { document.body.style.overflow = "" }
  }, [open])

  const sidebarContent = (
    <>
      <div className="flex items-center justify-between px-6 py-5 border-b">
        <div className="flex items-center gap-2">
          <Wallet className="h-6 w-6 text-primary" />
          <span className="font-semibold text-lg">Finanças</span>
        </div>
        <Button variant="ghost" size="icon" className="md:hidden" onClick={() => setOpen(false)}>
          <X className="h-5 w-5" />
        </Button>
      </div>
      <nav className="flex-1 p-3 space-y-1">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/"}
            onClick={() => setOpen(false)}
            className={({ isActive }) => cn(
              "flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors",
              isActive
                ? "bg-primary/10 text-primary"
                : "text-muted-foreground hover:bg-muted hover:text-foreground"
            )}
          >
            <Icon className="h-4 w-4" />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t space-y-2">
        {user && (
          <div className="text-xs">
            <p className="font-medium truncate">{user.nome}</p>
            <p className="text-muted-foreground truncate">{user.email}</p>
          </div>
        )}
        {onLogout && (
          <Button variant="outline" size="sm" className="w-full" onClick={onLogout}>
            <LogOut className="h-4 w-4" /> Sair
          </Button>
        )}
      </div>
    </>
  )

  return (
    <div className="flex min-h-screen bg-muted/30">
      {/* Sidebar desktop */}
      <aside className="hidden md:flex w-60 flex-col border-r bg-card">
        {sidebarContent}
      </aside>

      {/* Drawer mobile */}
      <div
        className={cn(
          "fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden transition-opacity",
          open ? "opacity-100" : "opacity-0 pointer-events-none"
        )}
        onClick={() => setOpen(false)}
      />
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 w-64 flex flex-col bg-card border-r md:hidden transition-transform",
          open ? "translate-x-0" : "-translate-x-full"
        )}
      >
        {sidebarContent}
      </aside>

      <main className="flex-1 flex flex-col min-w-0">
        {/* Topbar mobile */}
        <div className="md:hidden flex items-center justify-between border-b bg-card px-4 py-3">
          <Button variant="ghost" size="icon" onClick={() => setOpen(true)} aria-label="Abrir menu">
            <Menu className="h-5 w-5" />
          </Button>
          <div className="flex items-center gap-2">
            <Wallet className="h-5 w-5 text-primary" />
            <span className="font-semibold">Finanças</span>
          </div>
          <ThemeToggle />
        </div>

        {/* Topbar desktop só com toggle de tema */}
        <div className="hidden md:flex items-center justify-end px-8 py-3 border-b bg-card">
          <ThemeToggle />
        </div>

        <div className="flex-1 p-4 md:p-8 overflow-auto">{children}</div>
      </main>
    </div>
  )
}
