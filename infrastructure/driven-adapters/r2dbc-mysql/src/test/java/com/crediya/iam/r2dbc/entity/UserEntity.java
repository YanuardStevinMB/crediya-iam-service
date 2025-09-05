package com.crediya.iam.r2dbc.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserEntityTest {

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        LocalDate birthdate = LocalDate.of(1990, 5, 15);
        UserEntity user = new UserEntity(
                1L,
                "John",
                "Doe",
                "john.doe@mail.com",
                birthdate,
                "DOC123",
                "5551234",
                "123 Main St",
                BigDecimal.valueOf(5000.0),
                2L,
                "secret"
        );

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@mail.com", user.getEmail());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals("DOC123", user.getIdentityDocument());
        assertEquals("5551234", user.getPhoneNumber());
        assertEquals("123 Main St", user.getAddress());
        assertEquals(BigDecimal.valueOf(5000.0), user.getBaseSalary());
        assertEquals(2L, user.getRoleId());
        assertEquals("secret", user.getPassword());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        UserEntity user = new UserEntity();

        assertNull(user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getBirthdate());
        assertNull(user.getIdentityDocument());
        assertNull(user.getPhoneNumber());
        assertNull(user.getAddress());
        assertNull(user.getBaseSalary());
        assertNull(user.getRoleId());
        assertNull(user.getPassword());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        LocalDate birthdate = LocalDate.of(2000, 1, 1);
        UserEntity user = new UserEntity();

        user.setId(5L);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice.smith@mail.com");
        user.setBirthdate(birthdate);
        user.setIdentityDocument("DOC999");
        user.setPhoneNumber("9876543");
        user.setAddress("456 Elm St");
        user.setBaseSalary(BigDecimal.valueOf(7000.0));
        user.setRoleId(3L);
        user.setPassword("pwd");

        assertEquals(5L, user.getId());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("alice.smith@mail.com", user.getEmail());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals("DOC999", user.getIdentityDocument());
        assertEquals("9876543", user.getPhoneNumber());
        assertEquals("456 Elm St", user.getAddress());
        assertEquals(BigDecimal.valueOf(7000.0), user.getBaseSalary());
        assertEquals(3L, user.getRoleId());
        assertEquals("pwd", user.getPassword());
    }

    @Test
    void builder_shouldCreateObjectCorrectly() {
        LocalDate birthdate = LocalDate.of(1985, 8, 20);
        UserEntity user = UserEntity.builder()
                .id(7L)
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@mail.com")
                .birthdate(birthdate)
                .identityDocument("DOC555")
                .phoneNumber("111222333")
                .address("789 Oak St")
                .baseSalary(BigDecimal.valueOf(8000.0))
                .roleId(4L)
                .password("superSecret")
                .build();

        assertEquals(7L, user.getId());
        assertEquals("Bob", user.getFirstName());
        assertEquals("Johnson", user.getLastName());
        assertEquals("bob.johnson@mail.com", user.getEmail());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals("DOC555", user.getIdentityDocument());
        assertEquals("111222333", user.getPhoneNumber());
        assertEquals("789 Oak St", user.getAddress());
        assertEquals(BigDecimal.valueOf(8000.0), user.getBaseSalary());
        assertEquals(4L, user.getRoleId());
        assertEquals("superSecret", user.getPassword());
    }

    @Test
    void toBuilder_shouldCreateCopyAndModify() {
        UserEntity user1 = UserEntity.builder()
                .id(10L)
                .firstName("Carol")
                .lastName("Brown")
                .email("carol.brown@mail.com")
                .build();

        UserEntity user2 = user1.toBuilder()
                .email("carol.updated@mail.com")
                .build();

        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getFirstName(), user2.getFirstName());
        assertEquals("carol.updated@mail.com", user2.getEmail());
    }
}