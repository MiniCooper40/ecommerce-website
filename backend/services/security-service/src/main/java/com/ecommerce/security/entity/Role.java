package com.ecommerce.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, nullable = false, unique = true)
    private RoleName name;

    @Column(name = "description")
    private String description;

    // Custom constructor
    public Role(RoleName name) {
        this.name = name;
    }

    // Enum for role names
    public enum RoleName {
        USER("Regular user with basic permissions"),
        CUSTOMER("Customer with purchase permissions"),
        ADMIN("Administrator with full permissions"),
        MODERATOR("Moderator with content management permissions");

        private final String description;

        RoleName(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Custom toString method
    @Override
    public String toString() {
        return name != null ? name.name() : "UNKNOWN";
    }
}