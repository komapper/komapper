package org.komapper.core.tx

import javax.sql.DataSource

class EmptyTransactionManager(private val dataSource: DataSource) : TransactionManager {
    override val isActive: Boolean = false
    override var isRollbackOnly: Boolean = false

    override fun setRollbackOnly() {
    }

    override fun begin(isolationLevel: TransactionIsolationLevel?) {
    }

    override fun commit() {
    }

    override fun suspend(): Transaction {
        throw UnsupportedOperationException()
    }

    override fun resume(tx: Transaction) {
    }

    override fun rollback() {
    }

    override fun getDataSource(): DataSource {
        return dataSource
    }
}
