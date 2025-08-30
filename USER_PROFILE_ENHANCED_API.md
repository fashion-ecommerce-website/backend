# User Profile Enhanced API Documentation

## Tổng quan tính năng User Profile Screen

Tính năng User Profile Screen được thiết kế để cung cấp một giao diện hoàn chỉnh cho việc quản lý thông tin cá nhân của user, bao gồm:

1. **List User Information** - Hiển thị đầy đủ thông tin user
2. **Edit User Profile** - Chỉnh sửa thông tin cá nhân
3. **Address Management** - Quản lý nhiều địa chỉ (1 user có thể có nhiều address)

## Database Schema

Dựa trên database schema thực tế:

```sql
CREATE TABLE "users" (
  "id" bigserial PRIMARY KEY,
  "email" varchar(320) UNIQUE NOT NULL,
  "password" text NOT NULL,
  "username" text,
  "phone" text,
  "avatar_url" text,
  "is_active" boolean DEFAULT true,
  "reason" text,
  "created_at" timestamp DEFAULT (now()),
  "updated_at" timestamp DEFAULT (now())
);
```

## API Endpoints

### 1. Lấy thông tin profile đầy đủ

```http
GET /api/user/profile/detail
Authorization: Bearer {token}
```

**Response:**
```json
{
  "userInfo": {
    "id": 1,
    "email": "user@example.com",
    "username": "username",
    "phone": "0123456789",
    "avatarUrl": "https://example.com/avatar.jpg",
    "isActive": true,
    "reason": null,
    "emailVerified": true,
    "phoneVerified": false,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "lastLoginAt": "2024-01-01T00:00:00",
    "roles": ["USER"]
  },
  "addresses": [
    {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "phone": "0123456789",
      "line": "123 Đường ABC",
      "ward": "Phường 1",
      "city": "TP.HCM",
      "province": "TP.HCM",
      "countryCode": "VN",
      "postalCode": "70000",
      "isDefault": true,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ],
  "stats": {
    "totalAddresses": 2,
    "defaultAddressCount": 1,
    "hasPhoneVerified": false,
    "hasEmailVerified": true,
    "accountStatus": "ACTIVE",
    "hasUsername": true,
    "hasPhone": true,
    "hasAvatar": true
  }
}
```

### 2. Cập nhật profile nâng cao

```http
PUT /api/user/profile/enhanced
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "new_username",
  "phone": "0987654321",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Response:** Tương tự như GET /api/user/profile/detail

### 3. Bulk update addresses

```http
POST /api/user/profile/addresses/bulk
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "operations": [
    {
      "operation": "CREATE",
      "addressData": {
        "fullName": "Nguyễn Văn B",
        "phone": "0123456789",
        "line": "456 Đường XYZ",
        "ward": "Phường 2",
        "city": "Hà Nội",
        "province": "Hà Nội",
        "countryCode": "VN",
        "postalCode": "10000",
        "isDefault": false
      }
    },
    {
      "id": 1,
      "operation": "UPDATE",
      "addressData": {
        "fullName": "Nguyễn Văn A Updated",
        "phone": "0123456789",
        "line": "123 Đường ABC Updated",
        "ward": "Phường 1",
        "city": "TP.HCM",
        "province": "TP.HCM",
        "countryCode": "VN",
        "postalCode": "70000",
        "isDefault": true
      }
    },
    {
      "id": 3,
      "operation": "DELETE"
    }
  ]
}
```

### 4. Kiểm tra độ hoàn thành profile

```http
GET /api/user/profile/completion
Authorization: Bearer {token}
```

**Response:**
```json
{
  "isComplete": false,
  "completionPercentage": 83
}
```

## API Endpoints gốc (vẫn được hỗ trợ)

### 1. Basic profile operations
- `GET /api/user/profile` - Lấy thông tin profile cơ bản
- `PUT /api/user/profile` - Cập nhật profile cơ bản

### 2. Address operations  
- `GET /api/user/addresses` - Lấy danh sách địa chỉ
- `POST /api/user/addresses` - Tạo địa chỉ mới
- `PUT /api/user/addresses/{id}` - Cập nhật địa chỉ
- `DELETE /api/user/addresses/{id}` - Xóa địa chỉ
- `POST /api/user/addresses/{id}/default` - Đặt địa chỉ mặc định

## Quy tắc Business Logic

### Profile Completion Rules (dựa trên DB schema thực tế)
1. **Email** - Bắt buộc, unique, max 320 ký tự
2. **Username** - Tùy chọn, nếu có thì phải unique
3. **Phone** - Tùy chọn, format 10-11 số
4. **Avatar URL** - Tùy chọn
5. **Address** - Phải có ít nhất 1 địa chỉ cho profile hoàn chỉnh
6. **Verification** - Email và phone đều phải được xác thực

### Profile Completion Calculation (6 points total)
1. **Username** (1 point) - Có username
2. **Phone** (1 point) - Có số điện thoại
3. **Avatar** (1 point) - Có avatar URL
4. **Address** (1 point) - Có ít nhất 1 địa chỉ
5. **Email Verified** (1 point) - Email đã được xác thực
6. **Phone Verified** (1 point) - Phone đã được xác thực

### Address Management Rules
1. **Default Address** - Mỗi user chỉ có 1 địa chỉ mặc định
2. **First Address** - Địa chỉ đầu tiên tự động được đặt làm mặc định
3. **Delete Default** - Khi xóa địa chỉ mặc định, địa chỉ khác sẽ tự động được chọn làm mặc định
4. **Ownership** - User chỉ có thể thao tác với địa chỉ của chính mình

### Validation Rules
1. **Username** - Nếu có thì từ 3-50 ký tự và phải unique
2. **Phone** - Nếu có thì phải theo format ^[0-9]{10,11}$
3. **Avatar URL** - Nếu có thì max 500 ký tự
4. **Phone Change** - Khi thay đổi số điện thoại, phone verification sẽ bị reset
5. **Bulk Operations** - Tất cả operations trong 1 request phải thành công

## Error Handling

Tất cả API đều trả về cấu trúc error chuẩn:

```json
{
  "timestamp": "2024-01-01T00:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists",
  "path": "/api/user/profile/enhanced"
}
```

## Notes

- Code được thiết kế để follow chính xác database schema hiện tại
- Không có thêm trường mới vào database
- Tất cả validation dựa trên cấu trúc database thực tế
- Hỗ trợ backward compatibility với các API endpoints cũ 