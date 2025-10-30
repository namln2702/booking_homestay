# Customer & Host Registration Flows

Tài liệu này mô tả chi tiết luồng đăng ký mới cho khách (Customer) và chủ nhà (Host) sau khi refactor các controller và service liên quan.

## 1. Nguyên tắc chung
- Đăng ký được tách thành 2 bước độc lập nhằm tận dụng `UserController` hiện tại:
  1. Tạo `User` cơ bản bằng một trong các endpoint đã có trong `UserController`.
  2. Sau khi nhận được `user_id`, gọi endpoint chuyên biệt để bổ sung thông tin mở rộng cho `Customer` hoặc `Host`.
- `UserController` giữ nguyên:  
  - `POST /user/login-register-with-email`  
  - `POST /user/confirm-email`  
  - `POST /user/login-register-with-google`
- Các controller mới chỉ tiếp nhận DTO đã hợp lệ (`@Valid`) và ủy quyền cho service xử lý. Service sẽ:
  - Đảm bảo `User` tồn tại (nếu không sẽ ném `ResourceNotFoundException`).  
  - Cập nhật các trường chung của `User` (username, name, phone, age, avatarUrl) nếu được truyền.  
  - Thiết lập và lưu thông tin mở rộng trong `tbl_customer` hoặc `tbl_hosts`.  
  - Đảm bảo giá trị `role` đúng với thực thể (`RoleUser.CUSTOMER` hoặc `RoleUser.HOST`).  
  - Trả về `ApiResponse<T>` chứa DTO tương ứng (CustomerDTO hoặc HostDTO).

## 2. Luồng đăng ký Customer
### Bước 1: Tạo user cơ bản
- Gọi `POST /user/login-register-with-email?email=` hoặc `POST /user/login-register-with-google?code=`.
- Backend tạo hoặc cập nhật `tbl_users`, trả về `user_id`.

### Bước 2: Bổ sung dữ liệu customer
- Endpoint: `POST /customers/register`
- Request body (`CustomerRegistrationRequest`):
  ```json
  {
    "userId": 123,
    "username": "optional",
    "name": "optional",
    "phone": "optional",
    "age": 25,
    "avatarUrl": "optional",
    "status": "ACTIVE",
    "dateOfBirth": "1995-10-05",
    "qrCodeUrl": "optional",
    "lastBooking": "optional"
  }
  ```
- Service xử lý:
  - Lấy `User` theo `userId`, cập nhật các trường chung nếu khác giá trị hiện tại.
  - Tìm `Customer` theo `userId`. Nếu chưa có, tạo mới với `role=RoleUser.CUSTOMER`.
  - Ghi nhận các trường mở rộng (status, dateOfBirth, qrCodeUrl, lastBooking).
  - Lưu `Customer` và trả về `ApiResponse<CustomerDTO>` với thông tin mới nhất.

### Kết quả
- `ApiResponse` status `200`, message:
  - `"Customer registered successfully"` nếu tạo mới.
  - `"Customer information updated successfully"` nếu cập nhật.
  - `"Customer information already up to date"` nếu payload không thay đổi gì.
- `data`: `CustomerDTO` bao gồm cả thông tin `User` và `Customer`.

## 3. Luồng đăng ký Host
### Bước 1: Tạo user cơ bản
- Giống customer: sử dụng các endpoint của `UserController` để lấy `user_id`.

### Bước 2: Bổ sung dữ liệu host
- Endpoint: `POST /hosts/register`
- Request body (`HostRegistrationRequest`):
  ```json
  {
    "userId": 456,
    "username": "optional",
    "name": "optional",
    "phone": "optional",
    "age": 30,
    "avatarUrl": "optional",
    "statusHost": "PENDING",
    "businessName": "My Homestay",
    "qrCodeUrl": "optional"
  }
  ```
- Service xử lý:
  - Kiểm tra `User` tồn tại, cập nhật các trường chung nếu cần.
  - Tìm `Host` theo `userId`. Nếu chưa có, tạo mới với `role=RoleUser.HOST`.
  - Thiết lập `statusHost`, `businessName`, `qrCodeUrl` từ request.
  - Lưu `Host` và trả về `ApiResponse<HostDTO>`.

### Kết quả
- `ApiResponse` status `200`, message:
  - `"Host registered successfully"` khi tạo mới.
  - `"Host information updated successfully"` khi cập nhật.
  - `"Host information already up to date"` khi không có thay đổi.
- `data`: `HostDTO` chứa thông tin user + host.

## 4. Ghi chú triển khai
- Các DTO đặt trong `src/main/java/org/example/do_an_v1/dto/request/`.
- Logic chia sẻ cập nhật trường `User` tập trung tại `UserRegistrationSupport`.
- `CustomerServiceImpl` và `HostServiceImpl` được đánh dấu `@Transactional` để đảm bảo atomic cho từng luồng đăng ký.

## 5. Kiểm tra trạng thái hồ sơ
- `GET /customers/{userId}`: trả về `ApiResponse<CustomerDTO>` hoặc `status=404` nếu chưa có hồ sơ customer.
- `GET /hosts/{userId}`: trả về `ApiResponse<HostDTO>` hoặc `status=404` nếu chưa có hồ sơ host.
- Front end có thể gọi các API này sau khi đăng nhập để quyết định hiển thị form hoàn thiện hồ sơ hay đưa người dùng vào luồng chính.
