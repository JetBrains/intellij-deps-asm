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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * A benchmark to measure the performance of several libraries when generating a "Hello World!"
 * class.
 *
 * @author Eric Bruneton
 */
@State(Scope.Thread)
public class GeneratorBenchmark {

  // The directories where the different versions of ASM can be found.
  private static final String BUILD_DIR = "/benchmarks/write/build/";
  private static final String ASM4_0 = BUILD_DIR + "asm4/";
  private static final String ASM5_0 = BUILD_DIR + "asm5/";
  private static final String ASM6_0 = BUILD_DIR + "asm6/";
  private static final String ASM6_1 = "/asm/build/classes/java/main/";

  // The fully qualified name of the ASMGenerator class.
  private static final String ASM_GENERATOR = "org.objectweb.asm.benchmarks.ASMGenerator";

  Generator asm4_0;
  Generator asm5_0;
  Generator asm6_0;
  Generator asm6_1;
  Generator aspectJBcel;
  Generator bcel;
  Generator cojen;
  Generator csgBytecode;
  Generator gnuByteCode;
  Generator jclassLib;
  Generator jiapi;
  Generator mozillaClassFile;

  /**
   * Prepares the benchmark by creating a {@link Generator} for each library to be tested.
   *
   * @throws Exception if an error occurs.
   */
  @Setup
  public void prepare() throws Exception {
    String userDir = System.getProperty("user.dir");
    asm4_0 = new ASMGeneratorFactory(userDir + ASM4_0).newAsmGenerator();
    asm5_0 = new ASMGeneratorFactory(userDir + ASM5_0).newAsmGenerator();
    asm6_0 = new ASMGeneratorFactory(userDir + ASM6_0).newAsmGenerator();
    asm6_1 = new ASMGeneratorFactory(userDir + ASM6_1).newAsmGenerator();
    aspectJBcel = new AspectJBCELGenerator();
    bcel = new BCELGenerator();
    cojen = new CojenGenerator();
    csgBytecode = new CsgBytecodeGenerator();
    gnuByteCode = new GnuByteCodeGenerator();
    jclassLib = new JClassLibGenerator();
    jiapi = new JiapiGenerator();
    mozillaClassFile = new MozillaClassFileGenerator();
    // Check that the correct versions of ASM have been loaded.
    if (!asm4_0.getVersion().equals("ASM4")
        || !asm5_0.getVersion().equals("ASM5")
        || !asm6_0.getVersion().equals("ASM6")
        || !asm6_1.getVersion().equals("ASM6")) {
      throw new IllegalStateException();
    }
  }

  @Benchmark
  public byte[] asm4_0() {
    return asm4_0.generateClass();
  }

  @Benchmark
  public byte[] asm5_0() {
    return asm5_0.generateClass();
  }

  @Benchmark
  public byte[] asm6_0() {
    return asm6_0.generateClass();
  }

  @Benchmark
  public byte[] asm6_1() {
    return asm6_1.generateClass();
  }

  @Benchmark
  public byte[] aspectJBcel() {
    return aspectJBcel.generateClass();
  }

  @Benchmark
  public byte[] bcel() {
    return bcel.generateClass();
  }

  @Benchmark
  public byte[] cojen() {
    return cojen.generateClass();
  }

  @Benchmark
  public byte[] csgBytecode() {
    return csgBytecode.generateClass();
  }

  @Benchmark
  public byte[] gnuByteCode() {
    return gnuByteCode.generateClass();
  }

  @Benchmark
  public byte[] jclassLib() {
    return jclassLib.generateClass();
  }

  @Benchmark
  public byte[] jiapi() {
    return jiapi.generateClass();
  }

  @Benchmark
  public byte[] mozillaClassFile() {
    return mozillaClassFile.generateClass();
  }

  /** A factory of {@link ASMGenerator} objects, using a specific version of the ASM library. */
  private static class ASMGeneratorFactory extends URLClassLoader {

    /**
     * Constructs a {@link ASMGeneratorFactory}.
     *
     * @param asmDirectory the directory where the ASM library classes can be found.
     * @throws MalformedURLException
     */
    ASMGeneratorFactory(final String asmDirectory) throws MalformedURLException {
      super(new URL[] {new URL("file://" + asmDirectory)});
    }

    /**
     * @return a new {@link ASMGenerator} instance.
     * @throws Exception
     */
    public Generator newAsmGenerator() throws Exception {
      return (Generator) loadClass(ASM_GENERATOR).newInstance();
    }

    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
      // Force the loading of the ASMGenerator class by this class loader (and not its parent). This
      // is needed to make sure that the classes it references (i.e. the ASM library classes) will
      // be loaded by this class loader too.
      if (name.equals(ASM_GENERATOR)) {
        try (InputStream inputStream = getResourceAsStream(name.replace('.', '/') + ".class")) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          byte[] data = new byte[inputStream.available()];
          int bytesRead;
          while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            outputStream.write(data, 0, bytesRead);
          }
          outputStream.flush();
          byte[] classFile = outputStream.toByteArray();
          Class<?> c = defineClass(name, classFile, 0, classFile.length);
          if (resolve) {
            resolveClass(c);
          }
          return c;
        } catch (Exception e) {
          throw new ClassNotFoundException(name, e);
        }
      }
      // Look for the specified class *first* in asmDirectory, *then* using the parent class loader.
      // This is the reverse of the default lookup order, and is necessary to make sure we load the
      // correct version of ASM (the parent class loader may have an ASM version in its class path,
      // because some components of the JMH framework depend on ASM).
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
