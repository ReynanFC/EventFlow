package com.reynan.inventoryservice.controller;

import com.reynan.inventoryservice.controller.docs.ProductControllerDocs;
import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductControllerDocs {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Override
    public ResponseEntity<ResponseProductDTO> createProduct(@Valid @RequestBody CreateProductDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<Page<ResponseProductDTO>> findAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.findAllProducts(pageable));
    }

    @GetMapping("/name/{name}")
    @Override
    public ResponseEntity<ResponseProductDTO> findByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.findByName(name));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
