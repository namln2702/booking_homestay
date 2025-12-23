# PROMPT: Quản Lý Tiền cho Admin (Finance Management)

## Tổng quan
Tạo giao diện quản lý tiền cho admin với các chức năng: Dashboard tổng quan, Quản lý Refund, và Theo dõi Giao dịch.

**Lưu ý:** Luồng quản lý Payout (Admin chuyển tiền cho Host) chưa được implement, sẽ được thêm sau.

---

## API 1: GET /admins/finance/dashboard - Dashboard Tổng Quan Tài Chính

**URL:** `GET /admins/finance/dashboard`  
**Headers:** `Authorization: Bearer <adminToken>`

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Finance dashboard retrieved successfully",
  "data": {
    "escrowBalance": 50000000.00,
    "todayRevenue": {
      "totalAmount": 5000000.00,
      "transactionCount": 5
    },
    "weekRevenue": {
      "totalAmount": 30000000.00,
      "transactionCount": 30
    },
    "monthRevenue": {
      "totalAmount": 100000000.00,
      "transactionCount": 100
    },
    "yearRevenue": {
      "totalAmount": 500000000.00,
      "transactionCount": 500
    },
    "todayPayout": {
      "totalAmount": 3000000.00,
      "transactionCount": 3
    },
    "weekPayout": {
      "totalAmount": 20000000.00,
      "transactionCount": 20
    },
    "monthPayout": {
      "totalAmount": 80000000.00,
      "transactionCount": 80
    },
    "yearPayout": {
      "totalAmount": 400000000.00,
      "transactionCount": 400
    },
    "todayRefund": {
      "totalAmount": 1000000.00,
      "transactionCount": 1
    },
    "weekRefund": {
      "totalAmount": 5000000.00,
      "transactionCount": 5
    },
    "monthRefund": {
      "totalAmount": 10000000.00,
      "transactionCount": 10
    },
    "yearRefund": {
      "totalAmount": 50000000.00,
      "transactionCount": 50
    },
    "netProfit": 50000000.00,
    "transactionCounts": {
      "bookingPaymentCount": 500,
      "payoutHostCount": 400,
      "refundCount": 50
    },
    "recentTransactions": [
      {
        "id": 1,
        "amount": 1000000.00,
        "transactionType": "BOOKING_PAYMENT",
        "status": "SUCCESS",
        "completedAt": "2024-12-01T10:30:00",
        "fromUserId": 10,
        "fromUserEmail": "customer@example.com",
        "toUserId": 1,
        "toUserEmail": "admin@example.com",
        "billId": 123,
        "billCode": "BILL-001"
      }
    ]
  }
}
```

---

## API 2: GET /admins/transactions - Danh Sách Tất Cả Transactions

**URL:** `GET /admins/transactions`  
**Headers:** `Authorization: Bearer <adminToken>`

**Query Parameters:**
- `page` (optional, default: 0): Số trang
- `size` (optional, default: 20): Số lượng items mỗi trang
- `type` (optional): `BOOKING_PAYMENT`, `PAYLOAD_HOST`, `REFUND`, `CUSTOMER_PAYMENT_ADMIN`, `ADMIN_PAYMENT_HOST`
- `status` (optional): `PENDING`, `SUCCESS`, `FAILED`
- `fromUserId` (optional): Filter theo người gửi
- `toUserId` (optional): Filter theo người nhận
- `billId` (optional): Filter theo bill

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transactions retrieved successfully",
  "data": {
    "page": 0,
    "size": 20,
    "total": 100,
    "items": [
      {
        "id": 1,
        "amount": 1000000.00,
        "transactionType": "BOOKING_PAYMENT",
        "status": "SUCCESS",
        "completedAt": "2024-12-01T10:30:00",
        "fromUserId": 10,
        "fromUserEmail": "customer@example.com",
        "toUserId": 1,
        "toUserEmail": "admin@example.com",
        "billId": 123,
        "billCode": "BILL-001",
        "proofImageUrl": null
      }
    ]
  }
}
```

---

## TypeScript Interfaces

```typescript
interface FinanceDashboardDTO {
  escrowBalance: number;
  todayRevenue: RevenueSummaryDTO;
  weekRevenue: RevenueSummaryDTO;
  monthRevenue: RevenueSummaryDTO;
  yearRevenue: RevenueSummaryDTO;
  todayPayout: PayoutSummaryDTO;
  weekPayout: PayoutSummaryDTO;
  monthPayout: PayoutSummaryDTO;
  yearPayout: PayoutSummaryDTO;
  todayRefund: RefundSummaryDTO;
  weekRefund: RefundSummaryDTO;
  monthRefund: RefundSummaryDTO;
  yearRefund: RefundSummaryDTO;
  netProfit: number;
  transactionCounts: TransactionCountDTO;
  recentTransactions: TransactionDTO[];
}

interface RevenueSummaryDTO {
  totalAmount: number;
  transactionCount: number;
}

interface PayoutSummaryDTO {
  totalAmount: number;
  transactionCount: number;
}

interface RefundSummaryDTO {
  totalAmount: number;
  transactionCount: number;
}

interface TransactionCountDTO {
  bookingPaymentCount: number;
  payoutHostCount: number;
  refundCount: number;
}

interface TransactionDTO {
  id: number;
  amount: number;
  transactionType: "BOOKING_PAYMENT" | "PAYLOAD_HOST" | "REFUND" | "CUSTOMER_PAYMENT_ADMIN" | "ADMIN_PAYMENT_HOST";
  status: "PENDING" | "SUCCESS" | "FAILED";
  completedAt: string | null;
  fromUserId: number;
  fromUserEmail: string;
  toUserId: number | null;
  toUserEmail: string | null;
  billId: number;
  billCode: string;
  proofImageUrl: string | null;
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
}
```

