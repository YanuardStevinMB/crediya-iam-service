package com.crediya.iam.r2dbc;

import com.crediya.iam.model.user.User;
import com.crediya.iam.r2dbc.entity.UserEntity;
import com.crediya.iam.r2dbc.mapper.UserEntityMapper;
import com.crediya.iam.r2dbc.roleRepository.RoleReactiveRepository;
import com.crediya.iam.r2dbc.userRepository.UserReactiveRepository;
import com.crediya.iam.r2dbc.userRepository.UserReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    private UserReactiveRepository repository;
    private UserEntityMapper userEntityMapper;
    private RoleReactiveRepository roleRepository;
    private UserReactiveRepositoryAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository = mock(UserReactiveRepository.class);
        userEntityMapper = mock(UserEntityMapper.class);
        roleRepository = mock(RoleReactiveRepository.class);
        objectMapper = mock(ObjectMapper.class);

        adapter = new UserReactiveRepositoryAdapter(repository, userEntityMapper, objectMapper, roleRepository);
    }

    @Test
    void existsByMail_shouldReturnTrueWhenExists() {
        when(repository.existsByEmail("test@mail.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByMail("TEST@mail.com"))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByEmail("test@mail.com");
    }

    @Test
    void existsByMail_shouldReturnFalseWhenNull() {
        StepVerifier.create(adapter.existsByMail(null))
                .expectNext(false)
                .verifyComplete();

        verify(repository, never()).existsByEmail(any());
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        User user = new User();
        user.setEmail("user@mail.com");

        when(repository.findByEmail("user@mail.com")).thenReturn(Mono.just(user));

        StepVerifier.create(adapter.findByEmail(" user@mail.com "))
                .expectNextMatches(u -> u.getEmail().equals("user@mail.com"))
                .verifyComplete();
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNull() {
        StepVerifier.create(adapter.findByEmail(null))
                .verifyComplete();
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void existUserForDocument_shouldMapEntityToDomain() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setIdentityDocument("123");
        entity.setEmail("doc@mail.com");

        User domain = new User();
        domain.setId(1L);
        domain.setIdentityDocument("123");
        domain.setEmail("doc@mail.com");

        when(repository.existUserForDocument("123")).thenReturn(Mono.just(entity));
        when(userEntityMapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.existUserForDocument("123"))
                .expectNextMatches(u -> u.getEmail().equals("doc@mail.com"))
                .verifyComplete();
    }

    @Test
    void existUserForDocument_shouldReturnEmptyWhenNull() {
        StepVerifier.create(adapter.existUserForDocument(null))
                .verifyComplete();
        verify(repository, never()).existUserForDocument(any());
    }

    @Test
    void save_shouldFailWhenRoleIdIsNull() {
        User user = new User();
        user.setEmail("x@mail.com");
        user.setRoleId(null);

        StepVerifier.create(adapter.save(user))
                .expectErrorMatches(err -> err instanceof IllegalArgumentException &&
                        err.getMessage().equals("El roleId es obligatorio"))
                .verify();
    }

    @Test
    void save_shouldFailWhenRoleIdDoesNotExist() {
        User user = new User();
        user.setId(1L);
        user.setEmail("x@mail.com");
        user.setRoleId(99L);

        when(roleRepository.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.save(user))
                .expectErrorMatches(err -> err instanceof IllegalArgumentException &&
                        err.getMessage().equals("El roleId no existe: 99"))
                .verify();
    }

    @Test
    void save_shouldMapAndPersistUserWhenRoleExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ok@mail.com");
        user.setRoleId(5L);

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setEmail("ok@mail.com");

        User domain = new User();
        domain.setId(1L);
        domain.setEmail("ok@mail.com");
        domain.setRoleId(5L);

        when(roleRepository.existsById(5L)).thenReturn(Mono.just(true));
        when(userEntityMapper.toEntity(user)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(userEntityMapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(user))
                .expectNextMatches(saved -> saved.getEmail().equals("ok@mail.com") && saved.getRoleId() == 5L)
                .verifyComplete();
    }
}