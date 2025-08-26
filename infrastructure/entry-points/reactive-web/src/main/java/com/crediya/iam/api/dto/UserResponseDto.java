package com.crediya.iam.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserResponseDto {
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
}