package com.ecommerce.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.CreateAddressRequest;
import com.ecommerce.order.entity.AddressType;
import com.ecommerce.order.service.AddressService;
import com.ecommerce.security.annotation.CurrentUserId;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {
    
    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDto>> getUserAddresses(@CurrentUserId String userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getUserAddress(@PathVariable Long id, @CurrentUserId String userId) {
        return ResponseEntity.ok(addressService.getUserAddress(id, userId));
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody CreateAddressRequest request, 
                                                   @CurrentUserId String userId) {
        return ResponseEntity.ok(addressService.createAddress(request, userId));
    }
    
    @GetMapping("/by-type")
    public ResponseEntity<List<AddressDto>> getUserAddressesByType(@RequestParam AddressType type, 
                                                                  @CurrentUserId String userId) {
        return ResponseEntity.ok(addressService.getUserAddressesByType(userId, type));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AddressDto>> getAllAddresses() {
        // Get all addresses regardless of type - need a new service method
        return ResponseEntity.ok(addressService.getAllAddresses());
    }
    
    @GetMapping("/admin/by-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AddressDto>> getAllAddressesByType(@RequestParam AddressType type) {
        return ResponseEntity.ok(addressService.getAddressesByType(type));
    }
    
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressDto> getAnyAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddress(id));
    }
}
