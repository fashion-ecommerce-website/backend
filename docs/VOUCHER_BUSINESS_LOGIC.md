# Voucher Business Logic Documentation

> Tài liệu mô tả chi tiết business logic của hệ thống Voucher, mapping với source code hiện tại.

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Cấu trúc dữ liệu](#2-cấu-trúc-dữ-liệu)
3. [Luồng xử lý chính](#3-luồng-xử-lý-chính)
4. [Business Rules](#4-business-rules)
5. [Security & Validation](#5-security--validation)

---

## 1. Tổng quan

### 1.1. Source Files

| File | Mô tả |
|------|-------|
| `VoucherServiceImpl.java` | Service chính xử lý business logic |
| `VoucherRepository.java` | Repository truy vấn database |
| `VoucherUsageRepository.java` | Repository quản lý lịch sử sử dụng voucher |
| `VoucherRankRuleRepository.java` | Repository quản lý quy tắc rank |
| `Voucher.java` | Entity voucher |
| `VoucherUsage.java` | Entity lịch sử sử dụng |
| `VoucherRankRule.java` | Entity quy tắc rank |

### 1.2. Enums

| Enum | Giá trị | Mô tả |
|------|---------|-------|
| `VoucherType` | `PERCENT`, `FIXED` | Loại voucher |
| `AudienceType` | `ALL`, `RANK` | Đối tượng áp dụng |
| `VoucherUsageStatus` | `APPLIED`, `CANCELLED` | Trạng thái sử dụng |

---

## 2. Cấu trúc dữ liệu

### 2.1. Voucher Entity

```java
// Voucher.java
@Entity
@Table(name = "vouchers")
public class Voucher {
    private Long id;
    private String name;                    // Tên voucher
    private String code;                    // Mã voucher (unique, 7 ký tự)
    private VoucherType type;               // PERCENT hoặc FIXED
    private BigDecimal value;               // Giá trị giảm
    private BigDecimal maxDiscount;         // Giảm tối đa (cho PERCENT)
    private BigDecimal minOrderAmount;      // Đơn hàng tối thiểu
    private Integer usageLimitTotal;        // Giới hạn tổng lượt dùng
    private Integer usageLimitPerUser;      // Giới hạn lượt dùng/user
    private LocalDateTime startAt;          // Thời gian bắt đầu
    private LocalDateTime endAt;            // Thời gian kết thúc
    private boolean isActive;               // Trạng thái kích hoạt
    private AudienceType audienceType;      // ALL hoặc RANK
}
```

---

## 3. Luồng xử lý chính

### 3.1. Validate Voucher (Customer)

**Method:** `validateVoucher(VoucherValidateRequest request, Long userId)`

```
┌─────────────────────────────────────────────────────────────────┐
│                    VALIDATE VOUCHER FLOW                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. findValidVoucherByCode(code, now, subtotal)                │
│     ├── Check: isActive = true                                  │
│     ├── Check: startAt <= now <= endAt                         │
│     └── Check: minOrderAmount <= subtotal                       │
│                         │                                       │
│                         ▼                                       │
│  2. checkUsageLimits(voucher, userId)                          │
│     ├── Check: userUsageCount < usageLimitPerUser              │
│     └── Check: totalUsageCount < usageLimitTotal               │
│                         │                                       │
│                         ▼                                       │
│  3. checkAudienceType(voucher, user)                           │
│     ├── ALL: always true                                        │
│     └── RANK: check user.rankId in voucherRankRules            │
│                         │                                       │
│                         ▼                                       │
│  4. calculateDiscount(voucher, subtotal)                       │
│     └── Return: discountPreview                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Source:** `VoucherServiceImpl.java` - Lines 54-109

---

### 3.2. Apply Voucher (Khi đặt hàng)

**Method:** `applyVoucher(VoucherValidateRequest request, Long userId, Long orderId)`

```
┌─────────────────────────────────────────────────────────────────┐
│                     APPLY VOUCHER FLOW                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. findValidVoucherByCodeForUpdate(code, now, subtotal)       │
│     └── PESSIMISTIC_WRITE lock (tránh race condition)          │
│                         │                                       │
│                         ▼                                       │
│  2. checkUsageLimits(voucher, userId)                          │
│     └── Re-validate sau khi lock                                │
│                         │                                       │
│                         ▼                                       │
│  3. checkAudienceType(voucher, user)  ⬅️ NEW                    │
│     └── Ngăn bypass sau khi bị demote rank                      │
│                         │                                       │
│                         ▼                                       │
│  4. calculateDiscount(voucher, subtotal)                       │
│                         │                                       │
│                         ▼                                       │
│  5. Create VoucherUsage record                                  │
│     └── status = APPLIED                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Source:** `VoucherServiceImpl.java` - Lines 212-250

---

### 3.3. Calculate Discount

**Method:** `calculateDiscount(Voucher voucher, Double subtotal)`

```
┌─────────────────────────────────────────────────────────────────┐
│                   CALCULATE DISCOUNT FLOW                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  switch (voucher.type):                                         │
│                                                                 │
│  ┌─ PERCENT ─────────────────────────────────────────────┐     │
│  │  discount = subtotal × value / 100                     │     │
│  │  if (discount > maxDiscount) → discount = maxDiscount  │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                 │
│  ┌─ FIXED ───────────────────────────────────────────────┐     │
│  │  discount = value                                      │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                 │
│  ┌─ SAFETY CAP ──────────────────────────────────────────┐     │
│  │  if (discount > subtotal) → discount = subtotal        │     │
│  │  (Ngăn tổng tiền âm)                                   │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Source:** `VoucherServiceImpl.java` - Lines 111-144

**Ví dụ:**

| Subtotal | Type | Value | MaxDiscount | Discount | Giải thích |
|----------|------|-------|-------------|----------|------------|
| 500.000đ | PERCENT | 20 | 80.000đ | 80.000đ | 500k × 20% = 100k > maxDiscount |
| 500.000đ | PERCENT | 10 | 100.000đ | 50.000đ | 500k × 10% = 50k < maxDiscount |
| 50.000đ | FIXED | 100.000đ | - | 50.000đ | Cap: discount ≤ subtotal |
| 200.000đ | FIXED | 50.000đ | - | 50.000đ | Bình thường |

---

## 4. Business Rules

### 4.1. Admin: Create Voucher

**Method:** `createAdminVoucher(AdminVoucherRequest request)`

| Rule | Validation | Source |
|------|------------|--------|
| Code tự động sinh | 7 ký tự [A-Z0-9], unique | `generateUniqueVoucherCode()` |
| **startAt >= now** | Không được tạo voucher với start_at trong quá khứ | `validateDatesForCreate()` |
| startAt < endAt | Required | `validateDatesForCreate()` |
| usageLimitTotal ≥ usageLimitPerUser | Required | `validateUsages()` |
| PERCENT value ≤ 100 | Required | Line 296-298 |
| usageLimitTotal, usageLimitPerUser | Not null | `validateUsages()` |

---

### 4.2. Admin: Update Voucher

**Method:** `updateAdminVoucher(Long id, AdminVoucherRequest request)`

#### Quy tắc theo thời gian:

```
Timeline:  ──────────────────────────────────────────────────────▶
                    │                      │
           now      │      start_at        │       end_at
                    │                      │
  ┌─────────────────┴──────────────────────┴─────────────────────┐
  │                                                               │
  │  CASE 1: now < start_at                                       │
  │  ┌───────────────────────────────────────────────────────┐   │
  │  │ ✅ Có thể sửa TẤT CẢ các trường                        │   │
  │  │ ⚠️  Nếu sửa start_at → start_at mới > now + 1 day      │   │
  │  └───────────────────────────────────────────────────────┘   │
  │                                                               │
  │  CASE 2: start_at ≤ now ≤ end_at (Đang hoạt động)            │
  │  ┌───────────────────────────────────────────────────────┐   │
  │  │ ✅ Chỉ cho phép TĂNG usage_limit_total                 │   │
  │  │ ❌ Không được thay đổi các trường khác                  │   │
  │  │ ❌ Không được GIẢM usage_limit_total                    │   │
  │  └───────────────────────────────────────────────────────┘   │
  │                                                               │
  │  CASE 3: now > end_at (Đã hết hạn)                           │
  │  ┌───────────────────────────────────────────────────────┐   │
  │  │ ❌ KHÔNG cho phép update                                │   │
  │  └───────────────────────────────────────────────────────┘   │
  │                                                               │
  └───────────────────────────────────────────────────────────────┘
```

**Source:** `VoucherServiceImpl.java` - Lines 312-368

---

### 4.3. Cancel Voucher Usage

**Method:** `cancelVoucherUsageByOrderId(Long orderId)`

```
┌─────────────────────────────────────────────────────────────────┐
│  Khi order bị cancel:                                           │
│  1. Tìm VoucherUsage theo orderId                               │
│  2. Cập nhật status = CANCELLED                                 │
│  → Voucher có thể được sử dụng lại                              │
└─────────────────────────────────────────────────────────────────┘
```

**Source:** `VoucherServiceImpl.java` - Lines 252-264

---

## 5. Security & Validation

### 5.1. Race Condition Prevention

```java
// VoucherRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT v FROM Voucher v WHERE v.code = :code ...")
Optional<Voucher> findValidVoucherByCodeForUpdate(...);
```

**Giải thích:**
- Sử dụng `PESSIMISTIC_WRITE` lock khi apply voucher
- Ngăn chặn nhiều request đồng thời vượt quá usage limit

---

### 5.2. Discount Cap (Ngăn tổng tiền âm)

```java
// VoucherServiceImpl.java - Lines 139-143
if (discount.compareTo(subtotalDecimal) > 0) {
    return subtotalDecimal;  // Cap tại subtotal
}
```

**Ví dụ lỗ hổng đã fix:**
| Trước | Sau |
|-------|-----|
| Subtotal: 50k, Voucher FIXED: 100k → Total: -50k ❌ | Discount cap: 50k → Total: 0đ ✅ |

---

### 5.3. Audience Type Check khi Apply

```java
// VoucherServiceImpl.java - Lines 229-234
UserEntity user = userRepository.findById(userId)...
if (!checkAudienceType(voucher, user)) {
    throw new ErrorException(..., "Voucher not applicable for your rank");
}
```

**Mục đích:** Ngăn user validate voucher (khi có rank hợp lệ) → bị demote rank → vẫn apply được voucher.

---

### 5.4. Null Safety

```java
// VoucherServiceImpl.java - Lines 451-463
private void validateUsages(AdminVoucherRequest request) {
    Integer total = request.getUsageLimitTotal();
    Integer perUser = request.getUsageLimitPerUser();
    
    if (total == null || perUser == null) {
        throw new IllegalArgumentException("Usage limit total and usage limit per user are required");
    }
    // ...
}
```

---

### 5.5. Performance: Filter Expired Vouchers

```java
// VoucherRepository.java
@Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.endAt >= :now ORDER BY v.startAt ASC")
List<Voucher> findActiveVouchersNotExpired(@Param("now") LocalDateTime now);
```

**Mục đích:** Chỉ load vouchers chưa hết hạn, tránh load toàn bộ database.

---

## 6. API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/vouchers/validate` | Validate voucher cho user |
| GET | `/api/vouchers/user` | Lấy danh sách voucher khả dụng cho user |
| GET | `/api/admin/vouchers` | Admin: Tìm kiếm vouchers |
| POST | `/api/admin/vouchers` | Admin: Tạo voucher mới |
| PUT | `/api/admin/vouchers/{id}` | Admin: Cập nhật voucher |
| GET | `/api/admin/vouchers/{id}` | Admin: Lấy chi tiết voucher |
| PATCH | `/api/admin/vouchers/{id}/toggle` | Admin: Toggle active/inactive |

---

## 7. Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-12-14 | Initial documentation |
| 1.1 | 2024-12-14 | Fix: Discount > Subtotal cap |
| 1.2 | 2024-12-14 | Fix: Apply voucher check audience type |
| 1.3 | 2024-12-14 | Fix: NPE in validateUsages |
| 1.4 | 2024-12-14 | Fix: Filter expired vouchers in getVouchersByUser |
| 1.5 | 2024-12-14 | Add: Update voucher time-based restrictions |
| 1.6 | 2024-12-14 | Add: Create voucher validation `start_at >= now` |
| 1.7 | 2024-12-14 | Add: Cronjob deactivate expired vouchers |

---

> **Note:** Tài liệu này được tạo tự động dựa trên source code. Vui lòng cập nhật khi có thay đổi logic.
