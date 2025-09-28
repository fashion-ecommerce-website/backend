package com.spring.fit.backend.order.domain.entity;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "color_label", columnDefinition = "text")
    private String colorLabel;

    @Column(name = "size_label", columnDefinition = "text")
    private String sizeLabel;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderDetail that = (OrderDetail) o;
        return Objects.equals(order, that.order) && Objects.equals(productDetail, that.productDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, productDetail);
    }
}
