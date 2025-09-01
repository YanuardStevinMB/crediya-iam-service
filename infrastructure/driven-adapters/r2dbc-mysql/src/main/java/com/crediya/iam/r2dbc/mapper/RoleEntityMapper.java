package com.crediya.iam.r2dbc.mapper;


import com.crediya.iam.model.role.Role;
import com.crediya.iam.r2dbc.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {

    default RoleEntity toEntity(Role domain) {
        if (domain == null) return null;
        return RoleEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }

    default Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
