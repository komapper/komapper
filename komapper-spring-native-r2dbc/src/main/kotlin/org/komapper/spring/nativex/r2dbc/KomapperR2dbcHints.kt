package org.komapper.spring.nativex.r2dbc

import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcSession
import org.komapper.spring.boot.autoconfigure.r2dbc.KomapperR2dbcAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint
import org.springframework.nativex.type.NativeConfiguration
import org.springframework.transaction.ReactiveTransactionManager

@NativeHint(
    trigger = KomapperR2dbcAutoConfiguration::class,
    types = [
        TypeHint(
            types = [
                ClockProvider::class,
                ExecutionOptions::class,
                R2dbcDatabase::class,
                R2dbcDatabaseConfig::class,
                R2dbcDataOperator::class,
                R2dbcDataTypeProvider::class,
                R2dbcDialect::class,
                R2dbcSession::class,
                Logger::class,
                LoggerFacade::class,
                ReactiveTransactionManager::class,
                StatementInspector::class,
                TemplateStatementBuilder::class,
            ],
        ),
        TypeHint(types = [Environment::class])
    ]
)
class KomapperR2dbcHints : NativeConfiguration
