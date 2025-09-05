package com.crediya.iam.model.role;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Nested
    class ConstructorTests {

        @Test
        void allArgsCtor_setsFields_withoutTrim() {
            Role role = new Role(1L, "  Admin  ", "  Operador  ");
            assertEquals(1L, role.getId());
            assertEquals("  Admin  ", role.getName());
            assertEquals("  Operador  ", role.getDescription());
        }
    }

    @Nested
    class GetterSetterTests {

        @Test
        void setters_keepValuesAsIs_noTrimOrNormalization() {
            Role role = new Role();
            role.setName("  Admin  ");
            role.setDescription("  Operador  ");

            assertEquals("  Admin  ", role.getName());
            assertEquals("  Operador  ", role.getDescription());
        }

        @Test
        void blankStrings_remainAsBlank_notEmptyString() {
            Role role = new Role();
            role.setName("   ");
            role.setDescription("   ");

            assertEquals("   ", role.getName());
            assertEquals("   ", role.getDescription());
        }

        @Test
        void nullValues_remainNull() {
            Role role = new Role();
            role.setName(null);
            role.setDescription(null);

            assertNull(role.getName());
            assertNull(role.getDescription());
        }
    }

    @Test
    void staticFactoryMethod_createsRole() {
        Role role = Role.create(5L, "Admin", "Super user");
        assertEquals(5L, role.getId());
        assertEquals("Admin", role.getName());
        assertEquals("Super user", role.getDescription());
    }
}
