package com.crediya.iam.model.role;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Role {

    private Long id;
    private String name;
    private String description;

    /**
     * Método de fábrica para crear instancias de Role.
     */
    public static Role create(Long id, String name, String description) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        return role;
    }
}
