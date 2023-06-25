package org.komapper.codegen;

import io.github.encryptorcode.pluralize.Pluralize;

final class StringUtil {

  public static String snakeToUpperCamelCase(String text) {
    return snakeToUpperCamelCase(text, false);
  }

  public static String snakeToUpperCamelCase(String text, boolean singularize) {
    if (text.isBlank()) {
      return text;
    }
    return capitalize(snakeToCamelCase(text, singularize));
  }

  public static String snakeToLowerCamelCase(String text) {
    return snakeToLowerCamelCase(text, false);
  }

  public static String snakeToLowerCamelCase(String text, boolean singularize) {
    if (text.isBlank()) {
      return text;
    }
    return snakeToCamelCase(text, singularize);
  }

  private static String snakeToCamelCase(String text, boolean singularize) {
    var list = text.split("_");
    var result = new StringBuilder();
    if (singularize) {
      result.append(singularize(list[0].toLowerCase()));
    } else {
      result.append(list[0].toLowerCase());
    }
    for (int i = 1; i < list.length; i++) {
      var remaining = list[i];
      if (singularize) {
        result.append(capitalize(singularize(remaining.toLowerCase())));
      } else {
        result.append(capitalize(remaining.toLowerCase()));
      }
    }
    return result.toString();
  }

  private static String capitalize(String text) {
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  private static String singularize(String text) {
    return Pluralize.singular(text);
  }
}
