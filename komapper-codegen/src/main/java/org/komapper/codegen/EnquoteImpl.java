package org.komapper.codegen;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

class EnquoteImpl implements Enquote {

  private static final Pattern JDBC_URL_PATTERN = Pattern.compile("^jdbc:(tc:)?([^:]*):.*");

  private final String driver;

  EnquoteImpl(String url) {
    driver = extractDriver(url);
  }

  private static String extractDriver(String url) {
    var matcher = JDBC_URL_PATTERN.matcher(url);
    if (matcher.matches()) {
      return matcher.group(2).toLowerCase();
    }
    throw new IllegalArgumentException("The driver in the JDBC URL is not found. url=" + url);
  }

  @NotNull
  @Override
  public String apply(@NotNull String identifier) {
    Objects.requireNonNull(identifier);
    switch (driver) {
      case "mysql":
      case "mariadb":
        return "`" + identifier + "`";
      case "sqlserver":
        return "[" + identifier + "]";
      default:
        return '"' + identifier + '"';
    }
  }
}
