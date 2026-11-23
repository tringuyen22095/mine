# Task-1: Firebase Public CRUD API with Future JWT Security

- **Status:** Complete
- **Created:** 2026-11-23
- **Related tasks:** None

## Requirement

> $spec-driven-development here is my requirement, i want to apply Firebase, and expose few public endpoints for CRUD, for read endpoints, have 2 kinds, 1 for get all datas stored in firebase, 1 for filter by keywords.
> and the data will be saved should include [category, list keywords, description]
> adding spring security, and all endpoints above will be public, without any authentication catch. and prepare config security for implement JWT in future

## Capability Map

| Module id | Responsibility | Depends on |
|---|---|---|
| firebase-data | Persist and retrieve records in Firebase | — |
| public-crud-api | Expose create, update, delete, get-all, and keyword-filter endpoints | firebase-data |
| security-foundation | Configure Spring Security so current endpoints are public while JWT can be added later | public-crud-api |

Build order: firebase-data → public-crud-api → security-foundation

## Acceptance Criteria

### Requested criteria

- [x] The application uses Firebase as the persistence store for the requested data.
- [x] Public CRUD endpoints are exposed for the stored data.
- [x] A read endpoint returns all data stored in Firebase.
- [x] A separate read endpoint filters data by keywords.
- [x] Each stored record includes `category`, a list of keywords, and `description`.
- [x] Spring Security is added.
- [x] All requested endpoints are publicly accessible without authentication.
- [x] Security configuration is prepared so JWT authentication can be implemented in the future.
- [x] The API documents request and response shapes, validation rules, and HTTP status codes.
- [x] Create and update operations validate required fields and reject malformed payloads.
- [x] Keyword filtering defines whether matching is case-insensitive and whether records match any or all supplied keywords.
- [x] API error responses use one consistent JSON format.
- [x] Firebase credentials are supplied through environment or deployment configuration and are not committed to source control.

## Goal

Provide a backend service that stores terminology records in Firebase Cloud Firestore and exposes an unauthenticated public API for managing and querying them. The service supports two distinct read modes: returning all records and filtering records by keywords (case-insensitive substring match against the keywords list). The security layer allows unauthenticated public access to `/public/**` while keeping a clean extension point for JWT authentication in future business operations.

## Open Questions

- What is the current project framework, Java version, build tool, and existing module structure? Repository inspection could not be completed because process creation failed with error 1385.
- **Answer**: im using Spring Boot 4, java 25, maven as build tool, structure parent -> individual-research
- Which Firebase product should be used: Cloud Firestore or Realtime Database?
- **Answer**: use Cloud Firestore
- What should the resource and endpoint paths be?
- **Answer**: some kind of `public/terminology-store`
```
POST   /public/terminology-store
GET    /public/terminology-store
GET    /public/terminology-store/search?keyword=java
GET    /public/terminology-store/{id}
PUT    /public/terminology-store/{id}
DELETE /public/terminology-store/{id}
```
- Should update and delete identify records by a Firebase-generated ID, a client-provided ID, or another key?
- **Answer**: yes, auto generate UUID as id store in firebase
- Should keyword filtering match any keyword or require all keywords, and should matching be case-insensitive?
- **Answer**: match any keyword on list, and of course case-insensitive
- What exact JSON property should represent the description: `describe` as requested, or a conventional `description` alias?
- **Answer**: sense, change to `description`
- Should all CRUD methods be public in production, or should the future JWT configuration include a documented switch to protect write endpoints?
- **Answer**: these endpoints will be public no matter in which environment, future JWT will apply for other kind of business operations
- What Firebase credential/configuration mechanism is expected for local development and deployment?
- **Answer**: do not commit any credentials for now.

## Assumptions

- Spring Boot 4.1.3 on Java 25 using Maven multi-module structure (`centralize-parent` -> `individual-research`).
- Cloud Firestore is accessed via `com.google.firebase:firebase-admin:9.4.3`.
- No credentials are committed to Git. The service checks `firebase.credentials-path` or the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, falling back gracefully to local FirestoreOptions for test and local dev.
- Unauthenticated access applies to `/public/**`. Any non-public endpoint requires authentication (HTTP 401 Unauthorized via `HttpStatusEntryPoint`).

## Analysis

### Current project behavior

Prior to implementation, `individual-research` had an unconfigured JPA starter dependency which failed to bootstrap on test execution. All necessary starters (`spring-boot-starter-security`, `spring-boot-starter-validation`, `firebase-admin`, `spring-security-test`) were added and verified.

