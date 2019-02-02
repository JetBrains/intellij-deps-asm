package org.objectweb.asm.test;

import java.util.StringTokenizer;

/**
 * Provides utility methods for the asm.test package.
 *
 * @author Eric Bruneton
 */
final class Util {

  private Util() {}

  static int getMajorJavaVersion() {
    String javaVersion = System.getProperty("java.version");
    String javaMajorVersion = new StringTokenizer(javaVersion, "._").nextToken();
    return Integer.parseInt(javaMajorVersion);
  }
}
