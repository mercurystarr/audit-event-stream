# audit-event-stream

An event-driven audit trail system built on Apache Kafka and Protobuf,
modelling the kind of identity event stream that runs inside every
enterprise IdP: login attempts, permission changes, and account mutations
flowing through a durable, schema-versioned pipeline with idempotent
consumers and dead-letter recovery.

## Status

Producer and consumer are both implemented and containerized. End-to-end
verification against a live Compose stack, the schema-evolution proof, and
integration tests are still in progress.

- [x] Proto schema (`proto/`) — envelope + oneof event types (login,
      permission change, account update)
- [x] Producer module — Kafka config (`acks=all`, idempotent producer), REST
      endpoints, publisher unit tests
- [x] Consumer module — manual-offset Kafka listener, Redis-backed
      idempotency store (`SETNX`), dead-letter recovery, sealed-interface
      domain model with exhaustive pattern-matching dispatch, virtual-thread
      listener container
- [x] Docker Compose — Kafka, Redis, producer, consumer with health checks
- [ ] End-to-end run against the live stack
- [ ] Integration tests (Testcontainers: Kafka round-trip, concurrent
      idempotency race)
- [ ] Schema evolution exercise (forward-compatibility proof)

## Stack

Kotlin DSL Gradle build, JDK 21, Spring Boot, Kafka, Protobuf, Redis.

