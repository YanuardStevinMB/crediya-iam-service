package com.crediya.iam.model.role.gateways;

import com.crediya.iam.model.role.Role;
import reactor.core.publisher.Mono;

public interface RoleRepository {
    Mono<Role> findById(Long id);
}
