# Hướng dẫn Test API Booking

## Endpoint
```
POST /customers/booking
```

## Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Request Body

### 1. Request tối giản (chỉ các trường bắt buộc)
```json
{
  "homestayId": 1,
  "checkIn": "2024-12-25T14:00:00",
  "checkOut": "2024-12-28T11:00:00",
  "pricePerDays": [
    { "day": "2024-12-25", "price": 1400000 },
    { "day": "2024-12-26", "price": 1400000 },
    { "day": "2024-12-27", "price": 1500000 }
  ],
  "listPersonHomestay": [
    { "type": "ADULTS", "quantity": 2 },
    { "type": "CHILDREN", "quantity": 1 }
  ]
}
```

### 2. Request đầy đủ (với thông tin người đặt)
```json
{
  "homestayId": 1,
  "checkIn": "2024-12-25T14:00:00",
  "checkOut": "2024-12-28T11:00:00",
  "pricePerDays": [
    { "day": "2024-12-25", "price": 1400000 },
    { "day": "2024-12-26", "price": 1400000 },
    { "day": "2024-12-27", "price": 1500000 }
  ],
  "listPersonHomestay": [
    { "type": "ADULTS", "quantity": 2 },
    { "type": "CHILDREN", "quantity": 1 },
    { "type": "BABY", "quantity": 1 }
  ],
  "customerBookingInfoDTO": {
    "name": "Trần Thị B",
    "email": "booking@example.com",
    "phoneNumber": "0987654321",
    "specialRequires": "Cần giường phụ cho trẻ em"
  }
}
```

## Các trường trong Request

| Trường | Loại | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `homestayId` | Long | ✅ | ID của homestay muốn đặt |
| `checkIn` | LocalDateTime | ✅ | Ngày giờ check-in (format: ISO-8601) |
| `checkOut` | LocalDateTime | ✅ | Ngày giờ check-out (format: ISO-8601) |
| `pricePerDays` | List<Object> | ✅ | Danh sách ngày và giá áp dụng cho từng ngày lưu trú |
| `listPersonHomestay` | List<Object> | ✅ | Phân bổ số lượng khách theo từng loại (ADULTS/CHILDREN/BABY) để kiểm tra sức chứa |
| `customerBookingInfoDTO` | Object | ❌ | Thông tin người đặt (nếu khác với customer đăng nhập) |

### pricePerDays

| Trường | Loại | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `day` | Date (yyyy-MM-dd) | ✅ | Ngày áp dụng giá |
| `price` | Float | ✅ | Giá cho ngày tương ứng |

### listPersonHomestay

| Trường | Loại | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `type` | Enum (`ADULTS`, `CHILDREN`, `BABY`) | ✅ | Loại khách |
| `quantity` | Integer | ✅ | Số lượng khách cho loại tương ứng (>= 0) |

### customerBookingInfoDTO

| Trường | Loại | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `name` | String | ❌ | Tên người đặt |
| `email` | String | ❌ | Email liên hệ |
| `phoneNumber` | String | ❌ | Số điện thoại |
| `specialRequires` | String | ❌ | Yêu cầu đặc biệt |

## Response Success (200)
```json
{
  "status": 200,
  "message": "Save bill success",
  "data": {
    "id": 1,
    "code": "ABC12345",
    "status": "PAYMENT_PENDING",
    "checkIn": "2024-12-25T14:00:00",
    "checkOut": "2024-12-28T11:00:00",
    "createdAt": "2024-12-20T10:00:00",
    ...
  }
}
```

## Response Errors

### 422 - Room has been booked
```json
{
  "status": 422,
  "message": "Room has been booked",
  "data": null
}
```

### 404 - Homestay not found
```json
{
  "status": 404,
  "message": "Homestay not exits",
  "data": null
}
```

### 404 - Customer profile not found
```json
{
  "status": 404,
  "message": "Customer profile not found for user id: 123",
  "data": null
}
```

## Lưu ý

1. **JWT Token**: API yêu cầu JWT token hợp lệ với quyền `ROLE_CUSTOMER`
2. **Customer Profile**: User phải có customer profile. Nếu chưa có, cần tạo trước khi booking
3. **Homestay Status**: Homestay phải có status `ACTIVE` để có thể booking
4. **Date Format**: Sử dụng format ISO-8601 cho LocalDateTime (ví dụ: `2024-12-25T14:00:00`)
5. **Check-in/Check-out**: `checkOut` phải sau `checkIn`
6. **pricePerDays**: Danh sách ngày phải liên tục từ check-in đến check-out và chưa bị đặt trước
7. **listPersonHomestay**: Bắt buộc khai báo, hệ thống sẽ kiểm tra tổng khách nằm trong khoảng `minGuest`-`maxGuest` và không vượt quá cấu hình của homestay

## Test với cURL

```bash
curl -X POST http://localhost:8080/customers/booking \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d @booking-test.json
```

## Test với Postman

1. Import file JSON vào Postman
2. Set method: `POST`
3. Set URL: `http://localhost:8080/customers/booking`
4. Thêm header: `Authorization: Bearer <YOUR_JWT_TOKEN>`
5. Chọn body type: `raw` -> `JSON`
6. Paste nội dung từ file JSON
7. Click Send








