package com.reynan.inventoryservice.mapper.contract;

import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.entities.Inventory;
import com.reynan.inventoryservice.entities.Product;

public interface InventoryMapper {

    Inventory toEntity(CreateInventoryDTO createInventoryDTO, Product product);
    ResponseInventoryDTO toResponseInventoryDTO(Inventory inventory);
}
