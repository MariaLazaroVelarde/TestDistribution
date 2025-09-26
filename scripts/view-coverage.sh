#!/bin/bash

# Script para visualizar el reporte de cobertura JaCoCo
# Uso: ./scripts/view-coverage.sh [puerto]

PORT=${1:-8080}
COVERAGE_DIR="target/site/jacoco"

echo "🔍 Verificando reporte de cobertura..."

if [ ! -d "$COVERAGE_DIR" ]; then
    echo "❌ No se encontró el reporte de cobertura."
    echo "💡 Ejecuta primero: mvn clean test jacoco:report"
    exit 1
fi

echo "✅ Reporte encontrado en: $COVERAGE_DIR"
echo "🚀 Iniciando servidor en puerto $PORT..."
echo "🌐 Abre tu navegador en: http://localhost:$PORT"
echo "📊 Reporte principal: http://localhost:$PORT/index.html"
echo ""
echo "⏹️  Presiona Ctrl+C para detener el servidor"
echo ""

# Intentar usar Python primero, luego Node.js
if command -v python3 &> /dev/null; then
    echo "🐍 Usando Python para servir el reporte..."
    cd "$COVERAGE_DIR" && python3 -m http.server "$PORT"
elif command -v node &> /dev/null; then
    echo "🟢 Usando Node.js para servir el reporte..."
    cd "$COVERAGE_DIR" && npx http-server -p "$PORT"
else
    echo "❌ No se encontró Python ni Node.js instalado."
    echo "💡 Instala Python3 o Node.js para usar este script."
    exit 1
fi
