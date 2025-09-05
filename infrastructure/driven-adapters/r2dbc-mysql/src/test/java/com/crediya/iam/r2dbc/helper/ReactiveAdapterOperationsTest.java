package com.crediya.iam.r2dbc.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class ReactiveAdapterOperationsTest {

    static class Domain {
        Long id;
        String name;

        Domain(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class Data {
        Long id;
        String name;

        Data(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    interface TestRepository extends ReactiveCrudRepository<Data, Long>, ReactiveQueryByExampleExecutor<Data> {}

    private TestRepository repository;
    private ObjectMapper mapper;
    private ReactiveAdapterOperations<Domain, Data, Long, TestRepository> adapter;

    @BeforeEach
    void setUp() {
        repository = mock(TestRepository.class);
        mapper = mock(ObjectMapper.class);

        adapter = new ReactiveAdapterOperations<>(repository, mapper, data -> new Domain(data.id, data.name)) {};
    }

    @Test
    void save_shouldMapAndReturnEntity() {
        Domain domain = new Domain(1L, "John");
        Data data = new Data(1L, "John");

        when(mapper.map(domain, Data.class)).thenReturn(data);
        when(repository.save(data)).thenReturn(Mono.just(data));

        StepVerifier.create(adapter.save(domain))
                .expectNextMatches(d -> d.id.equals(1L) && d.name.equals("John"))
                .verifyComplete();

        verify(repository).save(data);
    }

    @Test
    void saveAllEntities_shouldMapAndReturnEntities() {
        Domain d1 = new Domain(1L, "A");
        Domain d2 = new Domain(2L, "B");
        Data e1 = new Data(1L, "A");
        Data e2 = new Data(2L, "B");

        when(mapper.map(d1, Data.class)).thenReturn(e1);
        when(mapper.map(d2, Data.class)).thenReturn(e2);
        when(repository.saveAll(any(Flux.class))).thenReturn(Flux.just(e1, e2));

        StepVerifier.create(adapter.saveAllEntities(Flux.just(d1, d2)))
                .expectNextMatches(x -> x.id.equals(1L) && x.name.equals("A"))
                .expectNextMatches(x -> x.id.equals(2L) && x.name.equals("B"))
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEntity() {
        Data data = new Data(10L, "Jane");

        when(repository.findById(10L)).thenReturn(Mono.just(data));

        StepVerifier.create(adapter.findById(10L))
                .expectNextMatches(d -> d.id.equals(10L) && d.name.equals("Jane"))
                .verifyComplete();
    }

    @Test
    void findByExample_shouldReturnEntities() {
        Domain probe = new Domain(1L, "X");
        Data probeData = new Data(1L, "X");
        Data found = new Data(2L, "Y");

        when(mapper.map(probe, Data.class)).thenReturn(probeData);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(found));

        StepVerifier.create(adapter.findByExample(probe))
                .expectNextMatches(d -> d.id.equals(2L) && d.name.equals("Y"))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnEntities() {
        Data d1 = new Data(1L, "AA");
        Data d2 = new Data(2L, "BB");

        when(repository.findAll()).thenReturn(Flux.just(d1, d2));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(x -> x.id.equals(1L))
                .expectNextMatches(x -> x.id.equals(2L))
                .verifyComplete();
    }
}
