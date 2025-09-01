package com.crediya.iam.r2dbc.roleRepository;

import com.crediya.iam.model.role.Role;
import com.crediya.iam.model.role.gateways.RoleRepository;
import com.crediya.iam.r2dbc.entity.RoleEntity;
import com.crediya.iam.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.iam.r2dbc.mapper.RoleEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class RoleReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Role,
        RoleEntity,
        Long,
        RoleReactiveRepository
        > implements RoleRepository {

    private final RoleEntityMapper roleEntityMapper;

    public RoleReactiveRepositoryAdapter(
            RoleReactiveRepository roleRepository,
            RoleEntityMapper roleEntityMapper,
            ObjectMapper mapper
    ) {
        super(roleRepository, mapper, entity -> roleEntityMapper.toDomain(entity));
        this.roleEntityMapper = roleEntityMapper;
    }

    @Override
    public Mono<Role> findById(Long id) {
        if (id == null) return Mono.empty();

        return repository.findById(id)
                .map(roleEntityMapper::toDomain);
    }
}
