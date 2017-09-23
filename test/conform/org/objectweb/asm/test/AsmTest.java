/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
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

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.objectweb.asm.Opcodes;

/**
 * Base class for the parameterized ASM tests. For instance, to run a test on
 * all the precompiled test classes and, with both the ASM5 and the ASM6 API,
 * use a subclass such as the following:
 * 
 * <pre>
 * &#64;RunWith(Parameterized.class)
 * public class MyParameterizedTest extends AsmTest {
 * 
 *   &#64;Parameters(name = NAME)
 *   public static Collection<Object[]> data() {
 *     return data(Opcodes.ASM5, Opcodes.ASM6);
 *   }
 * 
 *   &#64;Test
 *   public void testSomeFeature() throws IOException {
 *     byte[] b = readClass();
 *     ClassWriter classWriter = new ClassWriter(asmApi, 0);
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Eric Bruneton
 */
@RunWith(Parameterized.class)
public abstract class AsmTest {

    /**
     * Naming pattern for the parameterized tests, used in test logs and
     * reports.
     */
    public static final String NAME = "{0}/{1}";

    /**
     * The fully qualified name of the precompiled test class used in the
     * current instance of this parameterized test.
     */
    @Parameter(0)
    public String className;

    /**
     * The ASM API version used in the current instance of this parameterized
     * test. One of {@link Opcodes.ASM4}, {@link Opcodes.ASM5} or
     * {@link Opcodes.ASM6}.
     */
    @Parameter(1)
    public int asmApi;

    /**
     * Rule that can be used in tests that except some exceptions to be thrown.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Builds a list of test parameters for a parameterized test. By returning
     * this in a static method annotated with &#64;Parameters in your subclass
     * of this class, your tests will be executed on all the precompiled test
     * classes, with all the given ASM API versions.
     * 
     * @param asmApis
     *            the ASM API versions that must be tested.
     * @return a list of pairs (className, asmApi), for all combinations of
     *         precompiled class names and given ASM API versions.
     */
    public static ArrayList<Object[]> data(int... asmApis) {
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        for (int asmApi : asmApis) {
            result.add(new Object[] { "DefaultPackage", asmApi });
            result.add(new Object[] { "jdk3.AllInstructions", asmApi });
            result.add(new Object[] { "jdk3.AllStructures", asmApi });
            result.add(new Object[] { "jdk3.AllStructures$1", asmApi });
            result.add(
                    new Object[] { "jdk3.AllStructures$InnerClass", asmApi });
            result.add(new Object[] { "jdk3.Attribute", asmApi });
            result.add(new Object[] { "jdk5.AllInstructions", asmApi });
            result.add(new Object[] { "jdk5.AllStructures", asmApi });
            result.add(new Object[] { "jdk5.AllStructures$EnumClass", asmApi });
            result.add(new Object[] { "jdk5.AllStructures$InvisibleAnnotation",
                    asmApi });
            result.add(new Object[] { "jdk8.AllFrames", asmApi });
            result.add(new Object[] { "jdk8.AllInstructions", asmApi });
            result.add(new Object[] { "jdk8.AllStructures", asmApi });
            result.add(new Object[] { "jdk8.AllStructures$1", asmApi });
            result.add(
                    new Object[] { "jdk8.AllStructures$InnerClass", asmApi });
            result.add(new Object[] { "jdk9.module-info", asmApi });
        }
        return result;
    }

    /**
     * Returns true if {@link #className} was generated with a JDK version which
     * is more recent than {@link #asmApi}. For instance, returns true for a
     * class compiled with the JDK 1.8 if the ASM API version is ASM4.
     * 
     * @return
     */
    public boolean classIsMoreRecentThanAsmApi() {
        if (className.startsWith("jdk8") && asmApi < Opcodes.ASM5) {
            return true;
        }
        if (className.startsWith("jdk9") && asmApi < Opcodes.ASM6) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if {@link #className} was generated with a JDK version which
     * is more recent than the JDK used to run the tests.
     * 
     * @return true if {@link #className} was generated with the JDK9 and the
     *         current JDK version is strictly less than 9.
     */
    public boolean classIsMoreRecentThanCurrentJdk() {
        String javaVersion = System.getProperty("java.version");
        if (className.startsWith("jdk9")
                && javaVersion.substring(3).compareTo("1.9") < 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the internal name of {@link #className}.
     */
    public String getInternalName() {
        return className.endsWith("module-info") ? "module-info"
                : className.replace('.', '/');
    }

    /**
     * Returns the content of {@link #className}.
     */
    public byte[] readClass() throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = ClassLoader.getSystemResourceAsStream(
                    className.replace('.', '/') + ".class");
            assertNotNull("Class not found " + className, inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] data = new byte[inputStream.available()];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Starts an assertion about the given class content.
     * 
     * @param classFile
     *            the content of a class.
     * @return the {@link ClassSubject} to use to make actual assertions about
     *         the given class.
     */
    public static ClassSubject assertThatClass(byte[] classFile) {
        return new ClassSubject(classFile);
    }

    /**
     * Helper to make assertions about a class.
     */
    public static class ClassSubject {
        /** The content of the class to be tested. */
        private final byte[] classFile;

        ClassSubject(byte[] classFile) {
            this.classFile = classFile;
        }

        /**
         * Asserts that a dump of the subject class into a string representation
         * contains the given string.
         * 
         * @param expectedString
         *            a string which should be contained in a dump of the
         *            subject class.
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
         * Asserts that the subject class is equal to the given class, modulo
         * some low level bytecode representation details (e.g. the order of the
         * constants in the constant pool, the order of attributes and
         * annotations, and low level details such as ldc vs ldc_w
         * instructions).
         * 
         * @param expectedString
         *            a string which should be contained in a dump of the
         *            subject class.
         */
        public void isEqualTo(byte[] expectedClassFile) {
            try {
                String dump = new ClassDump(classFile).toString();
                String expectedDump = new ClassDump(expectedClassFile)
                        .toString();
                assertEquals(expectedDump, dump);
            } catch (IOException e) {
                fail("Class can't be dumped");
            }
        }
    }

    /**
     * Loads the given class in a new class loader. Also tries to instantiate
     * the loaded class (if it is not an abstract or enum class), in order to
     * check that it passes the bytecode verification step.
     * 
     * @param className
     *            the name of the class to load.
     * @param classContent
     *            the content of the class to load.
     * @return whether the class was loaded successfully.
     */
    public static boolean loadAndInstantiate(String className,
            byte[] classContent) {
        ByteClassLoader byteClassLoader = new ByteClassLoader(className,
                classContent);
        try {
            Class<?> clazz = byteClassLoader.loadClass(className);
            if (!clazz.isEnum()
                    && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                ArrayList<Object> arguments = new ArrayList<Object>();
                for (Class<?> parameterType : constructor.getParameterTypes()) {
                    arguments.add(
                            Array.get(Array.newInstance(parameterType, 1), 0));
                }
                constructor.setAccessible(true);
                constructor.newInstance(
                        arguments.toArray(new Object[arguments.size()]));
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

    /**
     * A simple ClassLoader to test that a class can be loaded in the JVM.
     */
    static class ByteClassLoader extends ClassLoader {
        private final String className;
        private final byte[] classContent;
        private boolean classLoaded;

        public ByteClassLoader(String className, byte[] classContent) {
            this.className = className;
            this.classContent = classContent;
        }

        public boolean classLoaded() {
            return classLoaded;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            if (name.equals(className)) {
                classLoaded = true;
                return defineClass(className, classContent, 0,
                        classContent.length);
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }
}
