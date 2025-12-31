## Payment QR Validation API

`POST /api/check-payment-qr` verifies whether a hosted image contains a VietQR/EMVCo payment QR, extracts basic metadata, and reports validation status.

### Request

```http
POST /api/check-payment-qr
Content-Type: application/json

{
  "imageUrl": "https://cdn.example.com/uploads/qr.png"
}
```

`imageUrl` must point to a publicly reachable HTTP/HTTPS resource. The service downloads the image, attempts to decode all QR codes, and inspects the first payload that matches the VietQR `000201` signature.

### Response

```json
{
  "success": true,
  "message": "Valid VietQR payment code",
  "data": {
    "isQr": true,
    "isPaymentQr": true,
    "isValidStructure": true,
    "isValidCRC": true,
    "amount": 150000,
    "bankCode": "970415"
  }
}
```

Field definitions:

| Field | Description |
| ----- | ----------- |
| `success` | `true` when the image was processed successfully (even if not a payment QR); `false` only for bad requests (400) or server errors (500). |
| `message` | Human-friendly outcome, e.g. “No QR code detected”, “Invalid EMVCo structure”, “Valid VietQR payment code”. |
| `isQr` | At least one QR code was decoded from the image. |
| `isPaymentQr` | A QR payload with the VietQR/EMVCo prefix `000201` was found. |
| `isValidStructure` | The payload included the required TLV tags (`00`,`38`,`53`,`58`,`63`). |
| `isValidCRC` | CRC-CCITT (0x1021) computed over the payload matches the embedded CRC tag. |
| `amount` | Value from tag `54` (if provided and numeric). |
| `bankCode` | Merchant account identifier extracted from tag `38` (BIN at `01`, fallback to `00`). |

HTTP semantics:
- **200 OK** → image downloaded and decoded; response flags describe the outcome.
- **400 Bad Request** → missing/blank/invalid `imageUrl` or non-HTTP/HTTPS scheme.
- **500 Internal Server Error** → download/decoding failure (unreachable host, unreadable image, ZXing error, etc.).

### Internal Flow

1. `QrDecoderUtil` downloads the image and decodes all QR payloads with ZXing (supports multiple QR codes).
2. `PaymentQrValidationService` iterates through each payload and looks for `000201`.
3. `EmvCoParser` builds a map of TLV pairs; required tags must exist.
4. `CrcValidator` recomputes CRC-CCITT over the payload (excluding the CRC value) and compares it to tag `63`.
5. The service extracts amount (`54`) and merchant account info (`38`, nested TLVs for BIN/GUID).

### Postman Test

1. Create a collection request `POST {{baseUrl}}/api/check-payment-qr`.
2. Body → raw JSON:
   ```json
   {
     "imageUrl": "{{paymentQrImageUrl}}"
   }
   ```
   Define collection/environment variables:
   - `baseUrl` – REST base (e.g., `http://localhost:8080`)
   - `paymentQrImageUrl` – direct link to a hosted VietQR image
3. In the Tests tab, add:

```javascript
pm.test("status is 200", function () {
  pm.response.to.have.status(200);
});

const body = pm.response.json();
pm.test("payload structure is valid", function () {
  pm.expect(body).to.have.property("success", true);
  pm.expect(body).to.have.property("message").that.is.a("string");
  pm.expect(body).to.have.property("data");
  pm.expect(body.data).to.have.property("isQr").that.is.a("boolean");
  pm.expect(body.data).to.have.property("isPaymentQr").that.is.a("boolean");
  pm.expect(body.data).to.have.property("isValidStructure").that.is.a("boolean");
  pm.expect(body.data).to.have.property("isValidCRC").that.is.a("boolean");
});

pm.test("payment QR specifics", function () {
  if (body.data.isPaymentQr) {
    pm.expect(body.data.isValidStructure).to.eql(true);
    pm.expect(body.data.bankCode).to.be.a("string");
  } else {
    pm.expect(body.message).to.match(/not a payment qr|No QR code detected/i);
  }
});
```

Running the collection reports success when the API responds with `200 OK` and the JSON contract matches expectations. Duplicate requests can be scripted against multiple sample images to cover positive (valid QR) and negative (non-payment QR) scenarios.***
