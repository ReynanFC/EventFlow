package com.reynan.notificationservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Invoice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "sent_to_email", nullable = false, length = 150)
    private String sentToEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public Invoice() {}

    public Invoice(Long orderId, String sentToEmail) {
        this.orderId = orderId;
        this.sentToEmail = sentToEmail;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getSentToEmail() { return sentToEmail; }
    public LocalDateTime getSentAt() { return sentAt; }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Invoice invoice = (Invoice) object;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
