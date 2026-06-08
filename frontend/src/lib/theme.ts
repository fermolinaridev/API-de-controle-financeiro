export type Theme = "light" | "dark"
const KEY = "financas.theme"

export function getInitialTheme(): Theme {
  const stored = localStorage.getItem(KEY) as Theme | null
  if (stored === "light" || stored === "dark") return stored
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle("dark", theme === "dark")
  localStorage.setItem(KEY, theme)
}
