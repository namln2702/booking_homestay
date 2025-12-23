# PROMPT: Quản Lý Khiếu Nại (Complaints) cho Admin

## Tổng quan

Tạo giao diện quản lý khiếu nại (Complaints) cho admin với các chức năng: Xem danh sách tất cả khiếu nại, Xem chi tiết khiếu nại, và Xử lý khiếu nại (Đồng ý/Từ chối).

**Luồng khiếu nại:**
1. Customer tạo khiếu nại → Bill status = `HOST_COMPLAINT_PROCESSING`
2. Host xử lý:
   - Đồng ý: Bill status = `REFUNDED` (hoàn tiền)
   - Không đồng ý: Bill status = `ADMIN_COMPLAINT_PROCESSING` (chuyển lên admin)
3. Admin xử lý (khi bill ở trạng thái `ADMIN_COMPLAINT_PROCESSING`):
   - Đồng ý: Bill status = `REFUNDED`, tạo transaction REFUND với status `SUCCESS`
   - Từ chối: Bill status = `REJECTED`

---

## API Endpoints

### API 1: GET /admins/complaints - Lấy Danh Sách Tất Cả Khiếu Nại

**URL:** `GET /admins/complaints`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Query Parameters:**
- `page` (optional, default: 0): Số trang (bắt đầu từ 0)
- `size` (optional, default: 20): Số lượng items mỗi trang

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Complaints retrieved successfully",
  "data": {
    "page": 0,
    "size": 20,
    "total": 50,
    "items": [
      {
        "id": 1,
        "description": "Phòng không đúng như mô tả, thiếu nhiều tiện nghi",
        "createdAt": "2024-01-15T10:30:00",
        "adminName": "Nguyễn Văn Admin",
        "billId": 123,
        "billStatus": "ADMIN_COMPLAINT_PROCESSING",
        "homestayTitle": "Homestay ven biển Đà Nẵng",
        "imageUrls": [
          "https://example.com/image1.jpg",
          "https://example.com/image2.jpg"
        ]
      },
      {
        "id": 2,
        "description": "Dịch vụ không tốt, nhân viên không nhiệt tình",
        "createdAt": "2024-01-14T15:20:00",
        "adminName": "Trần Thị Admin",
        "billId": 124,
        "billStatus": "REFUNDED",
        "homestayTitle": "Homestay view núi Sapa",
        "imageUrls": [
          "https://example.com/image3.jpg"
        ]
      }
    ]
  }
}
```

**Error Responses:**
- `403`: Access denied (nếu không có quyền ADMIN)

---

### API 2: POST /admins/complaints/process-refund - Xử Lý Khiếu Nại

**URL:** `POST /admins/complaints/process-refund`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Request Body:**
```json
{
  "complaintId": 1,
  "approved": true
}
```

**Response khi APPROVE (200 OK):**
```json
{
  "status": 200,
  "message": "Complaint approved. Bill status changed to REFUNDED. Refund transaction created.",
  "data": {
    "billId": 123,
    "billStatus": "REFUNDED",
    "transactionId": 456,
    "transactionStatus": "SUCCESS",
    "amount": 5000000.00
  }
}
```

**Response khi REJECT (200 OK):**
```json
{
  "status": 200,
  "message": "Complaint rejected. Bill status changed to REJECTED.",
  "data": {
    "billId": 123,
    "billStatus": "REJECTED"
  }
}
```

**Error Responses:**
- `400`: Complaint ID is required / Approval decision is required / Bill must be in ADMIN_COMPLAINT_PROCESSING status / Bill total amount is not set
- `403`: Access denied / Admin account is not active
- `404`: Complaint not found / Bill not found / Admin user not found
- `500`: Admin user not found

**Validation Rules:**
- `complaintId`: Required
- `approved`: Required (boolean)
- Bill phải ở trạng thái `ADMIN_COMPLAINT_PROCESSING` mới có thể xử lý
- Khi approve, bill phải có `totalAmount` được set

**Business Rules:**
- **Khi APPROVE (`approved = true`)**:
  - Bill status chuyển thành `REFUNDED`
  - Tạo transaction REFUND mới với:
    - Type: `REFUND`
    - Status: `SUCCESS` (admin đã duyệt nên thành công luôn)
    - From: Admin User
    - To: Customer User
    - Amount: `bill.totalAmount` (100% giá trị bill)
    - CompletedAt: Thời gian hiện tại
  
- **Khi REJECT (`approved = false`)**:
  - Bill status chuyển thành `REJECTED`
  - Không tạo transaction REFUND

---

## TypeScript Interfaces

```typescript
interface ComplaintDTO {
  id: number;
  description: string;
  createdAt: string; // ISO 8601 format
  adminName: string | null;
  billId: number;
  billStatus: StatusBill;
  homestayTitle: string | null;
  imageUrls: string[] | null;
}

