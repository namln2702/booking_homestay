# Prompt: Admin Login UI

## Yêu cầu
Tạo giao diện đăng nhập cho ADMIN với username và password.

## API Endpoint
**POST** `/admins/login`

### Request Body
```typescript
interface AdminLoginRequest {
  username: string;  // Required
  password: string;  // Required
}
```

### Response Success (200)
```typescript
{
  status: 200,
  message: "Login successful",
  data: {
    token: string,
    user: {
      username: string,
      email: string,
      role: string[],  // ["ROLE_ADMIN"] hoặc ["ROLE_SUPER_ADMIN"]
      statusAdmin: "ACTIVE",
      // ... other user fields
    }
  }
}
```

### Response Errors
- **400**: Username hoặc password rỗng
- **401**: Username/password không đúng
- **403**: Admin account không active

## UI Requirements
1. Form đăng nhập với 2 fields:
   - Username (text input)
   - Password (password input)
2. Button "Đăng nhập"
3. Hiển thị lỗi validation và thông báo lỗi từ API
4. Sau khi login thành công:
   - Lưu token vào localStorage/sessionStorage
   - Lưu user info
   - Redirect đến trang admin dashboard
5. Loading state khi đang gửi request

## Validation
- Username: required, không được rỗng
- Password: required, không được rỗng

## Tech Stack
- React/TypeScript
- Axios/Fetch cho API calls
- Form validation library (optional)
- UI library: Ant Design / Material-UI / Tailwind CSS

