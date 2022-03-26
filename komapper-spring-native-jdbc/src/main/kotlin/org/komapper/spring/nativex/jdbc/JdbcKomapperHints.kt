package org.komapper.spring.nativex.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.StatementInspector
import org.komapper.core.TemplateStatementBuilder
import org.komapper.jdbc.JdbcDataFactory
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcSession
import org.komapper.spring.boot.autoconfigure.jdbc.JdbcKomapperAutoConfiguration
import org.springframework.core.env.Environment
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint
import org.springframework.nativex.type.NativeConfiguration
import org.springframework.transaction.PlatformTransactionManager

@NativeHint(
    trigger = JdbcKomapperAutoConfiguration::class,
    types = [
        TypeHint(
            types = [
                ClockProvider::class,
                ExecutionOptions::class,
                JdbcDatabase::class,
                JdbcDatabaseConfig::class,
                JdbcDataFactory::class,
                JdbcDataOperator::class,
                JdbcDataTypeProvider::class,
                JdbcDialect::class,
                JdbcSession::class,
                Logger::class,
                LoggerFacade::class,
                PlatformTransactionManager::class,
                StatementInspector::class,
                TemplateStatementBuilder::class,
            ],
        ),
        TypeHint(
            types = [
                Environment::class,
            ]
        )
    ]
)
class JdbcKomapperHints : NativeConfiguration
