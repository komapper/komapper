package org.komapper.core

import java.sql.JDBCType
import kotlin.reflect.KType

@ThreadSafe
interface SqlType {
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
     * The data type defined in the standard library.
     */
    val sqlType: JDBCType
}
