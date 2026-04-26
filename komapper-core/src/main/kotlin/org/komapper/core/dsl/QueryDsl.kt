package org.komapper.core.dsl

import org.intellij.lang.annotations.Language
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.context.ValuesContext
import org.komapper.core.dsl.element.With
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.ValuesDeclaration
import org.komapper.core.dsl.expression.ValuesExpression
import org.komapper.core.dsl.metamodel.EmptyMetamodel
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.query.DeleteQueryBuilder
import org.komapper.core.dsl.query.DeleteQueryBuilderImpl
import org.komapper.core.dsl.query.FlowSubquery
import org.komapper.core.dsl.query.InsertQueryBuilder
import org.komapper.core.dsl.query.InsertQueryBuilderImpl
import org.komapper.core.dsl.query.ScalarQuery
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl
import org.komapper.core.dsl.query.ScriptExecuteQuery
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl
import org.komapper.core.dsl.query.SelectQueryBuilder
import org.komapper.core.dsl.query.SelectQueryBuilderImpl
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.TemplateSelectQueryBuilderImpl
import org.komapper.core.dsl.query.UpdateQueryBuilder
import org.komapper.core.dsl.query.UpdateQueryBuilderImpl
import org.komapper.core.dsl.scope.ValuesScope

/**
 * The entry point for constructing queries.
 */
