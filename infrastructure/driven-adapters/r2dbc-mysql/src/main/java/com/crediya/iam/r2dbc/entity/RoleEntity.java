package com.crediya.iam.r2dbc.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("Rol") // Nombre de la tabla en la BD
@Getter
@Setter
@Builder
public class RoleEntity {
    @Id
    @Column("id_rol")
    private Long id;
    @Column("nombre")
    private  String name;
    @Column("Description")
    private  String description;
}
