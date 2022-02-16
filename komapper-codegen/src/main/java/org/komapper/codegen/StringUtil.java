package org.komapper.codegen;

final class StringUtil {

  public static String snakeToUpperCamelCase(String text) {
    if (text.isBlank()) {
      return text;
    }
    return capitalize(snakeToCamelCase(text));
  }

  public static String snakeToLowerCamelCase(String text) {
    if (text.isBlank()) {
      return text;
    }
    return snakeToCamelCase(text);
  }

  private static String snakeToCamelCase(String text) {
    var list = text.split("_");
    var result = new StringBuilder();
    result.append(list[0].toLowerCase());
    for (int i = 1; i < list.length; i++) {
      var remaining = list[i];
      result.append(capitalize(remaining.toLowerCase()));
    }
    return result.toString();
  }

  private static String capitalize(String text) {
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }
}
