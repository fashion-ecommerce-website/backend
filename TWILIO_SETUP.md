# Hướng dẫn cấu hình Twilio SMS

## 1. Tạo tài khoản Twilio

1. Truy cập [https://www.twilio.com](https://www.twilio.com)
2. Đăng ký tài khoản miễn phí
3. Xác thực số điện thoại của bạn

## 2. Lấy thông tin cấu hình

Sau khi đăng ký, bạn sẽ có:

- **Account SID**: Tìm trong Dashboard
- **Auth Token**: Tìm trong Dashboard (click để hiển thị)
- **Phone Number**: Mua một số điện thoại Twilio (có thể dùng trial number)

## 3. Cấu hình environment variables

### Cách 1: Tạo file `.env` (khuyến nghị)
```bash
TWILIO_ACCOUNT_SID=your_account_sid_here
TWILIO_AUTH_TOKEN=your_auth_token_here
TWILIO_PHONE_NUMBER=+1234567890
```

### Cách 2: Set environment variables
```bash
# Windows
set TWILIO_ACCOUNT_SID=your_account_sid_here
set TWILIO_AUTH_TOKEN=your_auth_token_here
set TWILIO_PHONE_NUMBER=+1234567890

# Linux/Mac
export TWILIO_ACCOUNT_SID=your_account_sid_here
export TWILIO_AUTH_TOKEN=your_auth_token_here
export TWILIO_PHONE_NUMBER=+1234567890
```

### Cách 3: Cập nhật trực tiếp trong application.properties
```properties
twilio.account.sid=your_account_sid_here
twilio.auth.token=your_auth_token_here
twilio.phone.number=+1234567890
```

## 4. Lưu ý quan trọng

- **Trial Account**: Chỉ có thể gửi SMS đến số điện thoại đã verify
- **Production**: Cần upgrade account để gửi SMS đến bất kỳ số nào
- **Chi phí**: SMS có phí, kiểm tra pricing tại [Twilio Pricing](https://www.twilio.com/pricing)

## 5. Test SMS

Sau khi cấu hình xong, bạn có thể test bằng cách:

1. Start ứng dụng
2. Gọi API register với số điện thoại thật
3. Kiểm tra SMS nhận được

## 6. Troubleshooting

### Lỗi "The number is unverified"
- Với trial account, chỉ có thể gửi SMS đến số đã verify
- Verify số điện thoại trong Twilio Console

### Lỗi "Authentication failed"
- Kiểm tra Account SID và Auth Token
- Đảm bảo không có khoảng trắng thừa

### Lỗi "Invalid phone number"
- Đảm bảo số điện thoại có format đúng
- Với Việt Nam: +84xxxxxxxxx

