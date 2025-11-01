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

---

## Current Progress – Authentication & Profile Overhaul

- Unified profile update endpoints now accept and return DTO payloads (`CustomerDTO`, `HostDTO`, `AdminDTO`) across `CustomerController`, `HostController`, and `ProfileController`, letting us plug in JWT later without touching business logic. Service layers (`CustomerServiceImpl`, `HostServiceImpl`, `ProfileServiceImpl`) map DTOs back to entities and guard derived fields (timestamps, last booking).
- Introduced `RequestIdentityResolver` to centralise how we determine the acting user. Admins may operate on other accounts via path parameters; all other roles fall back to the JWT claim. This keeps controllers clean and ready for future JWT enforcement.
- Admin flows now enforce role hierarchy: only `SUPER_ADMIN` can invite new admins, while `ADMIN`/`SUPER_ADMIN` can call privileged endpoints. `AdminServiceImpl` validates `LevelAdmin` directly; `AdminController` uses proper `@PreAuthorize` checks. Profile mappers populate identifiers (`idAdmin`, `idUser`) so responses no longer leak `null` IDs.
- JWT lifecycle tightened: `UserServiceImpl.checkUser` prioritises admin logins, ensures hosts always have a backing customer profile, and toggles `isOnline`. Tokens consistently reflect the highest active role, ensuring future `@PreAuthorize` annotations behave as expected.
- Seeding bootstrap for the first super admin: configurable via `bootstrap.super-admin.*` properties. On startup, it creates or updates the user profile (username, avatar, phone, age, isOnline=false), links the admin entity both ways, and only runs when no `SUPER_ADMIN` exists. Properties in `application.properties` now default to empty placeholders for safe environments.
- Added Postman collection (`docs/postman/admin-registration.postman_collection.json`) to exercise the admin invitation/activation flow with simple scripts that chain activation codes via environment variables.

## Feature Snapshot (Business Behaviour)

- **User Onboarding & Login**
  - Email OTP (`/users/login-register-with-email`) auto-creates a `User` + `Customer` on first contact. OTP verification issues a role-aware JWT (customer/admin/host) and sets `isOnline = true`; logout stores invalid tokens and flips presence offline.
  - Google OAuth login mirrors the email flow: create customer on first login, reject if email belongs to an admin.
- **Profile Management**
  - `/customers`, `/hosts`, `/profile/*` endpoints upsert profile details via DTOs. Derived fields (`createdAt`, `lastBooking`, etc.) remain server-controlled. Host updates implicitly require an existing customer record.
- **Admin Lifecycle**
  - `/admins/invite` requires bearer token with admin authority. Only users whose `Admin.levelAdmin = SUPER_ADMIN` can invite new admins; the endpoint returns the activation code (for QA only).
  - `/admins/activate` finalises the invitation, sets status to `ACTIVE`, and refreshes user contact info.
  - Bootstrap runner can seed the first super admin from config so the invite workflow can start.
- **Token/Authorization Strategy**
  - JWTs carry `id` and `scope = ROLE_*`. `RequestIdentityResolver` ensures path parameters are respected only for admins; other roles are bound to their own ID.
  - `checkUser` picks the highest privilege (admin > host > customer) when issuing tokens and guarantees host/customer consistency (host always implies a stored customer).
