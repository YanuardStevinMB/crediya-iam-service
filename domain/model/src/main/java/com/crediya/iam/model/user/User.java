package com.crediya.iam.model.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.Objects;

/**
 * Representa un usuario dentro del sistema IAM (Identity and Access Management).
 */
public class User {

    private Long id; // Identificador único del usuario
    private String firstName;
    private String lastName;
    private String email;
    private Date birthdate;
    private String identityDocument;
    private String phoneNumber;
    private BigDecimal baseSalary;
    private UUID roleId;

    // ====== Constructores ======
    public User() {}

    public User(Long id, String firstName, String lastName, String email,
                String identityDocument, String phoneNumber,
                BigDecimal baseSalary, Date birthdate,UUID roleId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.email = email;
        this.identityDocument = identityDocument;
        this.phoneNumber = phoneNumber;
        this.baseSalary = baseSalary;
        this.roleId = roleId;
    }

    // ====== Getters y Setters ======
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : null;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument != null ? identityDocument.trim() : null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
}
