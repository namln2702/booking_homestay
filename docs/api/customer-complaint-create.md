# Customer Complaint Create API – `POST /customers/complaints`

## Location & Purpose
- **Controller**: `CustomerController` wires the route and protects it with `ROLE_CUSTOMER` (`src/main/java/org/example/do_an_v1/controller/CustomerController.java:142-150`). The incoming request body is validated with `@Valid` and the authenticated user ID is resolved via `RequestIdentityResolver`.
- **Service flow**: `CustomerServiceImpl.createComplaint` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:1472-1541`) performs all ownership, status, and deadline checks, persists complaint details and images, assigns an admin reviewer, and transitions the bill to the complaint-processing state.
- **Use case**: Allow a guest to raise a complaint about one of their completed stays within the allowed timeframe so hosts/admins can review and resolve it.

---

## Request

```
POST /customers/complaints
Headers:
  Authorization: Bearer <JWT with ROLE_CUSTOMER>
Body: application/json
```

### Payload (`ComplaintDTO`)
| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `billId` | `Long` | Yes | Identifier of the bill/order the complaint relates to. Must belong to the caller. |
| `description` | `String` | Yes | Complaint details. Blank strings are rejected. |
| `imageUrls[]` | `List<String>` | No | Optional evidence images. Each non-empty URL becomes an `Image` entity linked to the complaint. |

Validation happens both via Bean Validation (`@Valid`) and in `CustomerServiceImpl` (null/blank checks with specific error messages).

---

## Behaviour & Rules

1. **Ownership** – The bill retrieved via `billId` must belong to the authenticated customer (`CustomerServiceImpl.java:1487-1496`). Otherwise a `403` is returned.
2. **Status guard** – Only bills in `COMPLAINT_PENDING` or `SUCCEED` may accept a new complaint (`CustomerServiceImpl.java:1498-1501`).
3. **Deadline window** – Guests can file a complaint for `N + 1` days after checkout, where `N` equals the number of nights between check-in and check-out (`CustomerServiceImpl.java:1503-1520`). Requests past that window return `422`.
4. **Images** – Optional `imageUrls` entries are persisted as `Image` rows and attached to the complaint (`CustomerServiceImpl.java:1529-1536`). Empty/null strings are ignored.
5. **Admin assignment** – The first available admin is assigned to the new complaint (`CustomerServiceImpl.java:1522-1527`). If no admin exists, the service returns `500`.
6. **Bill status update** – After saving the complaint, the bill status is set to `HOST_COMPLAINT_PROCESSING` to move it along the lifecycle (`CustomerServiceImpl.java:1538-1540`).

---

## Success Response (200)

All APIs return the common `ApiResponse<T>` envelope (`src/main/java/org/example/do_an_v1/payload/ApiResponse.java`). Example:

```json
{
  "status": 200,
  "message": "Complaint created successfully",
  "data": {
    "id": 42,
    "billId": 98,
    "adminId": 3,
    "description": "The room was not cleaned when we arrived.",
    "imageUrls": [
      "https://cdn.example.com/evidence/photo-1.jpg",
      "https://cdn.example.com/evidence/photo-2.jpg"
    ],
    "createdAt": "2024-08-09T10:32:00",
    "updatedAt": "2024-08-09T10:32:00"
  },
  "timestamp": 1723199520000
}
```

The `data` block mirrors `ComplaintDTO` as mapped by `ComplaintMapper.toDTO`, enriched with generated identifiers and timestamps.

---

## Error Cases
| Status | When it happens | Message source |
| --- | --- | --- |
| `400` | Missing `billId`, missing/blank `description`, or null payload | Explicit checks in `CustomerServiceImpl.createComplaint`. |
| `403` | Bill does not belong to the caller | Ownership validation (`CustomerServiceImpl.java:1489-1496`). |
| `404` | Bill ID not found | `billRepository.findById`. |
| `422` | Complaint window expired (`now > checkout + (N+1) days`) | Deadline guard (`CustomerServiceImpl.java:1511-1520`). |
| `400` | Bill not in `COMPLAINT_PENDING`/`SUCCEED` | Status guard (`CustomerServiceImpl.java:1498-1501`). |
| `500` | No admin exists to assign | Admin lookup fails (`CustomerServiceImpl.java:1522-1527`). |

Return payloads follow the `ApiResponse` structure with `data = null` for errors.

---

## Frontend Tips
1. Fetch order details via `GET /customers/me/orders/{billId}` to ensure `actions.canFileComplaint` is true before enabling the CTA.
2. Pre-fill the complaint form with the bill code or homestay info retrieved from the order detail response for clarity.
3. When uploading supporting images, host them externally (S3, CDN, etc.) and pass the resulting URLs in `imageUrls`. The backend simply stores the URLs; it does not handle binary uploads.
4. Surface backend error messages verbatim to the user—they already include the deadline and allowed window when applicable.
