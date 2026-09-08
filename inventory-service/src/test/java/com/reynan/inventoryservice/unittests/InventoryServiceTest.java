package com.reynan.inventoryservice.unittests;

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
import com.reynan.inventoryservice.service.InventoryService;
import com.reynan.inventoryservice.support.InventoryTestData;
import com.reynan.inventoryservice.support.ProductTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService - Unit Tests")
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMapperImpl mapper;

    @InjectMocks
    private InventoryService inventoryService;

    private Long inventoryId;
    private Product product;
    private CreateInventoryDTO request;
    private Inventory inventory;
    private ResponseInventoryDTO response;

    @BeforeEach
    void setUp() {
        inventoryId = InventoryTestData.INVENTORY_ID;
        product = ProductTestData.validCreateProduct();
        request = InventoryTestData.validCreateInventoryDTO(ProductTestData.PRODUCT_ID);
        inventory = new Inventory(product, request.quantity());
        response = InventoryTestData.randomResponseInventoryDTO(inventory);
    }

    @Nested
    @DisplayName("Create Inventory Tests")
    class CreateInventory {

        @Test
        @DisplayName("Should create inventory successfully when product exists and has no inventory")
        void shouldCreateInventorySuccessfullyWhenProductExistsAndHasNoInventory() {
            when(productRepository.findById(request.productId())).thenReturn(Optional.of(product));
            when(mapper.toEntity(request, product)).thenReturn(inventory);
            when(inventoryRepository.save(inventory)).thenReturn(inventory);
            when(mapper.toResponseInventoryDTO(inventory)).thenReturn(response);

            ResponseInventoryDTO inventoryResponse = inventoryService.createInventory(request);

            assertThat(inventoryResponse).isNotNull().isEqualTo(response);
            assertThat(response)
                    .returns(inventory.getId(), ResponseInventoryDTO::id)
                    .returns(inventory.getQuantity(), ResponseInventoryDTO::quantity);

            verify(productRepository).findById(request.productId());
            verify(inventoryRepository).save(inventory);
            verify(mapper).toEntity(request, product);
            verify(mapper).toResponseInventoryDTO(inventory);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
            when(productRepository.findById(request.productId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.createInventory(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found by ID: " + request.productId());

            verify(productRepository).findById(request.productId());
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when product already has inventory")
        void shouldThrowResourceAlreadyExistsExceptionWhenProductAlreadyHasInventory() {
            product.setInventory(inventory);

            when(productRepository.findById(request.productId())).thenReturn(Optional.of(product));

            ResourceAlreadyExistsException exception = assertThrows(
                    ResourceAlreadyExistsException.class,
                    () -> inventoryService.createInventory(request)
            );

            assertEquals("Inventory already exists for product ID: " + request.productId(), exception.getMessage());

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Inventory Tests")
    class UpdateInventory {

        @Test
        @DisplayName("Should add quantity to inventory successfully when inventory exists")
        void shouldAddQuantityToInventorySuccessfullyWhenInventoryExists() {
            int quantityToAdd = 5;

            when(inventoryRepository.findByIdWithLock(inventoryId)).thenReturn(Optional.of(inventory));
            when(mapper.toResponseInventoryDTO(inventory)).thenReturn(response);

            ResponseInventoryDTO result = inventoryService.updateInventory(quantityToAdd, inventoryId);

            assertNotNull(result);
            assertEquals(15, inventory.getQuantity());
            assertEquals(response, result);

            verify(inventoryRepository).findByIdWithLock(inventoryId);
            verify(mapper).toResponseInventoryDTO(inventory);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating a non-existing inventory")
        void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistingInventory() {
            when(inventoryRepository.findByIdWithLock(inventoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.updateInventory(5, inventoryId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Inventory not found for id: " + inventoryId);

            verify(inventoryRepository).findByIdWithLock(inventoryId);
            verify(mapper, never()).toResponseInventoryDTO(inventory);
        }
    }

    @Nested
    @DisplayName("Remove Inventory Tests")
    class RemoveInventory {

        @Test
        @DisplayName("Should remove quantity from inventory successfully when stock is sufficient")
        void shouldRemoveQuantityFromInventorySuccessfullyWhenStockIsSufficient() {
            int quantityToRemove = 4;

            when(inventoryRepository.findByIdWithLock(inventoryId)).thenReturn(Optional.of(inventory));
            when(mapper.toResponseInventoryDTO(inventory)).thenReturn(response);

            ResponseInventoryDTO result = inventoryService.removeInventory(quantityToRemove, inventoryId);

            assertThat(result).isEqualTo(response);
            assertThat(inventory.getQuantity()).isEqualTo(request.quantity() - quantityToRemove);
            verify(inventoryRepository).findByIdWithLock(inventoryId);
            verify(mapper).toResponseInventoryDTO(inventory);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when removing from a non-existing inventory")
        void shouldThrowResourceNotFoundExceptionWhenRemovingFromNonExistingInventory() {
            when(inventoryRepository.findByIdWithLock(inventoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.removeInventory(1, inventoryId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Inventory not found for id: " + inventoryId);

            verify(inventoryRepository).findByIdWithLock(inventoryId);
            verify(mapper, never()).toResponseInventoryDTO(any());
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
        void shouldThrowInsufficientStockExceptionWhenRequestedQuantityExceedsAvailableStock() {
            int quantityToRemove = request.quantity() + 1;

            when(inventoryRepository.findByIdWithLock(inventoryId)).thenReturn(Optional.of(inventory));

            assertThatThrownBy(() -> inventoryService.removeInventory(quantityToRemove, inventoryId))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessage("Insufficient stock for inventory ID " + inventoryId
                            + ". Requested: " + quantityToRemove
                            + ", Available: " + request.quantity());

            assertThat(inventory.getQuantity()).isEqualTo(request.quantity());
            verify(inventoryRepository).findByIdWithLock(inventoryId);
            verify(mapper, never()).toResponseInventoryDTO(any());
        }
    }
}
