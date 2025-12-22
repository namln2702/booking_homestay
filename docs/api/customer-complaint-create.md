# Customer Complaint Creation API – `POST /complaint/complaint`

## Location & Purpose
- **Controller**: `ComplaintController.userComplaint` (`src/main/java/org/example/do_an_v1/controller/ComplaintController.java:21-24`) exposes the endpoint under the `/complaint` base path.
- **Service flow**: `ComplaintServiceImpl.userComplaint` (`src/main/java/org/example/do_an_v1/service/impl/ComplaintServiceImpl.java:35-73`) performs all validations, status updates, and persistence.
- **Use case**: Allow a customer to submit a complaint for one of their bills during the allowed time window. This is a legacy entry-point that pre-dates the richer `/customers/complaints` flow and still operates on `ComplaintDTO`.

## Authentication / Security
The controller currently lacks `@PreAuthorize` annotations, so routing alone does not restrict callers. Deployments must ensure the endpoint is protected at the gateway layer (e.g., by routing it behind authenticated contexts) or add method-level security before exposing publicly.

## Request

```
POST /complaint/complaint
Headers:
  Content-Type: application/json
Body: ComplaintDTO payload (see below)
```

`ComplaintDTO` (`src/main/java/org/example/do_an_v1/dto/ComplaintDTO.java:13-27`) fields:

| Field | Required? | Description |
| --- | --- | --- |
| `id` | No | Ignored on create. |
| `description` | **Yes** | Complaint content. Saved directly to `Complaint.description`. |
| `createdAt` | **Yes** | Timestamp of complaint creation. Used to verify expiration window. |
| `billId` | **Yes** | Target bill identifier. Bill must exist; otherwise `RuntimeException("Bill not exits")` bubbles up. |
| `billStatus` | No | Not used during creation. |
| `homestayTitle` | No | Not used. |
| `imageUrls` | Optional | List of image URLs to attach. Each entry results in a persisted `Image` row via `ImageRepository.save`. |

### Time Window Validation
- Application property: `claim-expiration-date` (`ComplaintServiceImpl.java:30-31`). The service compares `bill.createdAt.plusDays(claimExpirationDate)` with the `createdAt` supplied in the payload using `Date.compareDate`.
- If the deadline is exceeded (`checkExpired == 1`), the service returns `ApiResponse<>(422, "Expired for complaint", null)`.

### Bill Status Update
Before validation completes, the bill status is set to `StatusBill.COMPLAINT_PENDING` (`ComplaintServiceImpl.java:37-44`), ensuring any subsequent workflows treat the order as complaint-related.

## Successful Response (`200`)

On success, the service persists:
1. Optional `Image` rows for every non-null/non-empty `imageUrls` entry (images are saved standalone without the `complaint` relation populated yet).
2. The `Complaint` entity with fields: `bill`, `description`, `listImage` (assigned as the saved images). The `admin` field is **not** set in this code path; downstream logic must ensure DB constraints are satisfied.

It then returns:

```json
{
  "status": 200,
  "message": "Create complaint success",
  "data": {
    "id": 123,
    "description": "Room was not cleaned",
    "createdAt": "2024-08-10T08:15:00",
    "bill": {
      "...": "Standard Complaint entity serialization"
    },
    "listImage": [
      {
        "id": 501,
        "image_url": "https://cdn.example.com/complaints/123/photo-1.jpg"
      }
    ]
  },
  "timestamp": 1714728000000
}
```

> Note: Because the `data` payload is the raw `Complaint` entity (`ComplaintServiceImpl.java:70-72`), the JSON shape depends on Jackson’s serialization of the entity graph (Bill, Images, Admin). Expect circular-reference-safe views if Jackson is configured for LAZY-loaded associations; otherwise, FE should only rely on fields defined in `ComplaintDTO`.

## Field Situations – `StatusBill` Transitions Triggered Here
Although `ComplaintDTO.billStatus` is unused, submitting a complaint affects the parent bill’s `status`. The backend strictly writes `StatusBill.COMPLAINT_PENDING` during this workflow. Downstream automations or host/admin actions may later move the bill through the complaint lifecycle:

| Status | How it’s reached (relative to this endpoint) |
| --- | --- |
| `COMPLAINT_PENDING` | Set immediately when this endpoint is invoked, signalling that a complaint has been logged and the host review window should open. |
| `HOST_COMPLAINT_PROCESSING` | Achieved when the more recent `/customers/complaints` API is used, or when additional logic promotes the bill after validation (not handled here). |
| `ADMIN_COMPLAINT_PROCESSING`, `PENDING_REFUNDED`, `REFUNDED`, `REJECTED` | Managed by host/admin services; this endpoint only seeds the initial complaint record. |

Frontends that invoke `/complaint/complaint` should therefore refresh the customer’s order/complaint summaries via `/customers/me/orders` and `/customers/me/complaints` to pick up the updated bill status.

## Error Responses
| HTTP | Message | Cause |
| --- | --- | --- |
| `422` | `Expired for complaint` | Complaint submitted after `claim-expiration-date` days have elapsed since bill creation. |
| `500` (Runtime) | `Bill not exits` | Bill ID not found (uncaught `RuntimeException`). |
| `500` | Hibernate/JPA validation errors (e.g., missing required fields on `Complaint` or `Image`). |

Because validation exceptions propagate as generic 500 responses, client applications should defensively handle unexpected `status` codes and parse the `message` field whenever available.
