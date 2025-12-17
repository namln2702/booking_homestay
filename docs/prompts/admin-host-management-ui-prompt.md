# PROMPT: Tạo Giao Diện Quản Lý HOST cho ADMIN

## Mục đích
Bạn là một AI chuyên tạo giao diện web. Hãy tạo đầy đủ UI/UX cho module **Admin quản lý Host** trong hệ thống quản lý homestay. Module này cho phép ADMIN xem danh sách hosts, xem chi tiết, và cập nhật trạng thái host.

---

## 📋 DANH SÁCH CẦN TẠO

### 1. **Trang chính: Danh sách Hosts** (`/admin/hosts` hoặc `/admin/hosts/list`)
### 2. **Modal/Drawer: Chi tiết Host** (mở từ trang danh sách)
### 3. **Component: Bảng danh sách hosts**
### 4. **Component: Filter tabs/dropdown**
### 5. **Component: Pagination**
### 6. **Component: Host Status Badge**
### 7. **Component: Update Host Status Modal** (cập nhật trạng thái host)

---

## 🔌 API ENDPOINTS

### 1. GET /hosts - Lấy danh sách hosts (có phân trang)
**URL:** `GET /hosts`  
**Headers:**
- `Authorization: Bearer <adminToken>`
- `Content-Type: application/json`

**Query Parameters:**
- `status` (optional, default: "PENDING"): Lọc theo trạng thái host
  - Giá trị hợp lệ: `"PENDING"`, `"ACTIVE"`, `"INACTIVE"`
- `page` (optional, default: 0): Số trang (bắt đầu từ 0)
- `size` (optional, default: 20): Số lượng items mỗi trang

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Hosts retrieved successfully",
  "data": {
    "page": 0,
    "size": 20,
    "total": 45,
    "items": [
      {
        "idHost": 1,
        "idUser": 10,
        "username": "host_user_1",
        "email": "host1@example.com",
        "phone": "0901234567",
        "isOnline": false,
        "avatarUrl": "https://cdn.example.com/avatars/host1.jpg",
        "age": 30,
        "name": "Nguyễn Văn A",
        "googleId": null,
        "statusHost": "PENDING",
        "businessName": "ABC Homestay",
        "qrCodeUrl": "https://cdn.example.com/qr/host1.png",
        "role": "HOST"
      }
    ]
  },
  "timestamp": 1735123500000
}
```

---

### 2. GET /admins/{userId} - Lấy chi tiết host theo userId
**URL:** `GET /admins/{userId}`  
**Headers:**
- `Authorization: Bearer <adminToken>`
- `Content-Type: application/json`

**Path Parameters:**
- `userId` (required): ID của user (cũng là idHost)

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Host detail retrieved successfully",
  "data": {
    "idHost": 1,
    "idUser": 10,
    "username": "host_user_1",
    "email": "host1@example.com",
    "phone": "0901234567",
    "isOnline": false,
    "avatarUrl": "https://cdn.example.com/avatars/host1.jpg",
    "age": 30,
    "name": "Nguyễn Văn A",
    "googleId": null,
    "statusHost": "PENDING",
    "businessName": "ABC Homestay",
    "qrCodeUrl": "https://cdn.example.com/qr/host1.png",
    "role": "HOST"
  },
  "timestamp": 1735123500000
}
```

**Error Responses:**
- `403`: Admin account is not active
- `404`: Host profile not found for user id {userId}

---

### 3. PUT /admins/host/status - Cập nhật trạng thái host
**URL:** `PUT /admins/host/status`  
**Headers:**
- `Authorization: Bearer <adminToken>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "idHost": 1,
  "statusHost": "ACTIVE"
}
```

**Response (200 OK - Cập nhật thành công):**
```json
{
  "status": 200,
  "message": "Host status updated successfully",
  "data": {
    "idHost": 1,
    "idUser": 10,
    "username": "host_user_1",
    "email": "host1@example.com",
    "phone": "0901234567",
    "isOnline": false,
    "avatarUrl": "https://cdn.example.com/avatars/host1.jpg",
    "age": 30,
    "name": "Nguyễn Văn A",
    "googleId": null,
    "statusHost": "ACTIVE",  // Đã được cập nhật
    "businessName": "ABC Homestay",
    "qrCodeUrl": "https://cdn.example.com/qr/host1.png",
    "role": "HOST"
  },
  "timestamp": 1735123500000
}
```

**Error Responses:**
- `400`: Host id is required / StatusHost is required
- `404`: Host not found for id {idHost}