interface ProcessComplaintRefundRequest {
  complaintId: number;
  approved: boolean;
}

interface ProcessComplaintRefundResponse {
  billId: number;
  billStatus: StatusBill;
  transactionId?: number; // Chỉ có khi approved = true
  transactionStatus?: StatusTransaction; // Chỉ có khi approved = true
  amount?: number; // Chỉ có khi approved = true
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

enum StatusBill {
  DEPOSIT_PENDING = "DEPOSIT_PENDING",
  DEPOSIT_SUCCESS = "DEPOSIT_SUCCESS",
  CHECKIN_PENDING = "CHECKIN_PENDING",
  CHECKIN_SUCCESS = "CHECKIN_SUCCESS",
  CHECKOUT_PENDING = "CHECKOUT_PENDING",
  SUCCEED = "SUCCEED",
  COMPLAINT_PENDING = "COMPLAINT_PENDING",
  HOST_COMPLAINT_PROCESSING = "HOST_COMPLAINT_PROCESSING",
  ADMIN_COMPLAINT_PROCESSING = "ADMIN_COMPLAINT_PROCESSING",
  REFUNDED = "REFUNDED",
  REJECTED = "REJECTED",
  CANCELLED = "CANCELLED",
  CHECKIN_EXPIRED = "CHECKIN_EXPIRED",
  REMAINING_PAYMENT_FAILED = "REMAINING_PAYMENT_FAILED"
}
```

---

## UI/UX Requirements

### 1. Trang Quản Lý Khiếu Nại (Main Page)

**Layout:**
- **Header Section**:
  - Title: "Quản lý Khiếu Nại"
  - Filter/Search bar:
    - Filter theo trạng thái bill (`billStatus`):
      - Tất cả
      - `ADMIN_COMPLAINT_PROCESSING` (Đang chờ xử lý) - Badge màu vàng
      - `REFUNDED` (Đã hoàn tiền) - Badge màu xanh
      - `REJECTED` (Đã từ chối) - Badge màu đỏ
    - Search theo mô tả khiếu nại hoặc tên homestay
  - Stats Cards:
    - Tổng số khiếu nại
    - Số khiếu nại đang chờ xử lý (`ADMIN_COMPLAINT_PROCESSING`)
    - Số khiếu nại đã hoàn tiền (`REFUNDED`)
    - Số khiếu nại đã từ chối (`REJECTED`)

- **Table Section**:
  - **Columns**:
    - ID (sắp xếp được)
    - Thời gian tạo (sắp xếp được, format: DD/MM/YYYY HH:mm)
    - Mô tả khiếu nại (có thể truncate, click để xem chi tiết)
    - Tên Homestay (có thể click để xem chi tiết homestay)
    - Trạng thái Bill (Badge màu sắc):
      - `ADMIN_COMPLAINT_PROCESSING`: Yellow/Orange
      - `REFUNDED`: Green
      - `REJECTED`: Red
      - Các trạng thái khác: Gray
    - Bill ID (có thể click để xem chi tiết bill)
    - Admin xử lý (tên admin, có thể null)
    - Actions:
      - Button "Xem chi tiết" (mở modal/drawer chi tiết)
      - Button "Xử lý" (chỉ hiển thị khi `billStatus = ADMIN_COMPLAINT_PROCESSING`)

  - **Features**:
    - Pagination (hiển thị số trang, tổng số items)
    - Sortable columns (ID, createdAt)
    - Row selection (để hỗ trợ batch operations trong tương lai)
    - Empty state khi không có data
    - Highlight các row có `billStatus = ADMIN_COMPLAINT_PROCESSING` (cần xử lý ngay)

---

### 2. Modal/Drawer Chi Tiết Khiếu Nại

**Layout:**
- **Header**:
  - Title: "Chi tiết Khiếu Nại"
  - Badge trạng thái bill
  - Button đóng

- **Content Sections**:
  - **Thông tin Khiếu Nại**:
    - ID khiếu nại
    - Thời gian tạo
    - Mô tả (full text, có thể scroll nếu dài)
    - Danh sách hình ảnh (image gallery):
      - Hiển thị thumbnails
      - Click để xem full size
      - Có thể zoom/pan
      - Lightbox view

  - **Thông tin Bill**:
    - Bill ID (có thể click để xem chi tiết bill)
    - Trạng thái Bill (Badge)
    - Tổng tiền (`totalAmount`)
    - Check-in / Check-out dates
    - Link đến trang chi tiết bill

  - **Thông tin Homestay**:
    - Tên homestay (có thể click để xem chi tiết)
    - Host name (nếu có)
    - Link đến trang chi tiết homestay

  - **Thông tin Customer**:
    - Customer name
    - Email
    - Phone (nếu có)
    - Link đến trang chi tiết customer

  - **Lịch sử Xử Lý**:
    - Timeline hiển thị các bước:
      1. Customer tạo khiếu nại (createdAt)
      2. Host xử lý (nếu có)
      3. Admin xử lý (nếu đã xử lý)
    - Hiển thị admin name và thời gian xử lý

- **Actions** (chỉ hiển thị khi `billStatus = ADMIN_COMPLAINT_PROCESSING`):
  - Button "Đồng ý" (màu xanh, mở confirm dialog)
  - Button "Từ chối" (màu đỏ, mở confirm dialog)

---

### 3. Confirm Dialog Xử Lý Khiếu Nại

**Layout cho APPROVE:**
- **Content**:
  - Icon checkmark (màu xanh)
  - Title: "Xác nhận Đồng ý Khiếu Nại"
  - Message: "Bạn có chắc chắn muốn đồng ý khiếu nại này?"
  - Warning: "Hệ thống sẽ tự động hoàn tiền 100% giá trị bill cho khách hàng"
  - **Thông tin chi tiết**:
    - Bill ID: {billId}
    - Tổng tiền hoàn: {totalAmount} VNĐ
    - Customer: {customerName}
  
- **Actions**:
  - Button "Hủy"
  - Button "Xác nhận Đồng ý" (màu xanh, submit form)

**Layout cho REJECT:**
- **Content**:
  - Icon warning (màu đỏ)
  - Title: "Xác nhận Từ chối Khiếu Nại"
  - Message: "Bạn có chắc chắn muốn từ chối khiếu nại này?"
  - Warning: "Bill sẽ chuyển sang trạng thái REJECTED và không được hoàn tiền"
  - **Thông tin chi tiết**:
    - Bill ID: {billId}
    - Customer: {customerName}
  
- **Actions**:
  - Button "Hủy"
  - Button "Xác nhận Từ chối" (màu đỏ, submit form)

---

## Code Structure

```typescript
// Component chính
const AdminComplaintManagementPage = () => {
  const [complaints, setComplaints] = useState<ComplaintDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [total, setTotal] = useState(0);
  const [filterStatus, setFilterStatus] = useState<StatusBill | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedComplaint, setSelectedComplaint] = useState<ComplaintDTO | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [processConfirm, setProcessConfirm] = useState<{
    complaint: ComplaintDTO;
    approved: boolean;
  } | null>(null);

