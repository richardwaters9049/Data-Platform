#!/bin/bash

# Start docker compose in the background
docker compose up -d

# Wait a moment for services to start
sleep 3

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "🚀 SERVICES ARE RUNNING - CLICKABLE LINKS:"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
echo "📱 Frontend:         http://localhost:5173"
echo "🔧 Backend API:      http://localhost:8081"
echo "💚 Backend Health:   http://localhost:8081/actuator/health"
echo "🐘 Postgres DB:      localhost:55432 (user: postgres, db: dataplatform)"
echo "🔴 Redis:            localhost:56379"
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
echo "To view logs, run: docker compose logs -f"
echo "To stop services, run: docker compose down"
echo ""
