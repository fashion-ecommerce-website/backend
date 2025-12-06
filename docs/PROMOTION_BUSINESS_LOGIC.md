# Promotion Business Logic

## Tổng quan

Hệ thống Promotion cho phép admin tạo các chương trình khuyến mãi áp dụng cho SKU (product variant), Product, hoặc Category trong một khoảng thời gian xác định.

---

## Nguyên tắc cốt lõi

> **1 SKU chỉ được có TỐI ĐA 1 promotion ACTIVE trong cùng 1 khoảng thời gian**

- Không cho phép tạo promotion mới nếu bất kỳ SKU nào đã có promotion active trong period đó
- Áp dụng cho cả 3 loại target: `SKU`, `PRODUCT`, `CATEGORY`
- Khi target là PRODUCT hoặc CATEGORY, hệ thống sẽ expand ra tất cả SKU IDs thuộc target đó để kiểm tra conflict

---

## Data Model

### Promotion Entity
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| name | String | Tên promotion (1-120 ký tự) |
| type | PromotionType | `PERCENT` hoặc `FIXED` |
| value | BigDecimal | Giá trị giảm (% hoặc số tiền cố định) |
| startAt | LocalDateTime | Thời gian bắt đầu |
| endAt | LocalDateTime | Thời gian kết thúc |
| isActive | Boolean | Trạng thái hoạt động |

### PromotionTarget Entity
| Field | Type | Description |
|-------|------|-------------|
| promotionId | Long | FK to Promotion |
| targetType | PromotionTargetType | `SKU`, `PRODUCT`, hoặc `CATEGORY` |
| targetId | Long | ID của target (SKU/Product/Category) |

---

## API Endpoints

### 1. Create Promotion
```
POST /api/admin/promotions
```

**Request:**
```json
{
  "name": "Summer Sale 2024",
  "type": "PERCENT",
  "value": 20,
  "startAt": "2025-01-01T00:00:00",
  "endAt": "2025-01-31T23:59:59",
  "isActive": true,
  "targets": [
    { "targetType": "CATEGORY", "targetId": 1 },
    { "targetType": "PRODUCT", "targetId": 5 },
    { "targetType": "SKU", "targetId": 10 }
  ]
}
```

**Response:** `201 Created` với PromotionResponse (bao gồm targets)

### 2. Update Promotion
```
PUT /api/admin/promotions/{id}
```

**Behavior:**
| Request | Hành vi |
|---------|---------|
| Có `targets: [...]` | Xóa targets cũ → Thêm targets mới |
| Có `targets: []` | Xóa tất cả targets |
| Không có field `targets` | Giữ nguyên targets hiện tại |

### 3. Toggle Active
```
POST /api/admin/promotions/{id}:toggle
```

**Behavior:**
| Chuyển đổi | Logic |
|------------|-------|
| Active → Inactive | ✅ Cho phép ngay |
| Inactive → Active | Kiểm tra tất cả SKUs có conflict không |

### 4. Search Promotions
```
GET /api/admin/promotions?name=&isActive=&from=&to=&type=&page=0&pageSize=20&sort=
```

### 5. Get Promotion by ID
```
GET /api/admin/promotions/{id}
```

### 6. Upsert Targets (thêm targets cho promotion đã tồn tại)
```
POST /api/admin/promotions/{id}/targets:upsert
```

### 7. List Targets
```
GET /api/admin/promotions/{id}/targets?type=&page=0&pageSize=50
```

### 8. Remove Targets
```
DELETE /api/admin/promotions/{id}/targets
```

---

## Validation Rules

### 1. Promotion Request Validation
| Rule | Error Message |
|------|---------------|
| name required | `name is required` |
| name length 1-120 | `name must be 1..120 chars` |
| type required | `type is required` |
| value required | `value is required` |
| value > 0 | `value must be > 0` |
| PERCENT value ≤ 100 | `PERCENT value must be <= 100` |
| startAt & endAt required | `startAt and endAt are required` |
| endAt > startAt | `endAt must be after startAt` |

### 2. Target Validation
| Rule | Error Message |
|------|---------------|
| Target tồn tại | `SKU/Product/Category not found: {id}` |
| Target phải ACTIVE | `SKU/Product/Category is not active: {id}` |

### 3. SKU Conflict Validation
```
SKU ID {skuId} already has a promotion (Promotion ID: {conflictingPromotionId}) 
in the specified time period. Please deactivate the existing promotion first.
```

### 4. Toggle Active Validation (Inactive → Active)
```
Cannot activate promotion: SKU ID {skuId} already has an active promotion 
(Promotion ID: {conflictingPromotionId}) in the time period {startAt} to {endAt}. 
Please deactivate the conflicting promotion first.
```

---

## SKU Conflict Detection Logic

### Khi tạo/cập nhật promotion với targets:

