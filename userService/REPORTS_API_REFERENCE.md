# Reports API Reference

**Base URL:** `http://<server>:8080`  
**All responses:** `Content-Type: application/json`  
**All requests:** `GET` only, no request body — all params are query params.  
**Date format:** `YYYY-MM-DD`

---

## 1. MIS Dashboard

### 1.1 KPI Summary
**Purpose:** Top-level dashboard numbers — sales value, collections, open orders, dispatch volume, monthly trend, and top distributor ranking.

```
GET /api/reports/mis/kpi-summary
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter KPIs for a single distributor |
| `startDate` | Date | No | Start of period (default: start of current year) |
| `endDate` | Date | No | End of period (default: today) |

**Response fields:**
```json
{
  "totalSalesValue": 0,
  "totalCollections": 0,
  "openOrdersCount": 11,
  "dispatchVolume": 52,
  "monthlyTrend": [],
  "distributorRanking": [
    { "distributorId": 29, "distributorName": "...", "totalValue": 1935136.28, "orderCount": 6 }
  ]
}
```

---

### 1.2 Recent Invoices
**Purpose:** Last N invoices for the MIS dashboard quick-view panel.

```
GET /api/reports/mis/recent-invoices
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

**Response:** Array of invoice objects (same shape as Sales Invoice Grid rows).

---

## 2. Inventory Reports

### 2.1 Inventory Snapshot
**Purpose:** Current stock levels for all products. Use `category` to filter by product type.

```
GET /api/reports/inventory/snapshot
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `category` | String | No | `FINISHED`, `RAW`, or `SCRAP`. Omit for all. |

**Response:** Array of inventory items with product name, quantity, unit, and threshold.

---

### 2.2 Low Stock Alert
**Purpose:** All products currently at or below their minimum threshold.

```
GET /api/reports/inventory/low-stock
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `category` | String | No | `FINISHED`, `RAW`, or `SCRAP`. Omit for all. |

**Response:** Same shape as snapshot, only items where `quantity <= minimumThreshold`.

---

## 3. Stock Movement Reports

### 3.1 Combined Ledger
**Purpose:** All inward + outward stock movements in the period in one list.

```
GET /api/reports/stock-movement/ledger
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

### 3.2 Inward Movements (Purchase / GRN)
**Purpose:** Raw material receipts. Pass `supplierId` to get **Supplier-Wise Inward Report** — the frontend filters by the supplierId the user types.

```
GET /api/reports/stock-movement/inward
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |
| `supplierId` | String | No | Filter by supplier ID for Supplier-Wise Inward Report |

**Response fields include:** `materialCode`, `materialName`, `quantity`, `pricePerUnit`, `supplierId`, `supplierName`, `receivedAt`

---

### 3.3 Outward Movements (Consumption)
**Purpose:** Raw material consumption (used in production).

```
GET /api/reports/stock-movement/outward
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

## 4. Batch Management Reports

### 4.1 Active Batch Lifecycle
**Purpose:** All active production batches with their current stage/status.

```
GET /api/reports/batch/lifecycle
```

No parameters.

---

### 4.2 Expiry Alert
**Purpose:** Batches expiring within N days. Default is 30 days.

```
GET /api/reports/batch/expiry-alert
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `daysAhead` | Integer | No | Lookahead window in days (default: `30`) |

---

## 5. Production Reports

### 5.1 Production Run Log (Paginated)
**Purpose:** Paginated list of all production runs in the period.

```
GET /api/reports/production/log
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |
| `page` | Integer | No | Page number, 0-based (default: `0`) |
| `size` | Integer | No | Page size (default: `50`) |

---

### 5.2 Production Summary by Product
**Purpose:** Total production quantity grouped by product for the period.

```
GET /api/reports/production/summary
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

### 5.3 BOM Consumption (Planned vs Actual)
**Purpose:** For each production run — planned raw material usage from BOM vs actual consumed.

```
GET /api/reports/production/bom-consumption
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

## 6. Sales Reports

### 6.1 Invoice Grid (Paginated)
**Purpose:** Full list of sales invoices with filters. Used for the main Sales Report page.

```
GET /api/reports/sales/invoice-grid
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |
| `page` | Integer | No | Page number, 0-based (default: `0`) |
| `size` | Integer | No | Page size (default: `50`) |

**Response:** Paginated object with fields: `invoiceNumber`, `distributorId`, `distributorName`, `invoiceDate`, `totalAmount`, `taxAmount`, `grandTotal`, `invoiceStatus`, `gdnNumber`

---

### 6.2 Sales by Distributor
**Purpose:** Total invoiced value and invoice count grouped by each distributor.

```
GET /api/reports/sales/by-distributor
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

**Response:** `[ { "distributorId", "distributorName", "invoiceCount", "totalValue" } ]`

---

### 6.3 Monthly Revenue Trend
**Purpose:** Month-wise revenue chart data. Pass `distributorId` to scope to one distributor.

```
GET /api/reports/sales/monthly-trend
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

**Response:** `[ { "month", "year", "totalValue", "invoiceCount" } ]`

---

### 6.4 Sales by Product (Product-Wise Sales Report)
**Purpose:** Total quantity sold and revenue grouped by product (from invoice line items).

