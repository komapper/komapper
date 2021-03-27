package org.komapper.core.tx

import javax.sql.DataSource

interface TransactionManager {
    val isActive: Boolean
    val isRollbackOnly: Boolean

    fun setRollbackOnly()
    fun begin(isolationLevel: TransactionIsolationLevel? = null)
    fun commit()
    fun suspend(): Transaction
    fun resume(tx: Transaction)
    fun rollback()
    fun getDataSource(): DataSource
}
