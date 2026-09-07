package com.reynan.inventoryservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Inventory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Inventory() {}

    public Inventory(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void addToInventory(int quantity) {
        this.quantity += quantity;
    }

    public boolean removeFromInventory(int quantity) {

        if (this.quantity < quantity) return false;

        this.quantity -= quantity;

        return true;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Inventory inventory = (Inventory) object;
        return Objects.equals(id, inventory.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
