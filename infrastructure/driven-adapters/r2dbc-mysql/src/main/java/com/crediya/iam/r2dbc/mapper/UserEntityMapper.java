package com.crediya.iam.r2dbc.mapper;


import com.crediya.iam.model.user.User;
import com.crediya.iam.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    default UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .phoneNumber(domain.getPhoneNumber())
                .birthdate(domain.getBirthdate())
                .address(domain.getAddress())
                .identityDocument(domain.getPhoneNumber())
                .email(domain.getEmail())
                .baseSalary(domain.getBaseSalary() )
                .roleId(domain.getRoleId() )
                .password(domain.getPassword())
                .build();
    }

    default User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = User.create(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getBirthdate(),
                entity.getAddress(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getBaseSalary(),
                entity.getIdentityDocument(),
                entity.getRoleId(),
                entity.getPassword()
        );

        return user.withId(entity.getId());
    }
}