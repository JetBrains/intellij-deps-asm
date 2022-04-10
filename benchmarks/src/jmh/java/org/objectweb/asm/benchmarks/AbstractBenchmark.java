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
package org.objectweb.asm.benchmarks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * A benchmark to benchmark different versions of ASM and possibly other bytecode libraries.
 *
 * @author Eric Bruneton
 */
public abstract class AbstractBenchmark {

  // The root directory of the ASM project.
  private static final String ROOT_DIR = System.getProperty("user.dir");

  // The sub directories of ROOT_DIR where the different versions of ASM can be found.
  private static final String BUILD_DIR = "/benchmarks/build/";
  private static final String ASM4_0 = BUILD_DIR + "asm4.0/";
  private static final String ASM5_0 = BUILD_DIR + "asm5.0.1/";
  private static final String ASM6_0 = BUILD_DIR + "asm6.0/";
  private static final String ASM7_0 = BUILD_DIR + "asm7.0/";
  private static final String ASM8_0 = BUILD_DIR + "asm8.0.1/";
  private static final String ASM9_0 = BUILD_DIR + "asm9.0/";

  // The directories where the Java 1.5 input data classes for the benchmarks can be found.
  private static final String ASM_CORE_CURRENT = "/asm/build/classes/java/main/";
  private static final String ASM_TREE_CURRENT = "/asm-tree/build/classes/java/main/";

  // The directory where the Java 8 input data classes for the benchmarks can be found.
  private static final String INPUT_CLASSES_JAVA8 =
      "/benchmarks/build/input-classes-java8/io/vavr/control/";

  private final String asmBenchmarkClass;

  /**
   * Some Java 1.5 class files that can be used as input data for benchmarks. These classes do not
   * have stack map frames, invokedynamic, etc, and thus cannot be used to benchmark ASM performance
   * for these features. However, they can be used to compare ASM with other libraries that don't
   * support these class file features (including ASM itself, before ASM 5.0).
   */
  ArrayList<byte[]> classFiles;

  /**
   * Some Java 8 class files that can be used as input data for ASM benchmarks. These classes
   * contain stack map frames and invokedynamic instructions.
   */
  ArrayList<byte[]> java8classFiles;

  /** The ASM versions that can be benchmarked. */
  public enum AsmVersion {
    V4_0,
    V5_0,
    V6_0,
    V7_0,
    V8_0,
    V9_0,
    V_CURRENT;

    URL[] getUrls(final String baseUrl) throws MalformedURLException {
      switch (this) {
        case V4_0:
          return new URL[] {new URL(baseUrl + ASM4_0)};
        case V5_0:
          return new URL[] {new URL(baseUrl + ASM5_0)};
        case V6_0:
          return new URL[] {new URL(baseUrl + ASM6_0)};
        case V7_0:
          return new URL[] {new URL(baseUrl + ASM7_0)};
        case V8_0:
          return new URL[] {new URL(baseUrl + ASM8_0)};
        case V9_0:
          return new URL[] {new URL(baseUrl + ASM9_0)};
        case V_CURRENT:
          return new URL[] {
            new URL(baseUrl + ASM_CORE_CURRENT), new URL(baseUrl + ASM_TREE_CURRENT)
          };
        default:
          throw new AssertionError();
      }
    }
  }

  /**
   * Constructs a new {@link AbstractBenchmark}.
   *
   * @param asmBenchmarkClass the benchmark class to instantiate for the ASM benchmarks.
   */
  protected AbstractBenchmark(final String asmBenchmarkClass) {
    this.asmBenchmarkClass = asmBenchmarkClass;
  }

  /** Creates and populates {@link #classFiles} with some class files read from disk. */
  protected void prepareClasses() throws IOException {
    classFiles = new ArrayList<>();
    java8classFiles = new ArrayList<>();
    findClasses(new File(ROOT_DIR + ASM_CORE_CURRENT), classFiles);
    findClasses(new File(ROOT_DIR + ASM_TREE_CURRENT), classFiles);
    findClasses(new File(ROOT_DIR + INPUT_CLASSES_JAVA8), java8classFiles);
  }

  private static void findClasses(final File directory, final ArrayList<byte[]> classFiles)
      throws IOException {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        findClasses(file, classFiles);
      } else if (file.getName().endsWith(".class")) {
        classFiles.add(readInputStream(Files.newInputStream(file.toPath())));
      }
    }
  }

  private static byte[] readInputStream(final InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] data = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, bytesRead);
      }
      outputStream.flush();
      return outputStream.toByteArray();
    }
  }

  /** A factory of ASM benchmark objects, using a specific version of the ASM library. */
  class AsmBenchmarkFactory extends URLClassLoader {

    /**
     * Constructs an {@link AsmBenchmarkFactory}.
     *
     * @param asmVersion the ASM version to use.
     * @throws MalformedURLException if the ROOT_DIR path is malformed.
     */
    AsmBenchmarkFactory(final AsmVersion asmVersion) throws MalformedURLException {
      super(asmVersion.getUrls("file://" + ROOT_DIR));
    }

    /**
     * Returns a new instance of the class specified in the benchmark's constructor.
     *
     * @return a new instance of the class specified in the benchmark's constructor.
     * @throws ClassNotFoundException if the class can't be found.
     * @throws ReflectiveOperationException if the class can't be instantiated.
     */
    public Object newAsmBenchmark() throws ClassNotFoundException, ReflectiveOperationException {
      return loadClass(asmBenchmarkClass).newInstance();
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
      // Force the loading of the asmBenchmarkClass class by this class loader (and not its parent).
      // This is needed to make sure that the classes it references (i.e. the ASM library classes)
      // will be loaded by this class loader too.
      if (name.startsWith(asmBenchmarkClass)) {
        byte[] classFile;
        try {
          classFile = readInputStream(getResourceAsStream(name.replace('.', '/') + ".class"));
        } catch (IOException e) {
          throw new ClassNotFoundException(name, e);
        }
        Class<?> c = defineClass(name, classFile, 0, classFile.length);
        if (resolve) {
          resolveClass(c);
        }
        return c;
      }
      // Look for the specified class *first* in asmDirectories, *then* using the parent class
      // loader. This is the reverse of the default lookup order, and is necessary to make sure we
      // load the correct version of ASM (the parent class loader may have an ASM version in its
      // class path, because some components of the JMH framework depend on ASM).
      try {
        Class<?> c = findClass(name);
        if (resolve) {
          resolveClass(c);
        }
        return c;
      } catch (ClassNotFoundException e) {
        return super.loadClass(name, resolve);
      }
    }
  }
}
