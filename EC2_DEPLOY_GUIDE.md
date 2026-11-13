# CẨM NANG DEPLOY AWS EC2 (TỪ TERMINAL ĐẾN PRODUCTION HTTPS)

> **Thông tin máy chủ:**
> - Elastic IP: `13.251.220.182`
> - Domain tự động: `13-251-220-182.sslip.io`
> - File chìa khóa SSH: `khoa-nha-ec2.pem`

---

## 1. KẾT NỐI VÀO MÁY CHỦ EC2 (CHẠY TRÊN WINDOWS POWERSHELL)

Mở PowerShell tại thư mục chứa file `khoa-nha-ec2.pem`:

```powershell
# Phân quyền lại cho file .pem trên Windows (chỉ chạy 1 lần đầu nếu bị lỗi "Permissions are too open")
icacls.exe "khoa-nha-ec2.pem" /reset
icacls.exe "khoa-nha-ec2.pem" /grant:r "$($env:USERNAME):(R)"
icacls.exe "khoa-nha-ec2.pem" /inheritance:r

# Kết nối SSH vào máy chủ EC2
ssh -i "khoa-nha-ec2.pem" ubuntu@13.251.220.182
```

---

## 2. TẠO 2GB SWAP (RAM ẢO CHO MÁY T3.MICRO)

Chạy trên Terminal của EC2:

```bash
# Cắt 2GB từ ổ SSD làm file swap
sudo fallocate -l 2G /swapfile

# Phân quyền bảo mật cho file swap
sudo chmod 600 /swapfile

# Định dạng thành swap
sudo mkswap /swapfile

# Bật swap
sudo swapon /swapfile

# Lưu cấu hình để tự động kích hoạt mỗi khi khởi động lại máy
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Kiểm tra (thấy Swap: 2.0Gi là thành công)
free -h
```

---

## 3. CÀI ĐẶT DOCKER & DOCKER COMPOSE

```bash
# Cập nhật danh sách gói phần mềm
sudo apt update -y && sudo apt upgrade -y

# Cài đặt Docker và plugin Docker Compose
sudo apt install -y docker.io docker-compose-v2

# Cấp quyền cho user 'ubuntu' dùng docker không cần gõ sudo
sudo usermod -aG docker $USER
newgrp docker

# Kiểm tra phiên bản docker
docker --version
docker compose version
```

---

## 4. CLONE SOURCE CODE & CẤU HÌNH BIẾN MÔI TRƯỜNG (.env)

```bash
# Clone source code từ GitHub
git clone <URL_REPO_GITHUB_CUA_BAN>

# Di chuyển vào thư mục dự án chứa file docker-compose.yml
cd Backend

# Tạo và chỉnh sửa file .env
nano .env
```

Nội dung file `.env` cần điền:

```env
# Database
POSTGRES_DB=fit_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=MatKhauDatabaseCuaBan123!

# AWS S3 & CloudFront
AWS_REGION=ap-southeast-1
AWS_S3_BUCKET_NAME=fit-shop-images-2026
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_CLOUDFRONT_DOMAIN=https://d23u4o4xh16173.cloudfront.net

# Redis (mặc định theo docker-compose)
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# Email OTP & Mail Report
SPRING_MAIL_USERNAME=thinhndde170386@fpt.edu.vn
SPRING_MAIL_PASSWORD=drps xxtq wdmh vqnr
DAILY_REPORT_EMAIL=lhphuc12102003@gmail.com

# Stripe
STRIPE_API_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Frontend URL
FRONTEND_URL=https://frontend-customer-two.vercel.app
```

*(Bấm `Ctrl + O` -> `Enter` để lưu, `Ctrl + X` để thoát).*

---

## 5. BUILD VÀ KHỞI ĐỘNG CÁC CONTAINER

```bash
# Build và chạy ngầm toàn bộ dịch vụ (PostgreSQL, Redis, Spring Boot)
docker compose up -d --build

# Xem danh sách container đang chạy (phải có trạng thái Up)
docker compose ps

# Xem camera/logs của Spring Boot (Ctrl + C để thoát xem log)
docker compose logs -f app
```

---

## 6. CÀI ĐẶT & CẤU HÌNH REVERSE PROXY NGINX

```bash
# Cài đặt Nginx
sudo apt install -y nginx

# Tạo cấu hình chuyển tiếp request từ cổng 80 vào cổng Spring Boot 8080
sudo tee /etc/nginx/sites-available/fit-backend << 'EOF'
server {
    listen 80;
    server_name 13-251-220-182.sslip.io;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Kích hoạt site fit-backend và gỡ bỏ site default mặc định
sudo ln -sf /etc/nginx/sites-available/fit-backend /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Kiểm tra cú pháp cấu hình Nginx
sudo nginx -t

# Khởi động lại Nginx
sudo systemctl restart nginx
```

---

## 7. CẤP CHỨNG CHỈ BẢO MẬT SSL HTTPS MIỄN PHÍ (CERTBOT)

```bash
# Cài đặt công cụ Certbot cho Nginx
sudo apt install -y certbot python3-certbot-nginx

# Tự động xin và cấu hình chứng chỉ HTTPS cho domain sslip.io
sudo certbot --nginx -d 13-251-220-182.sslip.io
# Nhập email của bạn, gõ 'Y' đồng ý điều khoản, gõ 'N' từ chối share mail
```

---

## 8. CÁC LỆNH BẢO TRÌ NHANH SAU NÀY

```bash
# Khởi động lại toàn bộ docker nếu máy chủ từng bị khởi động lại
cd ~/Backend && docker compose up -d

# Tắt toàn bộ dịch vụ docker
docker compose down

# Khởi động lại Nginx
sudo systemctl restart nginx
```

---

## 9. BASE URL PRODUCTION

- **Public Endpoint Test:**
  `https://13-251-220-182.sslip.io/api/categories`
- **Biến môi trường cấu hình trên Vercel (Type: Plaintext/Config):**
  ` `
