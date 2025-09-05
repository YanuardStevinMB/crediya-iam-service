package com.crediya.iam.api.config.userMapper;

import com.crediya.iam.api.dto.UserResponseDto;
import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.api.userMapper.UserMapper;
import com.crediya.iam.model.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toModel_shouldMapAllFields() {
        UserSaveDto dto = new UserSaveDto(
                1L,                                   // id
                "Alice",                              // firstName
                "Smith",                              // lastName
                "alice.smith@example.com",            // email
                LocalDate.of(1985, 8, 15),            // birthdate
                "XYZ789",                             // identityDocument
                "555-6789",                           // phoneNumber
                new BigDecimal("5000.00"),            // baseSalary
                "456 Oak Ave",                        // address
                "securePass123",                      // password
                3L                                    // roleId
        );


        User user = mapper.toModel(dto);

        assertNotNull(user);
        assertEquals("Alice", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals(LocalDate.of(1985, 8, 15), user.getBirthdate());
        assertEquals("456 Oak Ave", user.getAddress());
        assertEquals("555-6789", user.getPhoneNumber());
        assertEquals("alice.smith@example.com", user.getEmail());
        assertEquals(new BigDecimal("5000.00"), user.getBaseSalary());
        assertEquals("XYZ789", user.getIdentityDocument());
        assertEquals(3L, user.getRoleId());
        assertEquals("securePass123", user.getPassword());
    }

    @Test
    void toModel_nullInput_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toResponseDto_shouldMapEntityToDto() {
        User user = User.create(
                "Alice",
                "Smith",
                LocalDate.of(1985, 8, 15),
                "456 Oak Ave",
                "555-6789",
                "alice.smith@example.com",
                new BigDecimal("5000.00"),
                "XYZ789",
                3L,
                "securePass123"
        );

        UserResponseDto dto = mapper.toResponseDto(user);

        assertNotNull(dto);
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("alice.smith@example.com", dto.getEmail());
        assertEquals(new BigDecimal("5000.00"), dto.getBaseSalary());
        assertEquals("XYZ789", dto.getIdentityDocument());
        assertEquals(3L, dto.getRoleId());
    }
}
