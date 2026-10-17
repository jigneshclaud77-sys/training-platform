# Training Platform — Java Backend

A small modular backend for a training platform where instructors publish courses and visitors enroll in published courses.

The primary goal of this project is **maintainable architecture and extensibility**, rather than complex business functionality.

## Technology Stack

- Java 21
- Spring Boot
- Maven
- H2
- Spring Data JPA / Hibernate
- Flyway
- JUnit 5
- ArchUnit
- Spring Application Events

## Architecture

The application follows a **multi-module Maven architecture** with clear domain boundaries.

```text
                              ┌─────────────────────┐
                              │         app         │
                              │ Spring Boot         │
                              │ Application         │
                              └──────────┬──────────┘
                                         │
                  ┌──────────────────────┼──────────────────────┐
                  │                      │                      │
                  ▼                      ▼                      ▼
          ┌───────────────┐      ┌───────────────┐      ┌────────────────┐
          │    catalog    │      │  enrollments  │      │ notifications  │
          │    domain     │      │    domain     │      │    domain      │
          └───────┬───────┘      └───────┬───────┘      └───────┬────────┘
                  │                      │                      │
            ┌─────┴─────┐          ┌─────┴─────┐          ┌─────┴─────┐
            │           │          │           │          │           │
            ▼           ▼          ▼           ▼          ▼           ▼
       catalog-api  catalog-core  enroll-api  enroll-core  notif-api  notif-core
```

Each domain is divided into:

- `*-api` — public contracts, immutable DTOs and domain events.
- `*-core` — domain implementation, entities, repositories, services, controllers and migrations.

The `api` modules contain no Spring or JPA dependencies.

## Module Dependencies

The dependency direction is intentionally restricted:

```text
app ──► catalog-core ───────────► catalog-api
   │
   ├──► enrollments-core ───────► enrollments-api
   │            └──────────────► catalog-api        (CoursesApi)
   │
   └──► notifications-core ─────► notifications-api
                └──────────────► enrollments-api    (EnrollmentCreatedEvent)
```

Every arrow points to an `api` module or from `app` to a `core`. There is no `core` → `core` arrow.

A domain `core` module may depend on another domain's `api`, but **never another domain's `core`**.

JPA entities do not cross domain boundaries. Communication across boundaries uses DTOs and interfaces.

These rules are enforced at build/test level using ArchUnit.

## Business Domains

### Catalog

Owns courses.

Supported operations:

```text
POST /api/v1/courses
POST /api/v1/courses/{id}/publish
GET  /api/v1/courses/{id}
```

Only published courses can accept enrollments.

### Enrollments

Owns visitor enrollments.

Supported operations:

```text
POST /api/v1/enrollments
GET  /api/v1/enrollments?courseId={courseId}
```

Before creating an enrollment, the domain verifies through the `CoursesApi` contract that:

1. The course exists.
2. The course is published.
3. The course has available capacity.

The enrollments domain does not directly access catalog entities, repositories or tables.

## Project Structure

```text
training-platform/
│
├── pom.xml
├── README.md
│
├── catalog/
│   ├── catalog-api/
│   └── catalog-core/
│
├── enrollments/
│   ├── enrollments-api/
│   └── enrollments-core/
│
├── notifications/
│   ├── notifications-api/
│   └── notifications-core/
│
└── app/
    ├── src/main/java/
    └── src/test/java/
```

Within each core module, packages are organized by **subdomain/capability**, rather than creating global `controllers`, `services`, `repositories` and `entities` packages.

## Database

H2 is used as the relational database.

Database schema changes are managed using Flyway.

Each domain owns its persistence concerns and migrations.

Example:

```text
catalog-core
└── src/main/resources/
    └── db/migration/
        └── V1__create_courses.sql

enrollments-core
└── src/main/resources/
    └── db/migration/
        └── V2__create_enrollments.sql
```

## Build and Run

Requires JDK 21. Maven or the wrapper (`mvnw.cmd` on Windows).

```bash
./mvnw clean verify                  # build + run all tests (unit, integration, ArchUnit)
./mvnw install -DskipTests                  # build all modules, skipping tests
java -jar app/target/app-0.0.1-SNAPSHOT.jar   # start the app on http://localhost:8080
```

The database is in-memory H2, created and migrated by Flyway on startup. No setup is needed, and data is lost on restart.

## Key Architectural Decisions

### 1. `api` / `core` module split per domain
 │
 │ **Chosen:** two Maven modules per domain: `*-api` (records, events, interfaces) and `*-core` (implementation).
 │
 │ **Rejected:** a single module with a package convention.
 │
 │ A convention is only discipline. With separate modules a `core` can see another domain's `api` and nothing else, and ArchUnit closes the remaining gaps. Cost: more poms.

### 2. Events live in the publisher's `api` module

**Chosen:** `EnrollmentCreatedEvent` is in `enrollments-api`; `CoursePublishedEvent` is in `catalog-api`. Handlers sit in the consuming domain.

**Rejected:** a shared `events` module that every domain depends on.

A shared module becomes a coupling point that every change touches. This way `notifications-core` needs only `enrollments-api`, and the publisher never knows who listens. Cost: a consumer depends on the publisher's `api`.

### 3. Business rules as plain, Spring-free classes

**Chosen:** published and capacity checks are static methods in a package-private `EnrollmentRules`, called by the service.

