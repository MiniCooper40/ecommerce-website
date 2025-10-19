package com.ecommerce.order.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address entity representing customer addresses
 */
@Entity
@Table(name = "addresses")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AddressType type;
    
    @Column(name = "street", nullable = false, length = 255)
    private String street;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "state", nullable = false, length = 100)
    private String state;
    
    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;
    
    @Column(name = "country", nullable = false, length = 100)
    private String country;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Custom constructor for common use case
    public Address(AddressType type, String street, String city, String state, String zipCode, String country) {
        this.type = type;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    /**
     * Get full address as formatted string
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
    }
}
