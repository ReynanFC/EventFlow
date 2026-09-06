package com.reynan.inventoryservice.mapper.impl;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.entities.Product;
import com.reynan.inventoryservice.mapper.contract.ProductMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(CreateProductDTO createProductDTO) {

        Product product = new Product(createProductDTO.name(), createProductDTO.price());

        return product;
    }

    @Override
    public ResponseProductDTO toResponseProductDTO(Product product) {

        ResponseProductDTO dto = new ResponseProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCreatedAt()
        );

        return dto;
    }
}
