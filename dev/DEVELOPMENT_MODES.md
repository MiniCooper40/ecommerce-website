# Development Environment Modes

This document explains the different development environment configurations available and when to use each one.

## 📁 File Structure

```
dev/
├── docker-compose.yml           # DEFAULT: Infrastructure only (DEV mode)
├── docker-compose-dev.yml       # Explicit infrastructure only
├── docker-compose-full.yml      # All services in containers
├── docker-compose-minio-only.yml # MinIO only
├── dev-env.ps1                  # Windows management script
├── dev-env.sh                   # Linux/Mac management script
└── README.md                    # Original documentation
```

## 🚀 Development Modes

### DEV Mode (Default) - Infrastructure Only

**Best for**: Daily development, debugging, fast iteration

**What runs in containers:**

- PostgreSQL (Database)
- Redis (Cache)
- MinIO (S3 Storage)
- Kafka + Zookeeper (Events)
- Kafka UI (Monitoring)

**What runs natively:**

- All Spring Boot services (Eureka, Security, Gateway, Catalog, Cart, Order)

**Advantages:**

- ⚡ Fast startup and restart of services
- 🐛 Easy debugging with IDE
- 🔄 Hot reload and instant changes
- 💾 Lower memory usage
- 🔧 Full IDE integration

### FULL Mode - All Services in Containers

**Best for**: Integration testing, production-like environment, CI/CD

**What runs in containers:**

- All infrastructure services
- All Spring Boot services
- Complete service mesh with health checks

**Advantages:**

- 🏗️ Production-like environment
- 🔗 Service discovery and networking exactly like production
- 🧪 Full integration testing
- 📦 Consistent environment across team

## 💻 Usage Examples

### DEV Mode (Infrastructure Only)

```bash
# Start infrastructure
./dev-env.sh start
# or
./dev-env.ps1 start

# Run services natively (in separate terminals)
cd backend/services/eureka-server && mvn spring-boot:run
cd backend/services/security-service && mvn spring-boot:run
cd backend/services/gateway && mvn spring-boot:run
cd backend/services/catalog-service && mvn spring-boot:run
cd backend/services/cart-service && mvn spring-boot:run
cd backend/services/order-service && mvn spring-boot:run
```

### FULL Mode (All Services)

```bash
# Option 1: Using full command
./dev-env.sh full
./dev-env.ps1 full

# Option 2: Using flag
./dev-env.sh start --full
./dev-env.ps1 start -Full

# Option 3: Direct compose file
docker-compose -f docker-compose-full.yml up -d
```

### MinIO Only (Minimal)

```bash
# For S3 testing only
docker-compose -f docker-compose-minio-only.yml up -d
```

## 🔧 Configuration Profiles

Both modes use the same Spring application configurations but different profiles:

### DEV Mode Configuration

Spring services use **default profile** when running natively:

- Connect to `localhost:5432` (PostgreSQL)
- Connect to `localhost:6379` (Redis)
- Connect to `localhost:9092` (Kafka)
- Connect to `localhost:9000` (MinIO)
- Connect to `localhost:8761` (Eureka)

### FULL Mode Configuration

Spring services use **docker profile** when in containers:

- Connect to `postgres:5432`
- Connect to `redis:6379`
- Connect to `kafka:9093`
- Connect to `minio:9000`
- Connect to `eureka-server:8761`

## 📊 Performance Comparison

| Aspect              | DEV Mode                                     | FULL Mode              |
| ------------------- | -------------------------------------------- | ---------------------- |
| **Startup Time**    | ~30s infrastructure + instant service starts | ~2-3 minutes           |
| **Memory Usage**    | ~2GB containers + native JVMs                | ~4-6GB all containers  |
| **Service Restart** | 1-2 seconds                                  | 30-60 seconds          |
| **Debugging**       | Full IDE support                             | Limited (remote debug) |
| **Hot Reload**      | Yes (with spring-boot-devtools)              | No                     |

## 🎯 When to Use Each Mode

### Use DEV Mode When:

- ✅ Daily development work
- ✅ Debugging application logic
- ✅ Writing new features
- ✅ Testing individual services
- ✅ Working on frontend integration
- ✅ Quick iterations needed

### Use FULL Mode When:

- ✅ Integration testing
- ✅ Testing service discovery
- ✅ Testing container networking
- ✅ Preparing for deployment
- ✅ Demonstrating to stakeholders
- ✅ CI/CD pipeline testing

### Use MinIO Only When:

- ✅ Testing S3 image upload functionality
- ✅ Developing catalog service features
- ✅ Working on frontend image handling

## 🛠️ Management Commands

### Common Commands (Both Modes)

```bash
# Start environment
./dev-env.sh start           # DEV mode
./dev-env.sh start --full    # FULL mode
./dev-env.sh full           # FULL mode shortcut

# Monitor and manage
./dev-env.sh status         # Show running containers
./dev-env.sh logs           # Follow all logs
./dev-env.sh minio          # Open MinIO console
./dev-env.sh test-s3        # Test S3 functionality

# Cleanup
./dev-env.sh stop           # Stop containers
./dev-env.sh clean          # Remove containers and volumes
```

### PowerShell (Windows)

```powershell
# Same commands with .ps1 extension
.\dev-env.ps1 start
.\dev-env.ps1 start -Full
.\dev-env.ps1 full
.\dev-env.ps1 status
.\dev-env.ps1 clean
```

## 🔗 Service URLs

### Infrastructure (Available in Both Modes)

- **MinIO Console**: http://localhost:9001 (`minioadmin/minioadmin`)
- **MinIO S3 API**: http://localhost:9000
- **PostgreSQL**: localhost:5432 (`postgres/password`)
- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **Kafka UI**: http://localhost:8085

### Applications (DEV Mode - Run Natively)

- **Eureka**: http://localhost:8761
- **Security**: http://localhost:8081
- **Gateway**: http://localhost:8080
- **Catalog**: http://localhost:8082
- **Order**: http://localhost:8083
- **Cart**: http://localhost:8084

### Applications (FULL Mode - In Containers)

Same URLs as above, but services run in containers with proper health checks and dependencies.

## 🚨 Troubleshooting

### DEV Mode Issues

- **Port conflicts**: Make sure no other services are running on the application ports
- **Native service won't start**: Check if infrastructure is running first
- **Database connection**: Verify PostgreSQL is accessible on localhost:5432

### FULL Mode Issues

- **Slow startup**: Wait for health checks to pass (check with `docker-compose ps`)
- **Service dependency failures**: Check logs with `./dev-env.sh logs [service-name]`
- **Memory issues**: FULL mode requires more resources

### General Issues

- **Docker daemon**: Ensure Docker is running
- **Port conflicts**: Check if ports 5432, 6379, 9000, 9001, 9092 are available
- **File permissions**: Ensure scripts are executable (`chmod +x dev-env.sh`)

## 💡 Pro Tips

1. **DEV Mode for Development**: Use DEV mode for 90% of development work
2. **FULL Mode for Testing**: Switch to FULL mode before committing major changes
3. **Mixed Mode**: You can run some services natively and others in containers by modifying the compose files
4. **IDE Integration**: Configure your IDE to use the DEV mode ports for debugging
5. **Database Persistence**: Data persists between restarts unless you use `clean` command

## 🔄 Migration Between Modes

To switch from one mode to another:

```bash
# Stop current mode
./dev-env.sh stop

# Start different mode
./dev-env.sh start        # DEV mode
./dev-env.sh start --full # FULL mode
```

Data in PostgreSQL, Redis, and MinIO will persist across mode switches unless you use the `clean` command.
