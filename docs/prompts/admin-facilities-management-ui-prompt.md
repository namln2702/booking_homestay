# PROMPT: Quản Lý Facilities cho Admin

## Tổng quan

Tạo giao diện quản lý Facilities (Tiện nghi) cho admin với các chức năng: Xem danh sách, Tạo mới, Cập nhật, Xóa, và Tạo hàng loạt (Batch Create).

**Facilities** là các tiện nghi của homestay (ví dụ: "Điều hòa", "Máy sấy", "Smart TV", "WiFi", v.v.) được phân loại theo category (General, Bedroom, Bathroom, Kitchen, Special, View). Admin có thể quản lý danh sách các facilities này để host có thể chọn khi tạo/cập nhật homestay.

---

## API Endpoints

### API 1: GET /facilities - Lấy Tất Cả Facilities

**URL:** `GET /facilities`  
**Headers:** `Authorization: Bearer <adminToken>` (không bắt buộc, public endpoint)

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Facilities retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Điều hòa",
      "category": "General"
    },
    {
      "id": 2,
      "name": "WiFi",
      "category": "General"
    },
    {
      "id": 3,
      "name": "Máy sấy",
      "category": "Bathroom"
    },
    {
      "id": 4,
      "name": "Smart TV",
      "category": "Bedroom"
    }
  ]
}
```

---

### API 2: GET /facilities/{id} - Lấy Facility Theo ID

**URL:** `GET /facilities/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (không bắt buộc, public endpoint)

