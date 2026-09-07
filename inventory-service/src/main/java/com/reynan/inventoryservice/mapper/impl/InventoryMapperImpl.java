package com.reynan.inventoryservice.mapper.impl;

import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.entities.Inventory;
import com.reynan.inventoryservice.entities.Product;
import com.reynan.inventoryservice.mapper.contract.InventoryMapper;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public Inventory toEntity(CreateInventoryDTO createInventoryDTO, Product product) {

        return new Inventory(product, createInventoryDTO.quantity());
    }

    @Override
    public ResponseInventoryDTO toResponseInventoryDTO(Inventory inventory) {

        return new ResponseInventoryDTO(
                inventory.getId(),
                inventory.getQuantity(),
                inventory.getUpdatedAt()
        );
    }
}
