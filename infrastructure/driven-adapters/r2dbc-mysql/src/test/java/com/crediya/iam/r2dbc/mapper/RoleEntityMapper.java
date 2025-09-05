package com.crediya.iam.r2dbc.mapper;


import com.crediya.iam.model.role.Role;
import com.crediya.iam.r2dbc.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleEntityMapperTest {

    private RoleEntityMapper mapper;

    @BeforeEach
    void setUp() {
        // Como la interfaz solo tiene métodos default, instanciamos con clase anónima
        mapper = new RoleEntityMapper() {};
    }

    @Test
    void toEntity_shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Role domain = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrador del sistema")
                .build();

        RoleEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("ADMIN", entity.getName());
        assertEquals("Administrador del sistema", entity.getDescription());
    }

    @Test
    void toDomain_shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_shouldMapAllFields() {
        RoleEntity entity = RoleEntity.builder()
                .id(2L)
                .name("USER")
                .description("Usuario final")
                .build();

        Role domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(2L, domain.getId());
        assertEquals("USER", domain.getName());
        assertEquals("Usuario final", domain.getDescription());
    }
}