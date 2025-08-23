package com.crediya.iam.r2dbc.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleDto {

    private Long id;

    private  String name;

    private  String description;
}