---

## UI/UX Requirements

### 1. Dashboard Tổng Quan (Finance Dashboard)

**Layout:**
- **Header Cards**: 4 cards lớn hiển thị:
  - Số dư Escrow (màu xanh dương)
  - Tổng thu hôm nay (màu xanh lá)
  - Tổng chi hôm nay (màu cam)
  - Lợi nhuận ròng (màu tím)

- **Period Tabs**: Tabs để chuyển đổi giữa Today / Week / Month / Year
  - Mỗi tab hiển thị: Revenue, Payout, Refund cho kỳ đó

- **Charts Section**:
  - **Line Chart**: Xu hướng Revenue/Payout/Refund theo thời gian (7 ngày gần nhất)
  - **Pie Chart**: Phân bổ theo loại transaction (Booking Payment, Payout Host, Refund)

- **Transaction Counts Cards**: 3 cards nhỏ hiển thị số lượng từng loại transaction

- **Recent Transactions Table**: Bảng 10 giao dịch gần đây nhất
  - Columns: ID, Type, Amount, From, To, Status, Time
  - Click vào row để xem chi tiết

**Features:**
- Auto refresh mỗi 30 giây
- Export báo cáo (PDF/Excel)
- Filter theo thời gian (date picker)

---

### 2. Quản Lý Refund

**Layout:**
- Filter: Status (PENDING, SUCCESS, FAILED)
- Table hiển thị: Transaction ID, Customer, Bill, Amount, Complaint (nếu có), Status, Actions
- Action: Button "Xác nhận" → Modal upload proof image (cho refund PENDING)

**Modal Xác Nhận Refund:**
- Form upload proof image
- Hiển thị thông tin transaction: ID, Amount, Customer, Bill, Complaint
- Button "Xác nhận" → Gọi API confirm refund
- Sau khi xác nhận: Refresh danh sách

---

### 3. Theo Dõi Giao Dịch (Transaction Management)

**Layout:**
- **Filters Section**:
  - Type dropdown: All / BOOKING_PAYMENT / PAYLOAD_HOST / REFUND
  - Status dropdown: All / PENDING / SUCCESS / FAILED
  - From User ID (input)
  - To User ID (input)
  - Bill ID (input)
  - Date Range picker
  - Button "Tìm kiếm" và "Reset"

- **Table**:
  - Columns: ID, Type (badge), Amount, From User, To User, Bill Code, Status (badge), Completed At, Actions
  - Sortable columns
  - Pagination ở cuối

- **Detail Modal/Drawer**:
  - Click vào row để xem chi tiết
  - Hiển thị đầy đủ thông tin transaction
  - Proof image (nếu có) - có thể zoom
  - Link đến Bill detail, User detail

---

## Code Structure

```typescript
// Component chính
const FinanceManagementPage = () => {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'refund' | 'transactions'>('dashboard');
  const [dashboardData, setDashboardData] = useState<FinanceDashboardDTO | null>(null);
  
  // Fetch dashboard
  const fetchDashboard = async () => {
    const response = await api.get('/admins/finance/dashboard');
    setDashboardData(response.data.data);
  };
  
  // Fetch transactions with filters
  const fetchTransactions = async (filters: TransactionFilters) => {
    const params = new URLSearchParams();
    if (filters.type) params.append('type', filters.type);
    if (filters.status) params.append('status', filters.status);
    // ... other filters
    const response = await api.get(`/admins/transactions?${params.toString()}`);
    return response.data.data;
  };
  
  useEffect(() => {
    if (activeTab === 'dashboard') {
      fetchDashboard();
      // Auto refresh every 30 seconds
      const interval = setInterval(fetchDashboard, 30000);
      return () => clearInterval(interval);
    }
  }, [activeTab]);
  
  return (
    <div>
      <Tabs value={activeTab} onChange={setActiveTab}>
        <Tab value="dashboard" label="Dashboard" />
        <Tab value="refund" label="Quản lý Refund" />
        <Tab value="transactions" label="Giao dịch" />
      </Tabs>
      
      {activeTab === 'dashboard' && <DashboardView data={dashboardData} />}
      {activeTab === 'refund' && <RefundManagementView />}
      {activeTab === 'transactions' && <TransactionManagementView />}
    </div>
  );
};
```

---

## Business Logic

1. **Escrow Balance**: Tổng tiền trong Escrow = Tổng Revenue - (Tổng Payout + Tổng Refund)
   - Lưu ý: Payout data chỉ để thống kê, chưa có chức năng quản lý payout
2. **Transaction Filters**: Hỗ trợ filter mạnh mẽ để tìm kiếm giao dịch
3. **Dashboard**: Hiển thị thống kê tổng quan về Revenue, Payout, Refund (chỉ xem, không có chức năng quản lý payout)

---

## UI Libraries Suggested

- **Charts**: Recharts, Chart.js, or ApexCharts
- **Tables**: Ant Design Table, Material-UI Table, or TanStack Table
- **Date Picker**: Ant Design DatePicker, Material-UI DatePicker
- **File Upload**: Ant Design Upload, React Dropzone
- **Icons**: Material Icons, Ant Design Icons

---

## Notes

- Tất cả API đều yêu cầu JWT token trong header
- Format số tiền: Sử dụng `Intl.NumberFormat` để format VND (ví dụ: 1.000.000 đ)
- Format ngày giờ: Sử dụng `date-fns` hoặc `dayjs` để format (ví dụ: "01/12/2024 10:30")
- Loading states: Hiển thị skeleton/loading khi fetch data
- Error handling: Hiển thị toast/notification khi có lỗi
- Responsive: Đảm bảo UI hoạt động tốt trên mobile và desktop

