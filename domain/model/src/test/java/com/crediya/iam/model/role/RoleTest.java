package com.crediya.iam.model.role;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleTest {

    @Nested
    @DisplayName("Constructores")
    class ConstructorTests {

        @Test
        @DisplayName("Ctor vacío deja todos los campos en null")
        void noArgsCtor_initializesNulls() {
            Role r = new Role();
            assertNull(r.getId());
            assertNull(r.getName());
            assertNull(r.getDescription());
        }

        @Test
        @DisplayName("Ctor con args asigna campos y hace trim en name/description")
        void allArgsCtor_setsFields_andTrims() {
            Role r = new Role(10L, "  Admin  ", "  Rol del sistema  ");

            assertEquals(10L, r.getId());
            assertEquals("Admin", r.getName());
            assertEquals("Rol del sistema", r.getDescription());
        }
    }

    @Nested
    @DisplayName("Getters/Setters y normalizaciones")
    class GetterSetterTests {

        @Test
        @DisplayName("setName/setDescription hacen trim; nulos se conservan como null")
        void setters_trimAndHandleNulls() {
            Role r = new Role();

            r.setName("  Operador  ");
            r.setDescription("  Puede gestionar usuarios  ");

            assertEquals("Operador", r.getName());
            assertEquals("Puede gestionar usuarios", r.getDescription());

            r.setName(null);
            r.setDescription(null);

            assertNull(r.getName());
            assertNull(r.getDescription());
        }

        @Test
        @DisplayName("Cadenas en blanco quedan como \"\" tras trim (no null)")
        void blankStrings_becomeEmptyString_afterTrim() {
            Role r = new Role();

            r.setName("   ");
            r.setDescription("   ");

            assertEquals("", r.getName());
            assertEquals("", r.getDescription());
        }

        @Test
        @DisplayName("setId asigna correctamente el identificador")
        void setId_setsId() {
            Role r = new Role();
            assertNull(r.getId());

            r.setId(1L);
            assertEquals(1L, r.getId());

            r.setId(999L);
            assertEquals(999L, r.getId());
        }
    }
}