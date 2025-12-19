# Admin Billing & Finance APIs

This document summarizes the new admin endpoints that expose bill listings, per-bill transactions, and top-level finance reporting for the frontend team. Authentication/authorization is temporarily relaxed per current phase; once role enforcement comes back these endpoints will require an admin token.

## List All Bills (`GET /admins/bills`)

### Request
| Query | Type | Required | Description |
|-------|------|----------|-------------|
| `page` | number | No (default `0`) | Zero-based page index. |
| `size` | number | No (default `20`) | Page size. |
| `status` | `StatusBill` | No | Filter by bill status (e.g., `SUCCEED`, `REFUNDED`). |
| `customerId` | number | No | Only bills created by this customer id. |
| `hostId` | number | No | Bills belonging to homestays owned by this host id. |
| `homestayId` | number | No | Bills for a specific homestay id. |

Example:
```
GET /admins/bills?page=0&size=10&status=SUCCEED
```

### Response
`ApiResponse<PageResponse<List<BillDTO>>>`

```json
{
  "status": 200,
  "message": "Bills retrieved successfully",
  "data": {
    "page": 0,
    "size": 10,
    "total": 53,
    "items": [
      {
        "id": 123,
        "code": "BILL-2024-000123",
        "status": "SUCCEED",
        "createdAt": "2024-05-01T08:30:00",
        "updatedAt": "2024-05-04T10:15:00",
        "checkIn": "2024-05-10T14:00:00",
        "checkOut": "2024-05-15T11:00:00",
        "actualCheckin": "2024-05-10T13:55:00",
        "totalAmount": 3500000,
        "customerDTO": { "...": "..." },
        "customerBookingInfoDTO": { "...": "..." },
        "homestayDTO": {
          "id": 77,
          "title": "Cozy Loft",
          "status": "ACTIVE",
          "hostId": 9,
          "city": "Da Nang"
        },
        "addressDTO": { "...": "..." },
        "homestayDailyPricesDTOS": [
          { "id": 991, "price": 700000 }
        ],
        "transactions": [
          {
            "id": 810,
            "amount": 1050000,
            "transactionType": "CUSTOMER_PAYMENT_ADMIN_FIRST",
            "status": "SUCCESS",
            "billId": 123
          }
        ]
      }
    ]
  },
  "timestamp": 1714711111111
}
```

Key fields:
- `totalAmount`: 100% bill value.
- `transactions`: includes deposit, remaining payment, refund records (type + status).
- `customerDTO`, `homestayDTO`, and `addressDTO` give enough context for list/table views.

## Bill Transactions (`GET /admins/bills/{billId}/transactions`)

### Request
- Path parameter `billId` (number) – required.

Example:
```
GET /admins/bills/123/transactions
```

### Response
`ApiResponse<List<TransactionDTO>>`

```json
{
  "status": 200,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": 820,
      "amount": 2450000,
      "transactionType": "CUSTOMER_PAYMENT_ADMIN_SECOND",
      "status": "SUCCESS",
      "completedAt": "2024-05-10T15:40:00",
      "fromUserId": 55,
      "fromUserEmail": "customer@example.com",
      "toUserId": 2,
      "toUserEmail": "admin@example.com",
      "billId": 123,
      "billCode": "BILL-2024-000123",
      "proofImageUrl": null
    },
    {
      "id": 810,
      "amount": 1050000,
      "transactionType": "CUSTOMER_PAYMENT_ADMIN_FIRST",
      "status": "SUCCESS",
      "completedAt": "2024-04-30T18:00:00",
      "fromUserId": 55,
      "toUserId": 2,
      "billId": 123,
      "billCode": "BILL-2024-000123"
    }
  ],
  "timestamp": 1714711111111
}
```

Notes:
- List is sorted newest-first by transaction `createdAt`.
- Each transaction contains sender/receiver ids/emails to render flows (customer→admin, admin→host, admin→customer refund).
- `proofImageUrl` is populated for admin-confirmed refunds; the UI can display or link the evidence image.

## Finance Report (`GET /admins/reports/finance`)

### Request
- No query/body parameters.

### Response
`ApiResponse<AdminFinanceReportResponse>`

```json
{
  "status": 200,
  "message": "Finance report generated successfully",
  "data": {
    "totalBills": 214,
    "completedBills": 173,
    "totalRevenueFromCustomers": 812500000,
    "totalPayoutToHosts": 610000000,
    "totalRefundsToCustomers": 35000000,
    "netRevenue": 167500000
  },
  "timestamp": 1714711111111
}
```

Field meaning:
- `totalRevenueFromCustomers`: Sum of successful customer→admin transactions (`CUSTOMER_PAYMENT_ADMIN*` and `BOOKING_PAYMENT`).
- `totalPayoutToHosts`: Sum of successful admin→host transfers (`ADMIN_PAYMENT_HOST`, `PAYLOAD_HOST`).
- `totalRefundsToCustomers`: Sum of successful refund transactions.
- `netRevenue`: Revenue minus payouts minus refunds.
- `completedBills`: Number of bills in terminal states (SUCCEED, REFUNDED, REJECTED, CHECKIN_EXPIRED, CANCELLED).

## Notes

- Data comes from `Bill` and `Transaction` tables directly. Current implementation uses in-memory filtering/pagination for bills; if volume grows we can migrate to a JPA spec later.
- No admin role verification is required yet, so the frontend can integrate immediately; remember to add token handling once security is re-enabled.
