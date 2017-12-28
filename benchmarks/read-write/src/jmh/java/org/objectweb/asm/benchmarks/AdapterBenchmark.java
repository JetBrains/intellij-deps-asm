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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * A benchmark to measure the performance of several Java bytecode libraries when reading and
 * writing Java classes with no intermediate transformation.
 *
 * @author Eric Bruneton
 */
@State(Scope.Thread)
public class AdapterBenchmark {

  // The directories where the different versions of ASM can be found.
  private static final String BUILD_DIR = "/benchmarks/read-write/build/";
  private static final String ASM4_0 = BUILD_DIR + "asm4.0/";
  private static final String ASM5_0 = BUILD_DIR + "asm5.0.1/";
  private static final String ASM6_0 = BUILD_DIR + "asm6.0/";
  private static final String ASM_CORE_6_1 = "/asm/build/classes/java/main/";
  private static final String ASM_TREE_6_1 = "/asm-tree/build/classes/java/main/";

  // The fully qualified name of the ASMAdapter class.
  private static final String ASM_ADAPTER = "org.objectweb.asm.benchmarks.ASMAdapter";

  private Adapter asm4_0;
  private Adapter asm5_0;
  private Adapter asm6_0;
  private Adapter asm6_1;
  private Adapter aspectJBcel;
  private Adapter bcel;
  private Adapter javassist;
  private Adapter serp;

  private List<byte[]> classFiles;

  /**
   * Prepares the benchmark by creating an {@link Adapter} for each library to be tested, and by
   * loading some test data (i.e. some classes to "adapt").
   *
   * @throws Exception if an error occurs.
   */
  @Setup
  public void prepare() throws Exception {
    String userDir = System.getProperty("user.dir");
    String baseUrl = "file://" + userDir;
    asm4_0 = new ASMAdapterFactory(new URL[] {new URL(baseUrl + ASM4_0)}).newAsmAdapter();
    asm5_0 = new ASMAdapterFactory(new URL[] {new URL(baseUrl + ASM5_0)}).newAsmAdapter();
    asm6_0 = new ASMAdapterFactory(new URL[] {new URL(baseUrl + ASM6_0)}).newAsmAdapter();
    asm6_1 =
        new ASMAdapterFactory(
                new URL[] {new URL(baseUrl + ASM_CORE_6_1), new URL(baseUrl + ASM_TREE_6_1)})
            .newAsmAdapter();

    aspectJBcel = new AspectJBCELAdapter();
    bcel = new BCELAdapter();
    javassist = new JavassistAdapter();
    serp = new SERPAdapter();
    // Check that the correct versions of ASM have been loaded.
    if (!asm4_0.getVersion().equals("ASM4")
        || !asm5_0.getVersion().equals("ASM5")
        || !asm6_0.getVersion().equals("ASM6")
        || !asm6_1.getVersion().equals("ASM6")) {
      throw new IllegalStateException();
    }

    classFiles = new ArrayList<byte[]>();
    findClasses(new File(userDir + ASM_CORE_6_1));
    findClasses(new File(userDir + ASM_TREE_6_1));
  }

  private void findClasses(final File directory) throws IOException {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        findClasses(file);
      } else if (file.getName().endsWith(".class")) {
        classFiles.add(readInputStream(new FileInputStream(file)));
      }
    }
  }

  private static byte[] readInputStream(final InputStream inputStream) throws IOException {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] data = new byte[inputStream.available()];
      int bytesRead;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, bytesRead);
      }
      outputStream.flush();
      return outputStream.toByteArray();
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Nothing to do.
      }
    }
  }

  @Benchmark
  public void getClassInfo_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.getClassInfo(classFile));
    }
  }

  @Benchmark
  public void getClassInfo_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.getClassInfo(classFile));
    }
  }

  @Benchmark
  public void getClassInfo_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.getClassInfo(classFile));
    }
  }

  @Benchmark
  public void getClassInfo_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.getClassInfo(classFile));
    }
  }

  @Benchmark
  public void getClassObjectModel_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.getClassObjectModel(classFile));
    }
  }

  @Benchmark
  public void getClassObjectModel_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.getClassObjectModel(classFile));
    }
  }

  @Benchmark
  public void getClassObjectModel_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.getClassObjectModel(classFile));
    }
  }

  @Benchmark
  public void getClassObjectModel_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.getClassObjectModel(classFile));
    }
  }

  @Benchmark
  public void read_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.read(classFile));
    }
  }

  @Benchmark
  public void read_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.read(classFile));
    }
  }

  @Benchmark
  public void read_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.read(classFile));
    }
  }

  @Benchmark
  public void read_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.read(classFile));
    }
  }

  @Benchmark
  public void readAndWrite_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_aspectJBcel(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(aspectJBcel.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_bcel(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(bcel.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_javassist(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(javassist.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWrite_serp(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(serp.readAndWrite(classFile, /* computeMaxs = */ false));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeFrames_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.readAndWriteWithComputeFrames(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeFrames_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.readAndWriteWithComputeFrames(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeFrames_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.readAndWriteWithComputeFrames(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeFrames_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.readAndWriteWithComputeFrames(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_aspectJBcel(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(aspectJBcel.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_bcel(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(bcel.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithComputeMaxs_serp(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(serp.readAndWrite(classFile, /* computeMaxs = */ true));
    }
  }

  @Benchmark
  public void readAndWriteWithCopyPool_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.readAndWriteWithCopyPool(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithCopyPool_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.readAndWriteWithCopyPool(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithCopyPool_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.readAndWriteWithCopyPool(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithCopyPool_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.readAndWriteWithCopyPool(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithObjectModel_asm4_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm4_0.readAndWriteWithObjectModel(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithObjectModel_asm5_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm5_0.readAndWriteWithObjectModel(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithObjectModel_asm6_0(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_0.readAndWriteWithObjectModel(classFile));
    }
  }

  @Benchmark
  public void readAndWriteWithObjectModel_asm6_1(final Blackhole blackhole) {
    for (byte[] classFile : classFiles) {
      blackhole.consume(asm6_1.readAndWriteWithObjectModel(classFile));
    }
  }

  /** A factory of {@link ASMAdapter} objects, using a specific version of the ASM library. */
  private static class ASMAdapterFactory extends URLClassLoader {

    /**
     * Constructs an {@link ASMAdapterFactory}.
     *
     * @param asmDirectories the directories where the ASM library classes can be found.
     */
    ASMAdapterFactory(final URL[] asmDirectories) {
      super(asmDirectories);
    }

    /**
     * @return a new {@link ASMAdapter} instance.
     * @throws Exception
     */
    public Adapter newAsmAdapter() throws Exception {
      return (Adapter) loadClass(ASM_ADAPTER).newInstance();
    }

    protected Class<?> loadClass(final String name, final boolean resolve)
        throws ClassNotFoundException {
      // Force the loading of the ASMAdapter class by this class loader (and not its parent). This
      // is needed to make sure that the classes it references (i.e. the ASM library classes) will
      // be loaded by this class loader too.
      if (name.startsWith(ASM_ADAPTER)) {
        try {
          byte[] classFile =
              readInputStream(getResourceAsStream(name.replace('.', '/') + ".class"));
          Class<?> c = defineClass(name, classFile, 0, classFile.length);
          if (resolve) {
            resolveClass(c);
          }
          return c;
        } catch (Exception e) {
          throw new ClassNotFoundException(name, e);
        }
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