**Các giá trị statusHost hợp lệ:**
- `"PENDING"` - Chờ duyệt
- `"ACTIVE"` - Đã duyệt / Đang hoạt động
- `"INACTIVE"` - Đã vô hiệu hóa

---

## TypeScript Interfaces

```typescript
// HostDTO
interface HostDTO {
  idHost: number;
  idUser: number;
  username: string | null;
  email: string;
  phone: string | null;
  isOnline: boolean | null;
  avatarUrl: string | null;
  age: number | null;
  name: string | null;
  googleId: string | null;
  statusHost: "PENDING" | "ACTIVE" | "INACTIVE";
  businessName: string | null;
  qrCodeUrl: string | null;
  role: "HOST" | "CUSTOMER" | "ADMIN";
}

// PageResponse
interface PageResponse<T> {
  page: number;
  size: number;
  total: number;
  items: T;
}

// ApiResponse
interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  timestamp: number;
}

// StatusHost enum
type StatusHost = "PENDING" | "ACTIVE" | "INACTIVE";

// HostStatusUpdateRequest
interface HostStatusUpdateRequest {
  idHost: number;
  statusHost: StatusHost;
}
```

---

## 🎨 THIẾT KẾ CHI TIẾT

### **TRANG 1: Admin Host Management Page**

#### Layout Structure:
```
┌─────────────────────────────────────────────────────────┐
│  Header (Admin Dashboard Header)                        │
├─────────────────────────────────────────────────────────┤
│  Breadcrumb: Admin > Quản lý Host                      │
│  Title: "Quản lý Host"                                 │
├─────────────────────────────────────────────────────────┤
│  [Filter Tabs]                                          │
│  ┌─────┐ ┌─────┐ ┌─────┐                               │
│  │ Tất cả │ │ Chờ duyệt │ │ Đã duyệt │ │ Đã vô hiệu │  │
│  └─────┘ └─────┘ └─────┘ └─────┘                      │
├─────────────────────────────────────────────────────────┤
│  [Table: Danh sách Hosts]                              │
│  ┌───────────────────────────────────────────────────┐ │
│  │ STT │ Tên │ Email │ SĐT │ Doanh nghiệp │ Status │ │
│  ├───────────────────────────────────────────────────┤ │
│  │  1  │ ... │ ... │ ... │ ... │ [Badge] │ [Actions]│ │
│  │  2  │ ... │ ... │ ... │ ... │ [Badge] │ [Actions]│ │
│  └───────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  [Pagination]                                           │
│  ← Previous │ Trang 1/3 │ Next →                      │
└─────────────────────────────────────────────────────────┘
```

#### Chi tiết từng phần:

**1. Header Section:**
- **Breadcrumb**: `Admin > Quản lý Host`
- **Page Title**: "Quản lý Host" (font size: 24px, font-weight: bold)
- **Subtitle**: "Quản lý và cập nhật trạng thái các host trên hệ thống" (màu xám, font size: 14px)

**2. Filter Tabs Section:**
- **Layout**: Horizontal tabs
- **Tabs**:
  - Tab 1: "Tất cả" (All) - `status = null`
  - Tab 2: "Chờ duyệt" (Pending) - `status = "PENDING"` - **Mặc định active**
  - Tab 3: "Đã duyệt" (Active) - `status = "ACTIVE"`
  - Tab 4: "Đã vô hiệu" (Inactive) - `status = "INACTIVE"`
- **Styling**:
  - Active tab: màu primary (xanh dương), underline
  - Inactive tab: màu xám
  - Hover effect: đổi màu nhẹ
- **Badge số lượng** (optional): Hiển thị số lượng hosts trong mỗi tab

**3. Table Section:**

**Columns:**
| STT | Tên Host | Email | Số điện thoại | Tên doanh nghiệp | Trạng thái | Thao tác |
|-----|----------|-------|---------------|------------------|------------|----------|
| 1 | Nguyễn Văn A | host1@example.com | 0901234567 | ABC Homestay | [Badge] | [Buttons] |

**Column Details:**
- **STT**: Số thứ tự (width: 60px, text-align: center)
- **Tên Host**: 
  - Hiển thị `name` hoặc `username` (fallback)
  - Có thể thêm avatar nhỏ (40x40px, circular) bên trái tên
  - Font-weight: 500
- **Email**: 
  - Màu xanh, có thể click để copy
  - Font-size: 14px
- **Số điện thoại**: 
  - Format: `090-123-4567` (nếu có)
  - Hiển thị "-" nếu null
- **Tên doanh nghiệp**: 
  - Hiển thị `businessName`
  - Hiển thị "-" nếu null
- **Trạng thái**: 
  - Component `HostStatusBadge` (xem chi tiết bên dưới)
