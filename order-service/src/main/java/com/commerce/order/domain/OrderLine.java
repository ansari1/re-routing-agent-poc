package com.commerce.order.domain;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public record OrderLine(String sku, int quantity, BigDecimal unitPrice) {
}
