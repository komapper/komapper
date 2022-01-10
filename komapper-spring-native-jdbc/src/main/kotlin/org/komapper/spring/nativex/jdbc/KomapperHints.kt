package org.komapper.spring.nativex.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcSession
import org.komapper.spring.boot.autoconfigure.jdbc.KomapperAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint
import org.springframework.nativex.type.NativeConfiguration
import java.util.Optional

@NativeHint(
    trigger = KomapperAutoConfiguration::class,
    types = [
        TypeHint(
            types = [
                ClockProvider::class,
                ExecutionOptions::class,
                JdbcDatabase::class,
                JdbcDatabaseConfig::class,
                JdbcDataFactory::class,
                JdbcDataType::class,
                JdbcDialect::class,
                JdbcSession::class,
                Logger::class,
                LoggerFacade::class,
                StatementInspector::class,
                TemplateStatementBuilder::class,
            ],
            typeNames = [
                "org.komapper.jdbc.JdbcDatabase\$Companion",
                "org.komapper.spring.boot.autoconfigure.jdbc.KomapperAutoConfiguration\$databaseConfig\$1"
            ]
        ),
        TypeHint(
            types = [
                Environment::class,
                Optional::class,
            ]
        )
    ]
)
class KomapperHints : NativeConfiguration
