# ASH – Spring Boot Multi-Datasource Training Project

Spring Boot 3 · Java 21 · Oracle + PostgreSQL · JWT RS256 + Keycloak · Redis · Kafka · MapStruct

---

## Tech Stack

| | |
|-|-|
| Framework | Spring Boot 3.5.7 |
| Security | Spring Security 6, JWT RS256 (JJWT 0.12.3), Keycloak 24 |
| ORM | Spring Data JPA / Hibernate, HikariCP |
| Cache | Redis |
| Messaging | Kafka |
| HTTP Client | OpenFeign |
| Mapping | MapStruct 1.5.5 · Lombok 1.18.30 |

---

## Quick Start

```bash
# Start all infrastructure
docker compose up -d

# Build & run app
./mvnw spring-boot:run
```

> Oracle needs manual user setup after first boot — see [Oracle setup](#oracle-setup).

---

## Project Structure

```
src/main/
├── resources/
│   ├── application.properties   # server, Redis, Kafka, JWT, Keycloak
│   ├── application.yml          # imports config/datasource.yml
│   ├── config/datasource.yml    # DB URLs + HikariCP pool (single source of truth)
│   ├── keys/private.pem         # RSA-2048 private key – sign tokens (never commit in prod)
│   ├── keys/public.pem          # RSA-2048 public key  – verify tokens
│   └── keycloak/ash-realm.json  # Keycloak realm auto-import (realm ash, client ash-mobile)
│
└── java/org/example/ash/
    ├── aop/              # @ValidateProduct, @Signature aspects
    ├── client/           # Feign → ServiceB
    ├── config/
    │   ├── datasource/
    │   │   ├── BaseJpaConfig.java          # abstract: buildDataSource/EMF/TM + dialectFor()
    │   │   ├── OracleJpaConfig.java        # @Primary, @MultiDataSource(ORACLE)
    │   │   ├── PostgresJpaConfig.java      # @MultiDataSource(POSTGRESQL)
    │   │   ├── annotation/MultiDataSource.java
    │   │   └── config/*ConnectionPoolConfig.java
    │   ├── DynamicDataSourceConfig.java    # oracleJdbcTemplate / postgresJdbcTemplate beans
    │   └── RedisConfig.java
    ├── configuration/    # GlobalExceptionHandler, RequestContext, WebConfig
    ├── controller/       # AuthController, CategoryController, ProductController
    ├── dto/              # request/ · response/ (BaseResponse, LoginResponse)
    ├── entity/oracle/    # Product, Category, User, Role (scanned by OracleJpaConfig)
    ├── exception/        # AppException, AppCode, ThrottleException…
    ├── mapper/           # EntityMapper<D,E,R>, CategoryMapper
    ├── repository/oracle/ # ICategoryRepo, IProductRepo, IUserRepo
    ├── security/
    │   ├── PasswordEncoderConfig.java       # BCrypt bean (isolated to break circular dep)
    │   ├── RsaKeyConfig.java                # loads RSAPrivateKey + RSAPublicKey from PEM
    │   ├── JwtTokenProvider.java            # generate (private) / verify (public)
    │   ├── JwtAuthenticationFilter.java     # filter ①: local RS256 token (web)
    │   ├── SecurityConfig.java              # filter chain, auth rules
    │   └── keycloak/
    │       ├── KeycloakConfig.java                     # NimbusJwtDecoder via JWKS
    │       ├── KeycloakJwtAuthenticationConverter.java # realm_access + resource_access → GrantedAuthority
    │       └── KeycloakAuthenticationFilter.java       # filter ②: Keycloak token (mobile)
    └── service/          # AuthService, CategoryService, ProductService, CacheService
```

---

## Authentication

Two providers, one filter chain, same endpoints:

| Client | Flow |
|--------|------|
| **Web** | `POST /auth/login` → app signs RS256 JWT with `keys/private.pem` |
| **Mobile** | Keycloak password/PKCE grant → app verifies via Keycloak JWKS |

**Filter order:**
```
① JwtAuthenticationFilter     – local token?  → set SecurityContext, done
② KeycloakAuthenticationFilter – already auth? → skip; else verify via Keycloak JWKS
```

Both filters skip `/auth/**`, `/actuator/**`, `/health` via `shouldNotFilter()`.

**Access rules:**

| Path | Access |
|------|--------|
| `/auth/**`, `/health`, `/actuator/**` | Public |
| `DELETE /**` | `ROLE_ADMIN` |
| everything else | authenticated |

**Regenerate RSA keys:**
```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/keys/private.pem
openssl rsa -pubout -in src/main/resources/keys/private.pem -out src/main/resources/keys/public.pem
```

**Keycloak test users** (auto-imported from `ash-realm.json`):

| Username | Password | Roles |
|----------|----------|-------|
| `mobile_user` | `mobile123` | `user` |
| `mobile_admin` | `admin123` | `user`, `admin` |

---

## API Endpoints

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/auth/register` | public | returns JWT |
| POST | `/auth/login` | public | returns JWT |
| GET | `/categories` | ✓ | |
| GET | `/categories/{id}` | ✓ | |
| POST | `/categories` | ✓ | |
| GET | `/products` | ✓ | |
| GET | `/products/{id}` | ✓ | Redis cached |
| POST | `/products` | ✓ | AOP name validation |
| DELETE | `/products` | ADMIN | cache evicted |
| POST | `/products/dynamic-db` | ✓ | Oracle JdbcTemplate |

**Example:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret"}' | jq -r '.data.token')

curl http://localhost:8080/categories -H "Authorization: Bearer $TOKEN"

# Mobile – Keycloak token
TOKEN=$(curl -s -X POST http://localhost:8180/realms/ash/protocol/openid-connect/token \
  -d "grant_type=password&client_id=ash-mobile&username=mobile_admin&password=admin123" \
  | jq -r '.access_token')

curl http://localhost:8080/categories -H "Authorization: Bearer $TOKEN"
```

---

## Infrastructure (docker-compose)

| Container | Port | Credentials |
|-----------|------|-------------|
| `ash-oracle` | 1521 | SYS/SYSTEM: `demo_pass` |
| `ash-postgres` | 5432 | `demo_user` / `demo_pass` / `demo_db` |
| `ash-redis` | 6379 | — |
| `ash-kafka` | 9092 | — |
| `ash-keycloak` | 8180 | admin / admin |
| `ash-keycloak-postgres` | — (internal) | `keycloak_user` / `keycloak_pass` |

### Oracle setup

```bash
docker exec -it ash-oracle sqlplus sys/demo_pass@FREEPDB1 as sysdba
```
```sql
CREATE USER demo_user IDENTIFIED BY demo_pass;
GRANT CONNECT, RESOURCE TO demo_user;
ALTER USER demo_user QUOTA UNLIMITED ON USERS;
```

---

## Multi-Datasource Config

```
application.properties  → imports config/datasource.yml
config/datasource.yml   → custom.multi-databases.{oracle,postgres}  +  datasource.pool.{oracle,postgres}

BaseJpaConfig (abstract)
├── OracleJpaConfig   @Primary  → repository.oracle  / entity.oracle
└── PostgresJpaConfig           → repository.postgres / entity.postgres

DynamicDataSourceConfig → @Bean oracleJdbcTemplate / postgresJdbcTemplate
```

---

## Mapper Contract

```
EntityMapper<D,E,R>  (D=DTO · E=Entity · R=Request)

toDto(E)→D   toEntity(R)→E   fromDto(D)→E
toListDto / toListEntity
toDto(Optional<E>) / toEntity(Optional<R>) / fromDto(Optional<D>)
partialUpdate(@MappingTarget E, D)   ← null-safe merge
```

---

## BaseResponse

```json
{ "status": 200, "data": {…} }
{ "status": 400, "error": "message" }
```
Factories: `BaseResponse.ok(data)` · `.created(data)` · `.badRequest(msg)` · `.internalError(msg)` · `.error(HttpStatus, msg)`

---

## Logging

Console (colored) · `logs/ash.log` (plain) · `logs/ash.json.log` (Logstash JSON) — 10 days / 10 files retention.