- **Thao tác**: 
  - Nút "Xem chi tiết" (primary, outline)
  - Nút "Cập nhật trạng thái" (secondary) - mở modal để chọn status mới

**Table Styling:**
- Striped rows (zebra pattern)
- Hover effect trên mỗi row
- Border: 1px solid #e5e7eb
- Border-radius: 8px
- Padding: 16px
- Header: background #f9fafb, font-weight: 600

**4. Empty State:**
- Hiển thị khi `items.length === 0`
- Icon: Empty box hoặc User group icon
- Text: "Không có host nào"
- Subtext: "Chưa có host nào trong danh sách này"

**5. Loading State:**
- Skeleton loader cho table (5-10 rows)
- Hoặc spinner ở giữa table

**6. Pagination Section:**
- Layout: Center aligned
- Hiển thị: "Hiển thị 1-20 / 45 hosts"
- Buttons: 
  - "← Trước" (disabled khi page = 0)
  - Input: "Trang [__]" (jump to page)
  - "Sau →" (disabled khi page = lastPage)
- Page numbers: [1] [2] [3] ... (nếu có nhiều trang)

---

### **COMPONENT: HostStatusBadge**

**Props:**
```typescript
interface HostStatusBadgeProps {
  status: "PENDING" | "ACTIVE" | "INACTIVE";
}
```

**Styling:**
- **PENDING**: 
  - Background: #fef3c7 (vàng nhạt)
  - Text color: #d97706 (cam đậm)
  - Border: 1px solid #fbbf24
  - Text: "Chờ duyệt"
  - Icon: Clock icon (optional)

- **ACTIVE**: 
  - Background: #d1fae5 (xanh lá nhạt)
  - Text color: #059669 (xanh lá đậm)
  - Border: 1px solid #10b981
  - Text: "Đã duyệt"
  - Icon: Check circle icon (optional)

- **INACTIVE**: 
  - Background: #f3f4f6 (xám nhạt)
  - Text color: #6b7280 (xám đậm)
  - Border: 1px solid #9ca3af
  - Text: "Đã vô hiệu"
  - Icon: X circle icon (optional)

**CSS:**
- Padding: 4px 12px
- Border-radius: 12px
- Font-size: 12px
- Font-weight: 500
- Display: inline-flex
- Align-items: center
- Gap: 4px (nếu có icon)

---

### **MODAL/DRAWER: Host Detail**

**Trigger:** Click nút "Xem chi tiết" từ table

**Layout (Modal - 800px width):**
```
┌─────────────────────────────────────────────┐
│  [X] Chi tiết Host                          │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐ │
│  │  [Avatar: 120x120px, circular]        │ │
│  │  Tên: Nguyễn Văn A                    │ │
│  │  Email: host1@example.com             │ │
│  └───────────────────────────────────────┘ │
│                                             │
│  Thông tin cá nhân:                        │
│  • Số điện thoại: 0901234567               │
│  • Tuổi: 30                                │
│  • Trạng thái online: [Badge]              │
│                                             │
│  Thông tin Host:                           │
│  • Tên doanh nghiệp: ABC Homestay          │
│  • Trạng thái: [HostStatusBadge]           │
│  • QR Code: [Image: 200x200px]             │
│                                             │
│  [Footer Buttons]                          │
│  [Đóng] [Cập nhật trạng thái]              │
└─────────────────────────────────────────────┘
```

**Chi tiết:**

**Header:**
- Title: "Chi tiết Host"
- Close button (X) ở góc phải trên

**Body Sections:**

**1. Profile Section (Top):**
- Avatar: 120x120px, circular, border 3px solid primary
- Tên: Font-size 20px, font-weight: 600
- Email: Màu xanh, có thể click để copy

**2. Thông tin cá nhân:**
- Label: "Thông tin cá nhân" (font-weight: 600, margin-bottom: 12px)
- Fields:
  - Số điện thoại: `phone || "-"`
  - Tuổi: `age || "-"`
  - Trạng thái online: Badge (xanh nếu `isOnline = true`, xám nếu false)
  - Username: `username || "-"`
  - Google ID: `googleId || "Không có"`

**3. Thông tin Host:**
- Label: "Thông tin Host" (font-weight: 600, margin-bottom: 12px)
- Fields:
  - Tên doanh nghiệp: `businessName || "-"`
  - Trạng thái: `<HostStatusBadge status={statusHost} />`
  - QR Code: 
    - Nếu có `qrCodeUrl`: Hiển thị image 200x200px
    - Nếu không: Text "Chưa có QR Code"