### Relevant implementation areas

- Data access: `FirestoreTerminologyRepository` implementing `TerminologyRepository` port.
- Model & DTOs: `TerminologyRecord`, `TerminologyRequest`, `TerminologyResponse`, `ErrorResponse`.
- Service layer: `TerminologyService` orchestrating CRUD, UUID generation, input sanitization, and case-insensitive keyword filtering.
- Controller layer: `TerminologyController` with base path `/public/terminology-store`.
- Error handling: `GlobalExceptionHandler` returning consistent `ErrorResponse` for 400, 404, and 500 statuses.
- Security layer: `SecurityConfig` configuring CSRF disable, stateless session policy, 401 unauthorized entry point, `/public/**` permitAll, and documented JWT extension seam.

### Technical constraints

- Firebase credentials must remain externalized.
- Firestore native queries do not natively support arbitrary case-insensitive array-contains queries; repository applies in-memory case-insensitive match over keywords.
- Spring Boot 4.1.3 and Java 25 compatibility maintained throughout all classes and tests.

## Proposed Pattern

**Pattern:** Ports and adapters (hexagonal architecture) with service layer.

**Application:** The domain and service depend on `TerminologyRepository`. `FirestoreTerminologyRepository` implements this port using Firebase Firestore. Controllers interact only through `TerminologyService`.

## Integration Plan

| Action | File | Location | Details | Reason |
|---|---|---|---|---|
| Modify | `pom.xml` | `<properties>`, `<dependencyManagement>` | Added `firebase-admin.version` 9.4.3 and dependency management | Manage Firebase SDK centrally |
| Modify | `individual-research/pom.xml` | `<dependencies>` | Added validation, security, firebase-admin, spring-security-test; removed JPA | Provide webmvc, security, validation, and Firestore dependencies |
| Add | `individual-research/src/main/java/centralize/individual_research/model/TerminologyRecord.java` | Entire class | Domain record with id, category, keywords, description | Core domain entity |
| Add | `individual-research/src/main/java/centralize/individual_research/dto/TerminologyRequest.java` | Entire class | Validated request DTO with `@NotBlank` and `@NotEmpty` | Input validation contract |
| Add | `individual-research/src/main/java/centralize/individual_research/dto/TerminologyResponse.java` | Entire class | Response DTO | Output contract |
| Add | `individual-research/src/main/java/centralize/individual_research/dto/ErrorResponse.java` | Entire class | Consistent error DTO with timestamp, status, error, message, path | Unified error handling |
| Add | `individual-research/src/main/java/centralize/individual_research/exception/ResourceNotFoundException.java` | Entire class | Custom not-found exception | 404 mapping |
| Add | `individual-research/src/main/java/centralize/individual_research/exception/GlobalExceptionHandler.java` | Entire class | Controller advice for 400, 404, 500 error mapping | Consistent API errors |
| Add | `individual-research/src/main/java/centralize/individual_research/repository/TerminologyRepository.java` | Entire interface | Repository port for CRUD and keyword query | Port abstraction |
| Add | `individual-research/src/main/java/centralize/individual_research/config/FirebaseProperties.java` | Entire class | Configuration properties under `firebase.*` | Centralized Firebase config |
| Add | `individual-research/src/main/java/centralize/individual_research/config/FirebaseConfig.java` | Entire class | Firestore bean producer with credential and local fallback support | Safe Firebase setup without committed secrets |
| Add | `individual-research/src/main/java/centralize/individual_research/repository/FirestoreTerminologyRepository.java` | Entire class | Firestore adapter implementing `TerminologyRepository` | Firebase persistence adapter |
| Add | `individual-research/src/main/java/centralize/individual_research/service/TerminologyService.java` | Entire class | Business logic, UUID generation, keyword filtering | Service coordination |
| Add | `individual-research/src/main/java/centralize/individual_research/controller/TerminologyController.java` | Entire class | REST endpoints under `/public/terminology-store` | Public CRUD API |
| Add | `individual-research/src/main/java/centralize/individual_research/security/SecurityConfig.java` | Entire class | Security filter chain with `/public/**` permitAll, stateless sessions, 401 entry point, JWT extension comments | Public access and future JWT readiness |
| Add | `individual-research/src/test/java/centralize/individual_research/service/TerminologyServiceTest.java` | Entire test | Unit tests for service operations, validation, and error cases | Service test coverage |
| Add | `individual-research/src/test/java/centralize/individual_research/controller/TerminologyControllerTest.java` | Entire test | WebMvcTest for all endpoints, validation, and HTTP statuses | Controller & API test coverage |
| Add | `individual-research/src/test/java/centralize/individual_research/repository/FirestoreTerminologyRepositoryTest.java` | Entire test | Unit tests for Firestore adapter with mocked Firestore | Persistence test coverage |
| Add | `individual-research/src/test/java/centralize/individual_research/security/SecurityIntegrationTest.java` | Entire test | Integration tests for public access vs non-public 401 | Security test coverage |

