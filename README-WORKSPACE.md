# E-commerce Multi-Project Workspace

This workspace is configured as a multi-project setup for VS Code with proper Java language server support for all microservices.

## Project Structure

### Backend Services (Java/Spring Boot)
- **Cart Service** - Shopping cart management
- **Catalog Service** - Product catalog management  
- **Order Service** - Order processing
- **Security Service** - Authentication and authorization
- **Gateway** - API Gateway
- **Eureka Server** - Service discovery

### Shared Libraries
- **Test Utils** - Common testing utilities

### Frontend
- **React/TypeScript** - Frontend application

## Opening the Workspace

1. **Recommended**: Open the workspace file directly:
   ```
   File > Open Workspace from File > ecommerce-workspace.code-workspace
   ```

2. **Alternative**: Open the root folder and VS Code will recognize the multi-project structure

## Features

### Java Language Support
- Each Java project is recognized as a separate Maven project
- Proper classpath resolution across all services
- IntelliSense and auto-completion work correctly
- No weird import behavior between projects
- Shared dependencies managed through parent POM

### Build Tasks
Available through `Ctrl+Shift+P` > "Tasks: Run Task":
- **Clean All Projects** - Clean all Java projects
- **Install All Projects** - Build and install all Java projects
- **Test All Projects** - Run tests for all Java projects
- **Compile All Projects** - Compile all Java projects
- Individual service build tasks for each microservice
- **Build Frontend** - Build the React application
- **Start Frontend Dev Server** - Start development server

### Debug Configurations
Pre-configured debug launches for each service:
- Debug Eureka Server
- Debug Gateway
- Debug Cart Service
- Debug Catalog Service
- Debug Order Service
- Debug Security Service

### Project Navigation
The Explorer panel shows each project separately:
- Frontend
- Cart Service
- Catalog Service
- Eureka Server
- Gateway
- Order Service
- Security Service
- Test Utils (Shared)
- Root

## Development Workflow

1. **Initial Setup**:
   ```bash
   # In the backend directory
   mvn clean install
   ```

2. **Service Development**:
   - Each service can be built independently
   - Shared test utilities are available to all services
   - Cross-service dependencies are properly resolved

3. **Frontend Development**:
   ```bash
   # In the frontend directory
   npm install
   npm run dev
   ```

## VS Code Extensions

Recommended extensions (will be suggested when opening the workspace):
- Extension Pack for Java
- Maven for Java
- Java Language Support
- Java Test Runner
- Java Debugger

## Maven Configuration

- Parent POM manages dependency versions
- All services inherit from the parent POM
- Shared test utilities available as a dependency
- Spring Boot and Spring Cloud versions centrally managed

## Troubleshooting

### If Java imports are not working:
1. Reload the window: `Ctrl+Shift+P` > "Developer: Reload Window"
2. Clean workspace: `Ctrl+Shift+P` > "Java: Clean Workspace"
3. Rebuild projects: Run "Install All Projects" task

### If projects are not recognized:
1. Check that each service has a `pom.xml` file
2. Ensure the workspace file includes all project folders
3. Reload VS Code window