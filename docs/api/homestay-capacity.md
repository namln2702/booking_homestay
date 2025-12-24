# Homestay Capacity Flow – Host Creation & Customer Booking

## Overview
To keep guest distribution consistent across search, creation, and booking, the backend now persists per-homestay capacity rows (`PersonHomestay`) and requires both hosts and customers to submit the same `listPersonHomestay` structure. This document explains the payloads exposed by the updated APIs so the frontend can collect and display the necessary data.

---

## Host Creates/Updates a Homestay

### Endpoint
`POST /homestay` (Authenticated host)

### Payload Changes
- `HomestayCreateRequest.listPersonHomestay` (required, `List<PersonCapacityRequest>`)
  - `type` – enum (`ADULTS`, `CHILDREN`, `BABY`)
  - `quantity` – integer >= 0
- Requests without this list or with negative quantities are rejected (`400`).

### Backend Handling
`HomestayServiceImpl.createHomestay` validates the list, resolves/creates `Person` rows via `PersonRepository`, and attaches `PersonHomestay` entities to the new homestay. Existing search queries (e.g., `HomestayRepository.findHomestay*`) now receive real data for adults/children/baby filters.

### Response
`HomestayDTO` and `HomestayDetailDTO` now include:

```jsonc
"personCapacities": [
  { "type": "ADULTS", "quantity": 4 },
  { "type": "CHILDREN", "quantity": 2 },
  { "type": "BABY", "quantity": 1 }
]
```

Use this to pre-fill host UIs or to inform customers about capacity limits on detail pages.

---

## Customer Books a Homestay

### Endpoint
`POST /customers/booking` (Authenticated customer)

### Payload Changes
- `BookingDTO.listPersonHomestay` (required, matches the structure above). Clients must collect how many adults/children/babies are included in the reservation.

### Validation
`CustomerServiceImpl.booking` runs `validateGuestDistribution` before locking dates:
1. Ensures every entry has a type + non-negative quantity.
2. Ensures the homestay exposes capacity for each requested type.
3. Rejects when requested quantity exceeds the configured limit per type.
4. Ensures the total guest count stays within `minGuest`/`maxGuest`.
5. Returns a `422` if the homestay has no capacity configuration or if any rule is violated.

### Error Examples
```json
{
  "status": 422,
  "message": "Requested adults exceeds capacity (5/4)",
  "data": null
}
```

```json
{
  "status": 422,
  "message": "Homestay does not accept baby",
  "data": null
}
```

### Response
Success responses are unchanged, but FE can reuse the `personCapacities` block from `HomestayDTO/HomestayDetailDTO` to build client-side pickers and validation before calling the API. When a booking succeeds, the backend now persists the submitted guest distribution into `BillGuest` rows so every bill keeps a snapshot of how many adults/children/babies were included.

### Order Detail Surface
`GET /customers/me/orders/{billId}` returns the exact guest distribution captured on the bill:

```json
"guestCapacity": [
  { "type": "ADULTS", "quantity": 4 },
  { "type": "CHILDREN", "quantity": 2 }
]
```

These values come from the stored `BillGuest` snapshot, so they always reflect what the customer entered—even if the host later updates the homestay capacity.

---

## Frontend Guidance

1. **Host UI**
   - Collect capacities for each type (`ADULTS`, `CHILDREN`, `BABY`) when creating a homestay.
   - Enforce non-negative integers and sync with server-provided defaults during edit flows.

2. **Customer Booking UI**
   - Render selectors for each allowed type using `personCapacities` from the homestay detail endpoint.
   - Ensure the sum respects the displayed min/max guest counts locally before submitting.
   - Surface backend error messages verbatim for edge cases (e.g., concurrent updates).

3. **Homestay Detail & Order Pages**
   - Show the configured capacity breakdown on detail pages and the actual booked distribution from `guestCapacity` on order detail screens so guests understand both the limits and what they submitted.

With these changes, FE can provide a consistent guest-capacity experience from discovery all the way to booking confirmation.