```
Target (SKU/PRODUCT/CATEGORY)
         │
         ▼
   Expand thành danh sách SKU IDs
   ┌────────────────────────────────────────┐
   │ SKU      → [targetId]                  │
   │ PRODUCT  → findSkuIdsByProductId()     │
   │ CATEGORY → findSkuIdsByCategoryId()    │
   └────────────────────────────────────────┘
         │
         ▼
   Với mỗi SKU, kiểm tra conflict:
   ┌─────────────────────────────────────────────────────┐
   │ Có promotion ACTIVE nào trong period mà:            │
   │ - Target trực tiếp SKU này? HOẶC                   │
   │ - Target Product chứa SKU này? HOẶC                │
   │ - Target Category chứa Product chứa SKU này?       │
   └─────────────────────────────────────────────────────┘
         │
         ▼
   Nếu có → Reject + Hiển thị Promotion ID conflict
   Nếu không → Allow
```

### SQL Query kiểm tra conflict:
```sql
SELECT p.id FROM promotion_targets pt
JOIN promotions p ON pt.promotion_id = p.id
WHERE p.is_active = true
AND p.start_at <= :endAt AND p.end_at >= :startAt
AND (
    (pt.target_type = 'SKU' AND pt.target_id = :skuId)
    OR (pt.target_type = 'PRODUCT' AND pt.target_id = (
        SELECT product_id FROM product_details WHERE id = :skuId
    ))
    OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
        SELECT pc.category_id FROM product_categories pc 
        WHERE pc.product_id = (
            SELECT product_id FROM product_details WHERE id = :skuId
        )
    ))
)
LIMIT 1
```

---

## Apply Promotion for SKU

### Method: `applyPromotionForSku(PromotionApplyRequest request)`

**Logic đơn giản hóa:**
- Vì đã validate 1 SKU chỉ có 1 promotion trong cùng period
- Chỉ cần query trực tiếp để tìm promotion duy nhất cho SKU

```java
// Tìm promotion ID duy nhất cho SKU tại thời điểm
Long promotionId = promotionTargetRepository.findPromotionIdForSkuAt(
    skuId, productId, categoryIds, at);

// Nếu có promotion → tính discount
// Nếu không → trả về giá gốc
```

### Tính Discount:
```java
if (promotion.getType() == PromotionType.PERCENT) {
    discount = basePrice * value / 100;
} else { // FIXED
    discount = value;
}
finalPrice = max(basePrice - discount, 0);
```

---

## Response Structure

### PromotionResponse
```json
{
  "id": 1,
  "name": "Summer Sale 2024",
  "type": "PERCENT",
  "value": 20,
  "startAt": "2025-01-01T00:00:00",
  "endAt": "2025-01-31T23:59:59",
  "isActive": true,
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00",
  "targets": [
    { "targetType": "CATEGORY", "targetId": 1 },
    { "targetType": "SKU", "targetId": 10 }
  ]
}
```

### PromotionApplyResponse
```json
{
  "basePrice": 100000,
  "finalPrice": 80000,
  "percentOff": 20,
  "promotionId": 1,
  "promotionName": "Summer Sale 2024"
}
```

---

## Conflict Scenarios

| Scenario | Result |
|----------|--------|
| Tạo promotion cho **SKU 1** (01/06 - 30/06) | ✅ OK |
| Tạo promotion cho **SKU 2** (01/06 - 30/06) | ✅ OK (khác SKU) |
| Tạo promotion cho **Product chứa SKU 1** (15/06 - 15/07) | ❌ Conflict |
| Tạo promotion cho **Category chứa SKU 1** (20/06 - 25/06) | ❌ Conflict |
| Deactivate promotion SKU 1 → Tạo promotion Category | ✅ OK |
| Toggle inactive→active (có SKU conflict) | ❌ Conflict |
| Toggle active→inactive | ✅ Luôn OK |

---

## Files Structure

```
promotion/
├── controller/
│   └── AdminPromotionController.java
├── domain/
│   ├── dto/
│   │   ├── request/
│   │   │   ├── PromotionRequest.java           # Có targets field
│   │   │   ├── PromotionApplyRequest.java
│   │   │   ├── PromotionTargetsUpsertRequest.java
│   │   │   └── PromotionTargetsRemoveRequest.java
│   │   └── response/
│   │       ├── PromotionResponse.java          # Có targets field
│   │       ├── PromotionApplyResponse.java
│   │       ├── PromotionTargetResponse.java
│   │       └── TargetsUpsertResult.java
│   └── entity/
│       ├── Promotion.java
│       ├── PromotionTarget.java
│       └── PromotionTargetId.java
├── repository/
│   ├── PromotionRepository.java
│   └── PromotionTargetRepository.java          # Conflict detection queries
└── service/
    ├── PromotionService.java
    └── impl/
        └── PromotionServiceImpl.java           # Business logic
```

---

## Related Repositories

### ProductDetailRepository
```java
// Kiểm tra SKU active (và Product của nó cũng active)
boolean existsActiveById(Long id);

// Lấy tất cả SKU IDs của 1 Product
List<Long> findSkuIdsByProductId(Long productId);

// Lấy tất cả SKU IDs của tất cả Products trong 1 Category
List<Long> findSkuIdsByCategoryId(Long categoryId);
```

### ProductMainRepository
```java
// Kiểm tra Product active
boolean existsActiveById(Long id);
```

### CategoryRepository
```java
// Kiểm tra Category active
boolean existsActiveById(Long id);
```

