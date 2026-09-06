package com.reynan.paymentservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ProcessedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    public ProcessedEvent() {}

    public ProcessedEvent(UUID eventId, Long orderId) {
        this.eventId = eventId;
        this.orderId = orderId;
    }

    public UUID getEventId() { return eventId; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
