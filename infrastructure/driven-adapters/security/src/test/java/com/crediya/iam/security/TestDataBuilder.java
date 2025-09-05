package com.crediya.iam.security;

import com.crediya.iam.model.role.Role;
import com.crediya.iam.model.user.User;
import com.crediya.iam.usecase.authenticate.TokenResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builder class for creating test data objects with sensible defaults.
 * Follows the Test Data Builder pattern for easy test data creation.
 */
public class TestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    // User builders
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static UserBuilder aValidUser() {
        return new UserBuilder()
                .withFirstName("John")
                .withLastName("Doe")
                .withEmail("john.doe@test.com")
                .withBirthdate(LocalDate.of(1990, 1, 1))
                .withAddress("123 Test Street")
                .withPhoneNumber("300-123-4567")
                .withBaseSalary(new BigDecimal("50000.00"))
                .withIdentityDocument("CC123456789")
                .withRoleId(1L);
    }

    public static UserBuilder anAdminUser() {
        return aValidUser()
                .withEmail("admin@test.com")
                .withRoleId(1L);
    }

    public static UserBuilder aUserWithId(Long id) {
        return aValidUser().withId(id);
    }

    // Role builders
    public static RoleBuilder aRole() {
        return new RoleBuilder();
    }

    public static RoleBuilder anAdminRole() {
        return new RoleBuilder()
                .withId(1L)
                .withName("ADMIN")
                .withDescription("Administrator role");
    }

    public static RoleBuilder aUserRole() {
        return new RoleBuilder()
                .withId(2L)
                .withName("USER")
                .withDescription("Standard user role");
    }

    // Token builders
    public static TokenResultBuilder aTokenResult() {
        return new TokenResultBuilder();
    }

    public static TokenResultBuilder aValidTokenResult() {
        return new TokenResultBuilder()
                .withToken("eyJhbGciOiJIUzI1NiJ9.test.token")
                .withType("Bearer")
                .withExpiresIn(3600L);
    }

    // Builder classes
    public static class UserBuilder {
        private Long id;
        private String firstName = "Test";
        private String lastName = "User";
        private String email = "test" + ID_COUNTER.getAndIncrement() + "@test.com";
        private LocalDate birthdate = LocalDate.of(1990, 1, 1);
        private String address = "Test Address";
        private String phoneNumber = "300-000-0000";
        private BigDecimal baseSalary = new BigDecimal("30000.00");
        private String identityDocument = "CC" + ID_COUNTER.get();
        private Long roleId = 2L;
        private String password = "$2a$10$hashedPassword";

        public UserBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withBirthdate(LocalDate birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public UserBuilder withAddress(String address) {
            this.address = address;
            return this;
        }

        public UserBuilder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder withBaseSalary(BigDecimal baseSalary) {
            this.baseSalary = baseSalary;
            return this;
        }

        public UserBuilder withIdentityDocument(String identityDocument) {
            this.identityDocument = identityDocument;
            return this;
        }

        public UserBuilder withRoleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            User user = User.create(firstName, lastName, birthdate, address, phoneNumber, 
                                  email, baseSalary, identityDocument, roleId, password);
            if (id != null) {
                user.withId(id);
            }
            return user;
        }
    }

    public static class RoleBuilder {
        private Long id = ID_COUNTER.getAndIncrement();
        private String name = "TEST_ROLE";
        private String description = "Test role description";

        public RoleBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public RoleBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Role build() {
            return new Role(id, name, description);
        }
    }

    public static class TokenResultBuilder {
        private String token = "test-token";
        private String type = "Bearer";
        private Long expiresIn = 3600L;

        public TokenResultBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        public TokenResultBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public TokenResultBuilder withExpiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public TokenResult build() {
            return new TokenResult(token, type, expiresIn);
        }
    }
}