**Footer:**
- Button "Đóng" (secondary/outline)
- Button "Cập nhật trạng thái" (primary) - mở modal cập nhật status

**Styling:**
- Modal width: 800px (desktop), 100% (mobile)
- Padding: 24px
- Border-radius: 8px
- Box-shadow: 0 10px 25px rgba(0,0,0,0.1)

---

### **MODAL: Update Host Status**

**Trigger:** Click nút "Cập nhật trạng thái" từ table hoặc modal chi tiết

**Layout:**
```
┌─────────────────────────────────────────────┐
│  [X] Cập nhật trạng thái Host              │
├─────────────────────────────────────────────┤
│  Host: Nguyễn Văn A (ID: 1)                │
│  Trạng thái hiện tại: [PENDING Badge]      │
│                                             │
│  Chọn trạng thái mới:                      │
│  ○ PENDING - Chờ duyệt                     │
│  ● ACTIVE - Đã duyệt                       │
│  ○ INACTIVE - Đã vô hiệu hóa               │
│                                             │
│  [Footer Buttons]                          │
│  [Hủy] [Xác nhận]                          │
└─────────────────────────────────────────────┘
```

**Chi tiết:**

**Header:**
- Title: "Cập nhật trạng thái Host"
- Close button (X)

**Body:**
- Hiển thị tên host và ID
- Hiển thị trạng thái hiện tại (badge)
- Radio buttons hoặc Select dropdown để chọn status mới:
  - PENDING - Chờ duyệt
  - ACTIVE - Đã duyệt
  - INACTIVE - Đã vô hiệu hóa
- Validation: Không cho chọn status giống với status hiện tại (disable option đó)

**Footer:**
- Button "Hủy" (secondary/outline)
- Button "Xác nhận" (primary) - gọi API update

**Flow:**
1. User chọn status mới
2. Click "Xác nhận"
3. Hiển thị loading
4. Gọi API `PUT /admins/host/status` với body:
   ```json
   {
     "idHost": 1,
     "statusHost": "ACTIVE"
   }
   ```
5. Success → Toast "Cập nhật trạng thái thành công" + Đóng modal + Refresh table
6. Error → Toast với message lỗi

---

## 🔄 USER FLOW

### Flow 1: Xem danh sách và cập nhật trạng thái
```
1. Admin vào trang /admin/hosts
   ↓
2. Trang load → Fetch GET /hosts?status=PENDING&page=0&size=20
   ↓
3. Hiển thị loading skeleton
   ↓
4. Render table với danh sách hosts PENDING
   ↓
5. Admin click tab "Đã duyệt" → Fetch lại với status=ACTIVE
   ↓
6. Admin click "Xem chi tiết" trên một host
   ↓
7. Mở modal chi tiết
   ↓
8. Admin click "Cập nhật trạng thái" trong modal
   ↓
9. Mở modal cập nhật status
   ↓
10. Admin chọn status mới (ví dụ: ACTIVE)
   ↓
11. Click "Xác nhận" → Gọi API PUT /admins/host/status
   ↓
12. Success → Toast + Đóng modal + Refresh table
```

### Flow 2: Cập nhật trạng thái trực tiếp từ table
```
1. Admin ở trang danh sách
   ↓
2. Click nút "Cập nhật trạng thái" trên một row
   ↓
3. Mở modal cập nhật status
   ↓
4. Chọn status mới → Confirm
   ↓
5. Success → Toast + Update row status trong table (hoặc refresh)
```

---

## 📱 RESPONSIVE DESIGN

### Desktop (≥ 1024px):
- Table full width
- Modal: 800px width, centered
- Tabs: Horizontal

### Tablet (768px - 1023px):
- Table: Scroll horizontal nếu cần
- Modal: 90% width
- Tabs: Horizontal (có thể scroll nếu nhiều)

### Mobile (< 768px):
- Table: Chuyển sang Card Layout
  - Mỗi host = 1 card
  - Card hiển thị: Avatar + Tên, Email, SĐT, Status, Actions
- Modal: Full screen hoặc 100% width
- Tabs: Có thể chuyển sang dropdown/select

**Card Layout Example (Mobile):**
```
┌─────────────────────────────┐
│  [Avatar] Nguyễn Văn A      │
│  📧 host1@example.com      │
│  📞 0901234567              │
│  🏢 ABC Homestay            │
│  [Status Badge]             │
│  [Xem chi tiết] [Cập nhật]  │
└─────────────────────────────┘
```

---

## 🎨 COLOR PALETTE & STYLING

