# Task-1: Firebase Public CRUD API with Future JWT Security

- **Status:** Proposed
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

- [ ] The application uses Firebase as the persistence store for the requested data.
- [ ] Public CRUD endpoints are exposed for the stored data.
- [ ] A read endpoint returns all data stored in Firebase.
- [ ] A separate read endpoint filters data by keywords.
- [ ] Each stored record includes `category`, a list of keywords, and `description`.
- [ ] Spring Security is added.
- [ ] All requested endpoints are publicly accessible without authentication.
- [ ] Security configuration is prepared so JWT authentication can be implemented in the future.
- [ ] The API documents request and response shapes, validation rules, and HTTP status codes.
- [ ] Create and update operations validate required fields and reject malformed payloads.
- [ ] Keyword filtering defines whether matching is case-insensitive and whether records match any or all supplied keywords.
- [ ] API error responses use one consistent JSON format.
- [ ] Firebase credentials are supplied through environment or deployment configuration and are not committed to source control.

## Goal

Provide a small backend service that stores records in Firebase and exposes an unauthenticated public API for managing and querying them. The service must support two distinct read modes: returning all records and filtering records by keywords. The security layer must allow the current public behavior while keeping a clean extension point for JWT authentication later.

The requested scope is limited to the backend API, Firebase persistence, security configuration, tests, and required API/configuration documentation. User registration, login, token issuance, authorization rules, and client applications are out of scope unless explicitly added later.

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

- The request concerns a Spring Boot application using Java and Spring Security, based on the explicit Spring Security requirement.
- The current turn is specification-only; no source implementation will be started until this specification is approved.
- Firebase credentials will be injected through configuration or environment variables rather than committed to the repository.
- Records will have a stable identifier so update and delete operations can target one record.
- The exact endpoint paths and filtering semantics remain provisional until clarified; the implementation plan must resolve them before coding.
- Because repository inspection failed, the project’s existing conventions, dependencies, tests, and Java version are not yet verified.

## Analysis

### Current project behavior

Not verified. The required repository inspection could not run because the execution environment rejected process creation with `CreateProcessWithLogonW failed: 1385`. No implementation changes have been made.

### Relevant implementation areas

The implementation is expected to include a Firebase data-access layer, a record model, a service layer containing CRUD and keyword-query behavior, REST controllers, request validation and error handling, Spring Security configuration, tests, and configuration documentation. Exact file paths must follow the existing repository structure once it is available.

### Technical constraints

- Firebase SDK selection depends on the answer to the Firestore versus Realtime Database question.
- Public access must be explicit and testable for every requested endpoint.
- JWT must not be falsely represented as active authentication if no token issuance or validation is implemented.
- Secrets and service-account credentials must remain outside source control.
- Existing project dependencies, Java release, package names, and test conventions must be preserved once inspected.

### Risks and compatibility

- Making write endpoints public creates an abuse and data-integrity risk; this is required by the current request but should be documented and designed for a future authorization boundary.
- Firebase query capabilities may constrain efficient keyword-array filtering, depending on the selected Firebase product and desired any/all semantics.
- Changing the data shape later from `describe` to `description` could break clients, so the requested property name should be confirmed before implementation.
- Public CORS behavior, rate limiting, pagination, and payload-size limits are not specified and may be needed before production exposure.

### Alternatives considered

- Firestore is likely preferable for document CRUD and structured queries, but Realtime Database remains possible and must be selected explicitly.
- A direct controller-to-Firebase implementation would be smaller initially, but a repository/service boundary better isolates Firebase-specific behavior and makes future testing and migration easier.
- Enabling JWT immediately would exceed the request because all current endpoints must remain public; preparing a stateless security configuration with a future authentication filter/provider seam is the safer interpretation.

### Recommended approach

Use a layered Spring Boot design: validated request/response DTOs, a domain record with Firebase ID, a Firebase repository adapter, a service for CRUD and keyword filtering, REST controllers for separate get-all and keyword-filter operations, centralized error handling, and a stateless Spring Security configuration that permits the current API paths. Keep JWT integration points documented and isolated without requiring authentication yet.

## Proposed Pattern

**Pattern:** Ports and adapters with a service layer.

**Application:** The API service depends on a repository port; the Firebase adapter implements that port. Controllers depend on the service rather than the Firebase SDK directly.

**Why it fits:** It keeps Firebase-specific SDK code isolated, supports focused unit tests, and leaves room to replace or evolve persistence without changing the public API.

**Example flow:** `RecordController → RecordService → RecordRepository → FirebaseRecordRepository`.

**Future security rule:** Keep authentication and authorization at the Spring Security boundary. When JWT is introduced, add token validation and authorization rules without changing the repository or domain model.

