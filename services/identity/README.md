# Identity Service

The Identity Service provides user authentication and authorization for the CleanSlice platform. It supports both JWT-based authentication and Keycloak integration.

## Features

- User registration and authentication
- JWT token generation and validation
- Keycloak integration for external identity management
- Role-based access control
- Dual authentication provider support

## Configuration

### Authentication Provider

You can choose between two authentication providers by setting the `auth.provider` property in `application.yml`:

- `jwt`: Use the built-in JWT authentication (default)
- `keycloak`: Use Keycloak for authentication

### JWT Configuration

```yaml
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24 hours in milliseconds
```

### Keycloak Configuration

```yaml
keycloak:
  server-url: http://localhost:8080
  realm: cleanslice
  resource: files-service
  credentials:
    secret: files-service-secret
  admin-username: admin
  admin-password: admin

auth:
  provider: keycloak
```

## Keycloak Setup

1. Start Keycloak server:
   ```bash
   docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
   ```

2. Import the realm configuration:
   - Access Keycloak admin console at http://localhost:8080
   - Login with admin/admin
   - Create a new realm using the configuration from `deploy/keycloak/cleanslice-realm.json`

3. Set the authentication provider in `application.yml`:
   ```yaml
   auth:
     provider: keycloak
   ```

## API Endpoints

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### User Management

- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile

## Running the Service

### With JWT Authentication

```bash
mvn spring-boot:run
```

### With Keycloak Integration

1. Start Keycloak server
2. Import realm configuration
3. Update `application.yml` to use `auth.provider: keycloak`
4. Run the service:
   ```bash
   mvn spring-boot:run
   ```

## Testing

Run the tests:

```bash
mvn test
```

## Database

The service uses PostgreSQL database for all environments (development, testing, and production). The database connections are configured as follows:

**Development/Production:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/identitydb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

**Testing:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/identitydb_test
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

**Note:** Identity service exclusively uses PostgreSQL and does not support H2 database.

## Architecture

The service follows hexagonal architecture with:

- **Domain**: Core business logic and entities
- **Application**: Use cases and ports
- **Infrastructure**: Adapters for external systems (JPA, REST, Keycloak)