@ThreadSafe
interface QueryDsl {
    /**
     * Creates a `WITH` query DSL.
     *
     * The example below defines a CTE named `employeeRank` that ranks employees within each department by salary,
     * then joins it back to the `employee` table to pick the top-paid employee in every department.
     * ```
     * val e = Meta.employee
     * val t = Meta.employeeRank
     * val subquery = QueryDsl.from(e).select(
     *     e.employeeId,
     *     e.employeeName,
     *     rank().over {
     *         partitionBy(e.departmentId)
     *         orderBy(e.salary.desc())
     *     },
     * )
     * val query = QueryDsl.with(t, subquery)
     *     .from(e)
     *     .innerJoin(t) { e.employeeId eq t.employeeId }
     *     .where { t.rank eq 1 }
     * ```
     *
     * @param metamodel the entity metamodel
     * @param subquery the subquery expression
     * @return the `WITH` query DSL
     */
    fun with(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl

    /**
     * Creates a `WITH` query DSL with multiple pairs of entity metamodels and subquery expressions.
     *
     * The example below declares two CTEs in a single `WITH` clause &mdash; one filtering addresses,
     * the other filtering high-salary employees &mdash; and joins them in the main query.
     * ```
     * val a = Meta.address
     * val e = Meta.employee
     * val addressSubquery = QueryDsl.from(a).where { a.addressId less 5 }
     * val employeeSubquery = QueryDsl.from(e).where { e.salary greater BigDecimal("2000") }
     * val query = QueryDsl.with(a to addressSubquery, e to employeeSubquery)
     *     .from(e)
     *     .innerJoin(a) { e.addressId eq a.addressId }
     * ```
     *
     * @param pairs the pairs of entity metamodels and subquery expressions
     * @return the `WITH` query DSL
     */
    fun with(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl

    /**
     * Creates a `WITH RECURSIVE` query DSL.
     *
     * The example below builds a recursive CTE that generates the integers 1..10
     * (a base case unioned with a recursive step) and then selects their sum from the CTE.
     * ```
     * val t = Meta.t
     * val subquery = QueryDsl.select(literal(1)).unionAll(
     *     QueryDsl.from(t).where { t.n less 10 }.select(t.n + 1),
     * )
     * val query = QueryDsl.withRecursive(t, subquery).from(t).select(sum(t.n))
     * ```
     *
     * @param metamodel the entity metamodel
     * @param subquery the subquery expression
     * @return the `WITH RECURSIVE` query DSL
     */
    fun withRecursive(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl

    /**
     * Creates a `WITH RECURSIVE` query DSL with multiple pairs of entity metamodels and subquery expressions.
     *
     * The example below declares two recursive CTEs in a single `WITH RECURSIVE` clause
     * (each generating a small integer sequence) and joins them on equal values.
     * ```
     * val t1 = Meta.t1
     * val t2 = Meta.t2
     * val subquery1 = QueryDsl.select(literal(1)).unionAll(
     *     QueryDsl.from(t1).where { t1.n less 10 }.select(t1.n + 1),
     * )
     * val subquery2 = QueryDsl.select(literal(1)).unionAll(
     *     QueryDsl.from(t2).where { t2.n less 5 }.select(t2.n + 1),
     * )
     * val query = QueryDsl.withRecursive(t1 to subquery1, t2 to subquery2)
     *     .from(t1)
     *     .innerJoin(t2) { t1.n eq t2.n }
     * ```
     *
     * @param pairs the pairs of entity metamodels and subquery expressions
     * @return the `WITH RECURSIVE` query DSL
     */
    fun withRecursive(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl

    /**
     * Creates a SELECT query builder.
     *
     * Basic filtering &mdash; selects all `Address` rows whose `street` equals `"STREET 1"`,
     * ordered by `addressId`:
     * ```
     * val a = Meta.address
     * val query: Query<List<Address>> = QueryDsl.from(a)
     *     .where { a.street eq "STREET 1" }
     *     .orderBy(a.addressId)
     * ```
     *
     * Inner join &mdash; joins `Address` and `Employee` on `addressId`:
     * ```
     * val a = Meta.address
     * val e = Meta.employee
     * val query: Query<List<Address>> = QueryDsl.from(a)
     *     .innerJoin(e) { a.addressId eq e.addressId }
     * ```
     *
     * Aggregation &mdash; counts employees per department, returning departments with at least 4 employees:
     * ```
     * val e = Meta.employee
     * val query: Query<List<Pair<Int?, Long?>>> = QueryDsl.from(e)
     *     .groupBy(e.departmentId)
     *     .having { count(e.employeeId) greaterEq 4L }
     *     .orderBy(e.departmentId)
     *     .select(e.departmentId, count(e.employeeId))
     * ```
     *
     * Pagination with pessimistic lock &mdash; locks the first three rows ordered by `addressId`:
     * ```
     * val a = Meta.address
     * val query: Query<List<Address>> = QueryDsl.from(a)
     *     .orderBy(a.addressId)
     *     .offset(10).limit(3)
     *     .forUpdate { nowait() }
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a SELECT query builder which uses a derived table.
     *
     * The example below builds a subquery that ranks employees by salary within each department,
     * then uses it as a derived table to filter only the top-ranked employees.
     * ```
     * val e = Meta.employee
     * val t = Meta.employeeRank
     * val subquery = QueryDsl.from(e).select(
     *     e.employeeId,
     *     e.employeeName,
     *     rank().over {
     *         partitionBy(e.departmentId)
     *         orderBy(e.salary.desc())
     *     },
     * )
     * val query = QueryDsl.from(t, subquery)
     *     .where { t.rank eq 1 }
     *     .orderBy(t.employeeId)
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @param subquery the derived table
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
        subquery: SubqueryExpression<*>,
    ): SelectQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a VALUES table constructor that can be used as a derived table.
     *
     * The example below constructs an inline two-row table from literals and uses it as a derived table
     * in a `SELECT ... FROM (VALUES ...)` query, ordered by `name`.
     * ```
     * val t = Meta.nameAndAmount
     * val rows = QueryDsl.values(t) {
     *     row {
     *         t.name eq "alice"
     *         t.amount eq BigDecimal("100")
     *     }
     *     row {
     *         t.name eq "bob"
     *         t.amount eq BigDecimal("200")
     *     }
     * }
     * val query = QueryDsl.from(t, rows).orderBy(t.name)
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel that defines the column shape of the VALUES rows
     * @param declaration the values declaration
     * @return the subquery expression representing the VALUES table constructor
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> values(
        metamodel: META,
        declaration: ValuesScope<ENTITY, ID, META>.() -> Unit,
    ): SubqueryExpression<ENTITY>

    /**
     * Creates a SELECT query for a single column expression.
     *
     * The example below issues a tableless `SELECT 1` and returns it as a single-element list.
     * ```
     * val query: Query<List<Int?>> = QueryDsl.select(literal(1))
     * ```
     *
     * @param A the type of the column expression
     * @param expression the column expression
     * @return a flow subquery for the column expression
     */
    fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?>

    /**
     * Creates a SELECT query for two column expressions.
     *
     * The example below issues a tableless `SELECT 1, 'a'` and returns each row as a `Pair`.
     * ```
     * val query: Query<List<Pair<Int?, String?>>> =
     *     QueryDsl.select(literal(1), literal("a"))
     * ```
     *
     * @param A the type of the first column expression
     * @param B the type of the second column expression
     * @param expression1 the first column expression
     * @param expression2 the second column expression
     * @return a flow subquery for the pair of column expressions
     */
    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A?, B?>>

    /**
     * Creates a SELECT query for three column expressions.
     *
     * The example below issues a tableless `SELECT 1, 'a', 2` and returns each row as a `Triple`.
     * ```
     * val query: Query<List<Triple<Int?, String?, Int?>>> =
     *     QueryDsl.select(literal(1), literal("a"), literal(2))
     * ```
     *
     * @param A the type of the first column expression
     * @param B the type of the second column expression
     * @param C the type of the third column expression
     * @param expression1 the first column expression
     * @param expression2 the second column expression
     * @param expression3 the third column expression
     * @return a flow subquery for the triple of column expressions
     */
    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A?, B?, C?>>

    /**
     * Creates a SELECT query for a scalar expression.
     *
     * The scalar query produced here yields a single value and can be embedded directly in another query.
     * The example below builds a `count()` subquery over `Address` and uses it as a scalar value
     * in a `WHERE` clause comparison.
     * ```
     * val a = Meta.address
     * val countSubquery = QueryDsl.from(a).select(count())
     * val query = QueryDsl.from(a).where { a.addressId greater countSubquery }
     * ```
     *
     * @param T the exterior type of the scalar expression
     * @param S the interior type of the scalar expression
     * @param expression the scalar expression
     * @return a scalar query for the scalar expression
     */
    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>,
    ): ScalarQuery<T?, T, S>

    /**
     * Creates a INSERT query builder.
     *
     * Single insert &mdash; inserts a single `Address` entity and returns it
     * (with any auto-generated values such as identifiers populated):
     * ```
     * val a = Meta.address
     * val address = Address(16, "STREET 16", 0)
     * val query: Query<Address> = QueryDsl.insert(a).single(address)
     * ```
     *
     * Multiple insert &mdash; inserts several rows in a single SQL statement:
     * ```
     * val a = Meta.address
     * val query: Query<List<Address>> = QueryDsl.insert(a).multiple(
     *     Address(16, "STREET 16", 0),
     *     Address(17, "STREET 17", 0),
     *     Address(18, "STREET 18", 0),
     * )
     * ```
     *
     * Batch insert &mdash; inserts each row using a separate statement (useful for very large lists):
     * ```
     * val a = Meta.address
     * val query: Query<List<Address>> = QueryDsl.insert(a).batch(
     *     Address(16, "STREET 16", 0),
     *     Address(17, "STREET 17", 0),
     *     Address(18, "STREET 18", 0),
     * )
     * ```
     *
     * UPSERT &mdash; on duplicate key, updates `departmentName` and concatenates `location`
     * using the conflicting row exposed via `excluded`:
     * ```
     * val d = Meta.department
     * val department: Department = Department(...)
     * val query = QueryDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
     *     d.departmentName eq "PLANNING2"
     *     d.location eq concat(d.location, concat("_", excluded.location))
     * }.single(department)
     * ```
     *
     * UPSERT (ignore) &mdash; inserts a row, silently skipping if a duplicate key exists:
     * ```
     * val a = Meta.address
     * val address: Address = Address(16, "STREET 16", 0)
     * val query: Query<Address?> = QueryDsl.insert(a).onDuplicateKeyIgnore().executeAndGet(address)
     * ```
     *
     * INSERT-SELECT &mdash; copies rows whose `addressId` is in 1..5 to an archive table:
     * ```
     * val a = Meta.address
     * val aa = Meta.address.clone(table = "ADDRESS_ARCHIVE")
     * val query: Query<Pair<Long, List<Int>>> = QueryDsl.insert(aa).select {
     *     QueryDsl.from(a).where { a.addressId between 1..5 }
     * }
     * ```
     *
     * Values DSL &mdash; inserts a row by assigning columns directly, without an entity instance:
     * ```
     * val a = Meta.address
     * val query: Query<Pair<Long, Int?>> = QueryDsl.insert(a).values {
     *     a.addressId eq 19
     *     a.street eq "STREET 16"
     *     a.version eq 0
     * }
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): InsertQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a UPDATE query builder.
     *
     * Single entity update &mdash; updates the row identified by the entity's primary key
     * (and increments the version when an `@KomapperVersion` property is present):
     * ```
     * val a = Meta.address
     * val address: Address = ...
     * val query: Query<Address> = QueryDsl.update(a).single(address)
     * ```
     *
     * Set + where &mdash; updates the `street` column for the row whose `addressId` is `1`,
     * returning the number of affected rows:
     * ```
     * val a = Meta.address
     * val query: Query<Long> = QueryDsl.update(a)
     *     .set { a.street eq "STREET 16" }
     *     .where { a.addressId eq 1 }
     * ```
     *
     * Batch update &mdash; updates each entity using a separate statement:
     * ```
     * val a = Meta.address
     * val query: Query<List<Address>> = QueryDsl.update(a).batch(address1, address2, address3)
     * ```
     *
     * Include &mdash; updates only the listed properties (here, just `departmentName`):
     * ```
     * val d = Meta.department
     * val department: Department = ...
     * val query: Query<Department> = QueryDsl.update(d).include(d.departmentName).single(department)
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): UpdateQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a DELETE query builder.
     *
     * Delete by where clause &mdash; deletes the row whose `addressId` is `15`,
     * returning the number of affected rows:
     * ```
     * val a = Meta.address
     * val query: Query<Long> = QueryDsl.delete(a).where { a.addressId eq 15 }
     * ```
     *
     * Delete a single entity &mdash; deletes by the entity's primary key
     * (and verifies the version when an `@KomapperVersion` property is present):
     * ```
     * val a = Meta.address
     * val address: Address = ...
     * val query: Query<Unit> = QueryDsl.delete(a).single(address)
     * ```
     *
     * Batch delete &mdash; deletes each entity using a separate statement:
     * ```
     * val a = Meta.address
     * val query: Query<Unit> = QueryDsl.delete(a).batch(address1, address2, address3)
     * ```
     *
     * Delete all rows &mdash; deletes every row in the table:
     * ```
     * val e = Meta.employee
     * val query: Query<Long> = QueryDsl.delete(e).all()
     * ```
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): DeleteQueryBuilder<ENTITY>

    /**
     * Creates a builder for constructing a SELECT query.
     *
     * The example below runs a templated SELECT statement &mdash; the `/*street*/'test'` comment denotes
     * a bind variable named `street` &mdash; and maps each result row to an `Address` instance.
     * ```
     * val sql = "select * from ADDRESS where street = /*street*/'test'"
     * val query: Query<List<Address>> = QueryDsl.fromTemplate(sql)
     *     .bind("street", "STREET 10")
     *     .select { row ->
     *         Address(
     *             row.getNotNull("address_id"),
     *             row.getNotNull("street"),
     *             row.getNotNull("version"),
     *         )
     *     }
     * ```
     *
     * @param sql the sql template
     * @return the builder
     */
    fun fromTemplate(
        @Language("sql") sql: String,
    ): TemplateSelectQueryBuilder

    /**
     * Creates a query for executing an arbitrary command.
     *
     * The example below executes a templated UPDATE statement with two bind variables
     * (`id` and `street`) and returns the number of affected rows.
     * ```
     * val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
     * val query: Query<Long> = QueryDsl.executeTemplate(sql)
     *     .bind("id", 15)
     *     .bind("street", "NY street")
     * ```
     *
     * @param sql the sql template
     * @return the query
     */
    fun executeTemplate(
        @Language("sql") sql: String,
    ): TemplateExecuteQuery

    /**
     * Creates a query for executing a script.
     *
     * The example below runs a multi-statement SQL script
     * (drop, recreate, and seed a table) as a single query.
     * ```
     * val query: Query<Unit> = QueryDsl.executeScript("""
     *     drop table if exists example;
     *     create table example (id integer not null primary key, value varchar(20));
     *     insert into example (id, value) values(1, 'test');
     * """.trimIndent())
     * ```
     *
     * @param sql the script to execute
     */
    fun executeScript(
        @Language("sql") sql: String,
    ): ScriptExecuteQuery

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * The example below issues `CREATE TABLE` statements (and any associated constraints)
     * for the `address` and `employee` entities.
     * ```
     * val query: Query<Unit> = QueryDsl.create(listOf(Meta.address, Meta.employee))
     * ```
     *
     * @param metamodels the entity metamodels
     */
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * The example below issues `CREATE TABLE` statements (and any associated constraints)
     * for the `address` and `employee` entities.
     * ```
     * val query: Query<Unit> = QueryDsl.create(Meta.address, Meta.employee)
     * ```
     *
     * @param metamodels the entity metamodels
     */
    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * The example below issues `DROP TABLE` statements (and any associated constraints)
     * for the `address` and `employee` entities.
     * ```
     * val query: Query<Unit> = QueryDsl.drop(listOf(Meta.address, Meta.employee))
     * ```
     *
     * @param metamodels the entity metamodels
     */
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * The example below issues `DROP TABLE` statements (and any associated constraints)
     * for the `address` and `employee` entities.
     * ```
     * val query: Query<Unit> = QueryDsl.drop(Meta.address, Meta.employee)
     * ```
     *
     * @param metamodels the entity metamodels
     */
    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery

    /**
     * The companion object for the `QueryDsl` interface, delegating to a new instance of `QueryDsl`.
     */
    companion object : QueryDsl by QueryDsl()
}

internal class QueryDslImpl(
    private val deleteOptions: DeleteOptions,
    private val insertOptions: InsertOptions,
    private val schemaOptions: SchemaOptions,
    private val scriptOptions: ScriptOptions,
    private val selectOptions: SelectOptions,
    private val templateExecuteOptions: TemplateExecuteOptions,
    private val templateSelectOptions: TemplateSelectOptions,
    private val updateOptions: UpdateOptions,
) : QueryDsl {
    override fun with(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl {
        val with = With(false, listOf(metamodel to subquery))
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun with(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl {
        val with = With(false, pairs.toList())
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun withRecursive(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl {
        val with = With(true, listOf(metamodel to subquery))
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun withRecursive(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl {
        val with = With(true, pairs.toList())
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, options = selectOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
        subquery: SubqueryExpression<*>,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, subquery, options = selectOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> values(
        metamodel: META,
        declaration: ValuesDeclaration<ENTITY, ID, META>,
    ): SubqueryExpression<ENTITY> {
        val rows = mutableListOf<List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>>()
        val scope = ValuesScope(metamodel, rows)
        scope.declaration()
        val context = ValuesContext(metamodel, rows, selectOptions)
        return ValuesExpression(context)
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        return from(EmptyMetamodel).select(expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A?, B?>> {
        return from(EmptyMetamodel).select(expression1, expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A?, B?, C?>> {
        return from(EmptyMetamodel).select(expression1, expression2, expression3)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        return from(EmptyMetamodel).select(expression)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): InsertQueryBuilder<ENTITY, ID, META> {
        return InsertQueryBuilderImpl(EntityInsertContext(metamodel, options = insertOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): UpdateQueryBuilder<ENTITY, ID, META> {
        return UpdateQueryBuilderImpl(EntityUpdateContext(metamodel, options = updateOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): DeleteQueryBuilder<ENTITY> {
        return DeleteQueryBuilderImpl(EntityDeleteContext(metamodel, options = deleteOptions))
    }

    override fun fromTemplate(
        @Language("sql") sql: String,
    ): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(TemplateSelectContext(sql, options = templateSelectOptions))
    }

    override fun executeTemplate(
        @Language("sql") sql: String,
    ): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(TemplateExecuteContext(sql, options = templateExecuteOptions))
    }

    override fun executeScript(
        @Language("sql") sql: String,
    ): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(ScriptContext(sql, options = scriptOptions))
    }

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(SchemaContext(metamodels, options = schemaOptions))
    }

    override fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return create(metamodels.toList())
    }

    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(SchemaContext(metamodels, options = schemaOptions))
    }

    override fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return drop(metamodels.toList())
    }
}

/**
 * Interface for creating a `WITH` query DSL.
 */
interface WithQueryDsl {
    /**
     * Creates a SELECT query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META>
}

internal data class WithQueryDslImpl(private val with: With, private val options: SelectOptions) : WithQueryDsl {
    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, with = with, options = options))
    }
}

/**
 * Creates a new instance of [QueryDsl].
 *
 * @param deleteOptions the options for DELETE queries
 * @param insertOptions the options for INSERT queries
 * @param schemaOptions the options for schema operations
 * @param scriptOptions the options for script operations
 * @param selectOptions the options for SELECT queries
 * @param templateExecuteOptions the options for template execute operations
 * @param templateSelectOptions the options for template select operations
 * @param updateOptions the options for UPDATE queries
 */
fun QueryDsl(
    deleteOptions: DeleteOptions = DeleteOptions.DEFAULT,
    insertOptions: InsertOptions = InsertOptions.DEFAULT,
    schemaOptions: SchemaOptions = SchemaOptions.DEFAULT,
    scriptOptions: ScriptOptions = ScriptOptions.DEFAULT,
    selectOptions: SelectOptions = SelectOptions.DEFAULT,
    templateExecuteOptions: TemplateExecuteOptions = TemplateExecuteOptions.DEFAULT,
    templateSelectOptions: TemplateSelectOptions = TemplateSelectOptions.DEFAULT,
    updateOptions: UpdateOptions = UpdateOptions.DEFAULT,
): QueryDsl {
    return QueryDslImpl(
        deleteOptions,
        insertOptions,
        schemaOptions,
        scriptOptions,
        selectOptions,
        templateExecuteOptions,
        templateSelectOptions,
        updateOptions,
    )
}
