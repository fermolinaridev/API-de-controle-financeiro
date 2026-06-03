#!/bin/bash
# Encerra backend (8080) e frontend (5173).

killed=0
for port in 8080 5173; do
    pids=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pids" ]; then
        echo "$pids" | xargs kill -9 2>/dev/null
        echo "🛑 Porta $port encerrada"
        killed=1
    else
        echo "✅ Porta $port já estava livre"
    fi
done

[ $killed -eq 1 ] && echo "Tudo parado." || echo "Nada estava rodando."
sleep 2
