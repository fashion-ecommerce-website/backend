package com.spring.fit.backend.wishlist.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistId implements Serializable {
    private Long user;
    private Long productDetail;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistId)) return false;
        WishlistId that = (WishlistId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(productDetail, that.productDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, productDetail);
    }
}
