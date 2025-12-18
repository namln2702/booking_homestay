# PROMPT: API Detail Admin và Customer

## API 1: GET /admins/{idAdmin} - Lấy chi tiết Admin
**URL:** `GET /admins/{idAdmin}`  
**Headers:** `Authorization: Bearer <adminToken>`

**Path Parameters:**
- `idAdmin` (required): ID của admin

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Admin detail retrieved successfully",
  "data": {
    "idAdmin": 1,
    "idUser": 5,
    "username": "admin1",
    "email": "admin1@example.com",
    "phone": "0901234567",
    "isOnline": false,
    "avatarUrl": "https://cdn.example.com/avatars/admin1.jpg",
    "age": 30,
    "name": "Nguyễn Văn Admin",
    "googleId": null,
    "status": "ACTIVE",
    "levelAdmin": "SUPER_ADMIN",
    "role": "ADMIN"
  }
}
```

**Error:**
- `400`: Admin id is required
- `404`: Admin not found for id {idAdmin}

---

## API 2: GET /admins/customers/{idCustomer} - Lấy chi tiết Customer
**URL:** `GET /admins/customers/{idCustomer}`  
**Headers:** `Authorization: Bearer <adminToken>`

**Path Parameters:**
- `idCustomer` (required): ID của customer

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Customer detail retrieved successfully",
  "data": {
    "idCustomer": 1,
    "idUser": 10,
    "username": "customer1",
    "email": "customer1@example.com",
    "phone": "0901234567",
    "isOnline": false,
    "avatarUrl": "https://cdn.example.com/avatars/customer1.jpg",
    "age": 25,
    "name": "Nguyễn Văn Customer",
    "googleId": null,
    "status": "ACTIVE",
    "dateOfBirth": "1999-01-01",
    "qrCodeUrl": "https://cdn.example.com/qr/customer1.png",
    "lastBooking": "2024-12-01",
    "role": "CUSTOMER",
    "listPreference": [1, 2, 3]
  }
}
```

**Error:**
- `400`: Customer id is required
- `404`: Customer not found for id {idCustomer}

---

## Yêu cầu giao diện

Tạo modal/drawer hiển thị chi tiết Admin hoặc Customer khi click "Xem chi tiết" từ bảng danh sách.

**Layout:**
- Avatar (120x120px, circular)
- Tên, Email, SĐT
- Thông tin cá nhân: Tuổi, Trạng thái online, Google ID
- Thông tin Admin: Level Admin, Status (badge)
- Thông tin Customer: Date of Birth, QR Code, Last Booking, Preferences
- Button "Đóng" và "Cập nhật trạng thái"

**TypeScript:**
```typescript
interface AdminDTO {
  idAdmin: number;
  idUser: number;
  username: string | null;
  email: string;
  phone: string | null;
  isOnline: boolean | null;
  avatarUrl: string | null;
  age: number | null;
  name: string | null;
  googleId: string | null;
  status: "ACTIVE" | "INACTIVE";
  levelAdmin: "ADMIN" | "SUPER_ADMIN" | null;
  role: "ADMIN";
}

interface CustomerDTO {
  idCustomer: number;
  idUser: number;
  username: string | null;
  email: string;
  phone: string | null;
  isOnline: boolean | null;
  avatarUrl: string | null;
  age: number | null;
  name: string | null;
  googleId: string | null;
  status: "ACTIVE" | "INACTIVE";
  dateOfBirth: string | null;
  qrCodeUrl: string | null;
  lastBooking: string | null;
  role: "CUSTOMER";
  listPreference: number[] | null;
}
```

