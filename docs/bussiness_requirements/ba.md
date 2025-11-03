# 📘 TÀI LIỆU ĐẶC TẢ CHỨC NĂNG HỆ THỐNG HOMESTAY BOOKING

*(Bản ánh xạ chuẩn với entity trong code Spring Boot – `org.example.do_an_v1`)*

## 1️⃣ Giới thiệu chung

**Homestay Booking** là hệ thống đặt và quản lý chỗ ở, kết nối giữa:

- **Customer** – người thuê homestay.
- **Host** – chủ sở hữu homestay.
- **Admin / Super Admin** – quản trị viên điều hành hệ thống và dòng tiền.

Mục tiêu hệ thống:

- Quản lý đặt phòng, thanh toán, đánh giá, khiếu nại.
- Quản lý homestay, tiện nghi, quy tắc và giá.
- Đảm bảo dòng tiền 3 chiều **Customer → Admin → Host**, có thể hoàn tiền (refund) khi khiếu nại hợp lệ.

---

## 2️⃣ Phân quyền người dùng

| Actor | Mô tả | Enum ánh xạ |
| --- | --- | --- |
| **User (chung)** | Đối tượng gốc của tất cả tài khoản | `User` entity |
| **Customer** | Người thuê phòng | `Customer` entity – `RoleUser.CUSTOMER` |
| **Host** | Chủ homestay | `Host` entity – `RoleUser.HOST` |
| **Admin** | Quản trị viên duyệt nội dung, xử lý khiếu nại | `Admin` entity – `LevelAdmin.ADMIN` |
| **Super Admin** | Quản trị viên cao nhất, quản lý hệ thống & payout | `Admin` entity – `LevelAdmin.SUPER_ADMIN` |

Tất cả các vai trò đều liên kết **1–1 với `User`**.

---

## 3️⃣ Chức năng người dùng (USER MODULE)

### 3.1. Đăng ký & Xác thực

**Entity:** `User`, `ConfirmEmail`, `Token`, `InvalidateToken`

- Đăng ký bằng email hoặc OAuth2 (`googleId`).
- Xác thực email qua `ConfirmEmail.code` (hết hạn `expired_at`).
- Khi đăng nhập → sinh `Token` (JWT / refresh).
- Khi đăng xuất → thêm vào `InvalidateToken`.

### 3.2. Thông tin cá nhân

- Cho phép sửa `name`, `avatarUrl`, `phone`, `age`.
- Không sửa `email` nếu chưa xác minh lại.
- `isOnline` dùng để giám sát trạng thái hoạt động (Admin tracking).

---

## 4️⃣ Chức năng quản trị (ADMIN MODULE)

**Entity:** `Admin`, `HomestayPenalties`, `Complaint`, `Transaction`, `Bill`

### 4.1. Quản lý Homestay

- Duyệt Homestay mới (`StatusHomestay.PENDING → ACTIVE`).
- Ban hoặc tạm đình chỉ homestay → ghi log trong `tbl_homestayPenaties`.
- Quản lý Host vi phạm.

### 4.2. Quản lý dòng tiền (Transaction)

- Theo dõi các `Transaction`:
    - `TypeTransaction.BOOKING_PAYMENT`: tiền từ Customer.
    - `TypeTransaction.PAYLOAD_HOST`: payout cho Host.
    - `TypeTransaction.REFUND`: hoàn tiền cho Customer.
- Thực hiện payout hàng tháng theo `StatusBill.SUCCEED`.

### 4.3. Khiếu nại & Refund

- Khiếu nại được tạo trong `Complaint`.
- Admin xử lý nếu Host không phản hồi trong 2 ngày.
- Nếu Admin **chấp nhận** → sinh `Transaction(REFUND)`.
- Nếu **từ chối** → `Bill.status = REJECTED`.

### 4.4. Quản lý tài khoản

- CRUD cho `User`, `Host`, `Customer`.
- Quản lý `status` (ACTIVE, INACTIVE).
- Duyệt Host mới đăng ký.

---

## 5️⃣ Chức năng Host (HOST MODULE)

**Entity:** `Host`, `Homestay`, `HomestayRule`, `HomestayDailyPrice`, `HomestayPenalties`, `BusinessQrCode`

### 5.1. Quản lý Homestay

- Tạo / sửa / xóa Homestay (`StatusHomestay.PENDING`, `ACTIVE`, `INACTIVE`).
- Chỉ được sửa homestay nếu là not `ACTIVE`
- Liên kết `Homestay` với:
    - `Address`
    - `Facilities`, `Amenities`
    - `HomestayRule` (quy định)
    - `HomestayDailyPrice` (giá theo ngày)
- Khi bị cảnh cáo → `warningCount` tăng.

### 5.2. Check-in / Check-out

- Khi khách đến, Host xác nhận check-in → `Bill.actualCheckinTime` cập nhật.
- Nếu quá hạn → `Bill.status = CHECKIN_EXPIRED`.
- Sau checkout → chờ khiếu nại (`COMPLAINT_PENDING`).

