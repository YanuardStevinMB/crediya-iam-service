package com.crediya.iam.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un usuario dentro del sistema IAM (Identity and Access Management).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthdate;
    private String identityDocument;
    private String phoneNumber;
    private BigDecimal baseSalary;
    private String address;
    private Long roleId;
    private Boolean active;
    private  String password;

    /** Fábrica de dominio (sin ID). */
    public static User create(String firstName, String lastName, LocalDate birthdate,
                              String address, String phoneNumber, String email,
                              BigDecimal baseSalary, String identityDocument,
                              Long roleId,String password
    ) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .birthdate(birthdate)
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .baseSalary(baseSalary)
                .identityDocument(identityDocument)
                .roleId(roleId)

                .active(Boolean.TRUE)
                .password(password)
                .build();
    }

    /** Devuelve la misma instancia con ID asignado (útil para mapper). */
    public User withId(Long id) {
        this.id = id;
        return this;
    }
}
