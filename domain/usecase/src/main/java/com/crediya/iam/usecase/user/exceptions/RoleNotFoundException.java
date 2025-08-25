package com.crediya.iam.usecase.user.exceptions;

public class RoleNotFoundException extends RuntimeException {
    private final Long roleId;
    public RoleNotFoundException(Long roleId) {
        super("El rol no existe");
        this.roleId = roleId;
    }
    public Long getRoleId() { return roleId; }
    public String getCode() { return "ROLE_NOT_FOUND"; }


}
