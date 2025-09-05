package com.crediya.iam.r2dbc.roleRepository;

import com.crediya.iam.model.role.Role;
import com.crediya.iam.r2dbc.entity.RoleEntity;
import com.crediya.iam.r2dbc.mapper.RoleEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleReactiveRepositoryAdapterTest {

    private RoleReactiveRepository roleReactiveRepository;
    private RoleEntityMapper roleEntityMapper;
    private ObjectMapper objectMapper;
    private RoleReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        roleReactiveRepository = mock(RoleReactiveRepository.class);
        roleEntityMapper = mock(RoleEntityMapper.class);
        objectMapper = mock(ObjectMapper.class);

        adapter = new RoleReactiveRepositoryAdapter(roleReactiveRepository, roleEntityMapper, objectMapper);
    }

    @Test
    void findById_shouldReturnEmptyWhenIdIsNull() {
        StepVerifier.create(adapter.findById(null))
                .verifyComplete();

        verify(roleReactiveRepository, never()).findById((Long) any());
    }

    @Test
    void findById_shouldReturnRoleWhenEntityExists() {
        RoleEntity entity = new RoleEntity();
        entity.setId(1L);
        entity.setName("ADMIN");

        Role domain = new Role();
        domain.setId(1L);
        domain.setName("ADMIN");

        when(roleReactiveRepository.findById(1L)).thenReturn(Mono.just(entity));
        when(roleEntityMapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findById(1L))
                .expectNextMatches(r -> r.getId().equals(1L) && r.getName().equals("ADMIN"))
                .verifyComplete();

        verify(roleReactiveRepository).findById(1L);
        verify(roleEntityMapper).toDomain(entity);
    }

    @Test
    void findById_shouldReturnEmptyWhenRepositoryReturnsEmpty() {
        when(roleReactiveRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(99L))
                .verifyComplete();

        verify(roleReactiveRepository).findById(99L);
        verify(roleEntityMapper, never()).toDomain(any());
    }
}
