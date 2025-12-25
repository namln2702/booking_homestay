# Customer Tier Feature Snapshot

## Overview
- Customer tiering remains read-only and derives entirely from booking history; no schema updates or behavior changes were introduced.
- A new `CustomerTier` enum (SILVER/GOLD/DIAMOND) and supporting DTOs isolate the feature from the existing booking/payment logic.

## Repository & Service Layer
- `BillRepository` now exposes a JPQL aggregation (`findTierStatsByCustomerId`) that counts SUCCEED and REFUNDED bills for a given customer via a projection interface (`CustomerTierStats`). No write queries were added.
- `CustomerTierService` + `CustomerTierServiceImpl` fetch the aggregated counts, calculate `effectiveOrders` (SUCCEED + REFUNDED * 0.4), and map the total to the correct tier before packaging it into `CustomerTierDTO`.

## API Surface
- New endpoint `GET /customers/{customerId}/tier` lets admins (or the customer acting as themselves) fetch the derived tier. Identity resolution leverages the existing `RequestIdentityResolver` so role rules stay consistent.
- Updated `GET /customers/me` to include tier information alongside the existing profile payload by returning a `CustomerProfileWithTierDTO` (customer profile + tier snapshot). If either underlying service call fails, the original error response is bubbled up.

## DTO Enhancements
- `CustomerTierDTO` exposes: `userId`, `tier`, `successfulBookings`, `refundedBookings`, and `effectiveOrders`.
- `CustomerProfileWithTierDTO` wraps the legacy `CustomerDTO` with the new tier DTO, keeping the derived data separate from persisted entities.

## Safety Guarantees
- No new tables/columns/migrations; no edits to booking, payment, escrow, or complaint flows.
- All tier calculations are read-only, stateless, and can be removed without impacting other features.
