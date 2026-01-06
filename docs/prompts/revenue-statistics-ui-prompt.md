# PROMPT: Giao Diện Thống Kê Doanh Thu (Revenue Statistics Dashboard)

## Tổng quan
Tạo giao diện thống kê doanh thu cho Admin với các chức năng: hiển thị tổng quan doanh thu, filter theo chu kỳ thanh toán host (từ 25 tháng trước đến 25 tháng hiện tại), và hiển thị các chỉ số tài chính chi tiết.

---

## API Specification

### Endpoint
**URL:** `GET /admins/reports/revenue-statistics`  
**Method:** `GET`  
**Authentication:** Required (Bearer Token với quyền `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN`)

### Query Parameters
- `month` (optional, Integer): Tháng (1-12). Nếu không truyền, dùng tháng hiện tại
- `year` (optional, Integer): Năm (ví dụ: 2025). Nếu không truyền, dùng năm hiện tại

**Ví dụ:**
- `GET /admins/reports/revenue-statistics?month=8&year=2025` → Thống kê từ 25-07-2025 đến 25-08-2025
- `GET /admins/reports/revenue-statistics` → Thống kê từ 25 tháng trước đến 25 tháng hiện tại

### Response Structure
```json
{
  "status": 200,
  "message": "Revenue statistics retrieved successfully for period: 2025-07-25 to 2025-08-25",
  "data": {
    "totalHostReceived": 10000000.00,        // Tổng số tiền host đã nhận (BigDecimal)
    "totalAdminCommission": 1000000.00,      // Tổng số tiền admin đã nhận - hoa hồng (BigDecimal)
    "totalCustomerRefunded": 500000.00,     // Tổng số tiền đã trả lại cho customer (BigDecimal)
    "totalCustomerPaid": 11500000.00,        // Tổng số tiền customer đã trả cho admin (BigDecimal)
    "hostReceivedCount": 50,                 // Số lượng transaction host đã nhận (long)
    "customerRefundedCount": 5,              // Số lượng transaction refund cho customer (long)
    "billsWithCommission": 100                // Số lượng bill có commission (long)
  }
}
```

### Business Logic - Chu Kỳ Thanh Toán
- **Chu kỳ thanh toán host:** Từ ngày 25 tháng trước đến ngày 25 tháng hiện tại
- **Ví dụ:** Nếu truyền `month=8, year=2025`:
  - Start date: 25-07-2025 00:00:00
  - End date: 25-08-2025 23:59:59.999
- **Nếu không truyền tháng/năm:** Dùng tháng/năm hiện tại và tính tương tự

---

## UI/UX Requirements

### 1. Layout Structure

#### Header Section
- **Title:** "Thống Kê Doanh Thu" (Revenue Statistics)
- **Subtitle:** Hiển thị khoảng thời gian đang thống kê
  - Format: "Từ [startDate] đến [endDate]"
  - Ví dụ: "Từ 25/07/2025 đến 25/08/2025"
- **Refresh button:** Reload dữ liệu

#### Filter Section
- **Month Selector:** Dropdown/Select với các tháng (1-12)
  - Label: "Tháng"
  - Default: Tháng hiện tại
- **Year Selector:** Input/Select với năm
  - Label: "Năm"
  - Default: Năm hiện tại
  - Validation: Năm từ 2000-2100
- **Apply Filter Button:** "Áp dụng" hoặc "Xem thống kê"
- **Reset Button:** "Đặt lại" (reset về tháng/năm hiện tại)

#### Statistics Cards Section
Hiển thị 4-6 cards chính với các chỉ số quan trọng:

**Card 1: Tổng Tiền Customer Đã Trả**
- Icon: 💰 (hoặc icon phù hợp)
- Title: "Tổng Tiền Customer Đã Trả"
- Value: Format tiền VNĐ (ví dụ: 11,500,000 ₫)
- Subtitle: "Từ các giao dịch thanh toán thành công"
- Color: Green/Blue

**Card 2: Tổng Tiền Host Đã Nhận**
- Icon: 🏠 (hoặc icon host)
- Title: "Tổng Tiền Host Đã Nhận"
- Value: Format tiền VNĐ
- Subtitle: "Số lượng giao dịch: [hostReceivedCount]"
- Color: Blue/Purple

**Card 3: Tổng Hoa Hồng Admin**
- Icon: 💼 (hoặc icon admin)
- Title: "Tổng Hoa Hồng Admin"
- Value: Format tiền VNĐ
- Subtitle: "Từ [billsWithCommission] bills"
- Color: Gold/Orange

**Card 4: Tổng Tiền Đã Hoàn Lại**
- Icon: 🔄 (hoặc icon refund)
- Title: "Tổng Tiền Đã Hoàn Lại"
- Value: Format tiền VNĐ
- Subtitle: "Số lượng giao dịch: [customerRefundedCount]"
- Color: Red/Orange

