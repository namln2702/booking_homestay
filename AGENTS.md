# Repository Guidelines

## Project Structure & Module Organization
The application is a Spring Boot service targeting Java 21. Core packages live under `src/main/java/org/example/do_an_v1`, with controllers exposing REST endpoints, services handling domain logic, repositories wrapping JPA access, and `dto`/`mapper` translating data models. Configuration, enums, and payload objects sit alongside the entities. Shared resources live in `src/main/resources`, while tests mirror the production namespace in `src/test/java/org/example/do_an_v1`.

## Build, Test, and Development Commands
- `./mvnw clean verify` – run compilation, format checks, and the full unit/integration test suite.
- `./mvnw test` – execute fast-running JUnit tests under `src/test/java`.
- `./mvnw spring-boot:run` – launch the API with the current profile; ideal for local iteration.
- `./mvnw spring-boot:run` – launch the API with the current profile; ideal for local iteration.

## Coding Style & Naming Conventions
Follow standard Java conventions: 4-space indentation, braces on new lines for types, and camelCase members. Classes and interfaces use PascalCase, while database entities mirror table intent (`Booking`, `Homestay`, etc.). Lombok builders are widely used; confirm generated methods before adding manual getters/setters. Prefer constructor injection inside services and keep Feign client interfaces in the `configuration` package.

## Testing Guidelines
JUnit 5 with Spring Boot’s test support backs the default scaffold. Name suites with the `*Tests` suffix (`BookingServiceTests`) and isolate slow or external calls by mocking repositories or Feign clients. Run `./mvnw test` before every push and focus on branch coverage for business services and exception flows. Add integration tests under `src/test/java/.../controller` when endpoints include security or external IO.

## Commit & Pull Request Guidelines
Existing history favors short, imperative commit subjects in lowercase (e.g., `add entity`, `verify email`). Keep each commit focused on one concern and add body context when behavior changes or migrations are required. Pull requests should include a concise summary, linked issue, verification notes (tests, manual steps), and screenshots for API or UI changes. Flag database migrations, external service dependencies, and new configuration keys in the description to ease reviewer setup.

## Configuration & Security Tips
Secrets and credentials must come from environment variables. Keep `spring.datasource.username=${USER_DB}` and `spring.datasource.password=${PASSWORD_DB}` set via your shell or secrets manager—never hard-code them. For local work, create an untracked `.env` or direnv config and export mail credentials before running `spring-boot:run`. Document new outbound env keys in `application.properties` with empty placeholders and reference them here so contributor setups stay aligned.
