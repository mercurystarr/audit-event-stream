# audit-event-stream

> 🚧 Work in progress — actively being built. 

An event-driven audit trail system built on Apache Kafka and Protobuf,
modelling the kind of identity event stream that runs inside every
enterprise IdP: login attempts, permission changes, and account mutations
flowing through a durable, schema-versioned pipeline with idempotent
consumers and dead-letter recovery.

## Status

Currently building out the producer and consumer modules. Not yet runnable
end-to-end — Docker Compose setup and full architecture docs land later.

- [x] Proto schema (`proto/`) — envelope + oneof event types
- [x] Producer module — Kafka config, publisher, REST endpoints
- [ ] Consumer module
- [ ] Docker Compose / end-to-end run
- [ ] Schema evolution exercise

## Stack

Kotlin DSL Gradle build, JDK 21, Spring Boot, Kafka, Protobuf, Redis.

