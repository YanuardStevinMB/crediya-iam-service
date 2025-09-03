# Testing Strategy - IAM Service

## Resumen

Esta documentación describe la estrategia completa de testing implementada para el servicio IAM, siguiendo los principios de Clean Architecture y mejores prácticas de testing en Spring Boot con WebFlux.

## Arquitectura de Testing

### Pirámide de Testing

```
                🔺
              /     \
            /   E2E   \    <- Tests End-to-End (Pocos, pero críticos)
          /             \
        /   Integration   \  <- Tests de Integración (Moderados)
      /                   \
    /      Unit Tests      \ <- Tests Unitarios (Muchos, rápidos)
  /_________________________\
```

### Layers de Testing

1. **Unit Tests (Dominio)**: Tests rápidos y aislados para lógica de negocio
2. **Integration Tests (Infraestructura)**: Tests con dependencias externas (DB, etc.)
3. **End-to-End Tests**: Tests de flujos completos de la aplicación
4. **Architecture Tests**: Validación de reglas de Clean Architecture

## Estructura de Tests

```
src/test/java/
├── com/crediya/iam/
│   ├── integration/              # Tests E2E
│   │   └── AuthenticationEndToEndTest.java
│   ├── ArchitectureTest.java     # Reglas arquitecturales
│   └── shared/                   # Utilidades de test
│       ├── TestConfiguration.java
│       └── TestDataBuilder.java
│
domain/
├── model/src/test/               # Tests de entidades
│   ├── user/UserTest.java
│   └── role/RoleTest.java
└── usecase/src/test/             # Tests de casos de uso
    ├── authenticate/AuthenticateUseCaseTest.java
    └── user/CreateUserUseCaseExtendedTest.java

infrastructure/
├── driven-adapters/
│   ├── r2dbc-mysql/src/test/     # Tests de repositorios
│   │   └── UserReactiveRepositoryAdapterIntegrationTest.java
│   └── security/src/test/        # Tests de seguridad
│       └── jwt/JwtTokenGeneratorAdapterTest.java
└── helpers/shared/src/test/      # Configuración común
    ├── TestConfiguration.java
    └── TestDataBuilder.java
```

## Tipos de Tests Implementados

### 1. Tests Unitarios de Dominio

**Ubicación**: `domain/model/src/test/` y `domain/usecase/src/test/`

**Características**:
- Testing aislado sin dependencias externas
- Uso de mocks para puertos/gateways
- Verificación de lógica de negocio pura
- Cobertura completa de casos edge

**Ejemplo**:
```java
@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseExtendedTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordService passwordService;
    // ...
}
```

### 2. Tests de Integración de Infraestructura

**Ubicación**: `infrastructure/*/src/test/`

**Características**:
- Uso de Testcontainers para bases de datos reales
- Tests de adapters que implementan puertos del dominio
- Validación de mappers y conversiones
- Testing de configuración específica de infraestructura

**Ejemplo**:
```java
@DataR2dbcTest
@Import(TestConfiguration.class)
class UserReactiveRepositoryAdapterIntegrationTest {
    // Tests con MySQL real usando Testcontainers
}
```

### 3. Tests End-to-End

**Ubicación**: `applications/app-service/src/test/java/integration/`

**Características**:
- WebTestClient para simular requests HTTP reales
- Base de datos completa con Testcontainers
- Validación de flujos completos de usuario
- Testing de casos de error y validaciones

**Ejemplo**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthenticationEndToEndTest {
    // Tests de flujos completos crear usuario -> autenticar
}
```

### 4. Tests de Arquitectura

**Ubicación**: `applications/app-service/src/test/java/ArchitectureTest.java`

**Características**:
- ArchUnit para validar reglas de Clean Architecture
- Verificación de dependencias entre capas
- Validación de patrones de naming
- Detección de violaciones arquitecturales

**Reglas Implementadas**:
- ✅ Dominio no debe depender de infraestructura
- ✅ Entidades de dominio sin anotaciones de Spring
- ✅ UseCases solo con dependencias permitidas
- ✅ Adapters implementan puertos del dominio
- ✅ Exceptions en la capa correcta

## Configuración y Herramientas

### TestContainers

Configurado en `TestConfiguration.java`:
- MySQL 8.0.37 para tests de integración
- Configuración compartida y reutilizable
- Contenedores con reuse habilitado para performance

### Test Data Builder

Patrón implementado en `TestDataBuilder.java`:
```java
User user = TestDataBuilder.aValidUser()
    .withEmail("test@example.com")
    .withRoleId(1L)
    .build();
```

### Coverage y Reporting

**Jacoco**: Configurado para reportes de cobertura
- Reports HTML en `build/reports/jacocoHtml/`
- Reports XML para CI/CD
- Threshold mínimo configurable

**PITest**: Mutation testing habilitado
- Detecta tests débiles que no capturan mutaciones
- Reports en `build/reports/pitest/`

**SonarQube**: Integración preparada
- Análisis de calidad de código
- Tracking de tech debt
- Métricas de maintainability

## Ejecutar Tests

### Localmente

**Windows**:
```bash
run-tests.bat
```

**Linux/macOS**:
```bash
chmod +x run-tests.sh
./run-tests.sh
```

**Manualmente**:
```bash
# Solo tests
./gradlew test

# Tests + coverage
./gradlew test jacocoMergedReport

# Tests + coverage + mutation
./gradlew test jacocoMergedReport pitestReportAggregate
```

### CI/CD

Para pipelines de CI, usar:
```bash
./gradlew clean test jacocoMergedReport --no-daemon
```

## Mejores Prácticas Implementadas

### Naming Conventions
- Tests terminan en `Test`
- Tests de integración incluyen `Integration` en el nombre
- Tests E2E incluyen `EndToEnd` en el nombre
- Métodos descriptivos: `shouldFailAuthentication_withWrongCredentials()`

### Organización
- `@Nested` classes para agrupar tests relacionados
- `@DisplayName` para descripciones claras en español
- Separación clara entre Arrange/Act/Assert

### Test Data Management
- TestDataBuilder para objetos complejos
- Datos realistas pero no sensibles
- Setup y teardown apropiados

### Error Testing
- Testing explícito de casos de error
- Validación de messages y códigos de error
- Testing de edge cases y boundary conditions

## Métricas y Coverage

### Targets de Coverage
- **Líneas**: >80%
- **Branches**: >70%
- **Métodos**: >85%

### Exclusiones de Coverage
```java
@ExcludeFromCodeCoverage // Para DTOs y POJOs simples
```

### Reporting
- HTML reports para desarrollo local
- XML reports para CI/CD integration
- JSON reports para tooling adicional

## Troubleshooting

### Problemas Comunes

**Tests lentos**:
- Verificar reutilización de TestContainers
- Usar perfiles de test específicos
- Limitar scope de @SpringBootTest

**Flaky tests**:
- StepVerifier.withVirtualTime() para tests reactivos
- Timeouts apropiados
- Cleanup de estado entre tests

**Memory issues**:
- Configurar heap size: `./gradlew test -Xmx2g`
- Fork per test class si necesario
- Profile memory usage

## Referencias

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)
- [Reactor Test](https://projectreactor.io/docs/test/release/api/)
- [TestContainers](https://www.testcontainers.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [Clean Architecture Testing](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