## Integration Plan

| Action | File | Location | Details | Reason |
|---|---|---|---|---|
| Add/Modify | Root build file, exact path to be confirmed | Dependencies/plugins | Add compatible Firebase Admin SDK and Spring Security dependencies only if absent; preserve the project’s Java and dependency-management strategy. | Enable Firebase persistence and security. |
| Add/Modify | Firebase configuration class, exact path to be confirmed | Configuration package | Initialize Firebase from external credentials/configuration and expose the selected Firebase client. | Centralize safe Firebase setup. |
| Add | Record model/DTO files, exact path to be confirmed | Domain/API packages | Represent identifier, `category`, keyword list, and `describe`; add validation according to approved rules. | Define the API and persistence contract. |
| Add | Repository port and Firebase adapter, exact paths to be confirmed | Data-access package | Implement create, find-by-ID, find-all, update, delete, and keyword query operations. | Isolate Firebase behavior. |
| Add | Service class, exact path to be confirmed | Service package | Coordinate validation, CRUD behavior, not-found handling, and keyword filtering. | Keep business behavior outside controllers. |
| Add | REST controller, exact path to be confirmed | Controller package | Expose public CRUD endpoints plus distinct get-all and keyword-filter read endpoints. | Provide the requested API. |
| Add | Exception handler, exact path to be confirmed | API/configuration package | Return consistent status codes and error payloads. | Make failures predictable for clients. |
| Add/Modify | Spring Security configuration, exact path to be confirmed | Security package | Permit all requested endpoints, configure stateless behavior as appropriate, and leave a documented JWT extension point without enforcing authentication now. | Satisfy current public access and future JWT readiness. |
| Add | Unit/integration test files, exact paths to be confirmed | Existing test source set | Test CRUD, both read modes, validation, Firebase adapter boundaries, and public endpoint access. | Prevent regressions and verify acceptance criteria. |
| Add/Modify | Configuration/API documentation, exact path to be confirmed | Existing docs/config files | Document Firebase setup, endpoint contracts, public-security warning, and future JWT boundary. | Make deployment and client use reproducible. |

## Implementation Steps

1. Inspect the repository, child `AGENTS.md` files, build files, existing tests, and current task specifications; resolve the provisional task ID and project conventions.
2. Confirm Firebase product, endpoint contract, record identifier, and keyword matching semantics.
3. Add or adapt Firebase configuration and the repository boundary.
4. Add the record model, DTO validation, service operations, and error handling.
5. Add CRUD, get-all, and keyword-filter controllers with the approved paths and status codes.
6. Add Spring Security with explicit public endpoint rules and documented JWT extension points.
7. Add tests incrementally, run narrow tests, then run the project’s broader validation commands.
8. Update this task specification with actual files, commands, results, decisions, and final status.

## Testing Strategy

- Unit tests for record validation, service behavior, not-found handling, and keyword matching semantics.
- Repository adapter tests using the project’s approved Firebase test strategy or a mock boundary; do not require production credentials in tests.
- Controller/API tests for CRUD request and response shapes, get-all, keyword filtering, invalid payloads, missing records, and expected HTTP statuses.
- Security tests proving every requested endpoint is accessible without authentication and that the configuration remains stateless/ready for a future JWT filter.
- Configuration tests proving missing or malformed Firebase configuration fails clearly without exposing credentials.
- Regression tests for any existing application behavior discovered during repository inspection.

## Validation Plan

Commands are not yet executed because process creation is currently unavailable. After repository inspection is restored, run the project-appropriate commands, typically:

```bash
mvn validate
mvn test
mvn verify
```

Expected results include successful compilation, passing unit/API/security tests, and no committed secret material. If the project is not Maven-based, use its existing build and test commands instead and record the exact commands here.

## Definition of Done

- [ ] All requested acceptance criteria pass.
- [ ] Recommended acceptance criteria are either completed or explicitly declined.
- [ ] The project builds successfully.
- [ ] Validation commands have been recorded.
- [ ] Documentation is updated where required.
- [ ] No unintended files are changed.
- [ ] Dependencies and plugins are documented.
- [ ] The implementation matches this task specification.
- [ ] The task status is updated to `Complete`.

## Notes

- This specification was created before implementation as required by the selected spec-driven workflow.
- Repository inspection and task-ID enumeration were attempted but blocked by the execution environment error `CreateProcessWithLogonW failed: 1385`; `Task-1` is therefore provisional and must be reconciled with existing `spec/Task-*.md` files before implementation.
- Approval of this specification should include answers to the blocking Open Questions, especially Firebase product and endpoint/filter semantics.
