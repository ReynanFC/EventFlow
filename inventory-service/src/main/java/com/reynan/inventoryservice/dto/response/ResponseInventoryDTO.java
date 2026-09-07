package com.reynan.inventoryservice.dto.response;

import java.time.LocalDateTime;

public record ResponseInventoryDTO(
        Long id,
        int quantity,
        LocalDateTime updatedAt
) {}
