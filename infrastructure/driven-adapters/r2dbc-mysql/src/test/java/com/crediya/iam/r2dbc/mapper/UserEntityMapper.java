package com.crediya.iam.r2dbc.mapper;

import com.crediya.iam.model.user.User;
import com.crediya.iam.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityMapperTest {

    private UserEntityMapper mapper;

    @BeforeEach
    void setUp() {
        // instanciamos interfaz como clase anónima porque solo tiene default methods
        mapper = new UserEntityMapper() {};
    }

    @Test
    void toEntity_shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_shouldMapAllFields() {
        User domain = User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234",
                "john.doe@mail.com",
                BigDecimal.valueOf(5000.0),
                "ID12345",
                2L,
                "secret"
        ).withId(10L);

        UserEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), entity.getBirthdate());
        assertEquals("123 Main St", entity.getAddress());
        assertEquals("5551234", entity.getPhoneNumber());
        assertEquals("john.doe@mail.com", entity.getEmail());
        assertEquals(BigDecimal.valueOf(5000.0), entity.getBaseSalary());
        // ⚠️ nota: en tu mapper usas domain.getPhoneNumber() como identityDocument
        assertEquals("5551234", entity.getIdentityDocument());
        assertEquals(2L, entity.getRoleId());
        assertEquals("secret", entity.getPassword());
    }

    @Test
    void toDomain_shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_shouldMapAllFields() {
        UserEntity entity = UserEntity.builder()
                .id(20L)
                .firstName("Jane")
                .lastName("Smith")
                .birthdate(LocalDate.of(1985, 5, 20))
                .address("456 Elm St")
                .phoneNumber("9876543")
                .email("jane.smith@mail.com")
                .baseSalary(BigDecimal.valueOf(5000.0))
                .identityDocument("DOC999")
                .roleId(3L)
                .password("hashedPass")
                .build();

        User domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(20L, domain.getId());
        assertEquals("Jane", domain.getFirstName());
        assertEquals("Smith", domain.getLastName());
        assertEquals(LocalDate.of(1985, 5, 20), domain.getBirthdate());
        assertEquals("456 Elm St", domain.getAddress());
        assertEquals("9876543", domain.getPhoneNumber());
        assertEquals("jane.smith@mail.com", domain.getEmail());
        assertEquals(BigDecimal.valueOf(5000.0), entity.getBaseSalary());
        assertEquals("DOC999", domain.getIdentityDocument());
        assertEquals(3L, domain.getRoleId());
        assertEquals("hashedPass", domain.getPassword());
    }
}