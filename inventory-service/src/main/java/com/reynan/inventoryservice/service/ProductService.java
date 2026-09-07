package com.reynan.inventoryservice.service;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.entities.Product;
import com.reynan.inventoryservice.exceptions.DuplicateResourceException;
import com.reynan.inventoryservice.exceptions.ResourceNotFoundException;
import com.reynan.inventoryservice.mapper.contract.ProductMapper;
import com.reynan.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final Logger log =  LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ResponseProductDTO createProduct(CreateProductDTO req) {
        log.debug("Creating product with name={}", req.name());

        if (productRepository.existsByName(req.name())) {
            log.warn("Product creation rejected because name={} is already in use", req.name());
            throw new DuplicateResourceException("Product with name '" + req.name() + "' already exists");
        }

        Product product = productRepository.save(productMapper.toEntity(req));
        log.info("Product created successfully: id={}, name={}", product.getId(), product.getName());

        return productMapper.toResponseProductDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ResponseProductDTO> findAllProducts(Pageable pageable) {
        log.debug("Listing products: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return productRepository.findAll(pageable)
                .map(productMapper::toResponseProductDTO);
    }

    public ResponseProductDTO findByName(String name) {
        log.debug("Searching product by name={}", name);

        return productRepository.findByName(name)
                .map(productMapper::toResponseProductDTO)
                .orElseThrow(() -> {
                    log.warn("Product not found by name={}", name);
                    return new ResourceNotFoundException("Product with name '" + name + "' not found");
                });
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id={}", id);

        if (!productRepository.existsById(id)) {
            log.warn("Product deletion failed because id={} was not found", id);
            throw new ResourceNotFoundException("Product with id '" + id + "' not found");
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully: id={}", id);
    }
}
