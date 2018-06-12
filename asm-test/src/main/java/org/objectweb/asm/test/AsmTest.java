// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Base class for the ASM tests. ASM can be used to read, write or transform any Java class, ranging
 * from very old (e.g. JDK 1.3) to very recent classes, containing all possible class file
 * structures. ASM can also be used with different variants of its API (ASM4, ASM5, ASM6, etc). In
 * order to test it thoroughly, it is therefore necessary to run read, write and transform tests,
 * for each API version, and for each class in a set of classes containing all possible class file
 * structures. The purpose of this class is to automate this process. For this it relies on:
 *
 * <ul>
 *   <li>a small set of hand-crafted classes designed to contain as much class file structures as
 *       possible (it is impossible to represent all possible bytecode sequences). These classes are
 *       called "precompiled classes" below, because they are not compiled as part of the build.
 *       Instead, they have been compiled beforehand with the appropriate JDKs (e.g. with the JDK
 *       1.3, 1.5, etc).
 *   <li>the JUnit framework for parameterized tests. Using the {@link #allClassesAndAllApis()}
 *       method, selected test methods can be instantiated for each possible (precompiled class, ASM
 *       API) tuple.
 * </ul>
 *
 * <p>For instance, to run a test on all the precompiled classes, with all the APIs, use a subclass
 * such as the following:
 *
 * <pre>
 * public class MyParameterizedTest extends AsmTest {
 *
 *   &#64;ParameterizedTest
 *   &#64;MethodSource(ALL_CLASSES_AND_ALL_APIS)
 *   public void testSomeFeature(PrecompiledClass classParameter, Api apiParameter) {
 *     byte[] b = classParameter.getBytes();
 *     ClassWriter classWriter = new ClassWriter(apiParameter.value(), 0);
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Eric Bruneton
 */
public abstract class AsmTest {

  /** The size of the temporary byte array used to read class input streams chunk by chunk. */
  private static final int INPUT_STREAM_DATA_CHUNK_SIZE = 4096;

  /** The name of JDK9 module classes. */
  private static final String MODULE_INFO = "module-info";

  /**
   * MethodSource name to be used in parameterized tests that must be instantiated for all possible
   * (precompiled class, api) pairs.
   */
  public static final String ALL_CLASSES_AND_ALL_APIS = "allClassesAndAllApis";

  /**
   * MethodSource name to be used in parameterized tests that must be instantiated for all
   * precompiled classes, with the latest api.
   */
  public static final String ALL_CLASSES_AND_LATEST_API = "allClassesAndLatestApi";

  /**
   * A precompiled class, hand-crafted to contain some set of class file structures. These classes
   * are not compiled as part of the build. Instead, they have been compiled beforehand, with the
   * appropriate JDKs (including some now very hard to download and install).
   */
  public enum PrecompiledClass {
    DEFAULT_PACKAGE("DefaultPackage"),
    JDK3_ALL_INSTRUCTIONS("jdk3.AllInstructions"),
    JDK3_ALL_STRUCTURES("jdk3.AllStructures"),
    JDK3_ANONYMOUS_INNER_CLASS("jdk3.AllStructures$1"),
    JDK3_ARTIFICIAL_STRUCTURES("jdk3.ArtificialStructures"),
    JDK3_INNER_CLASS("jdk3.AllStructures$InnerClass"),
    JDK3_LARGE_METHOD("jdk3.LargeMethod"),
    JDK5_ALL_INSTRUCTIONS("jdk5.AllInstructions"),
    JDK5_ALL_STRUCTURES("jdk5.AllStructures"),
    JDK5_ANNOTATION("jdk5.AllStructures$InvisibleAnnotation"),
    JDK5_ENUM("jdk5.AllStructures$EnumClass"),
    JDK5_LOCAL_CLASS("jdk5.AllStructures$1LocalClass"),
    JDK8_ALL_FRAMES("jdk8.AllFrames"),
    JDK8_ALL_INSTRUCTIONS("jdk8.AllInstructions"),
    JDK8_ALL_STRUCTURES("jdk8.AllStructures"),
    JDK8_ANONYMOUS_INNER_CLASS("jdk8.AllStructures$1"),
    JDK8_ARTIFICIAL_STRUCTURES("jdk8.ArtificialStructures"),
    JDK8_INNER_CLASS("jdk8.AllStructures$InnerClass"),
    JDK8_LARGE_METHOD("jdk8.LargeMethod"),
    JDK9_MODULE("jdk9.module-info"),
    JDK11_ALL_INSTRUCTIONS("jdk11.AllInstructions"),
    JDK11_ALL_STRUCTURES("jdk11.AllStructures"),
    JDK11_ALL_STRUCTURES_NESTED("jdk11.AllStructures$Nested"),
    JDK11_LAMBDA_CONDY("jdk11.LambdaCondy");

    private final String name;

    PrecompiledClass(final String name) {
      this.name = name;
    }

    /** @return the fully qualified name of this class. */
    public String getName() {
      return name;
    }

    /** @return the internal name of this class. */
    public String getInternalName() {
      return name.endsWith(MODULE_INFO) ? MODULE_INFO : name.replace('.', '/');
    }

    /**
     * Returns true if this class was compiled with a JDK which is more recent than the given ASM
     * API. For instance, returns true for a class compiled with the JDK 1.8 if the ASM API version
     * is ASM4.
     *
     * @param api an ASM API version.
     * @return whether this class was compiled with a JDK which is more recent than api.
     */
    public boolean isMoreRecentThan(final Api api) {
      if (name.startsWith("jdk8.") && api.value() < Api.ASM5.value()) {
        return true;
      }
      if (name.startsWith("jdk9.") && api.value() < Api.ASM6.value()) {
        return true;
      }
      return name.startsWith("jdk11.") && api.value() < Api.ASM7.value();
    }

    /**
     * Returns true if this class was compiled with a JDK which is more recent than the JDK used to
     * run the tests.
     *
     * @return true if this class was compiled with the JDK9 and the current JDK version is strictly
     *     less than 9.
     */
    public boolean isMoreRecentThanCurrentJdk() {
      if (name.startsWith("jdk9.")) {
        return getMajorJavaVersion() < 9;
      }
      if (name.startsWith("jdk11.")) {
        return getMajorJavaVersion() < 11;
      }
      return false;
    }

    /** @return the content of this class. */
    public byte[] getBytes() {
      return AsmTest.getBytes(name);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * An invalid class, hand-crafted to contain some set of invalid class file structures. These
   * classes are not compiled as part of the build. Instead, they have been compiled beforehand, and
   * then manually edited to introduce errors.
   */
  public enum InvalidClass {
    INVALID_BYTECODE_OFFSET("invalid.InvalidBytecodeOffset"),
    INVALID_CLASS_VERSION("invalid.InvalidClassVersion"),
    INVALID_CONSTANT_POOL_INDEX("invalid.InvalidConstantPoolIndex"),
    INVALID_CONSTANT_POOL_REFERENCE("invalid.InvalidConstantPoolReference"),
    INVALID_CP_INFO_TAG("invalid.InvalidCpInfoTag"),
    INVALID_ELEMENT_VALUE("invalid.InvalidElementValue"),
    INVALID_INSN_TYPE_ANNOTATION_TARGET_TYPE("invalid.InvalidInsnTypeAnnotationTargetType"),
    INVALID_OPCODE("invalid.InvalidOpcode"),
    INVALID_STACK_MAP_FRAME_TYPE("invalid.InvalidStackMapFrameType"),
    INVALID_TYPE_ANNOTATION_TARGET_TYPE("invalid.InvalidTypeAnnotationTargetType"),
    INVALID_VERIFICATION_TYPE_INFO("invalid.InvalidVerificationTypeInfo"),
    INVALID_WIDE_OPCODE("invalid.InvalidWideOpcode");

    private final String name;

    InvalidClass(final String name) {
      this.name = name;
    }

    /** @return the content of this class. */
    public byte[] getBytes() {
      return AsmTest.getBytes(name);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /** An ASM API version. */
  public enum Api {
    ASM4("ASM4", 4 << 16),
    ASM5("ASM5", 5 << 16),
    ASM6("ASM6", 6 << 16),
    ASM7("ASM7", 1 << 24 | 7 << 16);

    private final String name;
    private final int value;

    Api(final String name, final int value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Returns the int value of this version, as expected by ASM.
     *
     * @return one of the ASM4, ASM5, ASM6 or ASM7 constants from the ASM Opcodes interface.
     */
    public int value() {
      return value;
    }

    /**
     * Returns a human readable symbol corresponding to this version.
     *
     * @return one of "ASM4", "ASM5", "ASM6" or "ASM7".
     */
    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Builds a list of test arguments for a parameterized test. Parameterized test cases annotated
   * with <tt>@MethodSource("allClassesAndAllApis")</tt> will be executed on all the possible
   * (precompiledClass, api) pairs.
   *
   * @return all the possible (precompiledClass, api) pairs, for all the precompiled classes and all
   *     the given ASM API versions.
   */
  public static Stream<Arguments> allClassesAndAllApis() {
    return classesAndApis(Api.values());
  }

  /**
   * Builds a list of test arguments for a parameterized test. Parameterized test cases annotated
   * with <tt>@MethodSource("allClassesAndLatestApi")</tt> will be executed on all the precompiled
   * classes, with the latest api.
   *
   * @return all the possible (precompiledClass, ASM7) pairs, for all the precompiled classes.
   */
  public static Stream<Arguments> allClassesAndLatestApi() {
    return classesAndApis(Api.ASM7);
  }

  private static Stream<Arguments> classesAndApis(final Api... apis) {
    return Arrays.stream(PrecompiledClass.values())
        .flatMap(
            precompiledClass ->
                Arrays.stream(apis).map(api -> Arguments.of(precompiledClass, api)));
  }

  private static int getMajorJavaVersion() {
    String javaVersion = System.getProperty("java.version");
    String javaMajorVersion = new StringTokenizer(javaVersion, "._").nextToken();
    return Integer.parseInt(javaMajorVersion);
  }

  /**
   * Starts an assertion about the given class content.
   *
   * @param classFile the content of a class.
   * @return the {@link ClassSubject} to use to make actual assertions about the given class.
   */
  public static ClassSubject assertThatClass(final byte[] classFile) {
    return new ClassSubject(classFile);
  }

  /** Helper to make assertions about a class. */
  public static class ClassSubject {
    /** The content of the class to be tested. */
    private final byte[] classFile;

    ClassSubject(final byte[] classFile) {
      this.classFile = classFile;
    }

    /**
     * Asserts that a dump of the subject class into a string representation contains the given
     * string.
     *
     * @param expectedString a string which should be contained in a dump of the subject class.
     */
    public void contains(final String expectedString) {
      try {
        String dump = new ClassDump(classFile).toString();
        assertTrue(dump.contains(expectedString));
      } catch (IOException | IllegalArgumentException e) {
        fail("Class can't be dumped", e);
      }
    }

    /**
     * Asserts that the subject class is equal to the given class, modulo some low level bytecode
     * representation details (e.g. the order of the constants in the constant pool, the order of
     * attributes and annotations, and low level details such as ldc vs ldc_w instructions).
     *
     * @param expectedClassFile a class file content which should be equal to the subject class.
     */
    public void isEqualTo(final byte[] expectedClassFile) {
      try {
        String dump = new ClassDump(classFile).toString();
        String expectedDump = new ClassDump(expectedClassFile).toString();
        assertEquals(expectedDump, dump);
      } catch (IOException | IllegalArgumentException e) {
        fail("Class can't be dumped", e);
      }
    }
  }

  /**
   * Loads the given class in a new class loader. Also tries to instantiate the loaded class (if it
   * is not an abstract or enum class, or a module-info class), in order to check that it passes the
   * bytecode verification step. Checks as well that the class can be dumped, to make sure that the
   * class is well formed.
   *
   * @param className the name of the class to load.
   * @param classContent the content of the class to load.
   * @return whether the class was loaded successfully.
   */
  public static boolean loadAndInstantiate(final String className, final byte[] classContent) {
    try {
      new ClassDump(classContent);
    } catch (IOException | IllegalArgumentException e) {
      fail("Class can't be dumped, probably invalid", e);
    }
    if (className.endsWith(MODULE_INFO)) {
      if (getMajorJavaVersion() < 9) {
        throw new UnsupportedClassVersionError();
      } else {
        return true;
      }
    } else {
      return doLoadAndInstantiate(className, classContent);
    }
  }

  /**
   * Loads the given class in a new class loader. Also tries to instantiate the loaded class (if it
   * is not an abstract or enum class), in order to check that it passes the bytecode verification
   * step.
   *
   * @param className the name of the class to load.
   * @param classContent the content of the class to load.
   * @return whether the class was loaded successfully.
   */
  static boolean doLoadAndInstantiate(final String className, final byte[] classContent) {
    ByteClassLoader byteClassLoader = new ByteClassLoader(className, classContent);
    try {
      Class<?> clazz = byteClassLoader.loadClass(className);
      if (!clazz.isEnum() && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        ArrayList<Object> arguments = new ArrayList<>();
        for (Class<?> parameterType : constructor.getParameterTypes()) {
          arguments.add(Array.get(Array.newInstance(parameterType, 1), 0));
        }
        constructor.setAccessible(true);
        constructor.newInstance(arguments.toArray(new Object[arguments.size()]));
      }
    } catch (ClassNotFoundException e) {
      // Should never happen given the ByteClassLoader implementation.
      fail("Can't find class " + className, e);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      // Should never happen since we don't try to instantiate classes that can't be instantiated
      // (abstract and enum classes), we use setAccessible(true), and we create the appropriate
      // constructor arguments for the expected constructor parameters.
      fail("Can't instantiate class " + className, e);
    } catch (InvocationTargetException e) {
      // If an exception occurs in the invoked constructor, it means the class was successfully
      // verified first.
    }
    return byteClassLoader.classLoaded();
  }

  /** A simple ClassLoader to test that a class can be loaded in the JVM. */
  private static class ByteClassLoader extends ClassLoader {
    private final String className;
    private final byte[] classContent;
    private boolean classLoaded;

    ByteClassLoader(final String className, final byte[] classContent) {
      this.className = className;
      this.classContent = classContent;
    }

    boolean classLoaded() {
      return classLoaded;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
      if (name.equals(className)) {
        classLoaded = true;
        return defineClass(className, classContent, 0, classContent.length);
      } else {
        return super.loadClass(name, resolve);
      }
    }
  }

  private static byte[] getBytes(final String name) {
    String resourceName = name.replace('.', '/') + ".class";
    try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
      assertNotNull(inputStream, "Class not found " + name);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] data = new byte[INPUT_STREAM_DATA_CHUNK_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, bytesRead);
      }
      outputStream.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      fail("Can't read " + name, e);
      return new byte[0];
    }
  }
}
