# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Komapper is a Kotlin ORM library supporting both JDBC and R2DBC, with compile-time code generation using Kotlin Symbol Processing (KSP).

## Common Development Commands

### Build Commands
- `./gradlew build` - Build all modules
- `./gradlew spotlessApply` - Auto-format code (Kotlin with ktlint, Java with Google Java Format)
- `./gradlew spotlessCheck` - Check code formatting
- `./gradlew clean` - Clean build outputs

### Testing Commands
- `./gradlew test` - Run unit tests
- `./gradlew check` - Run all checks including tests and spotless
- `./gradlew h2` - Run tests with H2 database
- `./gradlew mariadb` - Run tests with MariaDB
- `./gradlew mysql` - Run tests with MySQL
- `./gradlew mysql5` - Run tests with MySQL 5.x
- `./gradlew oracle` - Run tests with Oracle
- `./gradlew postgresql` - Run tests with PostgreSQL
- `./gradlew sqlserver` - Run tests with SQL Server
- `./gradlew integration-test-jdbc:checkAll` - Run all JDBC integration tests
- `./gradlew integration-test-r2dbc:checkAll` - Run all R2DBC integration tests

### Running Single Tests
- `./gradlew test --tests "TestClassName"` - Run specific test class
- `./gradlew test --tests "TestClassName.methodName"` - Run specific test method
- Use `-PexcludeTags=tagName` to exclude tests with specific tags

## Architecture and Structure

### Module Organization
The project follows a modular architecture with clear separation of concerns:

1. **Core Modules**
   - `komapper-core`: Core functionality shared between JDBC and R2DBC
   - `komapper-annotation`: Annotations for entity mapping (@KomapperEntity, @KomapperId, etc.)
   - `komapper-processor`: KSP processor for compile-time code generation

2. **JDBC Modules**
   - `komapper-jdbc`: JDBC implementation
   - `komapper-starter-jdbc`: Quick starter for JDBC
   - `komapper-tx-jdbc`: JDBC transaction management
   - `komapper-dialect-*-jdbc`: Database-specific JDBC implementations

3. **R2DBC Modules**
   - `komapper-r2dbc`: R2DBC implementation
   - `komapper-starter-r2dbc`: Quick starter for R2DBC
   - `komapper-tx-r2dbc`: R2DBC transaction management
   - `komapper-dialect-*-r2dbc`: Database-specific R2DBC implementations

4. **Database Dialect Modules**
   - Separate modules for each database (H2, MariaDB, MySQL, Oracle, PostgreSQL, SQL Server)
   - Each has base, JDBC, and R2DBC variants

5. **Spring Integration**
   - `komapper-spring-*`: Spring Framework integration
   - `komapper-spring-boot-*`: Spring Boot auto-configuration and starters

6. **Testing**
   - `integration-test-core`: Shared test entities and utilities
   - `integration-test-jdbc`: JDBC integration tests
   - `integration-test-r2dbc`: R2DBC integration tests

### Key Design Principles
- **Compile-time Safety**: Uses KSP for code generation to avoid runtime reflection
- **Immutable Data Model**: Entity classes must be Kotlin data classes
- **Query DSL**: Type-safe query construction with `QueryDsl` API
- **Separation of Query Construction and Execution**: Same query objects work with both JDBC and R2DBC

### Build Configuration
- Uses Gradle with Kotlin DSL
- JVM target: 11 (17 for Spring modules)
- Kotlin API version: 1.8
- KSP2 enabled for better performance
- Automatic code formatting with Spotless (ktlint for Kotlin, Google Java Format for Java)

### Testing Approach
- JUnit 5 with Kotlin test framework
- Database-specific test suites using Testcontainers
- Integration tests organized by database type
- Tests run against actual database instances (not mocks)

## Important Files and Patterns

### Entity Definition Pattern
```kotlin
@KomapperEntity
data class EntityName(
    @KomapperId
    @KomapperAutoIncrement
    val id: Int = 0,
    val field: String,
    @KomapperVersion
    val version: Int = 0
)
```

### Query Pattern
```kotlin
val metamodel = Meta.entityName
val query = QueryDsl.from(metamodel).where { metamodel.field eq "value" }
```

### Database Connection
- JDBC: Use `JdbcDatabase` class
- R2DBC: Use `R2dbcDatabase` class
- Both support transaction management with `withTransaction` blocks

## Development Guidelines

- Always run `./gradlew spotlessApply` before committing
- Test changes against at least H2 database (`./gradlew h2`)
- Follow existing code patterns in neighboring modules
- Use appropriate dialect modules for database-specific features
- Ensure compatibility with both JDBC and R2DBC when modifying core functionality