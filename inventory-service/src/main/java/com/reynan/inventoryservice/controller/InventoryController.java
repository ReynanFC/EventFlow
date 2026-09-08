package com.reynan.inventoryservice.controller;

import com.reynan.inventoryservice.controller.docs.InventoryControllerDocs;
import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController implements InventoryControllerDocs {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Override
    public ResponseEntity<ResponseInventoryDTO> createInventory(
            @Valid @RequestBody CreateInventoryDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(request));
    }

    @PatchMapping("/{inventoryId}/add")
    @Override
    public ResponseEntity<ResponseInventoryDTO> addStock(
            @PathVariable Long inventoryId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(inventoryService.updateInventory(quantity, inventoryId));
    }

    @PatchMapping("/{inventoryId}/remove")
    @Override
    public ResponseEntity<ResponseInventoryDTO> removeStock(
            @PathVariable Long inventoryId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(inventoryService.removeInventory(quantity, inventoryId));
    }
}
