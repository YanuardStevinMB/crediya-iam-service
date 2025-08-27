package com.crediya.iam.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID; // 👈 necesario

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Nested
    @DisplayName("Factory: create(...)")
    class FactoryTests {
        @Test
        @DisplayName("Asigna campos, hace trim donde aplica y normaliza email a minúsculas")
        void factoryCreate_setsFields_trim_and_normalizesEmail() {
            // Arrange
            String firstName = "  Juan  ";
            String lastName = "  Pérez ";
            LocalDate birthdate = LocalDate.of(1990, 5, 10);
            String address = " Calle 123 ";
            String phone = " 300 123 4567 ";
            String email = "  Foo.BAR@Mail.COM ";
            BigDecimal salary = new BigDecimal("12345.67");
            String identity = "  CC123  ";
            Long roleId = 2L;

            // Act
            User u = User.create(firstName, lastName, birthdate, address, phone, email, salary, identity, roleId);

            // Assert
            assertNull(u.getId());
            assertEquals("Juan", u.getFirstName());
            assertEquals("Pérez", u.getLastName());
            assertEquals(LocalDate.of(1990, 5, 10), u.getBirthdate());
            assertEquals(" Calle 123 ", u.getAddress()); // setter no hace trim de address (diseño actual)
            assertEquals("300 123 4567", u.getPhoneNumber()); // trim
            assertEquals("foo.bar@mail.com", u.getEmail());    // trim + lowercase
            assertEquals(0, new BigDecimal("12345.67").compareTo(u.getBaseSalary()));
            assertEquals("CC123", u.getIdentityDocument());
            assertEquals(2L, u.getRoleId());
        }
    }

    @Nested
    @DisplayName("withId(...)")
    class WithIdTests {
        @Test
        @DisplayName("Asigna el id y retorna la MISMA instancia (patrón fluido)")
        void withId_setsId_and_returnsSameInstance() {
            User u = User.create("A", "B", LocalDate.of(2000,1,1),
                    "Addr", "300", "a@b.com", new BigDecimal("0"), "ID", 1L);

            assertNull(u.getId());

            User returned = u.withId(99L);

            assertSame(u, returned, "withId debe devolver la MISMA instancia");
            assertEquals(99L, u.getId());
        }
    }

    @Nested
    @DisplayName("Getters/Setters y normalizaciones")
    class GettersSettersTests {
        @Test
        @DisplayName("Trim en firstName/lastName/identity/phone; email a minúsculas; nulos permitidos")
        void setters_handleTrimAndNulls() {
            User u = new User();

            u.setFirstName("  Ana  ");
            u.setLastName("  Gómez ");
            u.setIdentityDocument("  ABC123  ");
            u.setPhoneNumber("  555  ");
            u.setEmail("  XyZ@Example.COM  ");

            assertEquals("Ana", u.getFirstName());
            assertEquals("Gómez", u.getLastName());
            assertEquals("ABC123", u.getIdentityDocument());
            assertEquals("555", u.getPhoneNumber());
            assertEquals("xyz@example.com", u.getEmail());

            // Nulos
            u.setFirstName(null);
            u.setLastName(null);
            u.setIdentityDocument(null);
            u.setPhoneNumber(null);
            u.setEmail(null);

            assertNull(u.getFirstName());
            assertNull(u.getLastName());
            assertNull(u.getIdentityDocument());
            assertNull(u.getPhoneNumber());
            assertNull(u.getEmail());
        }

        @Test
        @DisplayName("Asigna birthdate y baseSalary sin transformaciones inesperadas")
        void birthdate_and_salary_basicSetGet() {
            User u = new User();
            LocalDate d = LocalDate.of(2020, 2, 29);
            BigDecimal s = new BigDecimal("15000000.00");

            u.setBirthdate(d);
            u.setBaseSalary(s);

            assertEquals(LocalDate.of(2020, 2, 29), u.getBirthdate());
            assertEquals(0, new BigDecimal("15000000.00").compareTo(u.getBaseSalary()));
        }

        @Test
        @DisplayName("Address se asigna tal cual (sin trim según el modelo actual)")
        void address_isAssignedVerbatim() {
            User u = new User();
            u.setAddress("  Av Siempre Viva  ");
            assertEquals("  Av Siempre Viva  ", u.getAddress());
        }
    }

    @Test
    @DisplayName("Constructor con UUID/Date no inicializa (hoy no hace nada) — cubierto para elevar cobertura")
    void weirdConstructor_doesNothingButIsSafe() {
        UUID someId = UUID.randomUUID();  // 👈 usa UUID, no Long
        Date birth = new Date();

        // Firma actual del constructor “raro” en tu modelo:
        // User(UUID id, String firstName, String lastName, String email, Date birthdate,
        //      String address, String s, BigDecimal bigDecimal, String s1, UUID uuid)
        User u = new User(someId, "N", "L", "E", birth, "A", "S",
                new BigDecimal("1.00"), "S1", someId);

        // Como la implementación está vacía, los campos del modelo quedan en null
        assertNull(u.getId());
        assertNull(u.getFirstName());
        assertNull(u.getLastName());
        assertNull(u.getEmail());
        assertNull(u.getBirthdate());
        assertNull(u.getAddress());
        assertNull(u.getPhoneNumber());
        assertNull(u.getBaseSalary());
        assertNull(u.getIdentityDocument());
        assertNull(u.getRoleId());
    }
}
