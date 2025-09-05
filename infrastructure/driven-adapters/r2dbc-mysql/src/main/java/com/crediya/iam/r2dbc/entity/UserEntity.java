package com.crediya.iam.r2dbc.entity;

import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * UserEntity representa la entidad de usuario en la base de datos,
 * mapeada mediante Spring Data R2DBC.
 */
@Table("Usuario")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)

public class UserEntity {

    @Id
    @Column("id_usuario")
    private Long id;

    @Column("nombre")
    private String firstName;

    @Column("apellido")
    private String lastName;

    @Column("email")
    private String email;

    @Column("fecha_nacimiento")
    private LocalDate  birthdate;

    @Column("documento_identidad")
    private String identityDocument;

    @Column("telefono")
    private String phoneNumber;

    @Column("direccion")
    private String address;

    @Column("salario_base")
    private BigDecimal baseSalary;

    @Column("id_rol")
    private Long roleId;

    @Column("password")
    private String password;



}
