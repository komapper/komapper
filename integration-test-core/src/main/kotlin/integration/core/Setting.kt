package integration.core

import org.komapper.core.Database

interface Setting<DATABASE : Database> {
    val database: DATABASE
    val dbms: Dbms
    val createSql: String
    val resetSql: String?
}
