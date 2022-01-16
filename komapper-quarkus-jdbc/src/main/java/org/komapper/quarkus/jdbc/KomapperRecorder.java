package org.komapper.quarkus.jdbc;

import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.enterprise.inject.Default;
import org.komapper.core.ClockProvider;
import org.komapper.core.ExecutionOptions;
import org.komapper.core.Logger;
import org.komapper.core.LoggerFacade;
import org.komapper.core.StatementInspector;
import org.komapper.core.TemplateStatementBuilder;
import org.komapper.core.TemplateStatementBuilders;
import org.komapper.jdbc.DefaultJdbcDataFactory;
import org.komapper.jdbc.DefaultJdbcSession;
import org.komapper.jdbc.JdbcDataFactory;
import org.komapper.jdbc.JdbcDatabase;
import org.komapper.jdbc.JdbcDatabaseConfig;
import org.komapper.jdbc.JdbcDialect;
import org.komapper.jdbc.JdbcDialects;
import org.komapper.jdbc.JdbcSession;

@Recorder
public class KomapperRecorder {

  public Supplier<JdbcDatabaseConfig> configureJdbcDatabaseConfig(
      DataSourceDefinition dataSourceDefinition) {
    Objects.requireNonNull(dataSourceDefinition);
    return () -> {
      var container = Arc.container();
      var dataSourceResolver = container.instance(DataSourceResolver.class).get();
      var executionOptions = container.instance(ExecutionOptions.class).get();
      var clockProvider = container.instance(ClockProvider.class).get();
      var logger = container.instance(Logger.class).get();
      var loggerFacade = container.instance(LoggerFacade.class).get();
      var statementInspector = container.instance(StatementInspector.class).get();
      var id = UUID.randomUUID();
      var dialect = JdbcDialects.INSTANCE.get(dataSourceDefinition.driver, Collections.emptyList());
      var templateStatementBuilder = TemplateStatementBuilders.INSTANCE.get(dialect);
      var session = new DefaultJdbcSession(dataSourceResolver.resolve(dataSourceDefinition.name));
      var dataFactory = new DefaultJdbcDataFactory(session);
      var newExecutionOptions =
          executionOptions.plus(
              new ExecutionOptions(
                  dataSourceDefinition.batchSize,
                  dataSourceDefinition.maxRows,
                  dataSourceDefinition.fetchSize,
                  dataSourceDefinition.queryTimeout,
                  false));
      return new JdbcDatabaseConfig() {

        @Override
        public JdbcDataFactory getDataFactory() {
          return dataFactory;
        }

        @Override
        public JdbcSession getSession() {
          return session;
        }

        @Override
        public JdbcDialect getDialect() {
          return dialect;
        }

        @Override
        public TemplateStatementBuilder getTemplateStatementBuilder() {
          return templateStatementBuilder;
        }

        @Override
        public StatementInspector getStatementInspector() {
          return statementInspector;
        }

        @Override
        public LoggerFacade getLoggerFacade() {
          return loggerFacade;
        }

        @Override
        public Logger getLogger() {
          return logger;
        }

        @Override
        public ExecutionOptions getExecutionOptions() {
          return newExecutionOptions;
        }

        @Override
        public ClockProvider getClockProvider() {
          return clockProvider;
        }

        @Override
        public UUID getId() {
          return id;
        }
      };
    };
  }

  public Supplier<JdbcDatabase> configureJdbcDatabase(DataSourceDefinition dataSourceDefinition) {
    Objects.requireNonNull(dataSourceDefinition);
    return () -> {
      JdbcDatabaseConfig config = resolveJdbcDatabaseConfig(dataSourceDefinition);
      return JdbcDatabase.Companion.create(config);
    };
  }

  private JdbcDatabaseConfig resolveJdbcDatabaseConfig(DataSourceDefinition dataSourceDefinition) {
    Annotation qualifier = resolveQualifier(dataSourceDefinition);
    return Arc.container().instance(JdbcDatabaseConfig.class, qualifier).get();
  }

  private Annotation resolveQualifier(DataSourceDefinition dataSourceDefinition) {
    if (dataSourceDefinition.isDefault) {
      return Default.Literal.INSTANCE;
    }
    return new DataSource.DataSourceLiteral(dataSourceDefinition.name);
  }
}
