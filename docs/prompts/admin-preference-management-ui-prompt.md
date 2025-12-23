# PROMPT: Quản Lý Preference cho Admin

## Tổng quan


**Error Responses:**
- `400`: Preference id is required / Preference DTO is required / Preference with name '{name}' already exists
- `403`: Access denied
- `404`: Preference not found with id: {id}

**Validation Rules:**
- `name`: Optional (nếu không cung cấp thì giữ nguyên), max 100 characters
- `description`: Optional Tạo giao diện quản lý Preference (Sở thích) cho admin với các chức năng: Xem danh sách, Tạo mới, Cập nhật, Xóa, và Tạo hàng loạt (Batch Create).

**Preference** là các sở thích của khách hàng (ví dụ: "Beach", "Mountain", "City", "Quiet", "Pet-friendly", v.v.). Admin có thể quản lý danh sách các preference này để khách hàng có thể chọn khi đăng ký hoặc cập nhật profile.

---

## API Endpoints

### API 1: GET /preferences - Lấy Tất Cả Preferences

**URL:** `GET /preferences`  
**Headers:** `Authorization: Bearer <adminToken>` (không bắt buộc, public endpoint)

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Preferences retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Beach",
      "description": "Near the beach"
    },
    {
      "id": 2,
      "name": "Mountain",
      "description": "Mountain view"
    },
    {
      "id": 3,
      "name": "City",
      "description": "In the city center"
    }
  ]
}
```

---

### API 2: GET /preferences/{id} - Lấy Preference Theo ID

**URL:** `GET /preferences/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (không bắt buộc, public endpoint)

