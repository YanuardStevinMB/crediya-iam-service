package com.crediya.iam.r2dbc.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleEntityTest {

@Test
void allArgsConstructor_shouldSetAllFields() {
    RoleEntity role = new RoleEntity(1L, "ADMIN", "Administrador del sistema");

    assertEquals(1L, role.getId());
    assertEquals("ADMIN", role.getName());
    assertEquals("Administrador del sistema", role.getDescription());
}

@Test
void noArgsConstructor_shouldCreateEmptyObject() {
    RoleEntity role = new RoleEntity();

    assertNull(role.getId());
    assertNull(role.getName());
    assertNull(role.getDescription());
}

@Test
void settersAndGetters_shouldWorkCorrectly() {
    RoleEntity role = new RoleEntity();
    role.setId(2L);
    role.setName("USER");
    role.setDescription("Usuario final");

    assertEquals(2L, role.getId());
    assertEquals("USER", role.getName());
    assertEquals("Usuario final", role.getDescription());
}

@Test
void builder_shouldCreateObjectCorrectly() {
    RoleEntity role = RoleEntity.builder()
            .id(3L)
            .name("ASESOR")
            .description("Rol de asesor comercial")
            .build();

    assertEquals(3L, role.getId());
    assertEquals("ASESOR", role.getName());
    assertEquals("Rol de asesor comercial", role.getDescription());
}

@Test
void toBuilder_shouldCreateCopyAndModify() {
    RoleEntity role1 = RoleEntity.builder()
            .id(4L)
            .name("CLIENTE")
            .description("Rol de cliente")
            .build();

    RoleEntity role2 = role1.toBuilder()
            .name("CLIENTE VIP")
            .build();

    assertEquals(role1.getId(), role2.getId());
    assertEquals("CLIENTE VIP", role2.getName());
    assertEquals(role1.getDescription(), role2.getDescription());
}
}