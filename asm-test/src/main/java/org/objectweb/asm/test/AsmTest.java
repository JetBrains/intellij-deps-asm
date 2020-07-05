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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
   * The expected pattern (i.e. regular expression) that ASM's UnsupportedOperationException
   * messages are supposed to match.
   */
  public static final String UNSUPPORTED_OPERATION_MESSAGE_PATTERN = ".* requires ASM[56789].*";

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
    JDK8_ARTIFICIAL_STRUCTURES("jdk8.Artificial$()$Structures"),
    JDK8_INNER_CLASS("jdk8.AllStructures$InnerClass"),
    JDK8_LARGE_METHOD("jdk8.LargeMethod"),
    JDK9_MODULE("jdk9.module-info"),
    JDK11_ALL_INSTRUCTIONS("jdk11.AllInstructions"),
    JDK11_ALL_STRUCTURES("jdk11.AllStructures"),
    JDK11_ALL_STRUCTURES_NESTED("jdk11.AllStructures$Nested"),
    JDK14_ALL_STRUCTURES_RECORD("jdk14.AllStructures$RecordSubType"),
    JDK14_ALL_STRUCTURES_EMPTY_RECORD("jdk14.AllStructures$EmptyRecord"),
    JDK15_ALL_STRUCTURES("jdk15.AllStructures");

    private final String name;
    private byte[] bytes;

    PrecompiledClass(final String name) {
      this.name = name;
    }

    /**
     * Returns the fully qualified name of this class.
     *
     * @return the fully qualified name of this class.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the internal name of this class.
     *
     * @return the internal name of this class.
     */
    public String getInternalName() {
      return name.endsWith(ClassFile.MODULE_INFO) ? ClassFile.MODULE_INFO : name.replace('.', '/');
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
      if (name.startsWith("jdk11.") && api.value() < Api.ASM7.value()) {
        return true;
      }
      if (name.startsWith("jdk14.") && api.value() < Api.ASM8.value()) {
        return true;
      }
      return name.startsWith("jdk15.") && api.value() < Api.ASM9.value();
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
        return Util.getMajorJavaVersion() < 9;
      }
      if (name.startsWith("jdk11.")) {
        return Util.getMajorJavaVersion() < 11;
      }
      if (name.startsWith("jdk14.")) {
        return Util.getMajorJavaVersion() < 14;
      }
      if (name.startsWith("jdk15.")) {
        return Util.getMajorJavaVersion() < 15;
      }
      return false;
    }

    /**
     * Returns the content of this class.
     *
     * @return the content of this class.
     */
    public byte[] getBytes() {
      if (bytes == null) {
        bytes = AsmTest.getBytes(name);
      }
      return bytes.clone();
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

    /**
     * Returns the content of this class.
     *
     * @return the content of this class.
     */
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
    ASM7("ASM7", 7 << 16),
    ASM8("ASM8", 8 << 16),
    ASM9("ASM9", 9 << 16),
    ;

    private final String name;
    private final int value;

    Api(final String name, final int value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Returns the int value of this version, as expected by ASM.
     *
     * @return one of the ASM4, ASM5, ASM6, ASM7, ASM8 or ASM9 constants from the ASM Opcodes
     *     interface.
     */
    public int value() {
      return value;
    }

    /**
     * Returns a human readable symbol corresponding to this version.
     *
     * @return one of "ASM4", "ASM5", "ASM6" "ASM7", "ASM8" or "ASM9".
     */
    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Builds a list of test arguments for a parameterized test. Parameterized test cases annotated
   * with {@code @MethodSource("allClassesAndAllApis")} will be executed on all the possible
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
   * with {@code @MethodSource("allClassesAndLatestApi")} will be executed on all the precompiled
   * classes, with the latest api.
   *
   * @return all the possible (precompiledClass, ASM9) pairs, for all the precompiled classes.
   */
  public static Stream<Arguments> allClassesAndLatestApi() {
    return classesAndApis(Api.ASM9);
  }

  private static Stream<Arguments> classesAndApis(final Api... apis) {
    return Arrays.stream(PrecompiledClass.values())
        .flatMap(
            precompiledClass ->
                Arrays.stream(apis).map(api -> Arguments.of(precompiledClass, api)));
  }

  private static byte[] getBytes(final String name) {
    String resourceName = name.replace('.', '/') + ".class";
    try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Class not found " + name);
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] data = new byte[INPUT_STREAM_DATA_CHUNK_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, bytesRead);
      }
      outputStream.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new ClassFormatException("Can't read " + name, e);
    }
  }
}
