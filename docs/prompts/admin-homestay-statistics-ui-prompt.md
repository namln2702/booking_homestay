# PROMPT: API Thống Kê Homestay cho Admin

## API: GET /admins/homestays/statistics
**URL:** `GET /admins/homestays/statistics`  
**Headers:** `Authorization: Bearer <adminToken>`

**Request:** Không có body, không có query parameters

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "All homestay statistics retrieved successfully",
  "data": [
    {
      "homestayId": 1,
      "homestayTitle": "Beautiful Beach House",
      "totalBookings": 25,
      "totalRevenue": 125000000.00,
      "totalComplaints": 3
    },
    {
      "homestayId": 2,
      "homestayTitle": "Mountain View Cabin",
      "totalBookings": 15,
      "totalRevenue": 75000000.00,
      "totalComplaints": 1
    }
  ]
}
```

**Error Responses:**
- `401`: Unauthorized (thiếu hoặc token không hợp lệ)

---

## Yêu cầu giao diện

Tạo trang thống kê homestay cho Admin với bảng hiển thị danh sách thống kê.

**Layout:**
- Header: "Thống Kê Homestay"
- Bảng với các cột:
  - **ID Homestay** (homestayId)
  - **Tên Homestay** (homestayTitle)
  - **Tổng Số Booking** (totalBookings) - format số với dấu phẩy
  - **Tổng Doanh Thu** (totalRevenue) - format tiền VNĐ (ví dụ: 125,000,000 đ)
  - **Tổng Khiếu Nại** (totalComplaints) - highlight màu đỏ nếu > 0
- Có thể sắp xếp theo từng cột (sortable)
- Có thể tìm kiếm theo tên homestay
- Hiển thị tổng số homestays ở header
- Loading state khi đang fetch data
- Error handling khi API fail

**TypeScript:**
```typescript
interface HomestayStatisticsDTO {
  homestayId: number;
  homestayTitle: string | null;
  totalBookings: number;
  totalRevenue: number; // BigDecimal từ backend
  totalComplaints: number;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

**Features:**
- Format số: `totalBookings` hiển thị với dấu phẩy (ví dụ: 1,234)
- Format tiền: `totalRevenue` hiển thị VNĐ (ví dụ: 125,000,000 đ)
- Badge cảnh báo: Nếu `totalComplaints > 0` thì hiển thị badge màu đỏ
- Click vào row có thể navigate đến trang chi tiết homestay (nếu có)
- Responsive: Bảng có thể scroll ngang trên mobile

**UI/UX:**
- Sử dụng table component từ UI library (Ant Design, Material-UI, etc.)
- Màu sắc: 
  - Header: màu xanh đậm hoặc theo theme
  - Complaints > 0: badge đỏ
  - Revenue: màu xanh lá (tiền kiếm được)
- Icon: Có thể thêm icon cho mỗi metric (booking icon, money icon, warning icon)