  // Fetch complaints
  const fetchComplaints = async () => {
    setLoading(true);
    try {
      const response = await api.get('/admins/complaints', {
        params: { page, size }
      });
      setComplaints(response.data.data.items);
      setTotal(response.data.data.total);
    } catch (error) {
      // Handle error
    } finally {
      setLoading(false);
    }
  };

  // Process complaint
  const handleProcessComplaint = async (complaintId: number, approved: boolean) => {
    try {
      const response = await api.post('/admins/complaints/process-refund', {
        complaintId,
        approved
      });
      // Show success message
      setProcessConfirm(null);
      setIsDetailModalOpen(false);
      fetchComplaints();
    } catch (error) {
      // Handle error (bill not in correct status, etc.)
    }
  };

  // Filter complaints
  const filteredComplaints = complaints.filter(complaint => {
    const matchesStatus = filterStatus === null || complaint.billStatus === filterStatus;
    const matchesSearch = searchTerm === '' || 
      complaint.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (complaint.homestayTitle && complaint.homestayTitle.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchesStatus && matchesSearch;
  });

  // Stats calculation
  const stats = {
    total: complaints.length,
    pending: complaints.filter(c => c.billStatus === StatusBill.ADMIN_COMPLAINT_PROCESSING).length,
    refunded: complaints.filter(c => c.billStatus === StatusBill.REFUNDED).length,
    rejected: complaints.filter(c => c.billStatus === StatusBill.REJECTED).length,
  };

  useEffect(() => {
    fetchComplaints();
  }, [page, size]);

  const getStatusBadgeColor = (status: StatusBill): string => {
    switch (status) {
      case StatusBill.ADMIN_COMPLAINT_PROCESSING:
        return 'orange';
      case StatusBill.REFUNDED:
        return 'green';
      case StatusBill.REJECTED:
        return 'red';
      default:
        return 'default';
    }
  };

  return (
    <div>
      {/* Header */}
      <div>
        <h1>Quản lý Khiếu Nại</h1>
        <div>
          <Input.Search
            placeholder="Tìm kiếm theo mô tả hoặc tên homestay..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 400 }}
          />
          <Select
            placeholder="Lọc theo trạng thái"
            value={filterStatus}
            onChange={setFilterStatus}
            allowClear
            style={{ width: 250, marginLeft: 8 }}
          >
            <Select.Option value={null}>Tất cả</Select.Option>
            <Select.Option value={StatusBill.ADMIN_COMPLAINT_PROCESSING}>
              Đang chờ xử lý
            </Select.Option>
            <Select.Option value={StatusBill.REFUNDED}>Đã hoàn tiền</Select.Option>
            <Select.Option value={StatusBill.REJECTED}>Đã từ chối</Select.Option>
          </Select>
        </div>
      </div>

      {/* Stats Cards */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Tổng số khiếu nại" value={stats.total} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Đang chờ xử lý" 
              value={stats.pending} 
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Đã hoàn tiền" 
              value={stats.refunded} 
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="Đã từ chối" 
              value={stats.rejected} 
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Table */}
      <Table
        dataSource={filteredComplaints}
        loading={loading}
        rowKey="id"
        rowClassName={(record) => 
          record.billStatus === StatusBill.ADMIN_COMPLAINT_PROCESSING ? 'pending-row' : ''
        }
        columns={[
          {
            title: 'ID',
            dataIndex: 'id',
            sorter: (a, b) => a.id - b.id,
            width: 80,
          },
          {
            title: 'Thời gian tạo',
            dataIndex: 'createdAt',
            sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
            render: (date: string) => format(new Date(date), 'dd/MM/yyyy HH:mm'),
            width: 150,
          },
          {
            title: 'Mô tả',
            dataIndex: 'description',
            ellipsis: true,
            render: (text: string) => (
              <Tooltip title={text}>
                {text.length > 50 ? `${text.substring(0, 50)}...` : text}
              </Tooltip>
            ),
          },
          {
            title: 'Homestay',
            dataIndex: 'homestayTitle',
            render: (text: string) => text || '-',
          },
          {
            title: 'Trạng thái',
            dataIndex: 'billStatus',
            render: (status: StatusBill) => (
              <Tag color={getStatusBadgeColor(status)}>
                {getStatusLabel(status)}
              </Tag>
            ),
          },
          {
            title: 'Bill ID',
            dataIndex: 'billId',
            render: (billId: number) => (
              <Button type="link" onClick={() => {/* Navigate to bill detail */}}>
                #{billId}
              </Button>
            ),
          },
          {
            title: 'Admin xử lý',
            dataIndex: 'adminName',
            render: (name: string) => name || '-',
          },
          {
            title: 'Thao tác',
            width: 200,
            render: (_, record: ComplaintDTO) => (
              <Space>
                <Button
                  icon={<EyeOutlined />}
                  onClick={() => {
                    setSelectedComplaint(record);
                    setIsDetailModalOpen(true);
                  }}
                >
                  Chi tiết
                </Button>
                {record.billStatus === StatusBill.ADMIN_COMPLAINT_PROCESSING && (
                  <Button
                    type="primary"
                    onClick={() => {
                      setSelectedComplaint(record);
                      setIsDetailModalOpen(true);
                    }}
                  >
                    Xử lý
                  </Button>
                )}
              </Space>
            ),
          },
        ]}
        pagination={{
          current: page + 1,
          pageSize: size,
          total: total,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} khiếu nại`,
          onChange: (page, size) => {
            setPage(page - 1);
            setSize(size);
          },
        }}
      />

      {/* Detail Modal */}
      <ComplaintDetailModal
        open={isDetailModalOpen}
        complaint={selectedComplaint}
        onClose={() => {
          setIsDetailModalOpen(false);
          setSelectedComplaint(null);
        }}
        onProcess={(complaint, approved) => {
          setProcessConfirm({ complaint, approved });
        }}
      />

      {/* Process Confirm Dialog */}
      <ProcessComplaintConfirmDialog
        open={!!processConfirm}
        complaint={processConfirm?.complaint}
        approved={processConfirm?.approved}
        onClose={() => setProcessConfirm(null)}
        onConfirm={() => {
          if (processConfirm) {
            handleProcessComplaint(processConfirm.complaint.id, processConfirm.approved);
          }
        }}
      />
    </div>
  );
};

// Helper functions
const getStatusLabel = (status: StatusBill): string => {
  const labels: Record<StatusBill, string> = {
    ADMIN_COMPLAINT_PROCESSING: 'Đang chờ xử lý',
    REFUNDED: 'Đã hoàn tiền',
    REJECTED: 'Đã từ chối',
    // ... other statuses
  };
  return labels[status] || status;
};
```

---

## Business Logic

1. **Luồng Xử Lý Khiếu Nại**:
   - Chỉ có thể xử lý khi `billStatus = ADMIN_COMPLAINT_PROCESSING`
   - Khi approve: Tự động tạo transaction REFUND với status SUCCESS
   - Khi reject: Chỉ đổi status, không tạo transaction

2. **Validation Rules**:
   - `complaintId`: Required
   - `approved`: Required (boolean)
   - Bill phải ở trạng thái `ADMIN_COMPLAINT_PROCESSING`

3. **Status Management**:
   - `ADMIN_COMPLAINT_PROCESSING`: Cần xử lý ngay (highlight trong table)
   - `REFUNDED`: Đã hoàn tiền thành công
   - `REJECTED`: Đã từ chối, không hoàn tiền

4. **Permissions**:
   - Chỉ ADMIN và SUPER_ADMIN mới có quyền xem và xử lý

---

## UI Libraries Suggested

- **Tables**: Ant Design Table, Material-UI Table, or TanStack Table
- **Modals/Drawers**: Ant Design Modal/Drawer, Material-UI Dialog
- **Image Gallery**: Ant Design Image, react-image-gallery, react-lightbox
- **Timeline**: Ant Design Timeline, Material-UI Timeline
- **Badges/Tags**: Ant Design Tag, Material-UI Chip
- **Icons**: Ant Design Icons, Material Icons
- **Date Formatting**: date-fns, moment.js, dayjs

---

## Notes

- Tất cả API ADMIN yêu cầu JWT token trong header với quyền `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN`
- Format validation: Hiển thị lỗi rõ ràng cho từng field
- Loading states: Hiển thị skeleton/loading khi fetch data
- Error handling: Hiển thị toast/notification khi có lỗi
- Success feedback: Hiển thị thông báo thành công sau mỗi action
- Responsive: Đảm bảo UI hoạt động tốt trên mobile và desktop
- Accessibility: Đảm bảo các button và form có proper labels và ARIA attributes
- Image handling: Hỗ trợ preview, zoom, và download hình ảnh khiếu nại
- Real-time updates: Có thể thêm polling hoặc WebSocket để cập nhật real-time khi có khiếu nại mới
- Export: Có thể thêm tính năng export danh sách khiếu nại ra Excel/PDF

