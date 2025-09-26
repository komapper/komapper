package org.komapper.core

import java.sql.JDBCType
import kotlin.reflect.KType

@ThreadSafe
interface DataType {
    /**
     * The data type name.
     */
    val name: String

    /**
     * The corresponding type.
     * [KType.isMarkedNullable] must be false.
     */
    val type: KType

    /**
     * The JDBC type defined in the standard library.
     */
    val jdbcType: JDBCType
}
