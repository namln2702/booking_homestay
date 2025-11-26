# Create Facilities API - Postman Collection

Collection này chứa các API requests để tạo 5 Facilities mẫu phổ biến cho hệ thống homestay.

## 📋 API Endpoints

### POST /facilities/batch - Batch Create Facilities
**Mô tả**: Tạo mới một Facilities (tiện nghi) cho hệ thống.

**Authorization**: 
- **Bắt buộc**: JWT token của ADMIN hoặc SUPER_ADMIN
- Header: `Authorization: Bearer <adminJwtToken>`

**Request Body**:
```json
{
  "name": "Wi-Fi miễn phí",
  "category": "General"
}
```

**Mô tả**: Tạo nhiều Facilities cùng lúc (tối đa 50 facilities).

**Authorization**: 
- **Bắt buộc**: JWT token của ADMIN hoặc SUPER_ADMIN
- Header: `Authorization: Bearer <adminJwtToken>`

**Request Body**:
```json
{
  "facilities": [
    {
      "name": "Wi-Fi miễn phí",
      "category": "General"
    },
    {
      "name": "Điều hòa",
      "category": "General"
    },
    {
      "name": "TV thông minh",
      "category": "Bedroom"
    },
    {
      "name": "Máy giặt",
      "category": "Kitchen"
    },
    {
      "name": "Hồ bơi",
      "category": "Special"
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "status": 201,
  "message": "Facilities created successfully",
  "data": [
    {
      "id": 1,
      "name": "Wi-Fi miễn phí",
      "category": "General"
    },
    {
      "id": 2,
      "name": "Điều hòa",
      "category": "General"
    },
    {
      "id": 3,
      "name": "TV thông minh",
      "category": "Bedroom"
    },
    {
      "id": 4,
      "name": "Máy giặt",
      "category": "Kitchen"
    },
    {
      "id": 5,
      "name": "Hồ bơi",
      "category": "Special"
    }
  ]
}
```

---

## 📝 5 Facilities Mẫu trong Batch Request

Collection này chỉ chứa **1 request duy nhất** để tạo tất cả 5 facilities cùng lúc:

### Facilities được tạo:
1. **Wi-Fi miễn phí**
   - Category: General
   - Mô tả: Tiện ích Wi-Fi miễn phí cho khách

2. **Điều hòa**
   - Category: General
   - Mô tả: Điều hòa không khí

3. **TV thông minh**
   - Category: Bedroom
   - Mô tả: TV thông minh trong phòng ngủ

4. **Máy giặt**
   - Category: Kitchen
   - Mô tả: Máy giặt trong khu vực nhà bếp/giặt ủi

5. **Hồ bơi**
   - Category: Special
   - Mô tả: Hồ bơi - tiện ích đặc biệt

---

## 📋 Chi tiết các Fields

### Fields bắt buộc (Required)

| Field | Type | Mô tả | Validation |
|-------|------|-------|------------|
| `name` | String | Tên của Facilities | @NotBlank, max 100 characters |
| `category` | String | Danh mục của Facilities | @NotBlank, max 50 characters |

### Category Examples

Các category phổ biến:
- `"General"` - Tiện ích chung
- `"Bedroom"` - Phòng ngủ
- `"Bathroom"` - Phòng tắm
- `"Kitchen"` - Nhà bếp
- `"Special"` - Tiện ích đặc biệt
- `"View"` - Tầm nhìn

---

## 🚀 Cách sử dụng

### Bước 1: Import Collection
1. Mở Postman
2. Click `File` → `Import`
3. Chọn file `create-facilities.postman_collection.json`

### Bước 2: Setup Variables
1. **baseUrl**: URL của API server (mặc định: `http://localhost:8080`)
2. **adminJwtToken**: JWT token của ADMIN hoặc SUPER_ADMIN
   - Cần login với tài khoản admin trước
   - Lấy token từ response login
   - Set vào collection variable `adminJwtToken`

### Bước 3: Chạy Request
1. Chọn request **"Batch Create - 5 Facilities"**
2. Đảm bảo đã set `adminJwtToken` trong collection variables
3. Click `Send` để tạo tất cả 5 facilities cùng lúc
4. Sau đó có thể dùng **"Get All Facilities"** để xem kết quả

---

## ✅ Validation Rules

1. **Name**: Bắt buộc, không được rỗng, tối đa 100 ký tự
2. **Category**: Bắt buộc, không được rỗng, tối đa 50 ký tự

---

## ❌ Error Cases

### 400 Bad Request
- `"Facilities list cannot be null or empty"`: Batch request thiếu facilities list
- `"Cannot create more than 50 facilities at once"`: Vượt quá giới hạn batch (50)
- `"Facility at index X is null"`: Facility tại index X bị null
- `"Facility name is required at index X"`: Thiếu name tại index X
- `"Facility category is required at index X"`: Thiếu category tại index X
- `"Facility name must be at most 100 characters"`: Name quá dài
- `"Facility category must be at most 50 characters"`: Category quá dài

### 401/403 Unauthorized
- Không có JWT token hoặc token không hợp lệ
- Token không có quyền ADMIN/SUPER_ADMIN

---

## 📚 Tài liệu liên quan

- [FacilitiesService.java](../src/main/java/org/example/do_an_v1/service/FacilitiesService.java)
- [FacilitiesController.java](../src/main/java/org/example/do_an_v1/controller/FacilitiesController.java)
- [FacilitiesCreateRequest.java](../src/main/java/org/example/do_an_v1/dto/request/FacilitiesCreateRequest.java)
- [FacilitiesDTO.java](../src/main/java/org/example/do_an_v1/dto/FacilitiesDTO.java)

---

## 🔄 Next Steps

Sau khi tạo facilities:

1. **View All Facilities**: Xem tất cả facilities đã tạo
   - API: `GET /facilities` (public endpoint)
   - Request: "Get All Facilities" trong collection

2. **View Single Facility**: Xem chi tiết một facility
   - API: `GET /facilities/{id}` (public endpoint)
   - Request: "Get Facility by ID" trong collection (thay đổi ID trong URL)

3. **Use in Homestay**: Host có thể chọn facilities khi tạo homestay
   - API: `POST /hosts/me/homestays`
   - Field: `facilitiesIds` - danh sách ID của facilities đã tạo

---

**Version**: 1.0.0  
**Last Updated**: 2024-12-20  
**Author**: Booking Homestay Dev Team

