package com.ridelink.driver.service;

import com.ridelink.driver.dto.AvailabilityUpdateRequest;
import com.ridelink.driver.dto.DriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.ServiceAreaUpdateRequest;
import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Driver;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DriverService}, the business-logic layer of the
 * Driver & Vehicle Service. The repository is mocked so these tests exercise
 * only the service's rules (duplicate checks, not-found handling, mapping)
 * without needing a real database.
 */
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    // A reusable sample driver fixture shared by every test below.
    @BeforeEach
    void setUp() {
        driver = Driver.builder()
                .id("1")
                .userId("100")
                .availabilityStatus(AvailabilityStatus.UNAVAILABLE)
                .currentLocation("Colombo")
                .serviceArea("Colombo District")
                .build();
    }

    // Happy path: creating a profile for a userId with no existing driver record.
    @Test
    void createDriver_shouldCreateProfile_whenNotExists() {
        DriverRequest request = new DriverRequest("100", "Colombo", "Colombo District");

        when(driverRepository.findByUserId("100")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.createDriver(request);

        assertEquals("100", response.getUserId());
        assertEquals(AvailabilityStatus.UNAVAILABLE, response.getAvailabilityStatus());
    }

    // Negative scenario: enforces the "one driver profile per userId" business rule.
    @Test
    void createDriver_shouldThrow_whenProfileAlreadyExists() {
        DriverRequest request = new DriverRequest("100", "Colombo", "Colombo District");
        when(driverRepository.findByUserId("100")).thenReturn(Optional.of(driver));

        assertThrows(DuplicateResourceException.class, () -> driverService.createDriver(request));
    }

    // Happy path: the field GET /api/drivers/available (and Ride Service) relies on.
    @Test
    void updateAvailability_shouldUpdateStatus() {
        AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.AVAILABLE);

        when(driverRepository.findById("1")).thenReturn(Optional.of(driver));
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.updateAvailability("1", request);

        assertEquals(AvailabilityStatus.AVAILABLE, response.getAvailabilityStatus());
    }

    // Negative scenario: updating a non-existent driver must fail with 404-mapped exception.
    @Test
    void updateAvailability_shouldThrow_whenDriverNotFound() {
        AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.AVAILABLE);
        when(driverRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.updateAvailability("99", request));
    }

    // Covers the dedicated service-area endpoint added to satisfy the assignment brief.
    @Test
    void updateServiceArea_shouldUpdateArea() {
        ServiceAreaUpdateRequest request = new ServiceAreaUpdateRequest("Kandy District");

        when(driverRepository.findById("1")).thenReturn(Optional.of(driver));
        driver.setServiceArea("Kandy District");
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.updateServiceArea("1", request);

        assertEquals("Kandy District", response.getServiceArea());
    }

    // Verifies the query that backs the interservice contract with Ride Management Service.
    @Test
    void getAvailableDrivers_shouldReturnOnlyAvailableDrivers() {
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        when(driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.getAvailableDrivers();

        assertEquals(1, result.size());
        assertEquals(AvailabilityStatus.AVAILABLE, result.get(0).getAvailabilityStatus());
    }
}
