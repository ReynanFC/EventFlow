package com.reynan.inventoryservice.support;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class ProductTestData {

    public static final Long PRODUCT_ID = 1L;
    public static final String PRODUCT_NAME = "Test Product";
    public static final BigDecimal PRODUCT_PRICE = new BigDecimal("99.90");
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);

    private ProductTestData() {
    }

    public static CreateProductDTO validCreateProductDTO() {
        return new CreateProductDTO(PRODUCT_NAME, PRODUCT_PRICE);
    }

    public static CreateProductDTO randomCreateProductDTO() {
        return new CreateProductDTO(
                "Product-" + UUID.randomUUID().toString().substring(0, 8),
                randomPrice()
        );
    }

    public static ResponseProductDTO randomResponseProductDTO(CreateProductDTO request) {
        return new ResponseProductDTO(
                ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                request.name(),
                request.price(),
                LocalDateTime.now(),
                null
        );
    }

    private static BigDecimal randomPrice() {
        long cents = ThreadLocalRandom.current().nextLong(100, 100_001);
        return BigDecimal.valueOf(cents, 2);
    }
}