### Colors:
- **Primary**: #3b82f6 (Blue)
- **Success**: #10b981 (Green)
- **Warning**: #f59e0b (Amber)
- **Danger**: #ef4444 (Red)
- **Gray**: #6b7280
- **Background**: #f9fafb
- **Border**: #e5e7eb

### Typography:
- **Font Family**: Inter, -apple-system, sans-serif
- **Heading 1**: 24px, font-weight: 700
- **Heading 2**: 20px, font-weight: 600
- **Body**: 14px, font-weight: 400
- **Small**: 12px, font-weight: 400

### Spacing:
- Container padding: 24px
- Section margin: 24px
- Card padding: 16px
- Button padding: 8px 16px

### Shadows:
- Card: `0 1px 3px rgba(0,0,0,0.1)`
- Modal: `0 10px 25px rgba(0,0,0,0.1)`
- Button hover: `0 2px 4px rgba(0,0,0,0.1)`

---

## 🔌 API INTEGRATION

### Base URL:
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
```

### Service Functions:

**1. getHosts**
```typescript
const getHosts = async (
  status?: "PENDING" | "ACTIVE" | "INACTIVE",
  page: number = 0,
  size: number = 20
): Promise<ApiResponse<PageResponse<HostDTO[]>>> => {
  const params = new URLSearchParams();
  if (status) params.append('status', status);
  params.append('page', page.toString());
  params.append('size', size.toString());
  
  const response = await fetch(`${API_BASE_URL}/hosts?${params}`, {
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) throw new Error('Failed to fetch hosts');
  return response.json();
};
```

**2. getHostDetail**
```typescript
const getHostDetail = async (userId: number): Promise<ApiResponse<HostDTO>> => {
  const response = await fetch(`${API_BASE_URL}/admins/${userId}`, {
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) throw new Error('Failed to fetch host detail');
  return response.json();
};
```

**3. updateHostStatus**
```typescript
const updateHostStatus = async (
  idHost: number, 
  statusHost: StatusHost
): Promise<ApiResponse<HostDTO>> => {
  const response = await fetch(`${API_BASE_URL}/admins/host/status`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      idHost,
      statusHost
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update host status');
  }
  return response.json();
};
```

---

## 📦 COMPONENT STRUCTURE

```
src/
  pages/
    admin/
      hosts/
        index.tsx                    # Trang chính
  components/
    admin/
      hosts/
        HostListTable.tsx            # Bảng danh sách
        HostDetailModal.tsx          # Modal chi tiết
        UpdateHostStatusModal.tsx    # Modal cập nhật status
        HostStatusBadge.tsx          # Badge trạng thái
        HostFilterTabs.tsx           # Filter tabs
        HostPagination.tsx            # Pagination
  services/
    adminHostService.ts              # API calls
  types/
    host.ts                          # TypeScript types
  hooks/
    useHosts.ts                      # Custom hook (optional)
```

---

## ✅ CHECKLIST IMPLEMENTATION

### Core Features:
- [ ] Trang danh sách hosts với table
- [ ] Filter tabs (Tất cả, PENDING, ACTIVE, INACTIVE)
- [ ] Pagination
- [ ] Modal chi tiết host
- [ ] Modal cập nhật trạng thái host
- [ ] Status badges với màu sắc đúng
- [ ] Loading states
- [ ] Empty states
- [ ] Error handling với toast notifications

### UI/UX:
- [ ] Responsive design (Desktop, Tablet, Mobile)
- [ ] Card layout cho mobile
- [ ] Hover effects
- [ ] Smooth transitions
- [ ] Accessibility (aria-labels, keyboard navigation)

### Integration:
- [ ] API service layer
- [ ] TypeScript types
- [ ] Error handling
- [ ] Token management
- [ ] Optimistic updates (optional)

---

## 🚀 YÊU CẦU CUỐI CÙNG

Hãy tạo đầy đủ:
1. **Code components** với TypeScript
2. **Styling** (CSS/Tailwind/styled-components)
3. **API integration** hoàn chỉnh
4. **Error handling** và loading states
5. **Responsive design** cho mọi màn hình
6. **Accessibility** cơ bản

Code phải:
- Clean, dễ đọc
- Có comments cho logic phức tạp
- Tuân thủ best practices
- Có thể chạy ngay sau khi copy

**Framework/Library gợi ý:**
- React + TypeScript
- Next.js (nếu cần SSR)
- UI Library: Ant Design / Material-UI / Chakra UI / Tailwind CSS
- State Management: React Query / SWR (cho data fetching)
- Toast: react-toastify / sonner

---

**BẮT ĐẦU TẠO GIAO DIỆN NGAY BÂY GIỜ!** 🎨

