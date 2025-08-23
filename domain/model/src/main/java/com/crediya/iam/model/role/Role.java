package com.crediya.iam.model.role;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa un rol dentro del sistema IAM (Identity and Access Management).
 */
public final class Role {

    private Long id; // Identificador único del rol
    private String name;
    private String description;

    // ====== Constructores ======
    public Role() {}

    public Role(Long id, String name, String description) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.description = description != null ? description.trim() : null;
    }

    // ====== Getters y Setters ======
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }


}
