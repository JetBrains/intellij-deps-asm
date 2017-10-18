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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

/**
 * Base class for the parameterized ASM tests. ASM can be used to read, write or transform any Java
 * class, ranging from very old (e.g. JDK 1.3) to very recent classes, containing all possible class
 * file structures. ASM can also be used with different variants of its API (ASM4, ASM5 or ASM6). In
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
 *   <li>the JUnit framework for parameterized tests. Using the {@link #data(Api...)} method,
 *       subclasses of this class can be instantiated for each possible (test case, precompiled
 *       class, ASM API) tuple. In each instance, the test case can access the precompiled class and
 *       the API version to be tested with the {@link #classParameter} and {@link #apiParameter}
 *       fields.
 * </ul>
 *
 * For instance, to run a test on all the precompiled classes, with both the ASM5 and the ASM6 API,
 * use a subclass such as the following:
 *
 * <pre>
 * &#64;RunWith(Parameterized.class)
 * public class MyParameterizedTest extends AsmTest {
 *
 *   &#64;Parameters(name = NAME)
 *   public static Collection<Object[]> data() {
 *     return data(Api.ASM5, Api.ASM6);
 *   }
 *
 *   &#64;Test
 *   public void testSomeFeature() throws IOException {
 *     byte[] b = classParameter.getBytes();
 *     ClassWriter classWriter = new ClassWriter(apiParameter.value(), 0);
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Eric Bruneton
 */
@RunWith(Parameterized.class)
public abstract class AsmTest {

  /** Naming pattern for the parameterized tests, used in test logs and reports. */
  public static final String NAME = "{0}/{1}";

  /** The precompiled class to be tested in this parameterized test instance. */
  @Parameter(0)
  public PrecompiledClass classParameter;

  /** The ASM API version to be used in this parameterized test instance. */
  @Parameter(1)
  public Api apiParameter;

  /** Rule that can be used in tests that expect some exceptions to be thrown. */
  @Rule public ExpectedException thrown = ExpectedException.none();

  /**
   * A precompiled class, hand-crafted to contain some set of class file structures. These classes
   * are not compiled as part of the build. Instead, they have been compiled beforehand, with the
   * appropriate JDKs (including some now very hard to download and install).
   */
  public static enum PrecompiledClass {
    DEFAULT_PACKAGE("DefaultPackage"),
    JDK3_ALL_INSTRUCTIONS("jdk3.AllInstructions"),
    JDK3_ALL_STRUCTURES("jdk3.AllStructures"),
    JDK3_ANONYMOUS_INNER_CLASS("jdk3.AllStructures$1"),
    JDK3_ATTRIBUTE("jdk3.Attribute"),
    JDK3_INNER_CLASS("jdk3.AllStructures$InnerClass"),
    JDK3_LARGE_METHOD("jdk3.LargeMethod"),
    JDK5_ALL_INSTRUCTIONS("jdk5.AllInstructions"),
    JDK5_ALL_STRUCTURES("jdk5.AllStructures"),
    JDK5_ANNOTATION("jdk5.AllStructures$InvisibleAnnotation"),
    JDK5_ENUM("jdk5.AllStructures$EnumClass"),
    JDK8_ALL_FRAMES("jdk8.AllFrames"),
    JDK8_ALL_INSTRUCTIONS("jdk8.AllInstructions"),
    JDK8_ALL_STRUCTURES("jdk8.AllStructures"),
    JDK8_ANONYMOUS_INNER_CLASS("jdk8.AllStructures$1"),
    JDK8_INNER_CLASS("jdk8.AllStructures$InnerClass"),
    JDK8_LARGE_METHOD("jdk8.LargeMethod"),
    JDK9_MODULE("jdk9.module-info");

    private final String name;

    private PrecompiledClass(String name) {
      this.name = name;
    }

    /** Returns the fully qualified name of this class. */
    public String getName() {
      return name;
    }

    /** Returns the internal name of this class. */
    public String getInternalName() {
      return name.endsWith("module-info") ? "module-info" : name.replace('.', '/');
    }

    /**
     * Returns true if this class was compiled with a JDK which is more recent than the given ASM
     * API. For instance, returns true for a class compiled with the JDK 1.8 if the ASM API version
     * is ASM4.
     *
     * @param api an ASM API version.
     * @return whether this class was compiled with a JDK which is more recent than api.
     */
    public boolean isMoreRecentThan(Api api) {
      if (name.startsWith("jdk8") && api.value() < Api.ASM5.value()) {
        return true;
      }
      if (name.startsWith("jdk9") && api.value() < Api.ASM6.value()) {
        return true;
      }
      return false;
    }

    /**
     * Returns true if this class was compiled with a JDK which is more recent than the JDK used to
     * run the tests.
     *
     * @return true if this class was compiled with the JDK9 and the current JDK version is strictly
     *     less than 9.
     */
    public boolean isMoreRecentThanCurrentJdk() {
      if (name.startsWith("jdk9")) {
        final String V9 = "1.9";
        String javaVersion = System.getProperty("java.version");
        return javaVersion.substring(V9.length()).compareTo(V9) < 0;
      }
      return false;
    }