### 5.3. Khiếu nại (Complaint Handling)

- Host có 2 ngày để phản hồi khiếu nại.
- Nếu không phản hồi → admin tự động tiếp quản.
- Kết quả:
    - `HOST_COMPLAINT_APPROVED` → refund.
    - `ADMIN_COMPLAINT_REJECTED` → đơn hoàn tất.

---

## 6️⃣ Chức năng Customer (CUSTOMER MODULE)

**Entity:** `Customer`, `CustomerBookingInfo`, `Bill`, `Transaction`, `Complaint`, `Review`, `Image`

### 6.1. Đặt phòng

- Tạo `CustomerBookingInfo` → sinh `Bill`.
- Thanh toán → sinh `Transaction(BOOKING_PAYMENT)`.
- Sau khi thanh toán:
    - `Bill.status = CHECKIN_PENDING`.
    - Gửi mã check-in cho Customer.

### 6.2. Theo dõi hóa đơn

**Trạng thái (`StatusBill`)**

| Trạng thái | Mô tả |
| --- | --- |
| `PAYMENT_PENDING` | Đang chờ thanh toán |
| `PAYMENT_FAILED` | Thanh toán thất bại |
| `CHECKIN_PENDING` | Đã thanh toán, chờ check-in |
| `COMPLAINT_PENDING` | Đang ở hoặc chờ phản hồi |
| `REFUNDED` | Hoàn tiền thành công |
| `SUCCEED` | Hoàn tất, Host sẽ nhận payout |

### 6.3. Khiếu nại & Đánh giá

- Khiếu nại → tạo `Complaint`, `listImage`.
- Đánh giá sau checkout → tạo `Review`, `listImage`.

---

## 7️⃣ Quy tắc nghiệp vụ (Business Rules)

| STT | Quy tắc | Ánh xạ Entity |
| --- | --- | --- |
| 1 | Check-in hợp lệ 14h–15h | `Bill.checkIn` |
| 2 | Check-out trước 12h | `Bill.checkOut` |
| 3 | Khiếu nại trong 7 ngày | `Complaint` |
| 4 | Host xử lý trong 2 ngày | `Complaint → Admin` |
| 5 | Admin xử lý trong 1 ngày | `Admin` |
| 6 | Tổng thời gian giải quyết: 10 ngày | Toàn bộ flow |
| 7 | Homestay chỉ sửa khi `INACTIVE` | `Homestay.statusHomestay` |
| 8 | Admin có quyền ban homestay | `HomestayPenalties` |
| 9 | Mọi transaction có liên kết với 1 bill | `Transaction.bill` |
| 10 | Không có luồng trực tiếp Customer → Host | `Transaction.fromUser`, `toUser` |

---

## 8️⃣ Nghiệp vụ thanh toán trung gian (Escrow Flow)

**Entity:** `Transaction`, `Bill`, `User`, `Admin`, `Host`, `Customer`

**Enum:** `TypeTransaction`, `StatusTransaction`, `StatusBill`

### 🪣 Dòng tiền ba chiều

| Giai đoạn | Từ (fromUser) | Đến (toUser) | TypeTransaction | Mô tả |
| --- | --- | --- | --- | --- |
| 1️⃣ Customer thanh toán | Customer.user | Admin.user | BOOKING_PAYMENT | Tiền vào Escrow |
| 2️⃣ Refund khiếu nại | Admin.user | Customer.user | REFUND | Khi complaint hợp lệ |
| 3️⃣ Payout định kỳ | Admin.user | Host.user | PAYLOAD_HOST | Khi Bill `SUCCEED` |

**Chi tiết theo code:**

```java
Transaction.builder()
   .amount(amount)
   .transactionType(TypeTransaction.PAYLOAD_HOST)
   .status(StatusTransaction.SUCCESS)
   .fromUser(adminUser)
   .toUser(hostUser)
   .bill(bill)
   .build();

```

---

## 9️⃣ Quan hệ Entity tổng quát (ER Summary)

| Entity | Liên kết | Mối quan hệ |
| --- | --- | --- |
| User ↔ Admin/Host/Customer | 1–1 | Phân loại người dùng |
| User ↔ Transaction | 1–N | Dòng tiền gửi/nhận |
| Bill ↔ Transaction | 1–N | Giao dịch theo hóa đơn |
| Bill ↔ Homestay | N–1 | Mỗi hóa đơn thuộc 1 homestay |
| Bill ↔ Customer | N–1 | Mỗi hóa đơn thuộc 1 khách hàng |
| Complaint ↔ Admin | N–1 | Admin xử lý khiếu nại |
| Complaint ↔ Image | 1–N | Ảnh đính kèm khiếu nại |
| Homestay ↔ Host | N–1 | Homestay thuộc 1 Host |
| Homestay ↔ Facilities/Amenities | N–M | Tiện nghi & cơ sở vật chất |
| Review ↔ Customer/Homestay | N–1 | Đánh giá của khách |
| Preference ↔ Customer | N–M | Sở thích người dùng |

---