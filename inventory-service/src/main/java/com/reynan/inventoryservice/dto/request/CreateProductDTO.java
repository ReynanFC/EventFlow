package com.reynan.inventoryservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductDTO(

        @NotBlank(message = "This name is blank ou invalid")
        @Size(max = 20, message = "The name must have a maximum of 20 characters")
        String name,

        @DecimalMin(value = "0.00", inclusive = false, message = "This price")
        BigDecimal price
) {
        public CreateProductDTO {
            name = (name == null) ? null : name.trim();
        }
}
