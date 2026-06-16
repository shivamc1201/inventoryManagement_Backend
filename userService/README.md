# Inventory Management Backend — User Service

A comprehensive enterprise backend for inventory, sales, and operations management built with Spring Boot 4 and Java 21.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Run Locally](#run-locally)
  - [Run with Docker](#run-with-docker)
- [API Reference](#api-reference)
- [Architecture](#architecture)
- [Authentication & RBAC](#authentication--rbac)
- [Modules](#modules)

---

## Overview

This service is the core backend for the **Nector Inventory Management System** — an ERP platform covering inventory tracking, order management, dealer networks, sales hierarchy, HR, financial ledgers, and dispatch logistics. It exposes a REST API consumed by web and mobile frontends.

---

## Features

- **Inventory Management** — stock tracking for finished goods, raw materials, machine parts, promotional and scrap items
- **Order & Sales Management** — cart, order placement, proforma invoices, confirmation, and dealer sales tracking
- **Dealer & Distributor Network** — multi-tier dealer/distributor management with ledger accounts
- **Financial Ledger** — transaction history, payment approvals, invoice generation (PDF)
- **Bill of Materials (BOM)** — production planning with component tracking and cost breakdown
- **Dispatch / GDN** — Goods Dispatch Note generation and inventory verification
- **HR Module** — employee records, departments, designations
- **KPI Management** — define KPIs, assign to employees, track results via dashboards
- **Complaint Tracking** — customer complaints and feedback lifecycle
- **Role-Based Access Control (RBAC)** — 28+ role types mapped to 13+ features
- **File Uploads** — images via Cloudinary and AWS S3 with CloudFront CDN delivery
- **OTP / SMS** — Twilio integration for OTP-based authentication flows
- **Excel & PDF Export** — Apache POI for Excel, iText/Flying Saucer for PDF invoices

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0 |
| Persistence | Spring Data JPA (Hibernate) |
| Database | PostgreSQL (Neon serverless) |
| Connection Pool | HikariCP |
| Security | Spring Security + JWT |
| Mapping | MapStruct |
| PDF Generation | Flying Saucer + iText |
| Excel | Apache POI |
| Image Storage | Cloudinary, AWS S3 + CloudFront |
| SMS / OTP | Twilio |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Containerization | Docker (multi-stage) |

---

## Project Structure

```
src/main/java/com/nector/userservice/
├── aws/                    # AWS S3 image upload and retrieval
├── bom/                    # Bill of Materials (controller, service, entity, dto, repository)
├── cloudinary/             # Cloudinary image upload with retry logic
├── common/features/        # Feature enums for RBAC
├── config/                 # Spring configuration (security, async, encryption)
├── controller/             # Core REST controllers (17 controllers)
├── dispatch/               # GDN and dispatch management module
├── dto/                    # Data Transfer Objects (cart, HR, inventory, invoice, payment, sales)
├── enums/                  # Application-wide enums (RoleType, etc.)
├── exception/              # Custom exception classes
├── interceptors/           # Domain flow logic (login, logout, user create, orders, etc.)
├── ledger/                 # Financial ledger entities
├── model/                  # JPA entities (45+ domain models)
├── ordertracking/          # Order tracking entities and state machine
└── repository/             # Spring Data JPA repositories

src/main/resources/
├── application.properties  # Application configuration
└── schema.sql              # Database initialization script
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL database (local or [Neon](https://neon.tech))
- Docker (optional)

### Environment Variables

The application requires the following environment variables at runtime:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL — e.g. `jdbc:postgresql://host:5432/nector` |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `PORT` | Server port (default `8080`) |

AWS, Cloudinary, and Twilio credentials are stored as encrypted values in `application.properties` and do not require separate env vars unless overriding.

### Run Locally

```bash
# Build the JAR (skipping tests)
./mvnw clean package -DskipTests

# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/nector
export DB_USER=postgres
export DB_PASSWORD=your_password

# Run
java -jar target/custom_jar_for_aws-obf.jar
```

Or use the Spring Boot Maven plugin for development:

```bash
./mvnw spring-boot:run
```

### Run with Docker

```bash
# Build the image
docker build -t user-service:latest .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/nector \
  -e DB_USER=postgres \
  -e DB_PASSWORD=password \
  user-service:latest
```

---

## API Reference

Once the server is running, interactive API documentation is available at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Core Endpoints Summary

| Controller | Base Path Area | Responsibility |
|---|---|---|
| `UserPermissionController` | `/user` | User management and RBAC |
| `InventoryController` | `/inventory` | Stock create/update/query |
| `CartController` | `/cart` | Cart operations and order placement |
| `DealerController` | `/dealer` | Dealer lifecycle management |
| `InvoiceController` | `/invoice` | Invoice fetch and generation |
| `DealerSaleController` | `/dealer-sale` | Dealer sales recording |
| `DealerLedgerController` | `/ledger` | Dealer account transactions |
| `SalesController` | `/sales` | Sales order operations |
| `SalesHierarchyController` | `/hierarchy` | Regional/zonal sales structure |
| `RoleManagementController` | `/role` | Role CRUD |
| `RoleFeaturePermissionController` | `/role-feature` | Role-feature permission mapping |
| `KpiAdminController` | `/kpi-admin` | KPI definition and assignment (admin) |
| `KpiEmployeeController` | `/kpi-employee` | KPI tracking (employee view) |
| `PriceMasterController` | `/price` | Product price list management |
| `HrController` | `/hr` | Employee, department, designation |
| `DashboardController` | `/dashboard` | KPI summaries and analytics |
| `TransactionLedgerController`, `TransactionVoucherController`, `TransactionFundController`, `CashbookController` | `/api/transaction` | Transaction Master (ledgers, vouchers, funds) and Transaction Cashbook |

---

## Architecture

The service follows a **layered domain-driven design**:

```
HTTP Request
     │
     ▼
Controller (REST layer — input validation, response shaping)
     │
     ▼
Service (business logic, transactions)
     │
     ▼
Repository (Spring Data JPA — database access)
     │
     ▼
PostgreSQL
```

**Key patterns in use:**

- **Domain modules** — self-contained packages (bom, dispatch, ledger) each with their own controller, service, entity, repository, dto, and mapper layers
- **MapStruct** — compile-time DTO ↔ entity mapping; no reflection overhead at runtime
- **Interceptor flows** — complex multi-step flows (login, user creation, order processing) are isolated under `interceptors/`
- **Spring Retry** — Cloudinary uploads retry up to 3 times with a 30-second timeout
- **Async processing** — `@EnableAsync` supports background tasks (notifications, reports)
- **Credential encryption** — AWS and Twilio secrets are stored encrypted; decrypted at startup via `CredentialEncryption`

---

## Authentication & RBAC

**Authentication** uses JWT (stateless sessions). The `JwtAuthenticationFilter` validates tokens on each request. JWT expiry is 24 hours.

> **Note:** JWT enforcement is currently disabled in `SecurityConfig` — all endpoints permit unauthenticated access. Re-enable by uncommenting the filter chain in `SecurityConfig.java`.

**RBAC model:**

```
User → Role (RoleType enum, 28+ types)
Role → Features (13+ top-level features via RoleFeaturePermission)
```

Key role categories:

- Administrative: `SuperAdmin`, `Admin`
- Sales: `SalesManager`, `RegionalSalesManager`, `ZonalSalesManager`, `SalesOfficer`
- Operations: `InventoryManager`, `DispatchManager`, `ProductionManager`
- Finance: `AccountsManager`, `AccountsOfficer`
- Field: `Dealer`, `Distributor`, `FieldSalesExecutive`

Passwords are hashed with BCrypt.

---

## Modules

### Bill of Materials (BOM)
Production planning module. Defines finished product recipes with component quantities and additional costs. Supports multi-level BOM structures.

### Dispatch / GDN
Manages outbound logistics. Generates Goods Dispatch Notes (GDN), links to invoices, and records inventory verification at dispatch time.

### Financial Ledger
Double-entry style ledger tracking dealer account balances. Records all debit/credit transactions with audit logs.

### Order Tracking
State-machine based order lifecycle tracking with timestamped steps from placement through delivery.

### HR Master
Manages employee records linked to departments and designations. Integrates with KPI assignment.

### KPI Management
Admin-defined KPIs (Key Performance Indicators) assigned to employees. Employees submit results; dashboards aggregate performance metrics.

### Image Storage
Dual-provider image storage:
- **Cloudinary** — primary upload with retry logic, 10 MB limit
- **AWS S3 + CloudFront** — alternative storage with CDN delivery via `https://d1vi79nbg06y15.cloudfront.net`

### Transaction Master & Cashbook
Self-contained module (`com.nector.userservice.transaction`) backing the **Transaction Master** (Create Ledger, Voucher Entry, Add Fund) and **Transaction Cashbook** pages. All endpoints are under `/api/transaction` and return raw JSON objects/arrays (no response envelope), matching the Angular frontend's `transaction.service.ts` contract field-for-field.

**Endpoints:**

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/transaction/ledger` | Create a ledger (`EXPENSE`/`INCOME` + `under_group` pairing validated) |
| `GET` | `/api/transaction/ledgers` `[?type=]` | List ledgers, optionally filtered by `ledgerType` |
| `POST` | `/api/transaction/voucher` | Create a voucher (auto-numbered `VCH-###`, always `status=APPROVED`) |
| `GET` | `/api/transaction/vouchers` `[?ledgerName=]` | List all vouchers, or vouchers for one ledger |
| `GET` | `/api/transaction/voucher/{voucherNo}` | Fetch a single voucher (200 or 404) |
| `POST` | `/api/transaction/fund` | Add a fund (auto-numbered `FND-###`) |
| `GET` | `/api/transaction/funds` | List all funds |
| `GET` | `/api/transaction/cashbook?from=&to=` | Cashbook summary for a date range (opening/closing balance, per-ledger entries) |

**Numbering:** `VCH-###` and `FND-###` are generated from native Postgres sequences `voucher_seq` / `fund_seq` (created automatically on first startup via `TransactionNumberService`), so numbers stay monotonic even across deletions.

**Cashbook:** For a date range, every ledger with voucher activity in `[from, to]` produces an entry with `grossAmount`, `lessAdjustment`, and net `amount`. Funds (from both `OFFICE` and `FACTORY`) are summed and surfaced as a synthetic income entry **"Received From HO"**, whose breakdown is served via `GET /vouchers?ledgerName=Received From HO` (funds mapped into voucher shape). `closingBalance = openingBalance + totalIncome − totalExpense`, with `openingBalance` carried forward from all activity before `from` (base balance is 0).

**Seed data:** On first run (when `transaction_ledger` is empty), `TransactionDataSeeder` inserts the 16 standard expense/income ledgers (Wages, Spare Parts, Tea, Sale From Scrap, etc. — see spec §11). If no vouchers/funds exist yet, it also seeds 3 sample vouchers and 2 sample funds for June 2026 so the cashbook renders populated immediately (`GET /api/transaction/cashbook?from=2026-06-01&to=2026-06-30` returns `closingBalance = 29500`).

**Tests:** `CashbookServiceTest` (`src/test/java/.../transaction/service/`) verifies the §9 worked example balances exactly, including the opening-balance carry-forward to the following month.