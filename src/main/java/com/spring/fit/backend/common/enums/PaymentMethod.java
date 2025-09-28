package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CASH_ON_DELIVERY("CASH_ON_DELIVERY"),
    BANK_TRANSFER("BANK_TRANSFER"),
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    E_WALLET("E_WALLET"),
    PAYPAL("PAYPAL"),
    MOMO("MOMO"),
    ZALOPAY("ZALOPAY"),
    VNPAY("VNPAY");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
