package com.reynan.inventoryservice.service;

import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.entities.Inventory;
import com.reynan.inventoryservice.entities.Product;
import com.reynan.inventoryservice.exceptions.InsufficientStockException;
import com.reynan.inventoryservice.exceptions.ResourceAlreadyExistsException;
import com.reynan.inventoryservice.exceptions.ResourceNotFoundException;
import com.reynan.inventoryservice.mapper.impl.InventoryMapperImpl;
import com.reynan.inventoryservice.repository.InventoryRepository;
import com.reynan.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapperImpl mapper;

    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository, InventoryMapperImpl inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.mapper = inventoryMapper;
    }

    @Transactional(readOnly = true)
    public ResponseInventoryDTO findInventory(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapper.toResponseInventoryDTO(product.getInventory());
    }

    @Transactional
    public ResponseInventoryDTO createInventory(CreateInventoryDTO req) {
        log.info("Initiating inventory creation for product ID: {}", req.productId());

        Product product = findByIdProduct(req.productId());

        if (product.getInventory() != null) {
            log.warn("Failed to create inventory: Already exists for product ID: {}", req.productId());
            throw new ResourceAlreadyExistsException("Inventory already exists for product ID: " + req.productId());
        }

        Inventory inventory = inventoryRepository.save(mapper.toEntity(req, product));
        log.info("Inventory created successfully with ID: {} for product ID: {}", inventory.getId(), req.productId());

        return mapper.toResponseInventoryDTO(inventory);
    }

    @Transactional
    public ResponseInventoryDTO updateInventory(int quantity, Long inventoryId) {
        log.info("Request received to add quantity: {} to inventory ID: {}", quantity, inventoryId);

        Inventory inventory = inventoryRepository.findByIdWithLock(inventoryId)
                .orElseThrow(() -> {
                    log.warn("Failed to update inventory: ID {} not found", inventoryId);
                    return new ResourceNotFoundException("Inventory not found for id: " + inventoryId);
                });

        inventory.addToInventory(quantity);
        log.info("Successfully added quantity {} to inventory ID: {}. New balance: {}", quantity, inventoryId, inventory.getQuantity());

        return mapper.toResponseInventoryDTO(inventory);
    }

    /**
     * Removes a specified quantity from the inventory balance.
     * The method {@code removeFromInventory} inside the entity performs the decrement operation.
     *
     * @param quantity the amount to be removed
     * @param inventoryId the ID of the inventory record
     * @return {@link ResponseInventoryDTO} containing updated inventory information
     * @throws ResourceNotFoundException if the inventory is not found
     * @throws InsufficientStockException if the available stock is less than the requested quantity
     */
    @Transactional
    public ResponseInventoryDTO removeInventory(int quantity, Long inventoryId) {
        log.info("Request received to remove quantity: {} from inventory ID: {}", quantity, inventoryId);

        Inventory inventory = inventoryRepository.findByIdWithLock(inventoryId)
                .orElseThrow(() -> {
                    log.warn("Failed to remove stock: Inventory ID {} not found", inventoryId);
                    return new ResourceNotFoundException("Inventory not found for id: " + inventoryId);
                });

        if (!inventory.removeFromInventory(quantity)) {
            log.warn("Insufficient stock for inventory ID: {}. Requested: {}, Available: {}", inventoryId, quantity, inventory.getQuantity());
            throw new InsufficientStockException(
                    String.format("Insufficient stock for inventory ID %d. Requested: %d, Available: %d",
                            inventoryId, quantity, inventory.getQuantity())
            );
        }

        log.info("Successfully removed quantity {} from inventory ID: {}. Remaining balance: {}", quantity, inventoryId, inventory.getQuantity());

        return mapper.toResponseInventoryDTO(inventory);
    }

    private Product findByIdProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product lookup failed: ID {} not found", productId);
                    return new ResourceNotFoundException("Product not found");
                });
    }
}