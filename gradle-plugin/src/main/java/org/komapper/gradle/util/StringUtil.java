package org.komapper.gradle.util;

public final class StringUtil {
  public static String capitalize(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    char[] chars = text.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    return new String(chars);
  }
}
