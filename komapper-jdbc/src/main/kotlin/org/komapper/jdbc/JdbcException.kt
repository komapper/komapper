package org.komapper.jdbc

import java.sql.SQLException

class JdbcException(cause: SQLException) : RuntimeException(cause)
