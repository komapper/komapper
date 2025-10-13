package org.komapper.core

import java.sql.SQLType
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
     * The SQL type defined in the [java.sql] package.
     */
    val sqlType: SQLType
}
