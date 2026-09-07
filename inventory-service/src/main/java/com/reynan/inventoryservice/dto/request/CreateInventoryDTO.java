package com.reynan.inventoryservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryDTO(

        @NotNull
        Long productId,

        @DecimalMin(value = "0", inclusive = true)
        int quantity
) {}
