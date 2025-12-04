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
  "checkOut": "2024-12-28T11:00:00"
}
```
**Lưu ý:** Nếu không có `homestayDailyPriceIds`, hệ thống sẽ tự động tìm các HomestayDailyPrice trong khoảng thời gian check-in đến check-out.

### 2. Request với danh sách ID giá
```json
{
  "homestayId": 1,
  "checkIn": "2024-12-25T14:00:00",
  "checkOut": "2024-12-28T11:00:00",
  "homestayDailyPriceIds": [1, 2, 3]
}
```

### 3. Request đầy đủ (với thông tin người đặt)
```json
{
  "homestayId": 1,
  "checkIn": "2024-12-25T14:00:00",
  "checkOut": "2024-12-28T11:00:00",
  "actualCheckin": null,
  "homestayDailyPriceIds": [1, 2, 3],
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
| `actualCheckin` | LocalDateTime | ❌ | Thời gian check-in thực tế (thường null khi booking) |
| `homestayDailyPriceIds` | List<Long> | ❌ | Danh sách ID của HomestayDailyPrice. Nếu không có, hệ thống tự động tìm theo date range |
| `customerBookingInfoDTO` | Object | ❌ | Thông tin người đặt (nếu khác với customer đăng nhập) |

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
6. **HomestayDailyPriceIds**: Nếu truyền danh sách ID, các ID phải thuộc về homestay đang booking

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

