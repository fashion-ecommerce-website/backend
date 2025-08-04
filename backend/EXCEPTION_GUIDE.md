# Hướng dẫn sử dụng Exception Handling System

## 📋 Tổng quan

Hệ thống exception handling này cung cấp cách xử lý lỗi một cách nhất quán trong Spring Boot application.

## 🚀 Cách vận hành

### 1. ResourceNotFoundException (404)
**Khi nào sử dụng:** Khi không tìm thấy resource (user, product, etc.)

```java
// Cách 1: Sử dụng ExceptionUtils
throw ExceptionUtils.resourceNotFound("User", "id", userId);

// Cách 2: Tạo trực tiếp
throw new ResourceNotFoundException("User", "id", userId);
```

**Response:**
```json
{
    "status": 404,
    "message": "User not found with id : '123'",
    "path": "/api/users/123",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 2. ConflictException (409)
**Khi nào sử dụng:** Khi có conflict (email đã tồn tại, duplicate data)

```java
// Cách 1: Sử dụng ExceptionUtils
throw ExceptionUtils.conflict("User", "email", email);

// Cách 2: Tạo trực tiếp
throw new ConflictException("User", "email", email);
```

**Response:**
```json
{
    "status": 409,
    "message": "User already exists with email : 'test@email.com'",
    "path": "/api/users/register",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 3. ValidationException (400)
**Khi nào sử dụng:** Khi dữ liệu không hợp lệ (validation failed)

```java
// Cách 1: Một lỗi validation
throw ExceptionUtils.validationError("email", "Email không hợp lệ");

// Cách 2: Nhiều lỗi validation
Map<String, String> errors = new HashMap<>();
errors.put("email", "Email không hợp lệ");
errors.put("password", "Mật khẩu phải có ít nhất 6 ký tự");
throw ExceptionUtils.validationErrors(errors);
```

**Response:**
```json
{
    "status": 400,
    "message": "Validation failed",
    "path": "/api/users/validate",
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "email": "Email không hợp lệ",
        "password": "Mật khẩu phải có ít nhất 6 ký tự"
    }
}
```

### 4. BadRequestException (400)
**Khi nào sử dụng:** Khi request không hợp lệ

```java
throw ExceptionUtils.badRequest("Dữ liệu không được để trống");
```

**Response:**
```json
{
    "status": 400,
    "message": "Dữ liệu không được để trống",
    "path": "/api/users/bad-request",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 5. UnauthorizedException (401)
**Khi nào sử dụng:** Khi user chưa đăng nhập hoặc token không hợp lệ

```java
throw ExceptionUtils.unauthorized("Token không hợp lệ hoặc đã hết hạn");
```

**Response:**
```json
{
    "status": 401,
    "message": "Token không hợp lệ hoặc đã hết hạn",
    "path": "/api/users/profile",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 6. ForbiddenException (403)
**Khi nào sử dụng:** Khi user không có quyền truy cập

```java
throw ExceptionUtils.forbidden("Bạn không có quyền xóa user này");
```

**Response:**
```json
{
    "status": 403,
    "message": "Bạn không có quyền xóa user này",
    "path": "/api/users/123?role=USER",
    "timestamp": "2024-01-15T10:30:00"
}
```

## 🧪 Test các API

### 1. Test ResourceNotFoundException
```bash
GET http://localhost:8080/api/users/999
```

### 2. Test ConflictException
```bash
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
    "email": "existing@email.com",
    "password": "123456",
    "name": "Test User"
}
```

### 3. Test ValidationException
```bash
POST http://localhost:8080/api/users/validate
Content-Type: application/json

{
    "email": "invalid-email",
    "password": "123"
}
```

### 4. Test BadRequestException
```bash
POST http://localhost:8080/api/users/bad-request
Content-Type: application/json

""
```

### 5. Test UnauthorizedException
```bash
GET http://localhost:8080/api/users/profile
```

### 6. Test ForbiddenException
```bash
DELETE http://localhost:8080/api/users/123?role=USER
```

## 💡 Best Practices

### 1. Sử dụng ExceptionUtils
```java
// ✅ Tốt - Sử dụng utility
throw ExceptionUtils.resourceNotFound("User", "id", userId);

// ❌ Không tốt - Tạo trực tiếp
throw new ResourceNotFoundException("User", "id", userId);
```

### 2. Validation trong Service Layer
```java
@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> ExceptionUtils.resourceNotFound("User", "id", id));
    }
    
    public User createUser(UserRegistrationRequest request) {
        // Validate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ExceptionUtils.conflict("User", "email", request.getEmail());
        }
        
        // Validate data
        Map<String, String> errors = validateUserData(request);
        if (!errors.isEmpty()) {
            throw ExceptionUtils.validationErrors(errors);
        }
        
        return userRepository.save(new User(request));
    }
}
```

### 3. Exception trong Controller
```java
@RestController
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id); // Exception sẽ được throw từ service
        return ResponseEntity.ok(user);
    }
}
```

## 🔧 Cấu hình

Hệ thống này hoạt động tự động nhờ `@ControllerAdvice` trong `GlobalExceptionHandler`. Không cần cấu hình thêm gì.

## 📝 Lưu ý

1. **Tất cả exceptions sẽ được catch bởi GlobalExceptionHandler**
2. **Response format luôn nhất quán**
3. **HTTP status code được map đúng với loại exception**
4. **Timestamp được tự động thêm vào response**
5. **Path được lấy từ request**

## 🎯 Kết luận

Hệ thống này giúp:
- ✅ Xử lý lỗi một cách nhất quán
- ✅ Giảm code duplicate
- ✅ Dễ dàng maintain và debug
- ✅ Response format chuẩn cho frontend
- ✅ HTTP status code chính xác 