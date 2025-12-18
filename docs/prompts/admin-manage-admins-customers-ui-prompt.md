# PROMPT: Tạo Giao Diện Quản Lý ADMIN và CUSTOMER cho ADMIN

## Mục đích
Tạo giao diện web cho module **Admin quản lý Admin và Customer** trong hệ thống quản lý homestay. Module này cho phép ADMIN xem danh sách admin/customer, và cập nhật trạng thái của họ.

---

## 📋 API ENDPOINTS

### 1. GET /admins - Lấy danh sách tất cả Admin
**URL:** `GET /admins?page=0&size=20`  
**Headers:** `Authorization: Bearer <adminToken>`

**Query Parameters:**
- `page` (optional, default: 0): Số trang
- `size` (optional, default: 20): Số lượng items mỗi trang

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Admins retrieved successfully",
  "data": {
    "page": 0,
    "size": 20,
    "total": 10,
    "items": [
      {
        "idAdmin": 1,
        "idUser": 5,
        "username": "admin1",
        "email": "admin1@example.com",
        "phone": "0901234567",
        "name": "Nguyễn Văn Admin",
        "status": "ACTIVE",
        "levelAdmin": "SUPER_ADMIN",
        "role": "ADMIN"
      }
    ]
  }
}
```

---

### 2. GET /admins/customers - Lấy danh sách tất cả Customer
**URL:** `GET /admins/customers?page=0&size=20`  
**Headers:** `Authorization: Bearer <adminToken>`

**Query Parameters:**
- `page` (optional, default: 0): Số trang
- `size` (optional, default: 20): Số lượng items mỗi trang

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Customers retrieved successfully",
  "data": {
    "page": 0,
    "size": 20,
    "total": 50,
    "items": [
      {
        "idCustomer": 1,
        "idUser": 10,
        "username": "customer1",
        "email": "customer1@example.com",
        "phone": "0901234567",
        "name": "Nguyễn Văn Customer",
        "status": "ACTIVE",
        "role": "CUSTOMER"
      }
    ]
  }
}
```

---

### 3. PUT /admins/status - Cập nhật trạng thái Admin
**URL:** `PUT /admins/status`  
**Headers:** 
- `Authorization: Bearer <adminToken>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "idAdmin": 1,
  "status": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Admin status updated successfully",
  "data": {
    "idAdmin": 1,
    "status": "ACTIVE",
    ...
  }
}
```

**Status hợp lệ:** `"ACTIVE"`, `"INACTIVE"`

---

### 4. PUT /admins/customers/status - Cập nhật trạng thái Customer
**URL:** `PUT /admins/customers/status`  
**Headers:** 
- `Authorization: Bearer <adminToken>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "idCustomer": 1,
  "status": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Customer status updated successfully",
  "data": {
    "idCustomer": 1,
    "status": "ACTIVE",
    ...
  }
}
```

**Status hợp lệ:** `"ACTIVE"`, `"INACTIVE"`

---

## 🎨 YÊU CẦU GIAO DIỆN

### Trang 1: Quản lý Admin (`/admin/admins`)

**Layout:**
- Header: "Quản lý Admin"
- Bảng danh sách với columns: STT, Tên, Email, SĐT, Level Admin, Trạng thái, Thao tác
- Pagination ở cuối
- Nút "Cập nhật trạng thái" trên mỗi row → mở modal chọn status (ACTIVE/INACTIVE)

**Status Badge:**
- ACTIVE: xanh lá (#10b981)
- INACTIVE: xám (#6b7280)

---

### Trang 2: Quản lý Customer (`/admin/customers`)

**Layout:**
- Header: "Quản lý Customer"
- Bảng danh sách với columns: STT, Tên, Email, SĐT, Trạng thái, Thao tác
- Pagination ở cuối
- Nút "Cập nhật trạng thái" trên mỗi row → mở modal chọn status (ACTIVE/INACTIVE)

**Status Badge:**
- ACTIVE: xanh lá (#10b981)
- INACTIVE: xám (#6b7280)

---

### Modal: Cập nhật trạng thái

**Layout:**
- Title: "Cập nhật trạng thái [Admin/Customer]"
- Hiển thị tên và ID
- Radio buttons hoặc Select: ACTIVE / INACTIVE
- Buttons: [Hủy] [Xác nhận]

**Flow:**
1. Click "Xác nhận" → Gọi API update status
2. Success → Toast + Đóng modal + Refresh table
3. Error → Toast với message lỗi

---

## 📦 TypeScript Interfaces

```typescript
interface AdminDTO {
  idAdmin: number;
  idUser: number;
  username: string | null;
  email: string;
  phone: string | null;
  name: string | null;
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
  name: string | null;
  status: "ACTIVE" | "INACTIVE";
  role: "CUSTOMER";
}

interface PageResponse<T> {
  page: number;
  size: number;
  total: number;
  items: T;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  timestamp: number;
}
```

---

## ✅ CHECKLIST

- [ ] Trang danh sách Admin với table + pagination
- [ ] Trang danh sách Customer với table + pagination
- [ ] Modal cập nhật trạng thái (dùng chung cho cả Admin và Customer)
- [ ] Status badges với màu sắc đúng
- [ ] Loading states
- [ ] Empty states
- [ ] Error handling với toast
- [ ] Responsive design

---

**Tạo code đầy đủ với TypeScript, React hooks, API integration, và styling. Code phải clean, có comments, và tuân thủ best practices.**

