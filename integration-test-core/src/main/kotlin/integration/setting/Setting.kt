package integration.setting

interface Setting<CONFIG> {
    val config: CONFIG
    val dbms: Dbms
    val createSql: String
    val dropSql: String
    val resetSql: String?
    fun close() = Unit
}
