package org.komapper.quarkus.jdbc;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import org.komapper.core.ClockProvider;
import org.komapper.core.DefaultClockProvider;
import org.komapper.core.DefaultLoggerFacade;
import org.komapper.core.ExecutionOptions;
import org.komapper.core.Logger;
import org.komapper.core.LoggerFacade;
import org.komapper.core.Loggers;
import org.komapper.core.StatementInspector;
import org.komapper.core.StatementInspectors;

import javax.inject.Singleton;

@Singleton
@Unremovable
public class KomapperProducer {

  @Singleton
  @DefaultBean
  @Unremovable
  public ClockProvider clockProvider() {
    return new DefaultClockProvider();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public ExecutionOptions executionOptions() {
    return new ExecutionOptions();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public Logger logger() {
    return Loggers.INSTANCE.get();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public LoggerFacade loggerFacade(Logger logger) {
    return new DefaultLoggerFacade(logger);
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public StatementInspector statementInspector() {
    return StatementInspectors.INSTANCE.get();
  }
}