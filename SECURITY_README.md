# Spring Boot Security với JWT

## Tổng quan
Hệ thống security hoàn chỉnh cho Spring Boot với JWT authentication, bao gồm:
- JWT Token authentication
- Role-based authorization
- User management
- Exception handling
- CORS configuration

## Cấu trúc dự án

```
src/main/java/com/spring/fit/backend/security/
├── config/
│   ├── SecurityConfig.java          # Cấu hình Spring Security
│   └── DataInitializer.java         # Khởi tạo dữ liệu mẫu
├── dto/
│   ├── AuthResponse.java            # Response cho authentication
│   ├── LoginRequest.java            # Request đăng nhập
│   ├── RegisterRequest.java         # Request đăng ký
│   └── UserResponse.java            # Response thông tin user
├── entity/
│   ├── User.java                    # Entity User
│   └── Role.java                    # Enum Role
├── exception/
│   └── SecurityExceptionHandler.java # Xử lý exception security
├── jwt/
│   ├── JwtAuthenticationFilter.java # JWT Filter
│   └── JwtTokenProvider.java        # JWT Provider
├── repository/
│   └── UserRepository.java          # Repository cho User
├── service/
│   ├── AuthService.java             # Service authentication
│   └── CustomUserDetailsService.java # UserDetailsService
├── AuthController.java              # Controller authentication
├── AdminController.java             # Controller admin
└── PublicController.java            # Controller public
```

## API Endpoints

### Public Endpoints (Không cần authentication)
- `GET /api/public/health` - Kiểm tra trạng thái ứng dụng
- `GET /api/public/info` - Thông tin ứng dụng

### Authentication Endpoints
- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/me` - Lấy thông tin user hiện tại
- `GET /api/auth/admin` - Endpoint chỉ dành cho ADMIN
- `GET /api/auth/user` - Endpoint chỉ dành cho USER

### Admin Endpoints (Cần role ADMIN)
- `GET /api/admin/users` - Lấy danh sách tất cả users
- `GET /api/admin/users/{id}` - Lấy thông tin user theo ID
- `PUT /api/admin/users/{id}/enable` - Kích hoạt user
- `PUT /api/admin/users/{id}/disable` - Vô hiệu hóa user

## Cách sử dụng

### 1. Đăng ký tài khoản mới
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "User"
  }'
```

### 2. Đăng nhập
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Response sẽ trả về JWT token:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer"
}
```

### 3. Sử dụng JWT token
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Truy cập admin endpoint
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## Tài khoản mẫu

Khi khởi động ứng dụng, hệ thống sẽ tự động tạo 2 tài khoản mẫu:

### Admin User
- Username: `admin`
- Password: `admin123`
- Email: `admin@fit.com`
- Role: `ADMIN`

### Test User
- Username: `user`
- Password: `user123`
- Email: `user@fit.com`
- Role: `USER`

## Cấu hình

### JWT Configuration
```properties
app.jwt.secret=your-super-secret-jwt-key-that-should-be-very-long-and-secure-in-production
app.jwt.expiration=86400000
```

### Database Configuration
```properties
# Development
spring.datasource.url=jdbc:postgresql://localhost:5432/fit_backend_dev
spring.datasource.username=postgres
spring.datasource.password=password

# Production
spring.datasource.url=jdbc:postgresql://localhost:5432/fit_backend_prod
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
```

## Tính năng bảo mật

1. **JWT Token**: Sử dụng JWT để xác thực stateless
2. **Password Encryption**: Mật khẩu được mã hóa bằng BCrypt
3. **Role-based Authorization**: Phân quyền dựa trên role
4. **CORS Configuration**: Cấu hình CORS cho frontend
5. **Exception Handling**: Xử lý lỗi security một cách an toàn
6. **Input Validation**: Validate input với Bean Validation
7. **SQL Injection Protection**: Sử dụng JPA để tránh SQL injection

## Mở rộng

### Thêm Role mới
1. Thêm role vào enum `Role.java`
2. Cập nhật logic authorization trong controllers

### Thêm Permission
1. Tạo enum `Permission.java`
2. Thêm field permissions vào User entity
3. Cập nhật SecurityConfig với permission-based authorization

### Tích hợp với Database thực tế
1. Thay đổi database configuration trong `application.properties`
2. Thêm dependency cho database driver (MySQL, PostgreSQL, etc.)
3. Cập nhật JPA dialect

## Troubleshooting

### Lỗi thường gặp

1. **401 Unauthorized**: Token không hợp lệ hoặc đã hết hạn
2. **403 Forbidden**: Không có quyền truy cập
3. **400 Bad Request**: Dữ liệu đầu vào không hợp lệ

### Debug
- Bật debug logging trong `application.properties`
- Kiểm tra logs để xem chi tiết lỗi
- Sử dụng PgAdmin để kiểm tra database: `http://localhost:8081`
- Hoặc sử dụng psql: `psql -h localhost -U postgres -d fit_backend_dev` 