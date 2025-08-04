# PostgreSQL Setup Guide

## 🐘 Chuyển đổi từ H2 sang PostgreSQL

### 1. **Cài đặt PostgreSQL**

#### Option A: Sử dụng Docker (Khuyến nghị)
```bash
# Chạy PostgreSQL với Docker Compose
docker-compose up -d

# Kiểm tra containers
docker-compose ps

# Xem logs
docker-compose logs postgres
```

#### Option B: Cài đặt trực tiếp
- **Windows**: Tải từ https://www.postgresql.org/download/windows/
- **macOS**: `brew install postgresql`
- **Ubuntu**: `sudo apt-get install postgresql postgresql-contrib`

### 2. **Tạo Database**

#### Sử dụng psql:
```bash
# Kết nối PostgreSQL
psql -U postgres

# Tạo database
CREATE DATABASE fit_backend_dev;
CREATE DATABASE fit_backend_prod;

# Thoát
\q
```

#### Hoặc sử dụng script:
```bash
psql -U postgres -f database/init.sql
```

### 3. **Cấu hình Application**

#### Development Profile:
```bash
# Chạy với development profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### Production Profile:
```bash
# Chạy với production profile
mvn spring-boot:run -Dspring.profiles.active=prod
```

### 4. **Truy cập Database**

#### PgAdmin (Web Interface):
- URL: http://localhost:8081
- Email: admin@fit.com
- Password: admin123

#### Command Line:
```bash
# Kết nối database
psql -h localhost -U postgres -d fit_backend_dev

# Xem tables
\dt

# Xem dữ liệu users
SELECT * FROM users;

# Thoát
\q
```

### 5. **Environment Variables**

#### Development:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=password
export DB_URL=jdbc:postgresql://localhost:5432/fit_backend_dev
```

#### Production:
```bash
export DB_USERNAME=fit_user
export DB_PASSWORD=secure_password
export DB_URL=jdbc:postgresql://localhost:5432/fit_backend_prod
```

### 6. **Migration từ H2**

Nếu bạn có dữ liệu trong H2 cần chuyển sang PostgreSQL:

1. **Export từ H2:**
```sql
-- Trong H2 Console
SCRIPT TO 'backup.sql';
```

2. **Import vào PostgreSQL:**
```bash
psql -U postgres -d fit_backend_dev -f backup.sql
```

### 7. **Backup và Restore**

#### Backup:
```bash
pg_dump -U postgres -d fit_backend_dev > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### Restore:
```bash
psql -U postgres -d fit_backend_dev < backup_file.sql
```

### 8. **Performance Tuning**

#### Connection Pool:
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
```

#### Indexes:
```sql
-- Tạo indexes cho performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

### 9. **Troubleshooting**

#### Lỗi kết nối:
```bash
# Kiểm tra PostgreSQL service
sudo systemctl status postgresql

# Kiểm tra port
netstat -an | grep 5432

# Kiểm tra logs
tail -f /var/log/postgresql/postgresql-*.log
```

#### Lỗi authentication:
```sql
-- Cập nhật pg_hba.conf hoặc sử dụng:
ALTER USER postgres PASSWORD 'new_password';
```

### 10. **Monitoring**

#### Sử dụng pgAdmin:
- Performance Dashboard
- Query Analyzer
- Database Statistics

#### Command Line:
```sql
-- Xem active connections
SELECT * FROM pg_stat_activity;

-- Xem database size
SELECT pg_size_pretty(pg_database_size('fit_backend_dev'));

-- Xem table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

## 🎯 Kết luận

PostgreSQL đã được cấu hình thành công với:
- ✅ Multiple profiles (dev/prod)
- ✅ Docker support
- ✅ Connection pooling
- ✅ Performance optimization
- ✅ Backup/restore procedures
- ✅ Monitoring tools

Sẵn sàng cho production deployment! 🚀 