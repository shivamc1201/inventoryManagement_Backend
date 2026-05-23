# Dynamic Pricing — Change Log & API Reference

## Overview

Two separate pricing features were added in one session. Neither touches order creation, cart, or BOM production logic.

---

## Feature 1 — Raw Material Dynamic Pricing with BOM Approval

### What changed

When a raw material's price is updated (via existing endpoints), the system now:

1. **Records price history** — every old→new price change is saved to `raw_material_price_history`.
2. **Creates a pending approval request per affected BOM** — for every BOM component that uses that raw material, one `bom_price_change_requests` row is created with status `PENDING`.
3. **Admin decides per BOM** — approve → updates that BOM's component rate, recalculates all BOM cost fields, and syncs the finished product's selling price. Reject → BOM keeps the old rate.

### What does NOT change

- BOM recipe quantities — only prices move.
- BOM production (`POST /api/bill-of-materials/produce`) — still deducts raw material stock by quantity, completely unaffected.
- Order creation, cart, `CartItem.priceAtTime` snapshots — untouched.

### New database tables (auto-created)

| Table | Purpose |
|---|---|
| `raw_material_price_history` | One row per raw material price update. Tracks old price, new price, % change, timestamp. |
| `bom_price_change_requests` | One row per BOM component affected by a raw material price change. Status: PENDING → APPROVED or REJECTED. |

### Modified files

| File | Change |
|---|---|
| `RawProductServiceImpl.java` | Detects price change in `updateRawProduct` and `updateByMaterialCode`, calls `BomPriceChangeService.handleRawMaterialPriceChange`. |
| `BomComponentRepository.java` | Added `findByRawMaterialId(Long)` query. |
| `BomService.java` (interface) | Added `recomputeBomCosts(Long bomId)`. |
| `BomServiceImpl.java` | Implemented `recomputeBomCosts` — loads BOM, runs existing `computeCosts`, saves. |

### New files

| File | Purpose |
|---|---|
| `bom/entity/BomPriceChangeStatus.java` | Enum: `PENDING`, `APPROVED`, `REJECTED`. |
| `bom/entity/RawMaterialPriceHistory.java` | JPA entity for `raw_material_price_history`. |
| `bom/entity/BomPriceChangeRequest.java` | JPA entity for `bom_price_change_requests`. |
| `bom/repository/RawMaterialPriceHistoryRepository.java` | `findByRawMaterialIdOrderByChangedAtDesc`. |
| `bom/repository/BomPriceChangeRequestRepository.java` | Queries by status, rawMaterialId, bomId. |
| `bom/dto/BomPriceChangeResponseDto.java` | Response shape for pending/resolved requests. |
| `bom/dto/RawMaterialPriceHistoryDto.java` | Response shape for raw material price history. |
| `bom/dto/PriceChangeActionDto.java` | Request body for approve/reject (`resolvedBy`, `notes`). |
| `bom/service/BomPriceChangeService.java` | Service interface. |
| `bom/service/impl/BomPriceChangeServiceImpl.java` | Core approval logic. |
| `bom/controller/BomPriceChangeController.java` | REST endpoints. |

### Approval flow diagram

```
Admin updates raw material price
        │
        ▼
RawProductServiceImpl detects oldPrice ≠ newPrice
        │
        ├──► Save RawMaterialPriceHistory (always)
        │
        └──► For each BomComponent using this raw material:
                 └──► Save BomPriceChangeRequest (status=PENDING)

Admin calls POST /approve or /reject
        │
   APPROVE ────────────────────────────────────────────┐
        │                                               │
        ├── BomComponent.rate = newRate                 │
        ├── BomService.recomputeBomCosts(bomId)         │
        │     (recalculates totalComponentCost,         │
        │      effectiveCost, effectiveRatePerUnit)     │
        ├── FinishedProduct.price = effectiveRatePerUnit│
        └── Request.status = APPROVED                   │
                                                        │
   REJECT ─────────────────────────────────────────────┘
        │
        └── Request.status = REJECTED (BOM unchanged)
```

### API endpoints — `/api/bom/price-changes`

| Method | Path | Description |
|---|---|---|
| `GET` | `/pending` | All pending requests across all BOMs |
| `GET` | `/pending/raw-material/{rawMaterialId}` | Pending for one raw material |
| `GET` | `/pending/bom/{bomId}` | Pending for one BOM |
| `GET` | `/all` | Full audit trail (all statuses) |
| `GET` | `/history/{rawMaterialId}` | Price change history for a raw material |
| `PUT` | `/{requestId}/approve` | Approve one request |
| `PUT` | `/{requestId}/reject` | Reject one request |
| `PUT` | `/bulk-approve?rawMaterialId=X` | Approve all pending for a raw material |
| `PUT` | `/bulk-reject?rawMaterialId=X` | Reject all pending for a raw material |

**Request body for approve/reject (optional):**
```json
{
  "resolvedBy": "admin_username",
  "notes": "Accepted due to vendor rate revision"
}
```

