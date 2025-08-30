# Test LazyInitializationException Fix

## Các endpoint cần test:

### 1. User Profile Endpoints
```bash
# Test original getUserProfile
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer {your_token}"

# Test enhanced getUserProfileDetail  
curl -X GET "http://localhost:8080/api/user/profile/detail" \
  -H "Authorization: Bearer {your_token}"

# Test updateUserProfile
curl -X PUT "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer {your_token}" \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "phone": "0123456789"}'

# Test updateUserProfileEnhanced
curl -X PUT "http://localhost:8080/api/user/profile/enhanced" \
  -H "Authorization: Bearer {your_token}" \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "phone": "0123456789", "avatarUrl": "https://example.com/avatar.jpg"}'
```

### 2. Authentication Endpoints
```bash
# Test login (might trigger CustomUserDetailsService)
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'

# Test refresh token
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your_refresh_token"}'
```

### 3. Address Endpoints
```bash
# Test address operations
curl -X GET "http://localhost:8080/api/user/addresses" \
  -H "Authorization: Bearer {your_token}"

curl -X POST "http://localhost:8080/api/user/addresses" \
  -H "Authorization: Bearer {your_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "phone": "0123456789", 
    "line": "123 Test Street",
    "city": "Test City",
    "isDefault": true
  }'
```

## Các method đã được fix:

### UserRepository
- ✅ `findActiveUserByEmailWithRoles()` - added
- ✅ `findActiveUserByUsernameWithRoles()` - added
- ✅ `findByEmailWithRoles()` - added
- ✅ `findByIdWithRoles()` - added

### CustomUserDetailsService  
- ✅ `loadUserByUsername()` - now uses `findActiveUserByEmailWithRoles()`

### UserProfileServiceImpl
- ✅ `getUserProfile()` - now uses `findActiveUserByEmailWithRoles()`
- ✅ `updateUserProfile()` - now uses `findActiveUserByEmailWithRoles()`
- ✅ `getUserProfileDetail()` - already uses `findActiveUserByEmailWithRoles()`
- ✅ `updateUserProfileEnhanced()` - already uses `findActiveUserByEmailWithRoles()`

### AuthenticationServiceImpl
- ✅ `login()` - now uses `findActiveUserByEmailWithRoles()`
- ⚠️ `generateRefreshToken()` - still uses `findActiveUserByEmail()` but doesn't access userRoles

## Potential remaining issues:

1. Check if there are any other services accessing `user.getUserRoles()`
2. Check if any controllers directly access user entities
3. Check if any scheduled tasks or background jobs access user roles

## How to identify the exact source:

1. Enable debug logging for Hibernate:
```properties
logging.level.org.hibernate=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

2. Add stack trace logging to see which endpoint is being called when the error occurs

3. Check the timing - the error might be happening during:
   - Login process
   - JWT token validation
   - User profile access
   - Address operations 