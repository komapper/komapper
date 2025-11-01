# komapper-exposed-jdbc

SQL Template support for Exposed (JDBC) using Komapper's two-way SQL template engine.

> **⚠️ Experimental**: This module is experimental and the API may change in future versions. Use with caution in production environments.

## Overview

This module provides a DSL for executing raw SQL queries with type-safe parameter binding in Exposed JDBC transactions. It combines Exposed's transaction management and column type system with Komapper's powerful two-way SQL template engine.

## Features

- **Two-way SQL Templates**: Write SQL with both test data and bind variables in one template
- **Type-safe Parameter Binding**: Use Exposed's column types for automatic type conversion
- **Seamless Integration**: Works within Exposed's transaction blocks
- **Dynamic SQL**: Support for conditional clauses using template directives
- **IDE Support**: SQL syntax highlighting and validation with `@Language("sql")` annotation

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.komapper:komapper-exposed-jdbc:VERSION")
}
```

## Usage

### Basic Query

```kotlin
import org.komapper.exposed.jdbc.jdbcTemplate
import org.komapper.exposed.jdbc.arg
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

transaction {
    val result = jdbcTemplate {
        val title by arg("Read The Hobbit", Tasks.title)
        build(
            """
            select id, title from tasks
            where title = /* $title */''
            """
        )
    }.execute { rs ->
        val list = mutableListOf<Pair<Int, String>>()
        while (rs.next()) {
            list += rs.getInt("id") to rs.getString("title")
        }
        list
    }

    println(result) // [(2, "Read The Hobbit")]
}
```

### Parameter Binding

There are two ways to bind parameters:

#### 1. Using Exposed Column Definition

```kotlin
val result = jdbcTemplate {
    val id by arg(1, Tasks.id)
    val status by arg(Status.NEW, Tasks.statusName)
    build(
        """
        select title from tasks
        where id = /* $id */0
          and status_name = /* $status */''
        """
    )
}.execute { /* ... */ }
```

#### 2. Using Exposed Column Type

```kotlin
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.IntegerColumnType

val result = jdbcTemplate {
    val title by arg("Learn Exposed DAO", VarCharColumnType())
    val id by arg(1, IntegerColumnType())
    build(
        """
        select * from tasks
        where title = /* $title */''
          and id = /* $id */0
        """
    )
}.execute { /* ... */ }
```

### Dynamic SQL with Directives

Use template directives for conditional SQL:

```kotlin
val result = jdbcTemplate {
    val title by arg("Learn Exposed DAO", Tasks.title)
    val status by arg(null, Tasks.statusName)
    build(
        """
        select * from tasks
        where title = /* $title */''
        /*% if ($status != null) */
          and status_name = /* $status */''
        /*% end */
        order by id
        """
    )
}.execute { /* ... */ }
```

### Using Template Expressions

Komapper's template expressions can be used for string operations:

```kotlin
val result = jdbcTemplate {
    val title by arg("Hobbit", Tasks.title)
    build(
        """
        select id from tasks
        where title like /* $title.asSuffix() */''
        """
    )
}.execute { /* ... */ }
```

Available expression functions:
- `asSuffix()` - Adds `%` prefix: `"value"` → `"%value"`
- `asPrefix()` - Adds `%` suffix: `"value"` → `"value%"`
- `asInfix()` - Adds `%` on both sides: `"value"` → `"%value%"`
- `escape()` - Escapes special SQL characters

### Binding Multiple Parameters

```kotlin
val result = jdbcTemplate {
    val statusName by arg(Status.NEW, Tasks.statusName)
    val title by arg("Learn Exposed DAO", Tasks.title)
    build(
        """
        select title from tasks
        where title = /* $title */''
          and status_name = /* $statusName */''
        """
    )
}.execute { /* ... */ }
```

## Two-way SQL Templates

The template syntax allows you to write SQL that can be executed both as-is (for testing) and with bound parameters:

```sql
select * from tasks where id = /* $id */0
```

- `/* $id */` - The bind variable reference
- `0` - Test data that will be replaced when executed

This means you can copy the SQL directly into a SQL client and run it for testing.

For more details about SQL template syntax, directives, and expressions, see the [Komapper SQL Template documentation](https://www.komapper.org/docs/reference/query/querydsl/template/#sql-template).

## Transaction Context

All SQL template executions must be within an Exposed transaction:

```kotlin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

val db = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

transaction(db) {
    // Execute SQL templates here
    val result = jdbcTemplate { /* ... */ }.execute { /* ... */ }
}
```

## Comparison with Komapper JDBC

While both use the same template engine, this module:
- ✅ Works within Exposed's transaction management
- ✅ Uses Exposed's column type system
- ✅ Integrates with Exposed DAO and DSL
- ❌ Does not use Komapper's entity mapping
- ❌ Does not use Komapper's QueryDsl

Use this module when you want to:
- Use raw SQL in an Exposed-based application
- Leverage Exposed's transaction management
- Mix SQL templates with Exposed DAO or DSL queries

## Related Modules

- **komapper-exposed**: Core SQL template components for Exposed
- **komapper-exposed-r2dbc**: R2DBC version of SQL template support
- **komapper-template**: Komapper's template engine
- **komapper-jdbc**: Komapper's full JDBC support with entity mapping

## License

This module is part of the Komapper project and is licensed under the Apache License 2.0.

## Links

- [Komapper Documentation](https://www.komapper.org/docs/)
- [Exposed Framework](https://github.com/JetBrains/Exposed)
- [Komapper GitHub](https://github.com/komapper/komapper)