    /** Returns the content of this class. */
    public byte[] getBytes() {
      InputStream inputStream = null;
      try {
        inputStream = ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class");
        assertNotNull("Class not found " + name, inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[inputStream.available()];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
          outputStream.write(data, 0, bytesRead);
        }
        outputStream.flush();
        return outputStream.toByteArray();
      } catch (IOException e) {
        fail("Can't read " + name);
        return null;
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException ignored) {
          }
        }
      }
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /** An ASM API version. */
  public static enum Api {
    ASM4("ASM4", 4 << 16),
    ASM5("ASM5", 5 << 16),
    ASM6("ASM6", 6 << 16);

    private final String name;
    private final int value;

    private Api(String name, int value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Returns the int value of this version, as expected by ASM.
     *
     * @return one of {@link Opcodes.ASM4},{@link Opcodes.ASM5} or {@link Opcodes.ASM6}.
     */
    public int value() {
      return value;
    }

    /**
     * Returns a human readable symbol corresponding to this version.
     *
     * @return one of "ASM4", "ASM5" or "ASM6".
     */
    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Builds a list of test parameters for a parameterized test. By returning this in a static method
   * annotated with &#64;Parameters in a subclass of this class, each test case will be executed on
   * all the precompiled classes, with all the given ASM API versions.
   *
   * @param apis the ASM API versions that must be tested.
   * @return all the possible (precompiledClass, api) pairs, for all the precompiled classes and all
   *     the given ASM API versions.
   */
  public static List<Object[]> data(Api... apis) {
    PrecompiledClass[] values = PrecompiledClass.values();
    ArrayList<Object[]> result = new ArrayList<Object[]>();
    for (Api api : apis) {
      for (PrecompiledClass precompiledClass : values) {
        result.add(new Object[] {precompiledClass, api});
      }
    }
    return result;
  }

  /**
   * Starts an assertion about the given class content.
   *
   * @param classFile the content of a class.
   * @return the {@link ClassSubject} to use to make actual assertions about the given class.
   */
  public static ClassSubject assertThatClass(byte[] classFile) {
    return new ClassSubject(classFile);
  }

  /** Helper to make assertions about a class. */
  public static class ClassSubject {
    /** The content of the class to be tested. */
    private final byte[] classFile;

    ClassSubject(byte[] classFile) {
      this.classFile = classFile;
    }

    /**
     * Asserts that a dump of the subject class into a string representation contains the given
     * string.
     *
     * @param expectedString a string which should be contained in a dump of the subject class.
     */
    public void contains(String expectedString) {
      try {
        String dump = new ClassDump(classFile).toString();
        assertTrue(dump.contains(expectedString));
      } catch (IOException e) {
        fail("Class can't be dumped");
      }
    }

    /**
     * Asserts that the subject class is equal to the given class, modulo some low level bytecode
     * representation details (e.g. the order of the constants in the constant pool, the order of
     * attributes and annotations, and low level details such as ldc vs ldc_w instructions).
     *
     * @param expectedString a string which should be contained in a dump of the subject class.
     */
    public void isEqualTo(byte[] expectedClassFile) {
      try {
        String dump = new ClassDump(classFile).toString();
        String expectedDump = new ClassDump(expectedClassFile).toString();
        assertEquals(expectedDump, dump);
      } catch (IOException e) {
        fail("Class can't be dumped");
      }
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
  public static boolean loadAndInstantiate(String className, byte[] classContent) {
    ByteClassLoader byteClassLoader = new ByteClassLoader(className, classContent);
    try {
      Class<?> clazz = byteClassLoader.loadClass(className);
      if (!clazz.isEnum() && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        ArrayList<Object> arguments = new ArrayList<Object>();
        for (Class<?> parameterType : constructor.getParameterTypes()) {
          arguments.add(Array.get(Array.newInstance(parameterType, 1), 0));
        }
        constructor.setAccessible(true);
        constructor.newInstance(arguments.toArray(new Object[arguments.size()]));
      }
    } catch (ClassNotFoundException e) {
      // Should never happen given the ByteClassLoader implementation.
      fail("Can't find class " + className);
    } catch (InstantiationException e) {
      // Should never happen since we don't try to instantiate classes
      // that can't be instantiated (abstract and enum classes).
      fail("Can't instantiate class " + className);
    } catch (IllegalAccessException e) {
      // Should never happen since we use setAccessible(true).
      fail("Can't instantiate class " + className);
    } catch (IllegalArgumentException e) {
      // Should never happen since we create the appropriate constructor
      // arguments for the expected constructor parameters.
      fail("Can't instantiate class " + className);
    } catch (InvocationTargetException e) {
      // If an exception occurs in the invoked constructor, it means the
      // class was successfully verified first.
    }
    return byteClassLoader.classLoaded();
  }

  /** A simple ClassLoader to test that a class can be loaded in the JVM. */
  private static class ByteClassLoader extends ClassLoader {
    private final String className;
    private final byte[] classContent;
    private boolean classLoaded;

    ByteClassLoader(String className, byte[] classContent) {
      this.className = className;
      this.classContent = classContent;
    }

    boolean classLoaded() {
      return classLoaded;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (name.equals(className)) {
        classLoaded = true;
        return defineClass(className, classContent, 0, classContent.length);
      } else {
        return super.loadClass(name, resolve);
      }
    }
  }
}
