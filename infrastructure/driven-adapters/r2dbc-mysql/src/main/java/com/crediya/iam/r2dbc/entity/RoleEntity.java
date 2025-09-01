package com.crediya.iam.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("Rol")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @Column("id_rol")
    private Long id;

    @Column("nombre")
    private String name;

    @Column("description") // 👈 ojo con mayúscula/minúscula, debería coincidir con la BD
    private String description;
}