**Path Parameters:**
- `id` (required): ID của facility

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Facility retrieved successfully",
  "data": {
    "id": 1,
    "name": "Điều hòa",
    "category": "General"
  }
}
```

**Error Responses:**
- `400`: Facility id is required
- `404`: Facility not found for id {id}

---

### API 3: POST /facilities - Tạo Mới Facility

**URL:** `POST /facilities`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Request Body:**
```json
{
  "name": "Máy giặt",
  "category": "Kitchen"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Facility created successfully",
  "data": {
    "id": 5,
    "name": "Máy giặt",
    "category": "Kitchen"
  }
}
```

**Error Responses:**
- `400`: Facility DTO is required / Facility name is required / Facility category is required / Facility with name '{name}' already exists
- `403`: Access denied (nếu không có quyền ADMIN)

**Validation Rules:**
- `name`: Required, max 100 characters
- `category`: Required, max 50 characters
- Tên không được trùng (case-insensitive)
- Category phải là một trong: General, Bedroom, Bathroom, Kitchen, Special, View (hoặc theo enum/validation của backend)

---

### API 4: POST /facilities/batch - Tạo Nhiều Facilities Cùng Lúc

**URL:** `POST /facilities/batch`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Request Body:**
```json
{
  "facilities": [
    {
      "name": "Bếp gas",
      "category": "Kitchen"
    },
    {
      "name": "Lò vi sóng",
      "category": "Kitchen"
    },
    {
      "name": "Tủ lạnh",
      "category": "Kitchen"
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Facilities created successfully",
  "data": [
    {
      "id": 6,
      "name": "Bếp gas",
      "category": "Kitchen"
    },
    {
      "id": 7,
      "name": "Lò vi sóng",
      "category": "Kitchen"
    },
    {
      "id": 8,
      "name": "Tủ lạnh",
      "category": "Kitchen"
    }
  ]
}
```

**Error Responses:**
- `400`: Facilities list cannot be null or empty / Cannot create more than 50 facilities at once / Duplicate facility name '{name}' in request / Facility with name '{name}' already exists / Facility name is required at index {i} / Facility category is required at index {i}
- `403`: Access denied

**Validation Rules:**
- Danh sách không được rỗng
- Tối đa 50 facilities mỗi lần
- Không được trùng tên trong cùng request
- Không được trùng tên với facilities đã tồn tại
- Mỗi facility phải có name và category

---

### API 5: PUT /facilities/{id} - Cập Nhật Facility

**URL:** `PUT /facilities/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Path Parameters:**
- `id` (required): ID của facility cần cập nhật

**Request Body:**
```json
{
  "name": "Điều hòa nhiệt độ",
  "category": "General"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Facility updated successfully",
  "data": {
    "id": 1,
    "name": "Điều hòa nhiệt độ",
    "category": "General"
  }
}
```

**Error Responses:**
- `400`: Facility id is required / Facility DTO is required / Facility with name '{name}' already exists
- `403`: Access denied
- `404`: Facility not found with id: {id}

**Validation Rules:**
- `name`: Optional (nếu không cung cấp thì giữ nguyên), max 100 characters
- `category`: Optional (nếu không cung cấp thì giữ nguyên), max 50 characters
- Tên mới không được trùng với facility khác (trừ chính nó)

---

### API 6: DELETE /facilities/{id} - Xóa Facility

**URL:** `DELETE /facilities/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Path Parameters:**
- `id` (required): ID của facility cần xóa

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Facility deleted successfully",
  "data": null
}
```

**Error Responses:**
- `400`: Facility id is required
- `403`: Access denied
- `404`: Facility not found with id: {id}
- `400`: Cannot delete facility. It is currently associated with {count} homestay(s)

**Business Rules:**
- Không thể xóa facility nếu đang được sử dụng bởi homestay nào đó
- Nếu có homestay đang sử dụng, API sẽ trả về lỗi với thông báo số lượng homestay đang sử dụng
- Xóa là soft delete (set deleted = true), không xóa khỏi database

---

## TypeScript Interfaces

```typescript
interface FacilitiesDTO {
  id?: number;
  name: string;
  category: string; // General, Bedroom, Bathroom, Kitchen, Special, View
}

interface FacilitiesBatchCreateRequest {
  facilities: FacilitiesDTO[];
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

---

## UI/UX Requirements

### 1. Trang Quản Lý Facilities (Main Page)

**Layout:**
- **Header Section**:
  - Title: "Quản lý Facilities"
  - Button "Tạo mới" (mở modal tạo mới) - Chỉ hiển thị cho ADMIN
  - Button "Tạo hàng loạt" (mở modal batch create) - Chỉ hiển thị cho ADMIN
  - Search bar để tìm kiếm theo tên hoặc category
  - Filter dropdown theo category (General, Bedroom, Bathroom, Kitchen, Special, View)

- **Table Section**:
  - **Columns**:
    - ID (sắp xếp được)
    - Tên Facility (sắp xếp được, có thể search)
    - Category (sắp xếp được, có thể filter)
      - Badge màu khác nhau cho từng category:
        - General: Blue
        - Bedroom: Purple
        - Bathroom: Cyan
        - Kitchen: Orange
        - Special: Green
        - View: Pink
    - Số lượng Homestay đang sử dụng (nếu có API hỗ trợ, hoặc hiển thị badge "Đang sử dụng" nếu không thể xóa)
    - Actions: 
      - Icon "Sửa" (mở modal edit) - Chỉ hiển thị cho ADMIN
      - Icon "Xóa" (mở confirm dialog) - Chỉ hiển thị cho ADMIN

  - **Features**:
    - Pagination (nếu có nhiều facilities)
    - Sortable columns (ID, Name, Category)
    - Row selection (để hỗ trợ batch operations trong tương lai)
    - Empty state khi không có data
    - Group by category (optional, có thể toggle)

- **Stats Cards** (optional):
  - Tổng số Facilities
  - Số Facilities theo từng category
  - Số Facilities đang được sử dụng

---

### 2. Modal Tạo Mới Facility

**Layout:**
- **Form Fields**:
  - **Tên Facility** (required):
    - Input text
    - Max length: 100 characters
    - Real-time validation: kiểm tra trùng tên
    - Hiển thị character count: "X/100"
    - Placeholder: "VD: Điều hòa, WiFi, Máy sấy..."
  
  - **Category** (required):
    - Select dropdown với các options:
      - General
      - Bedroom
      - Bathroom
      - Kitchen
      - Special
      - View
    - Có thể thêm icon cho mỗi category
    - Hiển thị màu tương ứng với category

- **Actions**:
  - Button "Hủy" (đóng modal)
  - Button "Tạo" (submit form, disabled khi invalid)

- **Validation**:
  - Hiển thị lỗi real-time dưới mỗi field
  - Disable submit button khi form invalid
  - Hiển thị loading state khi đang submit

---

### 3. Modal Tạo Hàng Loạt (Batch Create)

**Layout:**
- **Header**:
  - Title: "Tạo nhiều Facilities"
  - Badge hiển thị số lượng: "0/50"

- **Form Section**:
  - **Dynamic Form List**:
    - Mỗi item có 2 fields: Name (required) và Category (required - dropdown)
    - Button "Xóa" ở mỗi row để xóa item đó
    - Button "Thêm dòng" để thêm item mới
    - Tối đa 50 items
  
  - **Bulk Actions**:
    - Button "Xóa tất cả"
    - Button "Import từ CSV" (optional, nếu có thời gian)
    - Button "Fill category" - Fill category giống nhau cho tất cả items

- **Preview Section** (optional):
  - Hiển thị preview danh sách sẽ được tạo
  - Group by category để dễ xem
  - Highlight các item có lỗi validation

- **Actions**:
  - Button "Hủy"
  - Button "Tạo tất cả" (disabled nếu có lỗi hoặc rỗng)

- **Validation**:
  - Kiểm tra trùng tên trong cùng form
  - Kiểm tra trùng tên với facilities đã tồn tại (có thể check khi blur)
  - Hiển thị lỗi cho từng row
  - Hiển thị tổng số lỗi ở header

---

### 4. Modal Cập Nhật Facility

**Layout:**
- Tương tự Modal Tạo Mới nhưng:
  - Title: "Cập nhật Facility"
  - Pre-fill form với data hiện tại
  - Button "Cập nhật" thay vì "Tạo"
  - Validation: cho phép giữ nguyên giá trị cũ (không bắt buộc phải thay đổi)

---

### 5. Confirm Dialog Xóa

**Layout:**
- **Content**:
  - Icon cảnh báo
  - Title: "Xác nhận xóa"
  - Message: "Bạn có chắc chắn muốn xóa facility '{name}'?"
  - Warning: "Hành động này không thể hoàn tác"
  
  - **Nếu facility đang được sử dụng**:
    - Hiển thị thông báo: "Không thể xóa facility này vì đang được sử dụng bởi {count} homestay"
    - Chỉ có button "Đóng"

- **Actions**:
  - Button "Hủy"
  - Button "Xóa" (màu đỏ, chỉ hiển thị nếu có thể xóa)

---

## Code Structure

```typescript
// Component chính
const FacilitiesManagementPage = () => {
  const [facilities, setFacilities] = useState<FacilitiesDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isBatchCreateModalOpen, setIsBatchCreateModalOpen] = useState(false);
  const [editingFacility, setEditingFacility] = useState<FacilitiesDTO | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<{ id: number; name: string } | null>(null);
  const isAdmin = useAuth().user?.role?.includes('ROLE_ADMIN') || useAuth().user?.role?.includes('ROLE_SUPER_ADMIN');

  // Fetch facilities
  const fetchFacilities = async () => {
    setLoading(true);
    try {
      const response = await api.get('/facilities');
      setFacilities(response.data.data);
    } catch (error) {
      // Handle error
    } finally {
      setLoading(false);
    }
  };

  // Create facility
  const handleCreate = async (data: FacilitiesDTO) => {
    try {
      const response = await api.post('/facilities', data);
      // Show success message
      setIsCreateModalOpen(false);
      fetchFacilities();
    } catch (error) {
      // Handle error (duplicate name, etc.)
    }
  };

  // Batch create
  const handleBatchCreate = async (data: FacilitiesBatchCreateRequest) => {
    try {
      const response = await api.post('/facilities/batch', data);
      // Show success message
      setIsBatchCreateModalOpen(false);
      fetchFacilities();
    } catch (error) {
      // Handle error (duplicate names, etc.)
    }
  };

  // Update facility
  const handleUpdate = async (id: number, data: FacilitiesDTO) => {
    try {
      const response = await api.put(`/facilities/${id}`, data);
      // Show success message
      setEditingFacility(null);
      fetchFacilities();
    } catch (error) {
      // Handle error
    }
  };

  // Delete facility
  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/facilities/${id}`);
      // Show success message
      setDeleteConfirm(null);
      fetchFacilities();
    } catch (error) {
      // Handle error (facility in use, etc.)
    }
  };

  // Filter facilities by search term and category
  const filteredFacilities = facilities.filter(f => {
    const matchesSearch = f.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      f.category.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === null || f.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Group facilities by category for stats
  const facilitiesByCategory = facilities.reduce((acc, facility) => {
    acc[facility.category] = (acc[facility.category] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  useEffect(() => {
    fetchFacilities();
  }, []);

  const categories = ['General', 'Bedroom', 'Bathroom', 'Kitchen', 'Special', 'View'];

  return (
    <div>
      {/* Header */}
      <div>
        <h1>Quản lý Facilities</h1>
        <div>
          <Input.Search
            placeholder="Tìm kiếm facility..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 300 }}
          />
          <Select
            placeholder="Lọc theo category"
            value={selectedCategory}
            onChange={setSelectedCategory}
            allowClear
            style={{ width: 200, marginLeft: 8 }}
          >
            {categories.map(cat => (
              <Select.Option key={cat} value={cat}>{cat}</Select.Option>
            ))}
          </Select>
          {isAdmin && (
            <>
              <Button onClick={() => setIsCreateModalOpen(true)} style={{ marginLeft: 8 }}>
                Tạo mới
              </Button>
              <Button onClick={() => setIsBatchCreateModalOpen(true)} style={{ marginLeft: 8 }}>
                Tạo hàng loạt
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Stats Cards */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic title="Tổng số Facilities" value={facilities.length} />
          </Card>
        </Col>
        {categories.map(cat => (
          <Col span={3} key={cat}>
            <Card>
              <Statistic title={cat} value={facilitiesByCategory[cat] || 0} />
            </Card>
          </Col>
        ))}
      </Row>

      {/* Table */}
      <Table
        dataSource={filteredFacilities}
        loading={loading}
        columns={[
          {
            title: 'ID',
            dataIndex: 'id',
            sorter: (a, b) => a.id! - b.id!,
          },
          {
            title: 'Tên',
            dataIndex: 'name',
            sorter: (a, b) => a.name.localeCompare(b.name),
          },
          {
            title: 'Category',
            dataIndex: 'category',
            sorter: (a, b) => a.category.localeCompare(b.category),
            render: (category: string) => (
              <Tag color={getCategoryColor(category)}>{category}</Tag>
            ),
          },
          {
            title: 'Thao tác',
            render: (_, record) => isAdmin ? (
              <Space>
                <Button
                  icon={<EditOutlined />}
                  onClick={() => setEditingFacility(record)}
                >
                  Sửa
                </Button>
                <Button
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => setDeleteConfirm({ id: record.id!, name: record.name })}
                >
                  Xóa
                </Button>
              </Space>
            ) : null,
          },
        ]}
      />

      {/* Create Modal */}
      {isAdmin && (
        <>
          <FacilityFormModal
            open={isCreateModalOpen}
            onClose={() => setIsCreateModalOpen(false)}
            onSubmit={handleCreate}
            title="Tạo mới Facility"
          />

          {/* Batch Create Modal */}
          <BatchFacilityFormModal
            open={isBatchCreateModalOpen}
            onClose={() => setIsBatchCreateModalOpen(false)}
            onSubmit={handleBatchCreate}
          />

          {/* Edit Modal */}
          {editingFacility && (
            <FacilityFormModal
              open={!!editingFacility}
              onClose={() => setEditingFacility(null)}
              onSubmit={(data) => handleUpdate(editingFacility.id!, data)}
              initialData={editingFacility}
              title="Cập nhật Facility"
            />
          )}

          {/* Delete Confirm */}
          <DeleteConfirmModal
            open={!!deleteConfirm}
            facility={deleteConfirm}
            onClose={() => setDeleteConfirm(null)}
            onConfirm={() => deleteConfirm && handleDelete(deleteConfirm.id)}
          />
        </>
      )}
    </div>
  );
};

// Helper function for category colors
const getCategoryColor = (category: string): string => {
  const colorMap: Record<string, string> = {
    General: 'blue',
    Bedroom: 'purple',
    Bathroom: 'cyan',
    Kitchen: 'orange',
    Special: 'green',
    View: 'pink',
  };
  return colorMap[category] || 'default';
};
```

---

## Business Logic

1. **Validation Rules**:
   - Tên facility: Required, max 100 characters, không được trùng (case-insensitive)
   - Category: Required, max 50 characters, phải là một trong các giá trị hợp lệ

2. **Duplicate Name Check**:
   - Khi tạo mới: Kiểm tra với tất cả facilities đã tồn tại (chưa bị xóa)
   - Khi cập nhật: Kiểm tra với tất cả facilities khác (trừ chính nó, chưa bị xóa)
   - Case-insensitive: "Điều hòa" và "điều hòa" được coi là trùng

3. **Delete Protection**:
   - Không thể xóa facility nếu đang được sử dụng bởi homestay
   - Hiển thị thông báo rõ ràng số lượng homestay đang sử dụng
   - Xóa là soft delete (set deleted = true)

4. **Batch Create**:
   - Tối đa 50 facilities mỗi lần
   - Kiểm tra trùng tên trong cùng request
   - Kiểm tra trùng tên với facilities đã tồn tại
   - Nếu có lỗi ở bất kỳ item nào, toàn bộ request sẽ fail

5. **Category Management**:
   - Categories: General, Bedroom, Bathroom, Kitchen, Special, View
   - Hiển thị màu sắc khác nhau cho từng category
   - Có thể filter và group by category

---

## UI Libraries Suggested

- **Tables**: Ant Design Table, Material-UI Table, or TanStack Table
- **Forms**: Ant Design Form, React Hook Form, Formik
- **Modals**: Ant Design Modal, Material-UI Dialog
- **Icons**: Ant Design Icons, Material Icons
- **Tags/Badges**: Ant Design Tag, Material-UI Chip
- **Select**: Ant Design Select, Material-UI Select
- **Validation**: Yup, Zod, hoặc built-in validation của form library

---

## Notes

- Tất cả API ADMIN yêu cầu JWT token trong header với quyền `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN`
- API GET `/facilities` và GET `/facilities/{id}` là public (không cần authentication)
- API POST `/facilities` và DELETE `/facilities/{id}` chỉ dành cho ADMIN
- Format validation: Hiển thị lỗi rõ ràng cho từng field
- Loading states: Hiển thị skeleton/loading khi fetch data
- Error handling: Hiển thị toast/notification khi có lỗi
- Success feedback: Hiển thị thông báo thành công sau mỗi action
- Responsive: Đảm bảo UI hoạt động tốt trên mobile và desktop
- Accessibility: Đảm bảo các button và form có proper labels và ARIA attributes
- Category colors: Sử dụng màu sắc nhất quán để phân biệt categories
- Filter và search: Hỗ trợ filter theo category và search theo tên