```
GET /api/reports/sales/by-product
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

**Response:** `[ { "productId", "productName", "totalQuantity", "totalAmount", "invoiceCount" } ]` — sorted by totalAmount DESC

---

### 6.5 Top Selling Products
**Purpose:** Top N products by quantity sold. Used for the Top Selling Products widget.

```
GET /api/reports/sales/top-products
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |
| `limit` | Integer | No | Number of top products to return (default: `10`) |

**Response:** Same shape as by-product, limited to top N sorted by totalQuantity DESC.

---

## 7. Receivables & Collections

### 7.1 Outstanding Balance
**Purpose:** Current unpaid invoice balance for a distributor.

```
GET /api/reports/receivables/outstanding
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | Yes | Distributor to check |

---

### 7.2 Ageing Buckets
**Purpose:** Outstanding amount bucketed by how overdue it is (0–30d, 31–60d, 61–90d, 90d+).

```
GET /api/reports/receivables/ageing
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | Yes | Distributor to check |

---

### 7.3 Collection History
**Purpose:** Payments received in the period. Omit `distributorId` to see all distributors.

```
GET /api/reports/receivables/collection-history
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

---

## 8. Sales Orders Reports

### 8.1 Sales Orders Grid (Paginated)
**Purpose:** Paginated list of all sales orders with status.

```
GET /api/reports/sales-orders/grid
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |
| `page` | Integer | No | Page number, 0-based (default: `0`) |
| `size` | Integer | No | Page size (default: `50`) |

**Response fields:** `orderId`, `distributorId`, `distributorName`, `orderDate`, `totalAmount`, `status`, `salespersonId`

---

### 8.2 Order Status Count
**Purpose:** Count of orders in each status (PENDING, CONFIRMED, DELIVERED, etc.). Used for dashboard status widgets.

```
GET /api/reports/sales-orders/status-count
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `distributorId` | Long | No | Filter by distributor |

**Response:** `{ "PENDING": 11, "CONFIRMED": 5, "DELIVERED": 30, ... }`

---

### 8.3 Salesman Performance
**Purpose:** Total orders and revenue grouped by salesperson. Omit dates for all-time.

```
GET /api/reports/sales-orders/salesman-performance
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | No | Start of period |
| `endDate` | Date | No | End of period |

**Response:** `[ { "salespersonId", "orderCount", "totalAmount" } ]` — sorted by totalAmount DESC

---

## 9. Dispatch & Delivery Reports

### 9.1 GDN Register (Paginated)
**Purpose:** All GDNs (Goods Dispatch Notes) with delivery status. Filter by `status` to get:
- **Pending Dispatch Report** → `status=PENDING`
- **Delivery Status Report** → `status=IN_TRANSIT`
- All delivered → `status=DELIVERED`

```
GET /api/reports/dispatch/register
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | String | No | `PENDING`, `IN_TRANSIT`, or `DELIVERED`. Omit for all. |
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |
| `page` | Integer | No | Page number, 0-based (default: `0`) |
| `size` | Integer | No | Page size (default: `50`) |

**Response fields:** `gdnId`, `gdnNumber`, `distributorId`, `distributorName`, `gdnDate`, `totalAmount`, `deliveryStatus`

---

### 9.2 Dispatch Summary
**Purpose:** Aggregated dispatch stats for the period (total GDNs, total value, breakdown by status).

```
GET /api/reports/dispatch/summary
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

## 10. Inventory Issues Reports

### 10.1 Issues by Type (Paginated)
**Purpose:** Internal inventory issues (spare parts, promotional items, scrap write-offs) paginated by type.

```
GET /api/reports/inventory-issues/by-type
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `itemType` | String | Yes | `SPARE_PARTS`, `PROMOTIONAL_ITEMS`, or `SCRAP_MATERIAL` |
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |
| `page` | Integer | No | Page number, 0-based (default: `0`) |
| `size` | Integer | No | Page size (default: `50`) |

---

### 10.2 Issues Summary by Type
**Purpose:** Total quantity and value issued grouped by item type for the period.

```
GET /api/reports/inventory-issues/summary
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

## 11. Scrap Management Reports

### 11.1 Scrap Lifecycle
**Purpose:** All scrap items with current lifecycle status (GENERATED → APPROVED → DISPOSED).

```
GET /api/reports/scrap/lifecycle
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

### 11.2 Scrap Disposal Status
**Purpose:** Breakdown of scrap by disposal status for the period.

```
GET /api/reports/scrap/disposal-status
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

### 11.3 Scrap Revenue
**Purpose:** Total approved scrap sale revenue for the period.

```
GET /api/reports/scrap/revenue
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | Date | Yes | Start of period |
| `endDate` | Date | Yes | End of period |

---

## Notes for Frontend

1. **Pagination** — all paginated endpoints return a Spring `Page<T>` wrapper:
   ```json
   {
     "content": [...],
     "totalElements": 150,
     "totalPages": 3,
     "size": 50,
     "number": 0
   }
   ```

2. **Date defaults** — if `startDate`/`endDate` are omitted, the server defaults to current calendar year start → today.

3. **Supplier-Wise Inward** is not a separate endpoint — call `/api/reports/stock-movement/inward` and pass the `supplierId` the user enters.

4. **Pending Dispatch / Delivery Status** — both use the same `/api/reports/dispatch/register` endpoint with different `status` values.

5. **Product-Wise Sales** populates from `invoice_line_items` — data only appears for invoices generated after this feature was deployed. Historical invoices will show empty until backfilled.
