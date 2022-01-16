package org.komapper.spring.nativex.r2dbc

import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcSession
import org.komapper.spring.boot.autoconfigure.r2dbc.KomapperAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint
import org.springframework.nativex.type.NativeConfiguration

@NativeHint(
    trigger = KomapperAutoConfiguration::class,
    types = [
        TypeHint(
            types = [
                ClockProvider::class,
                ExecutionOptions::class,
                R2dbcDatabase::class,
                R2dbcDatabaseConfig::class,
                R2dbcDataType::class,
                R2dbcDialect::class,
                R2dbcSession::class,
                Logger::class,
                LoggerFacade::class,
                StatementInspector::class,
                TemplateStatementBuilder::class,
            ],
            typeNames = [
                "org.komapper.jdbc.R2dbcDatabase\$Companion",
                "org.komapper.spring.boot.autoconfigure.r2dbc.KomapperAutoConfiguration\$databaseConfig\$1"
            ]
        ),
        TypeHint(types = [Environment::class])
    ]
)
class KomapperHints : NativeConfiguration
