package integration.setting

interface Setting<CONFIG> {
    val config: CONFIG
    val dbms: Dbms
    val createSql: String
    val resetSql: String?
}
