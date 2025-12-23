# Customer Profile Update API – `PUT /customers/me`

## Location & Purpose
- **Controller**: `CustomerController.upsertProfileCustomer` (`src/main/java/org/example/do_an_v1/controller/CustomerController.java:27-35`). Guarded by `@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")` and always resolves the acting user ID via `RequestIdentityResolver.requireUserId(null)`.
- **Service flow**: `CustomerServiceImpl.upsertCustomerProfile` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:69-220`). Performs validation, merges partial updates, manages preference replacements, and returns either a localized error payload or the refreshed `CustomerDTO`.
- **Use case**: Allow authenticated customers to edit their personal profile (name, phone, DOB, avatar/QR URLs, preferences) in a single transactional call.

## Request

```
PUT /customers/me
Headers:
  Authorization: Bearer <JWT with ROLE_CUSTOMER>
Body:
{
  "name": "Đức 3001",
  "phone": "0123456789",
  "dateOfBirth": "30/01/2003",
  "age": 22,
  "avatarUrl": "https://cdn.example.com/avatar.jpg",
  "qrCodeUrl": null,
  "listPreference": [1, 2]
}
```

Payload type: `CustomerProfileUpdateRequest` (`src/main/java/org/example/do_an_v1/dto/request/CustomerProfileUpdateRequest.java`). All fields are optional; omitting a field leaves the existing value unchanged. Sending an explicit `null` for `avatarUrl` or `qrCodeUrl` clears the stored value. `listPreference` is treated as "replace all": `[]` removes every preference, `null` leaves them untouched.

## Validation Rules
Validation is implemented directly inside `CustomerServiceImpl.upsertCustomerProfile` and results are accumulated into `errors[]` (see Error Response below).

| Field | Rules |
| --- | --- |
| `name` | Required overall (either existing DB value or payload). When provided: trim, non-empty, ≤ 100 chars. |
| `phone` | Required overall. When provided: trim, non-empty, ≤ 20 chars, matches regex `^[0-9+()\-\s]{6,20}$`. |
| `dateOfBirth` | Required overall. Format `dd/MM/yyyy`, must represent a valid date not in the future. |
| `age` | Backend recomputes age from DOB using `Period.between(dob, now)`; if the payload also includes `age`, it must match the computed value and lie between 0 and 150. |
| `avatarUrl`, `qrCodeUrl` | Optional. When provided and non-null, must be valid HTTP/HTTPS URLs (checked via `java.net.URI`). `null` removes the stored value. |
| `listPreference` | Optional array of preference IDs. `null` → no change. `[]` → remove all. Otherwise, IDs must be distinct, non-null, and exist in `PreferenceRepository`. Missing IDs produce a single error message listing the absent values. |

If any validation rule fails, the service returns:

```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "errors": [
      { "field": "name", "message": "Tên không được để trống" },
      { "field": "listPreference", "message": "Preference IDs [99] không tồn tại" }
    ]
  },
  "timestamp": 1714728000000
}
```

Errors are not short-circuited; multiple issues surface in one response.

## Business Logic Highlights
- **Identity**: The user ID comes exclusively from the JWT (`identityResolver.requireUserId(null)`), ensuring a customer can only update their own profile.
- **Email**: Never part of the payload; immutable.
- **DOB & Age**: When existing persisted DOB is invalid and the request omits `dateOfBirth`, the service forces a validation error so the user must resubmit a valid date.
- **Preferences**: Loaded via `preferenceRepository.findAllById`. The code builds a `HashSet` and only updates the join table when the requested set differs from the stored one.
- **Transactions**: The method is annotated with `@Transactional`, so user, customer, and preference updates succeed or roll back together.

## Success Response (200)

```json
{
  "status": 200,
  "message": "Cập nhật hồ sơ thành công",
  "data": {
    "idCustomer": 16,
    "idUser": 16,
    "name": "Đức 3001",
    "phone": "0123456789",
    "dateOfBirth": "30/01/2003",
    "age": 22,
    "avatarUrl": "https://cdn.example.com/avatar.jpg",
    "qrCodeUrl": null,
    "listPreference": [
      { "id": 1, "name": "View hồ", "description": "..." },
      { "id": 2, "name": "Gần biển", "description": "..." }
    ],
    "...": "Các trường khác từ CustomerDTO"
  },
  "timestamp": 1714728000000
}
```

`data` is the same `CustomerDTO` produced by `ProfileMapper.toCustomerDTO`, so it includes every mapped property (status, role, email, etc.), all formatted exactly like the rest of the customer APIs.

## Partial Update Behavior Summary
| Scenario | Backend Behavior |
| --- | --- |
| Field omitted entirely | Leave existing value untouched (except DOB/name/phone, which must exist somewhere). |
| Field present with `null` | Clear the stored value (supported for `avatarUrl`, `qrCodeUrl`). For other fields `null` triggers the normal "required" validation. |
| `listPreference` omitted | Keep current preferences. |
| `listPreference: []` | Remove every preference row for this customer. |
| Invalid preference IDs | Entire update fails with a single error listing the missing IDs; no partial preference changes occur. |

## Security Notes
- Requires JWT with `ROLE_CUSTOMER`.
- No ability to override `idUser`/`idCustomer` from the request body; everything keys off the authenticated user.
- Input strings are trimmed before validation, helping guard against leading/trailing whitespace issues.

## Test Cases Covered in Code
When writing FE tests or QA scenarios, mirror the following cases that the backend explicitly supports:
1. Update all fields successfully (valid payload).
2. Partial updates (send only changed fields).
3. Set `avatarUrl` or `qrCodeUrl` to `null` to remove them.
4. Replace preferences entirely (non-empty array).
5. Remove all preferences (`[]`).
6. Validation failures: missing required fields, malformed DOB, DOB in the future, mismatched age, invalid URLs, non-existent preference IDs.
7. Unauthorized access (missing/invalid JWT or missing `ROLE_CUSTOMER`).
