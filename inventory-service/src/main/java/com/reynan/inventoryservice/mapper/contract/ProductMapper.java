package com.reynan.inventoryservice.mapper.contract;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.entities.Product;

public interface ProductMapper {
    Product toEntity(CreateProductDTO createProductDTO);
    ResponseProductDTO toResponseProductDTO(Product product);
}
