#!/bin/bash
# Atalho clicável: sobe backend + frontend e abre o navegador.
# Dois cliques nesse arquivo (Finder) basta. Cmd+C em cada aba do Terminal para parar.

set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Mata o que estiver ocupando as portas (evita "Address already in use")
lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null || true
lsof -ti:5173 2>/dev/null | xargs kill -9 2>/dev/null || true

# Abre duas abas no Terminal: backend e frontend
osascript <<EOF
tell application "Terminal"
    activate
    do script "cd '$PROJECT_DIR' && echo '🔧 Backend (Spring Boot)…' && ./mvnw spring-boot:run"
    delay 1
    tell application "System Events" to keystroke "t" using {command down}
    delay 0.5
    do script "cd '$PROJECT_DIR/frontend' && echo '🎨 Frontend (Vite)…' && [ -d node_modules ] || npm install && npm run dev" in front window
end tell
EOF

# Espera o frontend responder e abre o navegador
echo "Aguardando frontend subir…"
for i in {1..60}; do
    if curl -sf -o /dev/null http://localhost:5173/; then
        open "http://localhost:5173"
        echo "✅ Aberto em http://localhost:5173"
        exit 0
    fi
    sleep 1
done

echo "⚠️  Frontend não respondeu em 60s. Verifique as abas do Terminal."
exit 1
