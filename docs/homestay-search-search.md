## Homestay Advanced Search Enhancements

### Overview
The `/api/homestays/search/advanced` endpoint continues to accept the same `AdvancedHomestaySearchDTO` payload and response DTOs, but its keyword filter now inspects a wider set of fields that describe a listing. This keeps the client contract unchanged while helping end users discover homestays using richer metadata.

### Fields Covered by Keyword Matching
The `HomestayRepository.searchHomestayAdvanced` native query now checks:

1. **Homestay core data** – `title`, `description`, and `category`.
2. **Address information** – `city`, `state`, and `address_line`.
3. **Host profile** – `business_name`, plus the linked user’s `name`, `username`, `email`, and `phone`.
4. **Rules** – every `tbl_homestay_rules.description` tied to the homestay.
5. **Facilities** – `tbl_facilities.name` and `category` through `tbl_homestays_list_facilities`.
6. **Amenities** – `tbl_amenities.name` and `description` through `tbl_homestays_list_amenities`.

All comparisons use `unaccent(lower(...))` with PostgreSQL’s trigram `%` operator, matching the previous behavior for `title`/`city` and preventing regressions in ranking.

### Logic Preservation
The following behavior remains untouched:

- Filtering by `city`, `state`, and headcount requirements.
- Availability checks via `tbl_homestay_daily_prices` / `tbl_price_per_days`.
- Result ordering (primary similarity on homestay title, then city, then creation date).
- Response DTO mapping with attached images.

Because the new matches live inside `OR EXISTS` subqueries, a homestay still appears only once even when multiple related rows hit the keyword.

### Validation
No automated tests were run for this repository-level SQL change. Please execute `./mvnw test` or call the endpoint in your environment to confirm performance and relevance with real data.***
