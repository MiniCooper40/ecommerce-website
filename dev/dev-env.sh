#!/bin/bash

# Development Environment Management Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

function show_help() {
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Start the development environment"
    echo "  stop      Stop the development environment"
    echo "  restart   Restart the development environment"
    echo "  clean     Stop and remove all containers and volumes"
    echo "  status    Show status of all services"
    echo "  logs      Show logs for all services"
    echo "  minio     Open MinIO console in browser"
    echo "  test-s3   Test S3 functionality"
    echo "  help      Show this help message"
}

function start_services() {
    echo "🚀 Starting development environment..."
    docker-compose -f "$COMPOSE_FILE" up -d
    echo "✅ Development environment started!"
    echo ""
    echo "📍 Service URLs:"
    echo "   MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
    echo "   MinIO S3 API:  http://localhost:9000"
    echo "   PostgreSQL:    localhost:5432 (postgres/password)"
    echo "   Redis:         localhost:6379"
}

function stop_services() {
    echo "🛑 Stopping development environment..."
    docker-compose -f "$COMPOSE_FILE" down
    echo "✅ Development environment stopped!"
}

function restart_services() {
    stop_services
    start_services
}

function clean_all() {
    echo "🧹 Cleaning up development environment..."
    docker-compose -f "$COMPOSE_FILE" down -v --remove-orphans
    echo "✅ Development environment cleaned!"
}

function show_status() {
    echo "📊 Development environment status:"
    docker-compose -f "$COMPOSE_FILE" ps
}

function show_logs() {
    docker-compose -f "$COMPOSE_FILE" logs -f
}

function open_minio() {
    echo "🌐 Opening MinIO console..."
    if command -v xdg-open > /dev/null; then
        xdg-open http://localhost:9001
    elif command -v open > /dev/null; then
        open http://localhost:9001
    else
        echo "Please open http://localhost:9001 in your browser"
    fi
}

function test_s3() {
    echo "🧪 Testing S3 functionality..."
    echo "Make sure the catalog service is running on port 8082"
    echo ""
    echo "1. Testing single file upload URL generation..."
    curl -X POST "http://localhost:8082/api/images/upload-url?fileName=test.jpg&contentType=image/jpeg" \
        -H "Content-Type: application/json" | jq '.'
    echo ""
    echo "2. Testing bulk upload URL generation..."
    curl -X POST "http://localhost:8082/api/images/bulk-upload-urls" \
        -H "Content-Type: application/json" \
        -d '{
            "files": [
                {
                    "fileName": "test1.jpg",
                    "contentType": "image/jpeg", 
                    "fileSize": 1024000
                },
                {
                    "fileName": "test2.png",
                    "contentType": "image/png",
                    "fileSize": 2048000
                }
            ]
        }' | jq '.'
    echo ""
    echo "3. Check MinIO console at http://localhost:9001 to see if bucket exists"
}

case "${1:-help}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    clean)
        clean_all
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    minio)
        open_minio
        ;;
    test-s3)
        test_s3
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac