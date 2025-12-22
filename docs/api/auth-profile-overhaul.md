# Authentication & Profile Overhaul – Frontend Briefing

This note gathers every backend change that impacts authentication, profile editing, and the customer self-service screens. Use it as a single reference while wiring the updated flows on the web or mobile frontend.

## Quick Highlights

- `CustomerController`, `HostController`, and `ProfileController` now speak DTOs end-to-end; the backend enforces derived fields and IDs.
- `RequestIdentityResolver` resolves the acting user from the JWT for every profile/order endpoint, keeping non-admins scoped to themselves.
- Admin lifecycle flows were hardened: only `SUPER_ADMIN` can invite new admins, activation codes expire after 24h, and responses surface the normalized DTOs.
- Customer order detail responses now include payment status, complaint state, and actionable permissions, enabling a richer "My Orders" detail screen.
- Bootstrapping, token emission, and Postman tooling were updated so QA and deployments stay consistent.

---

## 1. Profile APIs & DTOs

### Endpoint Matrix

| Endpoint | Method / Body | Auth | Notes |
| --- | --- | --- | --- |
| `/customers` | `POST` `CustomerDTO` | `ROLE_CUSTOMER` | Upserts the authenticated customer's profile. `idUser` (if sent) is overwritten by the JWT claim. |
| `/customers/me` | `GET` | `ROLE_CUSTOMER` | Returns the caller's `CustomerDTO`. |
| `/customers/{userId}` | `GET` | `ROLE_ADMIN`/`ROLE_SUPER_ADMIN` | Admins can fetch another user's customer profile by path ID. |
| `/profile/customers/{userId}` | `PUT` `CustomerProfileUpdateRequest` | Admin only | Creates or updates the selected user's customer profile. |
| `/hosts` | `POST` `HostRegistrationRequest` | `ROLE_CUSTOMER` → `ROLE_HOST` | Host sign-up. The service ensures a backing customer profile exists. |
| `/hosts/me` | `GET` | `ROLE_HOST` | Host profile snapshot (same DTO as admin views). |
| `/profile/hosts/{userId}` | `PUT` `HostProfileUpdateRequest` | Admin only | Backs admin edit surfaces. |
| `/profile/admins/{userId}` | `PUT` `AdminProfileUpdateRequest` | Admin only | Used when promoting/demoting admins after activation. |

### Response Shapes

`CustomerDTO` (`src/main/java/org/example/do_an_v1/dto/CustomerDTO.java`)

| Field | Type | Notes |
| --- | --- | --- |
| `idCustomer` / `idUser` | `Long` | `idUser` comes from the JWT; `idCustomer` mirrors it when the customer row is keyed by user. |
| `username`, `email`, `phone`, `name`, `age`, `avatarUrl`, `googleId` | | Copied from `User`. |
| `isOnline` | `Boolean` | Toggled by login/logout flows. |
| `listPreference` | `List<Long>` | Preference IDs derived from the join table. |
| `status` | `Status` enum | Customer lifecycle state. |
| `dateOfBirth`, `qrCodeUrl` | `String` | Optional profile metadata. |
| `lastBooking` | `String` | Server-derived from booking flows. |
| `role` | `RoleUser` | Always `CUSTOMER` for this DTO. |

`HostDTO` (`src/main/java/org/example/do_an_v1/dto/HostDTO.java`)

| Field | Type | Notes |
| --- | --- | --- |
| `idHost`, `idUser` | `Long` | `idHost` falls back to `idUser` when host rows are keyed by user ID. |
| `username`, `email`, `phone`, `name`, `age`, `avatarUrl`, `googleId`, `isOnline` | | Mirrored from the linked `User`. |
| `statusHost` | `StatusHost` | Current moderation state. |
| `businessName`, `qrCodeUrl` | `String` | Editable branding. |
| `role` | `RoleUser` | Always `HOST`. |

`AdminDTO` (`src/main/java/org/example/do_an_v1/dto/AdminDTO.java`)

| Field | Type | Notes |
| --- | --- | --- |
| `idAdmin`, `idUser` | `Long` | Admin IDs equal the user ID. |
| `username`, `email`, `phone`, `name`, `age`, `avatarUrl`, `googleId`, `isOnline` | | Same `User` projection. |
| `levelAdmin` | `LevelAdmin` | `SUPER_ADMIN` or `ADMIN`. |
| `status` | `Status` | `ACTIVE` after activation, `INACTIVE` otherwise. |
| `role` | `RoleUser` | Always `ADMIN`. |

### Writable Payloads

| DTO | Fields FE may send | Notes |
| --- | --- | --- |
| `CustomerProfileUpdateRequest` | `name`, `phone`, `age`, `avatarUrl`, `dateOfBirth`, `qrCodeUrl` | Validators enforce max lengths; leave `null` to skip a field. |
| `HostProfileUpdateRequest` | `name`, `phone`, `age`, `avatarUrl`, `businessName`, `qrCodeUrl`, `statusHost` | Used from admin tooling; status changes remain server-validated. |
| `AdminProfileUpdateRequest` | `name`, `phone`, `age`, `avatarUrl`, `levelAdmin`, `status` | Lets super admins rebalance levels and states without rebuilding the whole record. |

**Derived Field Guardrails**

- `ProfileServiceImpl` refuses to overwrite timestamps, `lastBooking`, or other computed values. Only mutable contact/branding fields are persisted.
- The service will create the customer/host/admin row on demand when it does not exist; clients can treat the endpoints as upserts.
- Host registration (`POST /hosts`) implicitly ensures a `Customer` row, so FE never has to issue extra calls.

---

## 2. Request Identity Resolver

`RequestIdentityResolver` (`src/main/java/org/example/do_an_v1/service/support/RequestIdentityResolver.java`) centralizes how controllers figure out who is acting:

- Non-admins never supply IDs. The resolver extracts the `id` JWT claim and injects it into DTOs before hitting the service layer.
- Admins continue to rely on path parameters (e.g., `/customers/{userId}`); the resolver only honors the path-supplied `userId` when the caller carries `ROLE_ADMIN`/`ROLE_SUPER_ADMIN`.
- If the JWT is missing, invalid, or lacks the `id` claim, the resolver fails fast with `IllegalArgumentException`, so FE immediately sees a `400/401` response instead of partial side-effects.

> **Frontend impact:** do not send `idUser` when editing your own profile—the backend overwrites it anyway. Admin tooling can keep using the `{userId}` segments that already exist.

---

## 3. Authentication & Token Lifecycle

### Email OTP Flow

1. `POST /user/log-reg/login-register-with-email?email=` issues a one-time code and implicitly creates `User + Customer` rows if the email is new.
2. `POST /user/log-reg/confirm-email` with the `CodeForEmail` payload verifies the OTP and delegates to `UserServiceImpl.checkUser`.

### Google OAuth Flow

`POST /user/log-reg/login-register-with-google?code=` exchanges the auth code for a token, fetches user info from Google, ensures a `Customer`, and then calls `checkUser`.

### Token Semantics

- `SecurityService#createTokenSystem` now emits a space-delimited `scope` claim. Hosts receive `ROLE_HOST ROLE_CUSTOMER`; admins only get `ROLE_ADMIN`.
- `checkUser` prioritizes admin roles, ensures hosts always have a backing customer profile, and toggles `User.isOnline` to `true` on login (and `false` on logout).
- Logout (`GET /user/logout`) invalidates the JWT by storing its `jti` in `InvalidateToken` and flips the `isOnline` flag off.

> FE only needs to hang onto the returned `token` string; every protected endpoint already expects `Authorization: Bearer <token>`.

---

## 4. Admin Lifecycle & Permissions

### Invitation → Activation

| Step | Endpoint | Body | Auth | Notes |
| --- | --- | --- | --- | --- |
| Invite | `POST /admins/invite` | `AdminInviteRequest` (`email`, `fullName`, optional `levelAdmin`, `phone`) | `SUPER_ADMIN` only | Creates/updates the `User` row, seeds/refreshes the `Admin`, and emails a 6-digit activation code that expires after 24h. The response (`AdminInvitationResponse`) mirrors the admin DTO and **also returns the code** for QA automation. |
| Activate | `POST /admins/activate` | `AdminActivationRequest` (`email`, `code`, optional `fullName`, `phone`) | Public | Verifies the code, hydrates the contact info, and marks the admin `ACTIVE`. |

Additional controllers (status updates, host/customer moderation, finance reports, etc.) continue to enforce admin authorities via `@PreAuthorize` (temporarily disabled on a few endpoints while authentication is being wired back in).

### Bootstrap Super Admin

Deployments can seed the very first super admin through `SuperAdminBootstrap` by supplying these properties (set via environment variables or a `.env`):

| Property | Purpose | Default |
| --- | --- | --- |
| `bootstrap.super-admin.email` | Required email for the seed user. | *(empty → bootstrap disabled)* |
| `bootstrap.super-admin.name` | Display name. | `Super Admin` |
| `bootstrap.super-admin.username` | Username/login hint. | `superadmin` |
| `bootstrap.super-admin.phone` | Optional phone. | *(empty)* |
| `bootstrap.super-admin.avatar-url` | Optional avatar. | *(empty)* |
| `bootstrap.super-admin.age` | Optional age. | `30` |

The runner only fires when no `SUPER_ADMIN` exists and will refresh contact info/idempotently on subsequent boots.

---

## 5. Customer Self-Service Enhancements

Customers now have parity between the list and detail views for their orders.

### `GET /customers/me/orders`

