## Kotlin SQL Mapper

Komapper is a simple database access library for Kotlin.

Supported databases are as follows:

- H2 1.4.200 or above

## Strengths

- Generate meta-models at compile-time using [google/ksp](https://github.com/google/ksp).
- Flexible and natural syntax for criteria queries.
- SQL templates, called “two-way SQL”.

## Status

In Development

## Criteria Query

```kotlin
// metamodels
val e = Employee_()
val d = Department_()

// execute query
val list = db.execute {
    EntityQuery.from(e).innerJoin(d) {
        e.departmentId eq d.departmentId
    }.where {
        d.departmentName inList listOf("RESEARCH", "SALES")
    }.associate(e, d) { employee, department ->
        employee.copy(department = department)
    }
}

// print
for (employee in list) {
    println(employee.department?.departmentName)
}
```

The above query issues the following SQL statement:

```sql
select
    t0_."EMPLOYEE_ID", t0_."EMPLOYEE_NO", t0_."EMPLOYEE_NAME",
    t0_."MANAGER_ID", t0_."HIREDATE",
    t0_."SALARY", t0_."DEPARTMENT_ID", t0_."ADDRESS_ID", t0_."VERSION",
    t1_."DEPARTMENT_ID", t1_."DEPARTMENT_NO", t1_."DEPARTMENT_NAME",
    t1_."LOCATION", t1_."VERSION"
from
    "EMPLOYEE" t0_
inner join
    "DEPARTMENT" t1_ on (t0_."DEPARTMENT_ID" = t1_."DEPARTMENT_ID")
where
    t1_."DEPARTMENT_NAME" in (?, ?)
```