**Rejected:** putting the checks in the service or entity, or relying on a database constraint.

Rules testable with a bare `new CourseDto(...)` need no Spring context or database, so unit tests are fast. A constraint cannot see another domain's course data anyway. Cost: one more small class.

### 4. A failing notification must not fail the enrollment

**Chosen:** the handler catches `RuntimeException` from the sender and logs it. It runs synchronously, inside the enrollment transaction.

**Rejected:** `@TransactionalEventListener(AFTER_COMMIT)` or async delivery.

The simple version keeps one thread and one transaction, with nothing extra to configure. Cost: a notification could be sent for a transaction that later rolls back, and nothing is retried.

### 5. Capacity is counted, not stored

**Chosen:** on each enroll, read `capacity` from `CoursesApi` and count the course's enrollment rows.

**Rejected:** a seat counter, kept by enrollments or by catalog.

Counting has no second source of truth to drift out of sync, and catalog stays unaware of enrollments. Cost: one count query per request, and a race on the last seat (see skipped items).

### 6. One shared Flyway location, globally numbered versions

**Chosen:** every core puts migrations in `db/migration`, with versions numbered across domains (`V1` catalog, `V2` enrollments).

**Rejected:** a separate Flyway location per domain.

It uses Flyway's defaults with no configuration in `app`. Each domain still owns its own tables and files. Cost: domains must coordinate version numbers, and a new migration needs a check of existing ones.

## Skipped Due to the Timebox
 - **Duplicate-enrollment checks, cancellation and unpublish.** The same email can enroll in a course twice, and a seat is never released.
 - **Pagination.** `GET /api/v1/enrollments?courseId=` returns every enrollment for the course in one list. Fine at this size, but it needs paging before a course can have many seats.
 - **Publishing isn't idempotent** Course.publish() only sets a flag, and CourseService.publish always fires CoursePublishedEvent. Publishing the same course twice emits the event twice, and a handler that did real work, like warming a cache, would run again.

## what did you have to touch to add the notifications domain, Why the list is short
- The event already existed. enrollments-core already published EnrollmentCreatedEvent through its EventPublisher, whether or not anything listened. Notifications only had to subscribe.
- The contracts were already in api modules. The event is in enrollments-api and NotificationSender is in notifications-api. notifications-core therefore depends only on those two api modules and never on another domain's core.
- The handler lives with the consumer. The publisher doesn't know its subscribers, so nothing on its side needs registering.
- The wiring was already done. The root pom.xml and app/pom.xml already listed the notifications modules from the skeleton step, and Spring component scanning picks up the new beans.

## Assumptions
1. A "notification" is a message addressed to the visitor's email through `NotificationSender`. The only implementation logs it, and a send failure never fails the enrollment.
2. Courses are never deleted, and enrollments refer to them by id only. V2__create_enrollments.sql has an index on course_id but no foreign key to courses. That is deliberate, because each domain owns its own tables. It relies on course ids staying valid and never being reused. There is no delete endpoint, so this holds today.
3. The same email may enroll in the same course more than once; duplicates are not rejected.
4. These are the HTTP status assumptions, taken from the controllers and exception handlers.
┌──────────────────────────────────────┬───────────────────┬────────────────────────────────────────────────────────────────────┐
│              Situation               │      Status       │                        Assumption behind it                        │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Course or enrollment created         │ 201 with a        │ A new resource is created, so the response points to it            │
│                                      │ Location header   │                                                                    │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Course fetched or published          │ 200 with the      │ Publish returns the updated course, not an empty 204               │
│                                      │ course body       │                                                                    │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Course id doesn't exist (get,        │ 404               │ Enrolling into a missing course is a missing resource, not a       │
│ publish, or enroll)                  │                   │ malformed request                                                  │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Enrolling in an unpublished course   │ 409               │ The request is valid but conflicts with the course's current state │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Enrolling in a full course           │ 409               │ Same reasoning: the state, not the input, is the problem           │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Blank title, city, name or email, or │ 400               │ These domain rule violations (IllegalArgumentException) are        │
│  capacity of 0 or less               │                   │ treated as bad input                                               │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Listing enrollments for an unknown   │ 200 with an empty │ Listing doesn't check that the course exists, so "no course" looks │
│ course                               │  list             │  the same as "no enrollments"                                      │
├──────────────────────────────────────┼───────────────────┼────────────────────────────────────────────────────────────────────┤
│ Publishing an already published      │ 200               │ Publishing again succeeds quietly, though it re-fires the event    │
│ course                               │                   │ (the idempotency gap I noted earlier)                              │
└──────────────────────────────────────┴───────────────────┴────────────────────────────────────────────────────────────────────┘

### Notifications

Notifications are an extensibility capability.

When an enrollment is created, a notification is produced through a domain-event-based integration.

Adding the notifications capability should not require modifications to `enrollments-core`.

The notification sender is designed so additional channels can be introduced independently.

`EnrollmentCreatedHandler` in `notifications-core` listens for `EnrollmentCreatedEvent`, builds a confirmation message and passes it to the `NotificationSender` contract. The first implementation, `LoggingNotificationSender`, logs `Notification to <email>: <message>`. A new channel is one more `NotificationSender` implementation; `enrollments-core` is not involved. A failing sender is logged and never fails the enrollment request.