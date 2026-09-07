package com.reynan.inventoryservice.repository;

import com.reynan.inventoryservice.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = "inventory")
    Page<Product> findAll(Pageable pageable);

    boolean existsByName(String name);

    Optional<Product> findByName(String name);
}