- Already documented in `docs/api/my-orders-and-complaints.md`.
- Returns an array of `CustomerOrderResponse` objects sorted by `createdAt` DESC.
- Each entry includes `billId`, `billCode`, `status`, `homestay` info, `checkIn/out`, `totalAmount`, optional `depositAmount`, `basePrice`, and `dailyPrices` (derived from `HomestayDailyPrice`).

### `GET /customers/me/orders/{billId}`

Returns the summary plus contextual status blocks:

```json
{
  "status": 200,
  "message": "Customer order detail retrieved successfully",
  "data": {
    "summary": { ...CustomerOrderResponse... },
    "paymentStatus": {
      "phase": "AWAITING_REMAINING_PAYMENT",
      "awaitingDeposit": false,
      "depositPaid": true,
      "depositPaidAt": "2025-03-01T12:00:00",
      "depositAmount": 900000.0,
      "awaitingRemainingPayment": true,
      "remainingPaymentRequired": true,
      "remainingPaid": false,
      "remainingAmount": 2100000.0,
      "paymentFailed": false,
      "awaitingRefund": false,
      "refunded": false
    },
    "complaintStatus": {
      "phase": "WINDOW",
      "complaintRelated": true,
      "inComplaintWindow": true,
      "underHostReview": false,
      "underAdminReview": false,
      "refundInProgress": false,
      "resolvedWithRefund": false,
      "resolvedWithoutRefund": false,
      "complaintDeadline": "2025-03-15T10:00:00",
      "withinComplaintDeadline": true,
      "latestComplaintId": 42,
      "canFileComplaint": true
    },
    "actions": {
      "canCancel": true,
      "canPayRemaining": true,
      "canCheckIn": false,
      "canFileComplaint": true
    }
  }
}
```

Field semantics:

`CustomerOrderPaymentStatusResponse`

| Field | Meaning |
| --- | --- |
| `phase` | Derived from `StatusBill` (`WAITING_DEPOSIT`, `AWAITING_REMAINING_PAYMENT`, `REFUND_PENDING`, etc.). |
| `awaitingDeposit` / `depositPaid` / `depositPaidAt` / `depositAmount` | Shows whether the 30% deposit has cleared. |
| `awaitingRemainingPayment` / `remainingPaymentRequired` / `remainingPaid` / `remainingPaidAt` / `remainingAmount` | Communicates the remaining 70% balance. |
| `paymentFailed` | True when the last remaining payment attempt failed. |
| `awaitingRefund` / `refunded` / `refundCompletedAt` | Tracks refund progress (pending vs. completed). |

`CustomerOrderComplaintStatusResponse`

| Field | Meaning |
| --- | --- |
| `phase` | `"WINDOW"`, `"HOST_REVIEW"`, `"ADMIN_REVIEW"`, `"RESOLVED_REFUNDED"`, etc. |
| `complaintRelated` | Whether the bill is currently in any complaint lifecycle. |
| `inComplaintWindow`, `underHostReview`, `underAdminReview` | Mutually exclusive flags mirroring the bill status. |
| `refundInProgress`, `resolvedWithRefund`, `resolvedWithoutRefund` | Refund outcomes. |
| `complaintDeadline`, `withinComplaintDeadline` | Deadline = checkout + (stay length + 1 day). |
| `latestComplaintId` | ID of the newest complaint (null when none exist). |
| `canFileComplaint` | Only true when still inside the window and the bill is `COMPLAINT_PENDING` or `SUCCEED`. |

`CustomerOrderActionPermissionResponse`

| Field | Meaning |
| --- | --- |
| `canCancel` | `true` while the bill is `DEPOSIT_PENDING`, `DEPOSIT_PAID`, or `REMAINING_PAYMENT_PENDING`. |
| `canPayRemaining` | `true` iff the bill is waiting for the remaining payment. |
| `canCheckIn` | Requires `REMAINING_PAYMENT_PENDING` **and** a paid deposit. |
| `canFileComplaint` | Mirrors `complaintStatus.canFileComplaint`. |

### Complaints Listing

`GET /customers/me/complaints` still returns `CustomerComplaintResponse` (bill snapshot + `ComplaintDTO`). The new detail endpoint links back through `complaintStatus.latestComplaintId` for deep-linking.

---

## 6. Supporting Assets

- Postman collection for the admin invite/activation flow: `docs/postman/admin-registration.postman_collection.json` (includes scripts to carry the activation code between requests).
- Customer orders + complaints doc: `docs/api/my-orders-and-complaints.md`.
- Controllers/services touched: `CustomerController`, `HostController`, `ProfileController`, `AdminController`, `CustomerServiceImpl`, `AdminServiceImpl`, `UserServiceImpl`, and `SuperAdminBootstrap`.

Keep this file close to the UI team; it can be served verbatim inside your documentation viewer or markdown renderer.
