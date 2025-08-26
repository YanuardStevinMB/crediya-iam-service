package com.crediya.iam.r2dbc.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@Builder
public class UserDto {


    private Long id;


    private String firstName;


    private String lastName;



    private String email;


    private LocalDate  birthdate;


    private String identityDocument;


    private String phoneNumber;


    private BigDecimal baseSalary;


    private Long roleId;
}