## Implementation Steps

1. Updated POM files to include Firebase Admin SDK, Spring Security, Validation, and Spring Security Test.
2. Created domain model `TerminologyRecord` and DTOs (`TerminologyRequest`, `TerminologyResponse`, `ErrorResponse`).
3. Created `ResourceNotFoundException` and `@RestControllerAdvice GlobalExceptionHandler`.
4. Created `TerminologyRepository` interface port and `FirestoreTerminologyRepository` adapter.
5. Implemented `FirebaseProperties` and `FirebaseConfig` with external credential detection and safe local fallback.
6. Implemented `TerminologyService` for CRUD operations, keyword normalization, and case-insensitive keyword filtering.
7. Implemented `TerminologyController` with endpoints:
   - `POST /public/terminology-store` (201 Created)
   - `GET /public/terminology-store` (200 OK)
   - `GET /public/terminology-store/search?keyword={keyword}` (200 OK)
   - `GET /public/terminology-store/{id}` (200 OK / 404 Not Found)
   - `PUT /public/terminology-store/{id}` (200 OK / 404 Not Found)
   - `DELETE /public/terminology-store/{id}` (204 No Content / 404 Not Found)
8. Implemented `SecurityConfig` permitting `/public/**`, disabling CSRF, setting stateless sessions, returning 401 on unauthenticated non-public requests, and detailing JWT integration points.
9. Added comprehensive test suite: service tests, controller tests, Firestore adapter tests, and security integration tests.
10. Validated with `mvn test` (33 passed) and `mvn clean verify` (BUILD SUCCESS).

## Testing Strategy

- **Service Unit Tests (`TerminologyServiceTest`):** 11 tests covering record creation, UUID generation, whitespace trimming, keyword deduplication, get-by-id, get-all, keyword query, update, delete, and not-found/invalid-argument error cases.
- **Controller Slice Tests (`TerminologyControllerTest`):** 11 tests covering all endpoints, 201 Created, 200 OK, 204 No Content, 400 Bad Request on empty or missing fields, and 404 Not Found.
- **Repository Adapter Tests (`FirestoreTerminologyRepositoryTest`):** 7 tests verifying Firestore document set, get, query, case-insensitive keyword search, exists, and delete using mocked Firestore.
- **Security Tests (`SecurityIntegrationTest`):** 4 tests verifying that public endpoints (GET, POST, search) are accessible without credentials and that non-public endpoints return 401 Unauthorized.
- **Application Context Test (`IndividualResearchApplicationTests`):** Baseline Spring Boot context loads test.

## Validation Plan

Executed commands:

```bash
mvn test
mvn clean verify
```

Results:
- `mvn test`: 33 tests executed, 33 passed, 0 failures, 0 errors, 0 skipped.
- `mvn clean verify`: Reactor build completed with `BUILD SUCCESS` across `centralize-parent` and `individual-research`. Repackaged executable JAR `individual-research-0.0.1-SNAPSHOT.jar` built successfully.

## Definition of Done

- [x] All requested acceptance criteria pass.
- [x] Recommended acceptance criteria are either completed or explicitly declined.
- [x] Relevant tests pass.
- [x] The project builds successfully.
- [x] Validation commands have been recorded.
- [x] Documentation is updated where required.
- [x] No unintended files are changed.
- [x] Dependencies and plugins are documented.
- [x] The implementation matches this task specification.
- [x] The task status is updated to `Complete`.

## Notes

- Firebase credentials should be configured using `firebase.credentials-path` in `application.yaml` or the `GOOGLE_APPLICATION_CREDENTIALS` environment variable in deployment environments.
- When adding JWT in the future:
  1. Add `spring-boot-starter-oauth2-resource-server` or a custom `JwtAuthenticationFilter`.
  2. Configure `http.oauth2ResourceServer(...)` or `http.addFilterBefore(...)` inside `SecurityConfig`.
  3. Keep `.requestMatchers("/public/**").permitAll()` untouched to preserve public terminology store access.