**Card 5 (Optional): Lợi Nhuận Ròng**
- Title: "Lợi Nhuận Ròng"
- Value: `totalCustomerPaid - totalHostReceived - totalCustomerRefunded`
- Subtitle: "Doanh thu sau khi trừ chi phí"
- Color: Green (nếu dương) / Red (nếu âm)

**Card 6 (Optional): Tỷ Lệ Hoa Hồng**
- Title: "Tỷ Lệ Hoa Hồng"
- Value: `(totalAdminCommission / totalCustomerPaid) * 100` + "%"
- Subtitle: "Tỷ lệ hoa hồng trên tổng doanh thu"

### 2. Detailed Statistics Table (Optional)
Bảng chi tiết các chỉ số:
- Tổng tiền customer đã trả
- Tổng tiền host đã nhận
- Tổng hoa hồng admin
- Tổng tiền đã hoàn lại
- Số lượng transaction host
- Số lượng transaction refund
- Số lượng bill có commission

### 3. Chart/Visualization (Optional)
- **Bar Chart:** So sánh các khoản thu/chi
  - X-axis: Các loại (Customer Paid, Host Received, Admin Commission, Refunded)
  - Y-axis: Số tiền (VNĐ)
- **Pie Chart:** Phân bổ doanh thu
  - Host Received
  - Admin Commission
  - Refunded
- **Line Chart:** Xu hướng theo thời gian (nếu có dữ liệu nhiều kỳ)

### 4. Formatting Requirements

#### Format Tiền Tệ
- **Currency:** VNĐ (Vietnamese Dong)
- **Format:** `[number] ₫` hoặc `[number] VNĐ`
- **Decimal:** 2 chữ số thập phân (nếu có)
- **Thousand Separator:** Dấu phẩy (,) hoặc dấu chấm (.)
- **Ví dụ:** 
  - `11,500,000.00 ₫`
  - `11.500.000 ₫` (format Việt Nam)

#### Format Số Lượng
- **Format:** Số nguyên, có dấu phẩy phân cách hàng nghìn
- **Ví dụ:** `1,234` hoặc `1.234`

#### Format Ngày Tháng
- **Format:** `DD/MM/YYYY` hoặc `DD-MM-YYYY`
- **Ví dụ:** `25/07/2025` hoặc `25-07-2025`

### 5. Loading & Error States

#### Loading State
- Hiển thị skeleton loader hoặc spinner khi đang fetch data
- Disable filter controls khi đang loading

#### Error State
- Hiển thị error message nếu API trả về lỗi
- Hiển thị validation error nếu month/year không hợp lệ
- Có button "Thử lại" để retry

#### Empty State
- Hiển thị message "Không có dữ liệu" nếu tất cả giá trị = 0
- Hiển thị icon/illustration phù hợp

### 6. Responsive Design
- **Desktop:** 4 cards per row (hoặc 3 cards per row)
- **Tablet:** 2 cards per row
- **Mobile:** 1 card per row
- Filter section: Stack vertically trên mobile

### 7. Color Scheme Suggestions
- **Primary:** Blue (#1890ff hoặc tương tự)
- **Success/Positive:** Green (#52c41a)
- **Warning:** Orange (#faad14)
- **Danger/Negative:** Red (#ff4d4f)
- **Info:** Blue (#1890ff)
- **Background:** Light gray (#f5f5f5) hoặc white

### 8. Additional Features

#### Export Data
- Button "Xuất Excel" hoặc "Export PDF"
- Export dữ liệu thống kê hiện tại

#### Print
- Button "In" để print trang thống kê

#### Date Range Display
- Hiển thị rõ ràng khoảng thời gian đang thống kê
- Có thể highlight chu kỳ thanh toán (25 tháng trước → 25 tháng hiện tại)

---

## Technical Requirements

### API Integration
- Sử dụng Bearer Token authentication
- Handle errors (400, 401, 403, 500)
- Retry logic cho failed requests
- Cache data nếu cần (optional)

### State Management
- Store filter state (month, year)
- Store API response data
- Handle loading/error states

### Validation
- Validate month: 1-12
- Validate year: 2000-2100
- Show error message nếu validation fail

---

## Example UI Flow

1. **Page Load:**
   - Default: Load data với tháng/năm hiện tại
   - Show loading state
   - Display data khi có response

2. **User Changes Filter:**
   - User chọn month = 8, year = 2025
   - Click "Áp dụng"
   - Show loading state
   - Update URL: `/reports/revenue-statistics?month=8&year=2025`
   - Fetch new data
   - Update display với khoảng thời gian mới: "Từ 25/07/2025 đến 25/08/2025"

3. **User Clicks Reset:**
   - Reset month/year về hiện tại
   - Reload data

---

## Notes
- Đảm bảo UI/UX thân thiện, dễ sử dụng
- Hiển thị số liệu rõ ràng, dễ đọc
- Responsive trên mọi thiết bị
- Có thể sử dụng các UI library như: Ant Design, Material-UI, Chakra UI, v.v.
- Format số tiền theo chuẩn Việt Nam

