# Customer Review APIs – `POST /customers/user/review`, `PUT /customers/user/review/{reviewId}`, `GET /customers/user/reviews`

## Location & Responsibilities
- **Controller**: `CustomerController.reviewHomestay`, `updateReviewHomestay`, and `getMyReviews` (`src/main/java/org/example/do_an_v1/controller/CustomerController.java:59-86`) expose the create/update/list routes under the `/customers` base path.
- **Service flow**: `CustomerServiceImpl.reviewHomestay` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:525-633`) validates first-time reviews, persists them, and recalculates the homestay’s aggregate rating. `CustomerServiceImpl.updateReviewHomestay` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:635-733`) performs ownership checks and replaces the review payload, while `CustomerServiceImpl.getCustomerReviews` (`src/main/java/org/example/do_an_v1/service/impl/CustomerServiceImpl.java:735-754`) retrieves all reviews belonging to the authenticated customer.
- **DTO**: Both endpoints accept `ReviewDTO` (`src/main/java/org/example/do_an_v1/dto/ReviewDTO.java:12-20`) with optional `ImageDTO` attachments for review photos.

## Authentication & Identity
- Both methods are gated by `@PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")`.
- `RequestIdentityResolver.requireUserId(null)` is invoked per request, so the authenticated customer’s user ID is injected into the service call; the API ignores any `customerId` provided in the payload.
- Responses are wrapped in `ApiResponse` (`src/main/java/org/example/do_an_v1/payload/ApiResponse.java:8-27`), yielding `{ "status": <httpCode>, "message": "...", "data": <ReviewDTO>, "timestamp": <epochMillis> }`.

## Shared Payload – `ReviewDTO`

| Field | Create Review | Update Review | Notes |
| --- | --- | --- | --- |
| `rating` | **Required**, integer 1–5. | Optional (if present, must stay in 1–5). | Validation happens inside `CustomerServiceImpl`. |
| `comment` | **Required**, non-blank. | Optional; blank strings are rejected. | Trimmed before persistence. |
| `homestayId` | **Required**; determines which homestay the review belongs to. | Ignored (review homestay cannot change). | Must already be booked by the caller. |
| `imageUrls[]` | Optional list of `{ "id": ?, "image_url": "..." }`. | Optional, but when supplied the list fully replaces stored images. | Empty or null `image_url` entries are filtered out. |
| `id`, `customerId` | Ignored; these are set server-side. | Ignored. | `customerId` is derived from the authenticated user. |

## Endpoint: Create Review – `POST /customers/user/review`

### Request
```
POST /customers/user/review
Authorization: Bearer <customer JWT>
Content-Type: application/json
Body: ReviewDTO
```

Example body:
```json
{
  "homestayId": 88,
  "rating": 5,
  "comment": "Great stay – spotless room and friendly host.",
  "imageUrls": [
    { "image_url": "https://cdn.example.com/reviews/88/img-1.jpg" },
    { "image_url": "https://cdn.example.com/reviews/88/img-2.jpg" }
  ]
}
```

### Validation & Business Rules
- Customer/user lookup is mandatory; missing profiles return `status=404` + `"Customer profile not found for this user"`.
- `homestayId` must exist; otherwise the service throws `IllegalArgumentException("Homestay not found...")` which is surfaced as a 500-series error.
- The caller must have at least one bill for the homestay (`billRepository.findByHomestayAndCustomer`) and the bill’s status must already be `SUCCEED` or `REFUNDED`; otherwise the API responds with `status=403`.
- Duplicate reviews are blocked: existing review for the same homestay+customer yields `status=409`.
- When images are provided, every `image_url` is persisted as an `Image` linked to the review.
- After saving, `CustomerServiceImpl` recalculates the homestay’s average rating across all reviews and updates `Homestay.rating`.