**Path Parameters:**
- `id` (required): ID của preference

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Preference retrieved successfully",
  "data": {
    "id": 1,
    "name": "Beach",
    "description": "Near the beach"
  }
}
```

**Error Responses:**
- `400`: Preference id is required
- `404`: Preference not found with id: {id}

---

### API 3: POST /preferences - Tạo Mới Preference

**URL:** `POST /preferences`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Request Body:**
```json
{
  "name": "Pet-friendly",
  "description": "Allows pets"
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Preference created successfully",
  "data": {
    "id": 4,
    "name": "Pet-friendly",
    "description": "Allows pets"
  }
}
```

**Error Responses:**
- `400`: Preference DTO is required / Preference name is required / Preference with name '{name}' already exists
- `403`: Access denied (nếu không có quyền ADMIN)

**Validation Rules:**
- `name`: Required, max 100 characters
- `description`: Optional, max 500 characters
- Tên không được trùng (case-insensitive)

---

### API 4: POST /preferences/batch - Tạo Nhiều Preferences Cùng Lúc

**URL:** `POST /preferences/batch`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Request Body:**
```json
{
  "preferences": [
    {
      "name": "Quiet",
      "description": "Quiet neighborhood"
    },
    {
      "name": "Nightlife",
      "description": "Near nightlife areas"
    },
    {
      "name": "Family-friendly",
      "description": "Suitable for families"
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Preferences created successfully",
  "data": [
    {
      "id": 5,
      "name": "Quiet",
      "description": "Quiet neighborhood"
    },
    {
      "id": 6,
      "name": "Nightlife",
      "description": "Near nightlife areas"
    },
    {
      "id": 7,
      "name": "Family-friendly",
      "description": "Suitable for families"
    }
  ]
}
```

**Error Responses:**
- `400`: Preferences list cannot be null or empty / Cannot create more than 50 preferences at once / Duplicate preference name '{name}' in request / Preference with name '{name}' already exists
- `403`: Access denied

**Validation Rules:**
- Danh sách không được rỗng
- Tối đa 50 preferences mỗi lần
- Không được trùng tên trong cùng request
- Không được trùng tên với preferences đã tồn tại

---

### API 5: PUT /preferences/{id} - Cập Nhật Preference

**URL:** `PUT /preferences/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Path Parameters:**
- `id` (required): ID của preference cần cập nhật

**Request Body:**
```json
{
  "name": "Beach Updated",
  "description": "Updated description for beach preference"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Preference updated successfully",
  "data": {
    "id": 1,
    "name": "Beach Updated",
    "description": "Updated description for beach preference"
  }
}
```(nếu không cung cấp thì giữ nguyên), max 500 characters
- Tên mới không được trùng với preference khác (trừ chính nó)

---

### API 6: DELETE /preferences/{id} - Xóa Preference

**URL:** `DELETE /preferences/{id}`  
**Headers:** `Authorization: Bearer <adminToken>` (yêu cầu quyền ADMIN hoặc SUPER_ADMIN)

**Path Parameters:**
- `id` (required): ID của preference cần xóa

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Preference deleted successfully",
  "data": null
}
```

**Error Responses:**
- `400`: Preference id is required
- `403`: Access denied
- `404`: Preference not found with id: {id}
- `400`: Cannot delete preference. It is currently associated with {count} customer(s)

**Business Rules:**
- Không thể xóa preference nếu đang được sử dụng bởi customer nào đó
- Nếu có customer đang sử dụng, API sẽ trả về lỗi với thông báo số lượng customer đang sử dụng

---

## TypeScript Interfaces

```typescript
interface PreferenceDTO {
  id?: number;
  name: string;
  description?: string | null;
}

interface PreferenceBatchCreateRequest {
  preferences: PreferenceDTO[];
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

---

## UI/UX Requirements

### 1. Trang Quản Lý Preference (Main Page)

**Layout:**
- **Header Section**:
  - Title: "Quản lý Preference"
  - Button "Tạo mới" (mở modal tạo mới)
  - Button "Tạo hàng loạt" (mở modal batch create)
  - Search bar để tìm kiếm theo tên

- **Table Section**:
  - **Columns**:
    - ID (sắp xếp được)
    - Tên Preference (sắp xếp được, có thể search)
    - Mô tả (có thể truncate nếu quá dài)
    - Số lượng Customer đang sử dụng (nếu có API hỗ trợ, hoặc hiển thị badge "Đang sử dụng" nếu không thể xóa)
    - Actions: 
      - Icon "Sửa" (mở modal edit)
      - Icon "Xóa" (mở confirm dialog)

  - **Features**:
    - Pagination (nếu có nhiều preferences)
    - Sortable columns (ID, Name)
    - Row selection (để hỗ trợ batch operations trong tương lai)
    - Empty state khi không có data

- **Stats Cards** (optional):
  - Tổng số Preferences
  - Số Preferences đang được sử dụng
  - Số Preferences chưa được sử dụng

---

### 2. Modal Tạo Mới Preference

**Layout:**
- **Form Fields**:
  - **Tên Preference** (required):
    - Input text
    - Max length: 100 characters
    - Real-time validation: kiểm tra trùng tên
    - Hiển thị character count: "X/100"
  
  - **Mô tả** (optional):
    - Textarea
    - Max length: 500 characters
    - Hiển thị character count: "X/500"
    - Placeholder: "Nhập mô tả cho preference này..."

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
  - Title: "Tạo nhiều Preferences"
  - Badge hiển thị số lượng: "0/50"

- **Form Section**:
  - **Dynamic Form List**:
    - Mỗi item có 2 fields: Name (required) và Description (optional)
    - Button "Xóa" ở mỗi row để xóa item đó
    - Button "Thêm dòng" để thêm item mới
    - Tối đa 50 items
  
  - **Bulk Actions**:
    - Button "Xóa tất cả"
    - Button "Import từ CSV" (optional, nếu có thời gian)

- **Preview Section** (optional):
  - Hiển thị preview danh sách sẽ được tạo
  - Highlight các item có lỗi validation

- **Actions**:
  - Button "Hủy"
  - Button "Tạo tất cả" (disabled nếu có lỗi hoặc rỗng)

- **Validation**:
  - Kiểm tra trùng tên trong cùng form
  - Kiểm tra trùng tên với preferences đã tồn tại (có thể check khi blur)
  - Hiển thị lỗi cho từng row
  - Hiển thị tổng số lỗi ở header

---

### 4. Modal Cập Nhật Preference

**Layout:**
- Tương tự Modal Tạo Mới nhưng:
  - Title: "Cập nhật Preference"
  - Pre-fill form với data hiện tại
  - Button "Cập nhật" thay vì "Tạo"
  - Validation: cho phép giữ nguyên giá trị cũ (không bắt buộc phải thay đổi)

---

### 5. Confirm Dialog Xóa

**Layout:**
- **Content**:
  - Icon cảnh báo
  - Title: "Xác nhận xóa"
  - Message: "Bạn có chắc chắn muốn xóa preference '{name}'?"
  - Warning: "Hành động này không thể hoàn tác"
  
  - **Nếu preference đang được sử dụng**:
    - Hiển thị thông báo: "Không thể xóa preference này vì đang được sử dụng bởi {count} khách hàng"
    - Chỉ có button "Đóng"

- **Actions**:
  - Button "Hủy"
  - Button "Xóa" (màu đỏ, chỉ hiển thị nếu có thể xóa)

---

## Code Structure

```typescript
// Component chính
const PreferenceManagementPage = () => {
  const [preferences, setPreferences] = useState<PreferenceDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isBatchCreateModalOpen, setIsBatchCreateModalOpen] = useState(false);
  const [editingPreference, setEditingPreference] = useState<PreferenceDTO | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<{ id: number; name: string } | null>(null);

  // Fetch preferences
  const fetchPreferences = async () => {
    setLoading(true);
    try {
      const response = await api.get('/preferences');
      setPreferences(response.data.data);
    } catch (error) {
      // Handle error
    } finally {
      setLoading(false);
    }
  };

  // Create preference
  const handleCreate = async (data: PreferenceDTO) => {
    try {
      const response = await api.post('/preferences', data);
      // Show success message
      setIsCreateModalOpen(false);
      fetchPreferences();
    } catch (error) {
      // Handle error (duplicate name, etc.)
    }
  };

  // Batch create
  const handleBatchCreate = async (data: PreferenceBatchCreateRequest) => {
    try {
      const response = await api.post('/preferences/batch', data);
      // Show success message
      setIsBatchCreateModalOpen(false);
      fetchPreferences();
    } catch (error) {
      // Handle error (duplicate names, etc.)
    }
  };

  // Update preference
  const handleUpdate = async (id: number, data: PreferenceDTO) => {
    try {
      const response = await api.put(`/preferences/${id}`, data);
      // Show success message
      setEditingPreference(null);
      fetchPreferences();
    } catch (error) {
      // Handle error
    }
  };

  // Delete preference
  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/preferences/${id}`);
      // Show success message
      setDeleteConfirm(null);
      fetchPreferences();
    } catch (error) {
      // Handle error (preference in use, etc.)
    }
  };

  // Filter preferences by search term
  const filteredPreferences = preferences.filter(p =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (p.description && p.description.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  useEffect(() => {
    fetchPreferences();
  }, []);

  return (
    <div>
      {/* Header */}
      <div>
        <h1>Quản lý Preference</h1>
        <div>
          <Input.Search
            placeholder="Tìm kiếm preference..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 300 }}
          />
          <Button onClick={() => setIsCreateModalOpen(true)}>Tạo mới</Button>
          <Button onClick={() => setIsBatchCreateModalOpen(true)}>Tạo hàng loạt</Button>
        </div>
      </div>

      {/* Table */}
      <Table
        dataSource={filteredPreferences}
        loading={loading}
        columns={[
          {
            title: 'ID',
            dataIndex: 'id',
            sorter: (a, b) => a.id - b.id,
          },
          {
            title: 'Tên',
            dataIndex: 'name',
            sorter: (a, b) => a.name.localeCompare(b.name),
          },
          {
            title: 'Mô tả',
            dataIndex: 'description',
            render: (text) => text || '-',
          },
          {
            title: 'Thao tác',
            render: (_, record) => (
              <Space>
                <Button
                  icon={<EditOutlined />}
                  onClick={() => setEditingPreference(record)}
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
            ),
          },
        ]}
      />

      {/* Create Modal */}
      <PreferenceFormModal
        open={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreate}
        title="Tạo mới Preference"
      />

      {/* Batch Create Modal */}
      <BatchPreferenceFormModal
        open={isBatchCreateModalOpen}
        onClose={() => setIsBatchCreateModalOpen(false)}
        onSubmit={handleBatchCreate}
      />

      {/* Edit Modal */}
      {editingPreference && (
        <PreferenceFormModal
          open={!!editingPreference}
          onClose={() => setEditingPreference(null)}
          onSubmit={(data) => handleUpdate(editingPreference.id!, data)}
          initialData={editingPreference}
          title="Cập nhật Preference"
        />
      )}

      {/* Delete Confirm */}
      <DeleteConfirmModal
        open={!!deleteConfirm}
        preference={deleteConfirm}
        onClose={() => setDeleteConfirm(null)}
        onConfirm={() => deleteConfirm && handleDelete(deleteConfirm.id)}
      />
    </div>
  );
};
```

---

## Business Logic

1. **Validation Rules**:
   - Tên preference: Required, max 100 characters, không được trùng (case-insensitive)
   - Mô tả: Optional, max 500 characters

2. **Duplicate Name Check**:
   - Khi tạo mới: Kiểm tra với tất cả preferences đã tồn tại
   - Khi cập nhật: Kiểm tra với tất cả preferences khác (trừ chính nó)
   - Case-insensitive: "Beach" và "beach" được coi là trùng

3. **Delete Protection**:
   - Không thể xóa preference nếu đang được sử dụng bởi customer
   - Hiển thị thông báo rõ ràng số lượng customer đang sử dụng

4. **Batch Create**:
   - Tối đa 50 preferences mỗi lần
   - Kiểm tra trùng tên trong cùng request
   - Kiểm tra trùng tên với preferences đã tồn tại
   - Nếu có lỗi ở bất kỳ item nào, toàn bộ request sẽ fail

---

## UI Libraries Suggested

- **Tables**: Ant Design Table, Material-UI Table, or TanStack Table
- **Forms**: Ant Design Form, React Hook Form, Formik
- **Modals**: Ant Design Modal, Material-UI Dialog
- **Icons**: Ant Design Icons, Material Icons
- **Validation**: Yup, Zod, hoặc built-in validation của form library

---

## Notes

- Tất cả API ADMIN yêu cầu JWT token trong header với quyền `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN`
- API GET `/preferences` và GET `/preferences/{id}` là public (không cần authentication)
- Format validation: Hiển thị lỗi rõ ràng cho từng field
- Loading states: Hiển thị skeleton/loading khi fetch data
- Error handling: Hiển thị toast/notification khi có lỗi
- Success feedback: Hiển thị thông báo thành công sau mỗi action
- Responsive: Đảm bảo UI hoạt động tốt trên mobile và desktop
- Accessibility: Đảm bảo các button và form có proper labels và ARIA attributes

