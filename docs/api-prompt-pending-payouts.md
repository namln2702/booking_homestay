# Prompt cho AI Frontend - API Lấy danh sách Host với Transaction đang chờ hoàn tiền

## Yêu cầu

Hãy viết code TypeScript/JavaScript để gọi API lấy danh sách các Host với các Homestay và các transaction đang chờ hoàn tiền (pending payout transactions).

## Thông tin API

**Endpoint:** `GET /admins/transactions/pending-payouts-by-host`

**Base URL:** `{API_BASE_URL}/admins/transactions/pending-payouts-by-host`

**Method:** `GET`

**Authentication:** 
- Yêu cầu Bearer Token trong header
- Token phải có quyền `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN`
- Header: `Authorization: Bearer {token}`

**Request Parameters:** Không có (không cần query params)

## Response Structure

### Success Response (200 OK)

```typescript
interface ApiResponse<T> {
  code: number;        // 200
  message: string;     // "Hosts with pending payout transactions retrieved successfully"
  data: T;
}

interface HostWithPendingPayoutTransactionsResponse {
  host: HostDTO;
  homestays: HomestayWithTransactionsDTO[];
}

interface HomestayWithTransactionsDTO {
  homestay: HomestayDTO;
  transactions: TransactionDTO[];
}

interface HostDTO {
  // Thông tin ID
  idHost: number;
  idUser: number;
  
  // Thông tin từ bảng User
  username: string | null;
  email: string | null;
  phone: string | null;
  isOnline: boolean | null;
  avatarUrl: string | null;
  age: number | null;
  name: string | null;
  googleId: string | null;
  
  // Thông tin từ bảng Host
  statusHost: "PENDING" | "ACTIVE" | "INACTIVE" | "BANNED";
  businessName: string | null;
  qrCodeUrl: string | null;
  role: "HOST";
}

interface HomestayDTO {
  id: number;
  hostId: number;
  title: string | null;
  description: string | null;
  category: string | null;
  rating: number | null;
  numbersOfReview: number | null;
  minGuest: number | null;
  maxGuest: number | null;
  numBedrooms: number | null;
  numBeds: number | null;
  numBathrooms: number | null;
  numKitchen: number | null;
  advancedPayment: number | null;
  warningCount: number | null;
  basePrice: number | null;
  status: "PENDING" | "ACTIVE" | "INACTIVE" | "REJECTED" | "BANNED";
  
  address: {
    addressLine: string | null;
    city: string | null;
    state: string | null;
    latitude: string | null;
    longitude: string | null;
  } | null;
  
  facilities: Array<{
    id: number;
    name: string | null;
    category: string | null;
  }> | null;
  
  amenities: Array<{
    id: number;
    name: string | null;
    description: string | null;
    imageUrl: string | null;
  }> | null;
  
  rules: Array<{
    id: number;
    description: string | null;
    ruleType: string;
  }> | null;
  
  dailyPrices: Array<{
    id: number;
    day: string; // Date ISO string
    price: number | null;
    booked: boolean | null;
  }> | null;
  
  images: Array<{
    id: number;
    imageUrl: string | null;
    primary: boolean | null;
  }> | null;
  
  personCapacities: Array<{
    type: "ADULTS" | "CHILDREN" | "BABY";
    quantity: number | null;
  }> | null;
}

interface TransactionDTO {
  id: number;
  amount: number; // BigDecimal được serialize thành number
  transactionType: "ADMIN_PAYMENT_HOST" | "PAYLOAD_HOST" | "REFUND" | "BOOKING_PAYMENT" | "CUSTOMER_PAYMENT_ADMIN" | "CUSTOMER_PAYMENT_ADMIN_FIRST" | "CUSTOMER_PAYMENT_ADMIN_SECOND";
  status: "PENDING" | "SUCCESS" | "FAILED";
  completedAt: string | null; // ISO DateTime string
  
  // Thông tin người gửi
  fromUserId: number | null;
  fromUserEmail: string | null;
  
  // Thông tin người nhận
  toUserId: number | null;
  toUserEmail: string | null;
  
  // Thông tin Bill
  billId: number | null;
  billCode: string | null;
  
  // Thông tin chứng minh
  proofImageUrl: string | null;
}
```

### Error Responses

- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** Token không có quyền ADMIN hoặc SUPER_ADMIN
- **500 Internal Server Error:** Lỗi server

## Ví dụ Response

```json
{
  "code": 200,
  "message": "Hosts with pending payout transactions retrieved successfully",
  "data": [
    {
      "host": {
        "idHost": 1,
        "idUser": 10,
        "username": "host_user_1",
        "email": "host1@example.com",
        "phone": "0123456789",
        "isOnline": true,
        "avatarUrl": "https://example.com/avatar.jpg",
        "age": 30,
        "name": "Nguyễn Văn A",
        "googleId": null,
        "statusHost": "ACTIVE",
        "businessName": "Homestay ABC",
        "qrCodeUrl": "https://example.com/qr.jpg",
        "role": "HOST"
      },
      "homestays": [
        {
          "homestay": {
            "id": 5,
            "hostId": 1,
            "title": "Homestay đẹp view biển",
            "description": "Mô tả homestay...",
            "category": "Beach",
            "rating": 4.5,
            "numbersOfReview": 20,
            "minGuest": 2,
            "maxGuest": 6,
            "numBedrooms": 2,
            "numBeds": 3,
            "numBathrooms": 2,
            "numKitchen": 1,
            "advancedPayment": 30,
            "warningCount": 0,
            "basePrice": 500000,
            "status": "ACTIVE",
            "address": {
              "addressLine": "123 Đường ABC",
              "city": "Nha Trang",
              "state": "Khánh Hòa",
              "latitude": "12.2388",
              "longitude": "109.1967"
            },
            "facilities": [],
            "amenities": [],
            "rules": [],
            "dailyPrices": [],
            "images": [],
            "personCapacities": []
          },
          "transactions": [
            {
              "id": 100,
              "amount": 2000000,
              "transactionType": "ADMIN_PAYMENT_HOST",
              "status": "PENDING",
              "completedAt": null,
              "fromUserId": 1,
              "fromUserEmail": "admin@example.com",
              "toUserId": 10,
              "toUserEmail": "host1@example.com",
              "billId": 50,
              "billCode": "BILL-2024-001",
              "proofImageUrl": null
            }
          ]
        }
      ]
    }
  ]
}
```

## Yêu cầu Implementation

1. **Tạo service/API function** để gọi endpoint này
2. **Xử lý authentication:** Đảm bảo gửi Bearer token trong header
3. **Xử lý errors:** Xử lý các trường hợp 401, 403, 500
4. **TypeScript types:** Định nghĩa đầy đủ các interface/type cho response
5. **Error handling:** Có thông báo lỗi rõ ràng cho user
6. **Loading state:** Quản lý trạng thái loading khi gọi API
7. **Empty state:** Xử lý trường hợp không có dữ liệu (data rỗng)

## Use Case

API này được sử dụng trong trang quản trị để:
- Hiển thị danh sách các Host đang có transaction chờ hoàn tiền
- Xem chi tiết các Homestay và transaction của từng Host
- Admin có thể xem và xử lý các transaction pending này

## Lưu ý

- Tất cả các transaction trong response đều có `status: "PENDING"` và `transactionType: "ADMIN_PAYMENT_HOST"` (hoặc "PAYLOAD_HOST")
- Response có thể trả về mảng rỗng nếu không có transaction nào đang chờ
- Mỗi Host có thể có nhiều Homestay, mỗi Homestay có thể có nhiều transaction pending