**Example response:**
```json
{
  "id": 1,
  "rawMaterialId": 5,
  "rawMaterialName": "Steel Rod",
  "bomId": 3,
  "bomName": "Assembly BOM A",
  "bomComponentId": 12,
  "finishedProductId": 7,
  "finishedProductName": "Finished Widget",
  "oldRate": 120.00,
  "newRate": 145.00,
  "priceChangePercent": 20.83,
  "status": "PENDING",
  "priceHistoryId": 2,
  "requestedAt": "2026-05-22T00:00:00Z",
  "resolvedAt": null,
  "resolvedBy": null,
  "notes": null
}
```

---

## Feature 2 — Product Price History (MachinePart, PromotionalItem, ScrapItem)

### What changed

For these three product types, when price is updated the new price is **saved immediately** (no approval). The system records the old and new price in a shared history table for tracking.

### MachinePart — price field added

MachinePart previously had no price field. The following were updated:

| File | Change |
|---|---|
| `model/MachinePart.java` | Added `price` column (`BigDecimal`, precision 10 scale 2). |
| `interceptors/products/model/MachinePartRequest.java` | Added `price` field. |
| `interceptors/products/impl/UpdateMachinePart.java` | Added `price` field. |
| `interceptors/products/model/MachinePartResponse.java` | Added `price` field to response. |
| `interceptors/products/impl/MachinePartServiceImpl.java` | Sets price on create/update, tracks history on price change. |

### Price history hooks added

| File | Change |
|---|---|
| `PromotionalItemServiceImpl.java` | `updatePromotionalItem` + `updateByItemCode` record history when price changes. |
| `ScrapItemServiceImpl.java` | `updateScrapItem` + `updateByItemCode` record history when price changes. |
| `MachinePartServiceImpl.java` | `updateMachinePart` + `updateByPartNumber` record history when price changes. |

### New database table (auto-created)

| Table | Purpose |
|---|---|
| `product_price_history` | One row per price change event. Discriminated by `product_type` column. |

### New files

| File | Purpose |
|---|---|
| `interceptors/products/model/ProductPriceHistory.java` | JPA entity + `ProductType` enum (`PROMOTIONAL_ITEM`, `SCRAP_ITEM`, `MACHINE_PART`). |
| `repository/ProductPriceHistoryRepository.java` | Queries by type+productId and by type. |
| `interceptors/products/service/ProductPriceHistoryService.java` | `record(...)`, `getHistory(...)`, `getAllByType(...)`. |
| `interceptors/products/ProductPriceHistoryController.java` | REST endpoints. |

### API endpoints — `/api/products/price-history`

| Method | Path | Description |
|---|---|---|
| `GET` | `/{productType}/{productId}` | Full price history for one specific item |
| `GET` | `/{productType}` | All price changes for a product type |

`productType` path value must be one of: `PROMOTIONAL_ITEM`, `SCRAP_ITEM`, `MACHINE_PART`

**Example response:**
```json
[
  {
    "id": 1,
    "productType": "MACHINE_PART",
    "productId": 4,
    "productName": "Hydraulic Seal",
    "productCode": "MP-001",
    "oldPrice": 250.00,
    "newPrice": 300.00,
    "priceChangePercent": 20.00,
    "changedAt": "2026-05-22T00:00:00Z"
  }
]
```

---

## How to manually test (step by step)

### Test Flow 1 — Raw material price change with BOM approval

```
1. POST /api/products/raw-materials             → create a raw material, note its ID
2. POST /api/bill-of-materials/create           → create a BOM using that raw material
3. PUT  /api/products/raw-materials/{id}        → update the price field to a new value
4. GET  /api/bom/price-changes/pending          → confirm a PENDING request was created
5. GET  /api/bom/price-changes/history/{rmId}   → confirm price history was recorded
6. PUT  /api/bom/price-changes/{requestId}/approve  { "resolvedBy": "admin" }
7. GET  /api/bill-of-materials/details/{bomId}  → verify effectiveRatePerUnit changed
8. GET  /api/products/{finishedProductId}        → verify selling price synced

Reject path instead of step 6:
6b. PUT /api/bom/price-changes/{requestId}/reject   { "resolvedBy": "admin", "notes": "Vendor error" }
7b. GET /api/bill-of-materials/details/{bomId}  → BOM rates unchanged
```

### Test Flow 2 — MachinePart price tracking

```
1. POST /api/products/machine-parts            → create a part with price field
2. PUT  /api/products/machine-parts/{id}       → update with a different price
3. GET  /api/products/price-history/MACHINE_PART/{id}  → confirm history recorded
```

### Test Flow 3 — PromotionalItem / ScrapItem price tracking

```
1. POST /api/products/promotional-items        → create with price
2. PUT  /api/products/promotional-items/{id}   → update with new price
3. GET  /api/products/price-history/PROMOTIONAL_ITEM/{id}  → confirm history
   (same flow for SCRAP_ITEM)
```

---

## What was NOT changed (safe to verify)

- `POST /api/bill-of-materials/produce` — production flow unchanged
- `POST /api/products/cart/...` — cart and order creation unchanged
- `CartItem.priceAtTime` — still a snapshot, unaffected by any price changes
- All existing raw material CRUD (except price change now also creates history)
- BOM create/update endpoints — recipe quantities unchanged
