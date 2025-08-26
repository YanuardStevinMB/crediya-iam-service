package com.crediya.iam.model.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un usuario dentro del sistema IAM (Identity and Access Management).
 */
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate   birthdate;
    private String identityDocument;
    private String phoneNumber;
    private BigDecimal baseSalary;
    private String address;
    private Long roleId;

    // ====== Constructores ======
    public User() { }

    public User(Long id, String firstName, String lastName, String email,
                String identityDocument, String phoneNumber,
                BigDecimal baseSalary, LocalDate birthdate, Long roleId, String address) {
        this.id = id;
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setIdentityDocument(identityDocument);
        setPhoneNumber(phoneNumber);
        this.baseSalary = baseSalary;
        this.birthdate = birthdate;
        this.roleId = roleId;
        this.address = address;
    }

    public User(UUID id, String firstName, String lastName, String email, Date birthdate, String address, String s, BigDecimal bigDecimal, String s1, UUID uuid) {
    }

    /** Fábrica de dominio (sin ID). */
    public static User create(String firstName, String lastName, LocalDate  birthdate,
                              String address, String phoneNumber, String email,
                              BigDecimal baseSalary, String identityDocument, Long roleId) {
        User u = new User();

        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setBirthdate(birthdate);
        u.setAddress(address);
        u.setPhoneNumber(phoneNumber);
        u.setEmail(email);
        u.setBaseSalary(baseSalary);
        u.setIdentityDocument(identityDocument);
        u.setRoleId(roleId);
        return u;
    }

    /** Devuelve la misma instancia con ID asignado (útil para mapper). */
    public User withId(Long id) {
        this.id = id;
        return this;
    }

    // ====== Getters y Setters ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : null;
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : null;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public LocalDate  getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate  birthdate) { this.birthdate = birthdate; }

    public String getIdentityDocument() { return identityDocument; }
    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument != null ? identityDocument.trim() : null;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }

    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }


}
