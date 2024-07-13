package integration.core

import org.komapper.core.Database

public interface Setting<DATABASE : Database> {
    public val database: DATABASE
    public val dbms: Dbms
    public val createSql: String
    public val resetSql: String?
}
