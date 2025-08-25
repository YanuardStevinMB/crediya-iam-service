package com.crediya.iam.api.userMapper;

import com.crediya.iam.api.dto.UserSaveDto;
import com.crediya.iam.model.user.User;
import com.crediya.iam.shared.mappers.DateMapper;
import com.crediya.iam.shared.mappers.EmailMapper;
import com.crediya.iam.shared.mappers.SalaryMapper;
import org.mapstruct.*;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {
                EmailMapper.class,
                SalaryMapper.class,
                DateMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    // Manual factory-based mapping due to domain factory method
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
                dto.roleId()        // Long
        );
    }
}

