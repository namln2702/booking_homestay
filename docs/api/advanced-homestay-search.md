# Advanced Homestay Search API

Provides a fuzzy, availability-aware search surface that powers the new booking discovery screen.

## Endpoint

| Method | Path | Controller | Service |
|--------|------|------------|---------|
| `POST` | `/api/homestays/search/advanced` | `HomestaySearchController#searchAdvanced` | `HomestayServiceImpl#searchHomestayAdvanced` |

- Request/response body uses the shared `ApiResponse` wrapper.
- Controller lives in `src/main/java/org/example/do_an_v1/controller/HomestaySearchController.java`.

## Request Body

```
{
  "keyword": "hoi an garden",
  "city": "Hội An",
  "state": "Minh An",
  "numberAdults": 2,
  "numberChildren": 1,
  "numberBaby": 0,
  "begin": "2025-12-28",
  "end": "2026-01-02"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `keyword` | string | Optional trigram search against homestay title and address parts (case-insensitive, diacritic-insensitive). |
| `city` / `state` | string | Optional exact filters; trimmed and downcased server-side. |
| `numberAdults` / `numberChildren` / `numberBaby` | integer | Optional capacity filters; only positive values matter. |
| `begin` / `end` | ISO `yyyy-MM-dd` string | Optional stay window. If both present, `begin` must be `<= end`. |

DTO definition: `src/main/java/org/example/do_an_v1/dto/AdvancedHomestaySearchDTO.java`.

## Behavior

1. **Input normalization** – Empty strings become `null`, strings are `trim().toLowerCase()`, and dates are parsed via `LocalDate.parse` (throws `IllegalArgumentException` on invalid format).
2. **Repository query** – `HomestayRepository#searchHomestayAdvanced` (native SQL) performs:
   - `unaccent(lower(...)) % ...` trigram matching to score keyword hits on titles and address fields.
   - City/state equality checks when supplied.
   - Capacity gating per traveler type through `tbl_person_homestay`.
   - Availability filtering by rejecting listings booked inside `[begin, end]`.
   - Ordering by trigram similarity (when keyword provided) then newest `created_at`.
3. **Response mapping** – Each `Homestay` is converted to `HomestayDTO` with images, wrapped in `ApiResponse` `{ status: 200, message: "Success", data: [...] }`.

## Sample Response

```
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 4010,
      "title": "Hoàn Kiếm Heritage",
      "city": "Hà Nội",
      "basePrice": 1500000,
      "rating": 4.7,
      "images": [
        { "url": "https://cdn.demo.vn/homestays/4010/cover.jpg" }
      ],
      "hostId": 3003,
      "...": "additional HomestayDTO fields"
    }
  ]
}
```

## Notes for Frontend

- Send only the filters the guest actually set; omitted or blank fields are treated as wildcards.
- Dates must be formatted as `yyyy-MM-dd`. Validate client-side to avoid `400` responses.
- Pagination is not yet exposed; results return as a full list. Apply client-side limits/spinners if the dataset grows.
