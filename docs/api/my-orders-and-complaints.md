# Customer Self-Service APIs: My Orders & My Complaints

These endpoints power the two FE screens listed below. Both are **read-only**, require a valid JWT bearer token, and automatically resolve the acting user via `RequestIdentityResolver`. No customer ID is accepted from the request.

| Endpoint | Purpose |
| --- | --- |
| `GET /customers/me/orders` | Unified list of every bill (order) belonging to the authenticated customer. |
| `GET /customers/me/complaints` | Lists every complaint raised by the authenticated customer along with key bill info. |

## Authentication Requirements

- `Authorization: Bearer <JWT>`
- JWT must contain the standard `id` claim; the backend infers the current user from the SecurityContext.
- Only principals holding `ROLE_CUSTOMER` may access these endpoints (enforced with `@PreAuthorize`).

All responses use the shared `ApiResponse<T>` envelope:

```json
{
  "status": 200,
  "message": "Customer orders retrieved successfully",
  "data": [ ... ],
  "timestamp": 1714728000000
}
```

---

## GET `/customers/me/orders`

Returns every bill tied to the current user, sorted by `createdAt` (newest → oldest). It covers ALL bill states (deposit pending, remaining payment pending, complaint, refunded, succeed, etc.).

### Request

```
GET /customers/me/orders
Headers:
  Authorization: Bearer <JWT>
```

No query parameters are supported.

### Successful Response (`200`)

`data` is an array of objects shaped by `CustomerOrderResponse`:

```json
[
  {
    "billId": 101,
    "billCode": "BILL-20250301-XYZ",
    "status": "REMAINING_PAYMENT_PENDING",
    "homestayName": "Villa Da Lat",
    "homestayId": 55,
    "checkIn": "2025-03-10T00:00:00",
    "checkOut": "2025-03-12T00:00:00",
    "totalAmount": 3000000.00,
    "depositAmount": 900000.00,
    "createdAt": "2025-03-01T10:20:00"
  }
]
```

### Field Notes

| Field | Description |
| --- | --- |
| `billId` | Internal bill identifier. |
| `billCode` | Human-readable code displayed throughout the app. |
| `status` | Current `StatusBill` (no new states introduced). |
| `homestayName` / `homestayId` | Quick context for the booked listing. |
| `checkIn`, `checkOut` | Stored as ISO timestamps (`LocalDateTime`). |
| `totalAmount` | Full booking cost (100%). |
| `depositAmount` | 30% escrow amount. Pulled from the first `CUSTOMER_PAYMENT_ADMIN_FIRST` transaction when available; otherwise derived from `totalAmount * 0.3`. |
| `createdAt` | Bill creation time; sorting key (DESC). |

### Error Responses

Standard `ApiResponse` errors apply (e.g., `404` if the customer profile is missing, `401` for invalid JWT).

---

## GET `/customers/me/complaints`

Returns every complaint the authenticated customer has filed, newest first. Each entry embeds the related bill metadata so FE can show context without additional calls.

### Request

```
GET /customers/me/complaints
Headers:
  Authorization: Bearer <JWT>
```

### Successful Response (`200`)

`data` is an array of `CustomerComplaintResponse` objects:

```json
[
  {
    "billId": 101,
    "billCode": "BILL-20250301-XYZ",
    "billStatus": "HOST_COMPLAINT_PROCESSING",
    "homestayId": 55,
    "homestayName": "Villa Da Lat",
    "checkIn": "2025-03-10T00:00:00",
    "checkOut": "2025-03-12T00:00:00",
    "billCreatedAt": "2025-03-01T10:20:00",
    "complaint": {
      "id": 17,
      "description": "Homestay was double-booked",
      "createdAt": "2025-03-13T08:15:00",
      "adminId": 2,
      "billId": 101,
      "imageUrls": [
        "https://cdn.example.com/complaints/17/photo-1.jpg"
      ]
    }
  }
]
```

### Field Notes

| Field | Description |
| --- | --- |
| `bill*` fields | Same as in the Orders endpoint; fixed snapshot for complaint context. |
| `complaint` | Existing `ComplaintDTO` payload (id, description, timestamps, adminId, billId, `imageUrls`). No additional fields were introduced. |

### Error Responses

- `404` when the authenticated user lacks a customer profile.
- `401/403` for invalid JWT or missing `ROLE_CUSTOMER`.

---

## Versioning & Usage Notes

- Both endpoints are live under the existing `/customers` controller and reuse the current JWT infrastructure; no extra headers or query params are required.
- These APIs are additive—no changes were made to legacy bill history or complaint creation endpoints.
