package com.reynan.inventoryservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseProductDTO(
        Long id,
        String name,
        BigDecimal price,
        LocalDateTime createdAt,
        ResponseInventoryDTO inventory
) {}
