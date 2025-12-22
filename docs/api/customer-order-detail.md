# Customer Order Detail API – `GET /customers/me/orders/{billId}`

## Location & Purpose
- **Controller**: `src/main/java/org/example/do_an_v1/controller/CustomerController.java:103-108` wires the route, enforces `ROLE_CUSTOMER`, and resolves the caller’s user ID through `RequestIdentityResolver`.
- **Service flow**: `CustomerServiceImpl.getCustomerOrderDetail` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:666-707`) performs ownership checks, loads transactions, and assembles the response payload.
- **Use case**: Fetch a single bill (order) that belongs to the authenticated customer together with payment/complaint timelines and actionable flags so the frontend can render a detailed order sheet.

## Request

```
GET /customers/me/orders/{billId}
Headers:
  Authorization: Bearer <JWT with ROLE_CUSTOMER>
Path params:
  billId (Long) – identifier of the bill to inspect.
```

The backend rejects requests when `billId` is missing, the authenticated profile is not a customer, or the bill does not belong to the caller (see error section).

## Success Response (200)

All controllers share the `ApiResponse<T>` envelope (`src/main/java/org/example/do_an_v1/payload/ApiResponse.java:12-23`):

```json
{
  "status": 200,
  "message": "Customer order detail retrieved successfully",
  "data": {
    "summary": {
      "billId": 98,
      "billCode": "BILL-2024-08-01",
      "status": "HOST_COMPLAINT_PROCESSING",
      "homestayName": "Seaside Villa",
      "homestayId": 41,
      "checkIn": "2024-08-05T14:00:00",
      "checkOut": "2024-08-08T11:00:00",
      "totalAmount": 4200000.00,
      "depositAmount": 1260000.00,
      "createdAt": "2024-07-20T09:12:00",
      "basePrice": 1400000.0,
      "dailyPrices": [
        {
          "dailyPriceId": 501,
          "date": "2024-08-05",
          "price": 1400000.0
        }
      ]
    },
    "paymentStatus": {
      "phase": "HOST_REVIEW",
      "awaitingDeposit": false,
      "depositPaid": true,
      "depositPaidAt": "2024-07-20T09:15:00",
      "depositAmount": 1260000.00,
      "awaitingRemainingPayment": false,
      "remainingPaymentRequired": false,
      "remainingPaid": false,
      "remainingPaidAt": null,
      "remainingAmount": 2940000.00,
      "paymentFailed": false,
      "awaitingRefund": false,
      "refunded": false,
      "refundCompletedAt": null
    },
    "complaintStatus": {
      "phase": "HOST_REVIEW",
      "complaintRelated": true,
      "inComplaintWindow": false,
      "underHostReview": true,
      "underAdminReview": false,
      "refundInProgress": false,
      "resolvedWithRefund": false,
      "resolvedWithoutRefund": false,
      "complaintDeadline": "2024-08-09T11:00:00",
      "withinComplaintDeadline": true,
      "latestComplaintId": 15,
      "canFileComplaint": false,
      "canCancelComplaint": true,
      "canUpdateComplaint": true
    },
    "actions": {
      "canCancel": false,
      "canPayRemaining": false,
      "canCheckIn": false,
      "canFileComplaint": false
    }
  },
  "timestamp": 1714728000000
}
```

## Field Reference

### `data.summary` (`CustomerOrderResponse`, `CustomerOrderResponse.java:20-33`)
| Field | Description |
| --- | --- |
| `billId`, `billCode` | Primary identifiers for the booking. `billCode` is human-facing while `billId` is used for internal calls. |
| `status` | Raw `StatusBill` enum (`src/main/java/org/example/do_an_v1/enums/StatusBill.java:5-89`). Every state in that enum can surface here. |
| `homestayId`, `homestayName` | Snapshot of the homestay at booking time. |
| `checkIn`, `checkOut`, `createdAt` | Stored as ISO timestamps (Java `LocalDateTime`). |
| `totalAmount` | 100% cost of the stay. |
| `depositAmount` | Calculated via `resolveDepositAmount` (`CustomerServiceImpl.java:832-858`), preferring actual transactions but falling back to 30% of `totalAmount`. |
| `basePrice`, `dailyPrices` | Optional nightly pricing breakdown; omitted fields return `null`/`[]`. |

#### `summary.status` Situations
Frontends can rely on `StatusBill` for end-to-end context. The table below lists every state and its meaning.

| Status | When it occurs (per `StatusBill.java`) |
| --- | --- |
| `DEPOSIT_PENDING` | Customer created a booking but has not paid the 30% deposit yet. |
| `DEPOSIT_PAID` | Deposit succeeded; waiting for check-in date. |
| `REMAINING_PAYMENT_PENDING` | Customer must settle the remaining 70% at or after check-in. |
| `REMAINING_PAYMENT_FAILED` | Remaining payment failed or timed out. |
| `CHECKIN_EXPIRED` | Customer missed check-in; order auto-closes. |
| `COMPLAINT_PENDING` | Guest can raise complaints (N+1 days window). |
| `HOST_COMPLAINT_PROCESSING` | Host is reviewing an active complaint. |
| `ADMIN_COMPLAINT_PROCESSING` | Complaint escalated to admins. |
| `PENDING_REFUNDED` | Approved refund awaits admin transfer. |
| `REFUNDED` | Complaint resolved with refund. |
| `REJECTED` | Complaint resolved without refund. |
| `SUCCEED` | Stay completed, no complaints/refunds outstanding. |
| `CANCELLED_REFUNDED` | Customer cancelled before deadline and received refund. |
| `CANCELLED` | Customer cancelled without refund (late cancellation). |

### `data.paymentStatus` (`CustomerOrderPaymentStatusResponse`, `CustomerServiceImpl.java:895-938`)
| Field | Meaning |
| --- | --- |
| `phase` | High-level lifecycle string derived exclusively from `StatusBill` via `determinePaymentPhase` (`CustomerServiceImpl.java:959-978`). See table below. |
| `awaitingDeposit` / `depositPaid` / `depositPaidAt` / `depositAmount` | Deposit collection state. |
| `awaitingRemainingPayment` / `remainingPaymentRequired` / `remainingPaid` / `remainingPaidAt` / `remainingAmount` | Remaining 70% settlement state. |
| `paymentFailed` | True when bill status equals `REMAINING_PAYMENT_FAILED`. |
| `awaitingRefund` | True when bill is `PENDING_REFUNDED` or when a refund transaction is still pending. |
| `refunded` / `refundCompletedAt` | Reflect either `StatusBill.REFUNDED`, `CANCELLED_REFUNDED`, or a successful refund transaction timestamp. |

#### All `paymentStatus.phase` Variants
Derived from a single enum-to-string mapping so the frontend can render deterministic badges.

| Phase value | Emitted when `summary.status` is… (`CustomerServiceImpl.java:959-978`) |
| --- | --- |
| `WAITING_DEPOSIT` | `DEPOSIT_PENDING` |
| `DEPOSIT_PAID_WAITING_CHECKIN` | `DEPOSIT_PAID` |
| `AWAITING_REMAINING_PAYMENT` | `REMAINING_PAYMENT_PENDING` |
| `REMAINING_PAYMENT_FAILED` | `REMAINING_PAYMENT_FAILED` |
| `CHECKIN_EXPIRED` | `CHECKIN_EXPIRED` |
| `COMPLAINT_WINDOW` | `COMPLAINT_PENDING` |
| `HOST_REVIEW` | `HOST_COMPLAINT_PROCESSING` |
| `ADMIN_REVIEW` | `ADMIN_COMPLAINT_PROCESSING` |
| `REFUND_PENDING` | `PENDING_REFUNDED` |
| `REFUNDED` | `REFUNDED` |
| `COMPLAINT_REJECTED` | `REJECTED` |
| `COMPLETED` | `SUCCEED` |
| `CANCELLED_REFUNDED` | `CANCELLED_REFUNDED` |
| `CANCELLED` | `CANCELLED` |
| `UNKNOWN` | Null/undefined status (should not occur in normal flows). |

### `data.complaintStatus` (`CustomerOrderComplaintStatusResponse`, `CustomerServiceImpl.java:896-938`)
| Field | Description |
| --- | --- |
| `phase` | Complaint lifecycle marker (see table below). |
| `complaintRelated` | True whenever the bill is inside any complaint-processing status. |
| `inComplaintWindow` | True only when the bill status is `COMPLAINT_PENDING`. |
| `underHostReview`, `underAdminReview`, `refundInProgress`, `resolvedWithRefund`, `resolvedWithoutRefund` | Convenience booleans mirroring the bill status buckets. |
| `complaintDeadline` | Calculated via `calculateComplaintDeadline` (`CustomerServiceImpl.java:1004-1018`) as checkout + (N+1) days. |
| `withinComplaintDeadline` | Tells the UI whether the window remains open. |
| `latestComplaintId` | Null when no complaint exists; otherwise the newest complaint ID for deep links. |
| `canFileComplaint` | True only when status is `COMPLAINT_PENDING` and still within N+1 days. |
| `canCancelComplaint` / `canUpdateComplaint` | True only when a complaint exists and bill status is `HOST_COMPLAINT_PROCESSING`. |

#### All `complaintStatus.phase` Variants
| Phase | Emitted when (`CustomerServiceImpl.java:981-993`) |
| --- | --- |
| `WINDOW` | Bill status `COMPLAINT_PENDING`. |
| `HOST_REVIEW` | `HOST_COMPLAINT_PROCESSING`. |
| `ADMIN_REVIEW` | `ADMIN_COMPLAINT_PROCESSING`. |
| `REFUND_PENDING` | `PENDING_REFUNDED`. |
| `RESOLVED_REFUNDED` | `REFUNDED` or `CANCELLED_REFUNDED`. |
| `RESOLVED` | `REJECTED`, `SUCCEED`, or `CANCELLED`. |
| `NONE` | Any status outside complaint-related flows (includes null safety fallback). |

### `data.actions` (`CustomerOrderActionPermissionResponse`, `CustomerServiceImpl.java:941-956`)
| Field | Backing logic |
| --- | --- |
| `canCancel` | True while the bill is at or before `COMPLAINT_PENDING` in the lifecycle timeline (`isStatusBeforeOrEqual`). |
| `canPayRemaining` | True only when `summary.status == REMAINING_PAYMENT_PENDING`. |
| `canCheckIn` | Requires `REMAINING_PAYMENT_PENDING` **and** `paymentStatus.depositPaid == true`. |
| `canFileComplaint` | Mirrors `complaintStatus.canFileComplaint`. |

## Error Cases
- `400` when `billId` is missing or invalid (`CustomerServiceImpl.java:669-674`).
- `404` when either the customer profile or the bill cannot be found (`CustomerServiceImpl.java:679-688`).
- `403` when the bill does not belong to the authenticated customer (`CustomerServiceImpl.java:690-692`).
- Standard Spring Security errors (`401/403`) when the JWT is missing or lacks `ROLE_CUSTOMER`.

## Frontend Usage Tips
1. Drive UI badges directly from `summary.status`, `paymentStatus.phase`, and `complaintStatus.phase` without inventing new enums; backend mappings are stable and centralized.
2. Use `actions` booleans to toggle CTA buttons; do not duplicate lifecycle logic in the UI.
3. When showing complaint info, prefer `complaintStatus.latestComplaintId` for deep links and fall back to `summary.status` to convey resolution state.
