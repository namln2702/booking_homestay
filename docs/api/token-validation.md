# Token Validation APIs – `GET /tokens/validate`, `POST /tokens/validate`

## Location & Purpose
- **Controller**: `TokenController` (`src/main/java/org/example/do_an_v1/controller/TokenController.java`) exposes two validation entry points under the `/tokens` base path.
- **Service layer**: Both endpoints delegate to `SecurityService.checkTokenExpiration` (`src/main/java/org/example/do_an_v1/service/impl/SecurityService.java:98-135`), which parses the JWT, verifies the signature, checks the expiry, and looks up revoked IDs through `InvalidateTokenRepository`.
- **Response model**: The result is wrapped in `ApiResponse<TokenValidationResponse>` (`src/main/java/org/example/do_an_v1/dto/response/TokenValidationResponse.java`), where `TokenValidationResponse` contains `isValid` and, when invalid, a human-readable `reason`.

> These endpoints currently have no `@PreAuthorize` annotations, so the gateway or API management layer should apply appropriate rate limiting if exposed publicly.

## `GET /tokens/validate`

### Usage
- Use when you want to pass tokens through headers or query parameters (e.g., quick checks from UI or Postman).
- The controller prioritizes the `Authorization` header and falls back to the `token` query parameter.

```
GET /tokens/validate
Authorization: Bearer <jwt-token>
```
or
```
GET /tokens/validate?token=<jwt-token>
```

### Success Responses
- **Valid token** → `status=200`, `message="Token hợp lệ"`, data payload with `isValid=true`.
- **Expired/invalid token** → `status=401`, `message=<reason from SecurityService>`, `isValid=false`.

Example:
```json
{
  "status": 200,
  "message": "Token hợp lệ",
  "data": {
    "isValid": true,
    "reason": null
  },
  "timestamp": 1715000000000
}
```

### Error Responses
| HTTP status | Message | When it occurs |
| --- | --- | --- |
| `400` | `Token is required. Please provide token via Authorization header ...` | Missing header and query parameter. |
| `401` | `Token error or expired`, `Token invalidate`, etc. | `SecurityService.verifyToken` throws `JOSEException` (expired, revoked, signature mismatch). |

## `POST /tokens/validate`

### Usage
- Accepts a JSON body when passing tokens explicitly from frontend logic.
- Request shape:

```http
POST /tokens/validate
Content-Type: application/json

{
  "token": "<jwt-token>"
}
```

### Responses
- Shares the same success/failure semantics as the GET variant: valid tokens return `status=200`, invalid ones return `status=401` and propagate the failure reason.
- Missing `token` in the body yields `status=400` with the message `Token is required in request body: {"token": "<your-jwt-token>"}`.

### Sample Invalid Token Response
```json
{
  "status": 401,
  "message": "Token error or expired",
  "data": {
    "isValid": false,
    "reason": "Token error or expired"
  },
  "timestamp": 1715000100000
}
```

## Frontend Integration Tips
- Cache the latest validation result to avoid redundant calls; tokens that fail validation should trigger a logout/refresh flow.
- Because the backend responds with both HTTP status and an explanatory `message`, prefer the `message`/`reason` fields to drive user-friendly prompts (e.g., “Session expired – please log in again.”).
- If your client already includes the JWT in the `Authorization` header for other requests, reuse that same header when calling `GET /tokens/validate` to avoid duplicating query parameters.
