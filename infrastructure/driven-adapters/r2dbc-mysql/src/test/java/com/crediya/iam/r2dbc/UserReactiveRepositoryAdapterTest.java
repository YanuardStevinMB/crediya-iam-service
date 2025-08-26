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

    @Mock
    UserReactiveRepository repository;

    @Mock
    RoleReactiveRepository roleRepository;

    @Mock
    UserEntityMapper userEntityMapper;

    @Mock
    ObjectMapper mapper;

    @InjectMocks
    UserReactiveRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new UserReactiveRepositoryAdapter(repository, userEntityMapper, mapper, roleRepository);
    }

    private User buildUser(String email, Long roleId) {
        return User.create(
                "Ana", "García",
                LocalDate.of(1990, 5, 10),
                "Calle 123",
                "3001234567",
                email,
                new BigDecimal("1200.50"),
                "CC1",
                roleId
        );
    }

    // ---------- existsByMail -----------

    @Test
    void existsByMail_null_returnsFalse() {
        StepVerifier.create(adapter.existsByMail(null))
                .expectNext(false)
                .verifyComplete();
        verifyNoInteractions(repository);
    }

    @Test
    void existsByMail_normalizesToLowercaseAndTrim() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByMail("  Ana@Example.com  "))
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(1)).existsByEmail("ana@example.com");
    }

    // ---------- save -----------

    @Test
    void save_roleIdNull_errors() {
        var u = buildUser("ana@example.com", null);

        StepVerifier.create(adapter.save(u))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("roleId es obligatorio"))
                .verify();

        verifyNoInteractions(repository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void save_roleNotExists_errors() {
        var u = buildUser("ana@example.com", 9L);
        when(roleRepository.existsById(9L)).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.save(u))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("roleId no existe"))
                .verify();

        verify(roleRepository, times(1)).existsById(9L);
        verifyNoInteractions(repository);
    }

    @Test
    void save_roleExists_mapsAndPersists_ok() {
        var u = buildUser("ana@example.com", 1L);
        var entity = new UserEntity(); // usa tu entity real
        var persisted = new UserEntity(); // simula retorno del repo
        var mappedDomain = buildUser("ana@example.com", 1L).withId(100L);

        when(roleRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(userEntityMapper.toEntity(u)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(persisted));
        when(userEntityMapper.toDomain(persisted)).thenReturn(mappedDomain);

        StepVerifier.create(adapter.save(u))
                .expectNextMatches(saved -> saved.getId() != null && saved.getId().equals(100L))
                .verifyComplete();

        verify(roleRepository, times(1)).existsById(1L);
        verify(repository, times(1)).save(entity);
        verify(userEntityMapper, times(1)).toDomain(persisted);
    }
}