### Success Response (`201`)
```json
{
  "status": 201,
  "message": "Review submitted successfully",
  "data": {
    "id": 321,
    "homestayId": 88,
    "customerId": 45,
    "rating": 5,
    "comment": "Great stay – spotless room and friendly host.",
    "imageUrls": [
      { "id": 9001, "image_url": "https://cdn.example.com/reviews/88/img-1.jpg", "isPrimary": null }
    ]
  },
  "timestamp": 1714826400000
}
```
`customerId` is included by `ReviewMapper` so the frontend can correlate the review with the caller.

## Endpoint: Update Review – `PUT /customers/user/review/{reviewId}`

### Request
```
PUT /customers/user/review/{reviewId}
Authorization: Bearer <customer JWT>
Content-Type: application/json
Body: ReviewDTO (include only the fields you want to change)
```

Example body:
```json
{
  "rating": 4,
  "comment": "Adjusting after the host resolved our issue.",
  "imageUrls": []
}
```

### Validation & Business Rules
- `reviewId` must belong to the authenticated customer; mismatches return `status=403` with `"You can only update your own reviews"`.
- The review must already exist; missing IDs raise `IllegalArgumentException("Review not found...")`.
- `rating` and `comment` are optional but, when provided, undergo the same range/non-blank checks as the create endpoint.
- `imageUrls` acts as a full replacement: the service deletes all previous images (`imageRepository.deleteAll`) before saving the new set. Supplying an empty array clears every attachment; omitting `imageUrls` leaves the existing photos untouched.
- The method maps the updated entity back to `ReviewDTO` and returns `status=200`.

### Success Response (`200`)
```json
{
  "status": 200,
  "message": "Review updated successfully",
  "data": {
    "id": 321,
    "homestayId": 88,
    "customerId": 45,
    "rating": 4,
    "comment": "Adjusting after the host resolved our issue.",
    "imageUrls": []
  },
  "timestamp": 1714828200000
}
```

## Endpoint: List Reviews – `GET /customers/user/reviews`

### Request
```
GET /customers/user/reviews
Authorization: Bearer <customer JWT>
```

### Behavior
- Uses the authenticated user ID (via `RequestIdentityResolver`) and fetches the associated `Customer`.
- Returns `404` when no customer profile exists for the account.
- Reviews are ordered newest-first (`reviewRepository.findByCustomerOrderByCreatedAtDesc`); each entry is mapped via `ReviewMapper`.
- Response payload is `List<ReviewDTO>` so FE receives the same shape as create/update responses, including `imageUrls`.

### Success Response (`200`)
```json
{
  "status": 200,
  "message": "Customer reviews retrieved successfully",
  "data": [
    {
      "id": 321,
      "homestayId": 88,
      "customerId": 45,
      "rating": 4,
      "comment": "Adjusting after the host resolved our issue.",
      "imageUrls": []
    },
    {
      "id": 298,
      "homestayId": 12,
      "customerId": 45,
      "rating": 5,
      "comment": "Loved this stay!",
      "imageUrls": [
        { "id": 9010, "image_url": "https://cdn.example.com/reviews/12/img.jpg", "isPrimary": null }
      ]
    }
  ],
  "timestamp": 1714829200000
}
```

## Failure Responses & Handling Tips

| `status` | Message (example) | When it happens |
| --- | --- | --- |
| `400/500` | `User id is required`, `Homestay id is required`, etc. | Validation failures triggered by `IllegalArgumentException`; surfaced as error responses by Spring’s exception handler. |
| `403` | `You must book this homestay before reviewing`, `You can only update your own reviews` | Business-rule guards on booking ownership or review ownership. |
| `404` | `Customer profile not found for this user`, `Homestay not found for id ...` | Missing customer profile or homestay. |
| `409` | `You have already reviewed this homestay` | Attempting to create a second review for the same homestay. |

Frontend clients should treat the `status` and `message` fields inside `ApiResponse` as the source of truth rather than the raw HTTP code when rendering error banners.
