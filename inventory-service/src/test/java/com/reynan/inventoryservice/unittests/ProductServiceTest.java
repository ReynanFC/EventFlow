package com.reynan.inventoryservice.unittests;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.entities.Inventory;
import com.reynan.inventoryservice.mapper.impl.ProductMapperImpl;
import com.reynan.inventoryservice.entities.Product;
import com.reynan.inventoryservice.mapper.contract.ProductMapper;
import com.reynan.inventoryservice.repository.ProductRepository;
import com.reynan.inventoryservice.service.ProductService;
import com.reynan.inventoryservice.support.ProductTestData;
import com.reynan.inventoryservice.exceptions.DuplicateResourceException;
import com.reynan.inventoryservice.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Test unit")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully when name is unique")
        void shouldCreateProductSuccessfully() {
            CreateProductDTO request = ProductTestData.randomCreateProductDTO();
            Product product = new Product(request.name(), request.price());
            ResponseProductDTO response = ProductTestData.randomResponseProductDTO(request);

            when(productRepository.existsByName(request.name())).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(product);
            when(productRepository.save(product)).thenReturn(product);
            when(mapper.toResponseProductDTO(product)).thenReturn(response);

            ResponseProductDTO result = productService.createProduct(request);

            assertThat(result).isEqualTo(response);
            verify(productRepository).existsByName(request.name());
            verify(productRepository).save(product);
            verify(mapper).toResponseProductDTO(product);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when product name already exists")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            CreateProductDTO request = ProductTestData.randomCreateProductDTO();

            when(productRepository.existsByName(request.name())).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Product with name '" + request.name() + "' already exists");

            verify(productRepository).existsByName(request.name());
            verify(productRepository, never()).save(any());
            verify(mapper, never()).toResponseProductDTO(any());
        }
    }

    @Nested
    @DisplayName("Find All Products Tests")
    class FindAllProductsTests {

        @Test
        @DisplayName("Should include inventory in product response when it exists")
        void shouldIncludeInventoryInProductResponse() {
            Product product = org.mockito.Mockito.mock(Product.class);
            Inventory inventory = org.mockito.Mockito.mock(Inventory.class);
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
            java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();

            when(product.getId()).thenReturn(1L);
            when(product.getName()).thenReturn("Notebook");
            when(product.getPrice()).thenReturn(new java.math.BigDecimal("1999.90"));
            when(product.getCreatedAt()).thenReturn(createdAt);
            when(product.getInventory()).thenReturn(inventory);
            when(inventory.getId()).thenReturn(2L);
            when(inventory.getQuantity()).thenReturn(12);
            when(inventory.getUpdatedAt()).thenReturn(updatedAt);

            ResponseProductDTO result = new ProductMapperImpl().toResponseProductDTO(product);

            assertThat(result.inventory())
                    .isEqualTo(new ResponseInventoryDTO(2L, 12, updatedAt));
        }

        @Test
        @DisplayName("Should return paged list of products")
        void shouldReturnPagedProducts() {

            Pageable pageable = PageRequest.of(0, 10);
            CreateProductDTO request = ProductTestData.randomCreateProductDTO();
            Product product = new Product(request.name(), request.price());
            ResponseProductDTO response = ProductTestData.randomResponseProductDTO(request);
            Page<Product> page = new PageImpl<>(List.of(product));

            when(productRepository.findAll(pageable)).thenReturn(page);
            when(mapper.toResponseProductDTO(product)).thenReturn(response);

            Page<ResponseProductDTO> result = productService.findAllProducts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().name()).isEqualTo(request.name());

            verify(productRepository).findAll(pageable);
            verify(mapper).toResponseProductDTO(product);
        }
    }

    @Nested
    @DisplayName("Find By Name Tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should return product when name exists")
        void shouldReturnProductWhenNameExists() {

            CreateProductDTO request = ProductTestData.randomCreateProductDTO();
            Product product = new Product(request.name(), request.price());
            ResponseProductDTO response = ProductTestData.randomResponseProductDTO(request);
            String name = request.name();

            when(productRepository.findByName(name)).thenReturn(Optional.of(product));
            when(mapper.toResponseProductDTO(product)).thenReturn(response);

            ResponseProductDTO result = productService.findByName(name);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(name);

            verify(productRepository).findByName(name);
            verify(mapper).toResponseProductDTO(product);
        }


        @Test
        @DisplayName("Should throw ResourceNotFoundException when name does not exist")
        void shouldThrowExceptionWhenNameNotFound() {

            String name = "not found";

            when(productRepository.findByName(name)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findByName(name))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product with name '" + name + "' not found");

            verify(productRepository).findByName(name);
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully when ID exists")
        void shouldDeleteProductSuccessfully() {
            Long id = 1L;

            when(productRepository.existsById(id)).thenReturn(true);

            productService.deleteProduct(id);

            verify(productRepository).existsById(id);
            verify(productRepository).deleteById(id);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existing ID")
        void shouldThrowExceptionWhenDeletingNonExistingId() {
            Long id = 99L;

            when(productRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product with id '" + id + "' not found");

            verify(productRepository).existsById(id);
            verify(productRepository, never()).deleteById(any());
        }
    }
}
