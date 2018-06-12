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

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * A benchmark to measure the memory usage of several Java bytecode libraries when reading Java
 * classes.
 *
 * @author Eric Bruneton
 */
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class MemoryBenchmark extends AbstractBenchmark {

  private Factory asm4_0;
  private Factory asm5_0;
  private Factory asm6_0;
  private Factory asm6_1;

  public MemoryBenchmark() {
    super("org.objectweb.asm.benchmarks.ASMFactory");
  }

  /**
   * Prepares the benchmark by creating a {@link Factory} for each library to be tested, and by
   * loading some test data (i.e. some classes to read).
   *
   * @throws Exception if an error occurs.
   */
  @Setup
  public void prepare() throws Exception {
    asm4_0 = (Factory) new AsmBenchmarkFactory(AsmVersion.V4_0).newAsmBenchmark();
    asm5_0 = (Factory) new AsmBenchmarkFactory(AsmVersion.V5_0).newAsmBenchmark();
    asm6_0 = (Factory) new AsmBenchmarkFactory(AsmVersion.V6_0).newAsmBenchmark();
    asm6_1 = (Factory) new AsmBenchmarkFactory(AsmVersion.V6_1).newAsmBenchmark();

    // Check that the correct versions of ASM have been loaded.
    if (!asm4_0.getVersion().equals("ASM4")
        || !asm5_0.getVersion().equals("ASM5")
        || !asm6_0.getVersion().equals("ASM6")
        || !asm6_1.getVersion().equals("ASM6")) {
      throw new IllegalStateException();
    }

    prepareClasses();
  }

  @Benchmark
  public void newClass_asm4_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm4_0.newClass(classFile));
    }
  }

  @Benchmark
  public void newClass_asm5_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm5_0.newClass(classFile));
    }
  }

  @Benchmark
  public void newClass_asm6_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm6_0.newClass(classFile));
    }
  }

  @Benchmark
  public void newClass_asm6_1() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm6_1.newClass(classFile));
    }
  }

  @Benchmark
  public void newClassNode_asm4_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm4_0.newClassNode(classFile));
    }
  }

  @Benchmark
  public void newClassNode_asm5_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm5_0.newClassNode(classFile));
    }
  }

  @Benchmark
  public void newClassNode_asm6_0() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm6_0.newClassNode(classFile));
    }
  }

  @Benchmark
  public void newClassNode_asm6_1() {
    for (byte[] classFile : classFiles) {
      MemoryProfiler.keepReference(asm6_1.newClassNode(classFile));
    }
  }
}
