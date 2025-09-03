package com.crediya.iam.model.user;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void create_shouldInitializeUserCorrectly() {
        User user = User.create(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                "5551234",
                "john.doe@mail.com",
                BigDecimal.valueOf(5000),
                "123456789",
                2L,
                "Passw0rd123"
        );

        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthdate());
        assertEquals("123 Main St", user.getAddress());
        assertEquals("5551234", user.getPhoneNumber());
        assertEquals("john.doe@mail.com", user.getEmail());
        assertEquals(BigDecimal.valueOf(5000), user.getBaseSalary());
        assertEquals("123456789", user.getIdentityDocument());
        assertEquals(2L, user.getRoleId());
        assertEquals("Passw0rd123", user.getPassword());
        assertTrue(user.getActive()); // ✅ por defecto siempre true
        assertNull(user.getId()); // no se asigna en factory
    }

    @Test
    void withId_shouldAssignIdAndReturnSameUser() {
        User user = User.create(
                "Jane",
                "Smith",
                LocalDate.of(1985, 5, 20),
                "456 Elm St",
                "9876543",
                "jane.smith@mail.com",
                BigDecimal.valueOf(6000),
                "DOC999",
                3L,
                "Secret123"
        );

        User returned = user.withId(10L);

        assertSame(user, returned); // retorna misma instancia
        assertEquals(10L, user.getId());
    }

    @Test
    void builder_shouldBuildUserWithAllFields() {
        User user = User.builder()
                .id(99L)
                .firstName("Alice")
                .lastName("Wonder")
                .birthdate(LocalDate.of(2000, 12, 31))
                .address("789 Oak St")
                .phoneNumber("111222333")
                .email("alice@mail.com")
                .baseSalary(BigDecimal.valueOf(7000))
                .identityDocument("DOC777")
                .roleId(5L)
                .active(false)
                .password("builderPwd")
                .build();

        assertNotNull(user);
        assertEquals(99L, user.getId());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Wonder", user.getLastName());
        assertEquals(LocalDate.of(2000, 12, 31), user.getBirthdate());
        assertEquals("789 Oak St", user.getAddress());
        assertEquals("111222333", user.getPhoneNumber());
        assertEquals("alice@mail.com", user.getEmail());
        assertEquals(BigDecimal.valueOf(7000), user.getBaseSalary());
        assertEquals("DOC777", user.getIdentityDocument());
        assertEquals(5L, user.getRoleId());
        assertFalse(user.getActive());
        assertEquals("builderPwd", user.getPassword());
    }

    @Test
    void settersAndGetters_shouldModifyValues() {
        User user = new User();
        user.setId(50L);
        user.setFirstName("Bob");
        user.setLastName("Marley");
        user.setBirthdate(LocalDate.of(1975, 6, 15));
        user.setAddress("Some Street");
        user.setPhoneNumber("444555666");
        user.setEmail("bob@mail.com");
        user.setBaseSalary(BigDecimal.valueOf(8000));
        user.setIdentityDocument("DOC555");
        user.setRoleId(9L);
        user.setActive(true);
        user.setPassword("pwd123");

        assertEquals(50L, user.getId());
        assertEquals("Bob", user.getFirstName());
        assertEquals("Marley", user.getLastName());
        assertEquals(LocalDate.of(1975, 6, 15), user.getBirthdate());
        assertEquals("Some Street", user.getAddress());
        assertEquals("444555666", user.getPhoneNumber());
        assertEquals("bob@mail.com", user.getEmail());
        assertEquals(BigDecimal.valueOf(8000), user.getBaseSalary());
        assertEquals("DOC555", user.getIdentityDocument());
        assertEquals(9L, user.getRoleId());
        assertTrue(user.getActive());
        assertEquals("pwd123", user.getPassword());
    }
}

