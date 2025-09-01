package com.crediya.iam.api.userMapper;

import com.crediya.iam.api.dto.UserResponseDto;
import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


import java.util.UUID;

@Mapper(
        componentModel = "spring",

        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    default User toModel(UserSaveDto dto) {
        if (dto == null) return null;
        return User.create(
              // Long
                dto.firstName(),
                dto.lastName(),
                dto.birthdate(),
                dto.address(),
                dto.phoneNumber(),
                dto.email(),

                dto.baseSalary(),
                dto.identityDocument(),
                dto.roleId(),
                dto.password()// Long
        );
    }

    UserResponseDto toResponseDto(User model);

}

