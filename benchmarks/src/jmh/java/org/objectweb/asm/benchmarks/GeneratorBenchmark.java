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
 * A benchmark to measure the performance of several libraries when generating a "Hello World!"
 * class.
 *
 * @author Eric Bruneton
 */
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class GeneratorBenchmark extends AbstractBenchmark {

  private Generator asm4_0;
  private Generator asm5_0;
  private Generator asm6_0;
  private Generator asm6_1;
  private Generator aspectJBcel;
  private Generator bcel;
  private Generator cojen;
  private Generator csgBytecode;
  private Generator gnuByteCode;
  private Generator jclassLib;
  private Generator jiapi;
  private Generator mozillaClassFile;

  public GeneratorBenchmark() {
    super("org.objectweb.asm.benchmarks.ASMGenerator");
  }

  /**
   * Prepares the benchmark by creating a {@link Generator} for each library to be tested.
   *
   * @throws Exception if an error occurs.
   */
  @Setup
  public void prepare() throws Exception {
    asm4_0 = (Generator) new AsmBenchmarkFactory(AsmVersion.V4_0).newAsmBenchmark();
    asm5_0 = (Generator) new AsmBenchmarkFactory(AsmVersion.V5_0).newAsmBenchmark();
    asm6_0 = (Generator) new AsmBenchmarkFactory(AsmVersion.V6_0).newAsmBenchmark();
    asm6_1 = (Generator) new AsmBenchmarkFactory(AsmVersion.V6_1).newAsmBenchmark();
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
}
