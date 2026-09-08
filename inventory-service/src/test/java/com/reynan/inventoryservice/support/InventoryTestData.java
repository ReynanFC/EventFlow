package com.reynan.inventoryservice.support;

import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.entities.Inventory;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public final class InventoryTestData {

    public static final Long INVENTORY_ID = 1L;
    public static final Long PRODUCT_ID = 1L;
    public static final int QUANTITY = 10;
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);

    private InventoryTestData() {
    }

    public static CreateInventoryDTO validCreateInventoryDTO(Long productId) {
        return new CreateInventoryDTO(productId, QUANTITY);
    }

    public static CreateInventoryDTO randomCreateInventoryDTO() {
        return new CreateInventoryDTO(
                ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                ThreadLocalRandom.current().nextInt(0, 10_001)
        );
    }

    public static ResponseInventoryDTO randomResponseInventoryDTO(Inventory inventory) {
        return new ResponseInventoryDTO(
                inventory.getProduct().getId(),
                inventory.getQuantity(),
                inventory.getUpdatedAt()
        );
    }
}
