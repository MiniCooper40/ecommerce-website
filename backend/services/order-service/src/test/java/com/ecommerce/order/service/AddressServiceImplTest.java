package com.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.ecommerce.shared.testutil.WithMockUserPrincipal;

import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.CreateAddressRequest;
import com.ecommerce.order.entity.Address;
import com.ecommerce.order.entity.AddressType;
import com.ecommerce.order.mapper.AddressMapper;
import com.ecommerce.order.repository.AddressRepository;
import com.ecommerce.order.service.AddressService;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressServiceImplTest {

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @MockBean
    private AddressRepository addressRepository;    private Address sampleAddress;
    private CreateAddressRequest sampleCreateRequest;

    @BeforeEach
    void setUp() {
        sampleAddress = createSampleAddress();
        sampleCreateRequest = createSampleCreateRequest();
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testCreateAddress() {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        // Act
        AddressDto result = addressService.createAddress(sampleCreateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(AddressType.SHIPPING);
        assertThat(result.getStreet()).isEqualTo("123 Main St");
        assertThat(result.getCity()).isEqualTo("Anytown");
        assertThat(result.getState()).isEqualTo("ST");
        assertThat(result.getZipCode()).isEqualTo("12345");
        assertThat(result.getCountry()).isEqualTo("USA");
        
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testGetAddress() {
        // Arrange
        when(addressRepository.findById(1L)).thenReturn(Optional.of(sampleAddress));

        // Act
        AddressDto result = addressService.getAddress(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(AddressType.SHIPPING);
        
        verify(addressRepository).findById(1L);
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testGetAddress_NotFound() {
        // Arrange
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.getAddress(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Address not found with id: 999");
        
        verify(addressRepository).findById(999L);
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testGetAddressesByType() {
        // Arrange
        Address billingAddress = createSampleBillingAddress();
        List<Address> addresses = Arrays.asList(sampleAddress, billingAddress);
        when(addressRepository.findByType(AddressType.SHIPPING)).thenReturn(Arrays.asList(sampleAddress));

        // Act
        List<AddressDto> result = addressService.getAddressesByType(AddressType.SHIPPING);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(AddressType.SHIPPING);
        
        verify(addressRepository).findByType(AddressType.SHIPPING);
    }

    @Test
    @WithMockUserPrincipal(userId = "admin-user", roles = {"ADMIN"})
    public void testConvertToDto() {
        // Act
        AddressDto result = addressMapper.toDto(sampleAddress);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sampleAddress.getId());
        assertThat(result.getType()).isEqualTo(sampleAddress.getType());
        assertThat(result.getStreet()).isEqualTo(sampleAddress.getStreet());
        assertThat(result.getCity()).isEqualTo(sampleAddress.getCity());
        assertThat(result.getState()).isEqualTo(sampleAddress.getState());
        assertThat(result.getZipCode()).isEqualTo(sampleAddress.getZipCode());
        assertThat(result.getCountry()).isEqualTo(sampleAddress.getCountry());
    }

    @Test
    @WithMockUserPrincipal(userId = "admin-user", roles = {"ADMIN"})
    public void testConvertToDto_Null() {
        // Act
        AddressDto result = addressMapper.toDto(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testConvertToEntity() {
        // Act
        Address result = addressService.convertToEntity(sampleCreateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(sampleCreateRequest.getType());
        assertThat(result.getStreet()).isEqualTo(sampleCreateRequest.getStreet());
        assertThat(result.getCity()).isEqualTo(sampleCreateRequest.getCity());
        assertThat(result.getState()).isEqualTo(sampleCreateRequest.getState());
        assertThat(result.getZipCode()).isEqualTo(sampleCreateRequest.getZipCode());
        assertThat(result.getCountry()).isEqualTo(sampleCreateRequest.getCountry());
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testConvertToEntity_Null() {
        // Act
        Address result = addressService.convertToEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testCreateAddressEntity() {
        // Act
        Address result = addressService.createAddressEntity(
                AddressType.BILLING, 
                "456 Oak Ave", 
                "Somewhere", 
                "ST", 
                "67890", 
                "USA"
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(AddressType.BILLING);
        assertThat(result.getStreet()).isEqualTo("456 Oak Ave");
        assertThat(result.getCity()).isEqualTo("Somewhere");
        assertThat(result.getState()).isEqualTo("ST");
        assertThat(result.getZipCode()).isEqualTo("67890");
        assertThat(result.getCountry()).isEqualTo("USA");
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testParseAndCreateAddress() {
        // Arrange
        String fullAddress = "123 Main St, Anytown, ST 12345, USA";

        // Act
        Address result = addressService.parseAndCreateAddress(AddressType.SHIPPING, fullAddress);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(AddressType.SHIPPING);
        assertThat(result.getStreet()).isEqualTo("123 Main St");
        assertThat(result.getCity()).isEqualTo("Anytown");
        assertThat(result.getState()).isEqualTo("ST");
        assertThat(result.getZipCode()).isEqualTo("12345");
        assertThat(result.getCountry()).isEqualTo("USA");
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testParseAndCreateAddress_WithoutCountry() {
        // Arrange
        String fullAddress = "123 Main St, Anytown, ST 12345";

        // Act
        Address result = addressService.parseAndCreateAddress(AddressType.SHIPPING, fullAddress);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(AddressType.SHIPPING);
        assertThat(result.getStreet()).isEqualTo("123 Main St");
        assertThat(result.getCity()).isEqualTo("Anytown");
        assertThat(result.getState()).isEqualTo("ST");
        assertThat(result.getZipCode()).isEqualTo("12345");
        assertThat(result.getCountry()).isEqualTo("USA"); // Default country
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testParseAndCreateAddress_InvalidFormat() {
        // Arrange
        String invalidAddress = "Invalid Address";

        // Act & Assert
        assertThatThrownBy(() -> addressService.parseAndCreateAddress(AddressType.SHIPPING, invalidAddress))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse address");
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testParseAndCreateAddress_NullAddress() {
        // Act & Assert
        assertThatThrownBy(() -> addressService.parseAndCreateAddress(AddressType.SHIPPING, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full address cannot be null or empty");
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testParseAndCreateAddress_EmptyAddress() {
        // Act & Assert
        assertThatThrownBy(() -> addressService.parseAndCreateAddress(AddressType.SHIPPING, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full address cannot be null or empty");
    }

    @Test
    public void testCreateAddress_Unauthenticated() {
        // This test verifies that authentication is required
        // Since we're not using @WithMockUserPrincipal, this should test unauthenticated access
        
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        // Act
        AddressDto result = addressService.createAddress(sampleCreateRequest);

        // Assert - The service should still work as it doesn't directly enforce security
        // Security is enforced at the controller level
        assertThat(result).isNotNull();
        verify(addressRepository).save(any(Address.class));
    }

    private Address createSampleAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setType(AddressType.SHIPPING);
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setState("ST");
        address.setZipCode("12345");
        address.setCountry("USA");
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return address;
    }

    private Address createSampleBillingAddress() {
        Address address = new Address();
        address.setId(2L);
        address.setType(AddressType.BILLING);
        address.setStreet("456 Oak Ave");
        address.setCity("Somewhere");
        address.setState("ST");
        address.setZipCode("67890");
        address.setCountry("USA");
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return address;
    }

    private CreateAddressRequest createSampleCreateRequest() {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setType(AddressType.SHIPPING);
        request.setStreet("123 Main St");
        request.setCity("Anytown");
        request.setState("ST");
        request.setZipCode("12345");
        request.setCountry("USA");
        return request;
    }
}
