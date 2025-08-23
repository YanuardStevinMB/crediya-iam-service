package com.crediya.iam.r2dbc.entity;

import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * UserEntity representa la entidad de usuario en la base de datos,
 * mapeada mediante Spring Data R2DBC.
 */
@Table("Usuario")
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @Column("id_usuario")
    private Long id; // Identificador único del usuario

    @Column("nombre")
    private String firstName;

    @Column("apellido")
    private String lastName;

    @Column("email")

    private String email;

    @Column("fecha_nacimiento")
    private LocalDate birthdate;

    @Column("documento_identidad")
    private String identityDocument;

    @Column("telefono")
    private String phoneNumber;

    @Column("salario_base")
    private BigDecimal baseSalary;

    @Column("id_rol")
    private Long roleId;


}
