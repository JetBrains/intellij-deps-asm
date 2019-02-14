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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotificationEmitter;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

/**
 * An {@link InternalProfiler} to measure the memory allocated per benchmark iteration.
 *
 * @author Eric Bruneton
 */
public class MemoryProfiler implements InternalProfiler {

  private static final Logger LOGGER = Logger.getLogger(MemoryProfiler.class.getName());

  private static Object[] references; // NOPMD(UnusedPrivateField): false positive.
  private static int referenceCount;

  private static final MemoryProbe memoryProbe = new MemoryProbe();
  private static long usedMemoryBeforeIteration;

  public static void keepReference(final Object reference) {
    references[referenceCount++] = reference;
  }

  @Override
  public String getDescription() {
    return "Adds used memory to the result.";
  }

  @Override
  public void beforeIteration(
      final BenchmarkParams benchmarkParams, final IterationParams iterationParams) {
    if (!appliesToBenchmark(benchmarkParams)) {
      return;
    }
    references = new Object[100000];
    referenceCount = 0;
    usedMemoryBeforeIteration = memoryProbe.getUsedMemory();
  }

  @Override
  public Collection<? extends Result> afterIteration(
      final BenchmarkParams benchmarkParams,
      final IterationParams iterationParams,
      final IterationResult result) {
    if (!appliesToBenchmark(benchmarkParams)) {
      return Collections.emptyList();
    }
    long usedMemoryAfterIteration = memoryProbe.getUsedMemory();
    references = null;

    long usedMemoryInIteration = usedMemoryAfterIteration - usedMemoryBeforeIteration;
    double usedMemoryPerOp =
        ((double) usedMemoryInIteration) / result.getMetadata().getMeasuredOps();
    List<Result> results = new ArrayList<>();
    results.add(new ScalarResult("+memory.used", usedMemoryPerOp, "bytes", AggregationPolicy.AVG));
    return results;
  }

  private static boolean appliesToBenchmark(final BenchmarkParams benchmarkParams) {
    return benchmarkParams.getBenchmark().contains("MemoryBenchmark");
  }

  static class MemoryProbe {

    private static final int MAX_WAIT_MILLIS = 20 * 1000;

    private final Object lock = new Object();

    private int gcCount;

    public MemoryProbe() {
      for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
        ((NotificationEmitter) gcBean)
            .addNotificationListener((notification, handback) -> notifyGc(), null, null);
      }
    }

    public long getUsedMemory() {
      try {
        systemGc(System.currentTimeMillis() + MAX_WAIT_MILLIS);
      } catch (InterruptedException e) {
        LOGGER.log(Level.WARNING, "Can't get used memory.");
        return -1;
      }
      return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    private void notifyGc() {
      synchronized (lock) {
        gcCount++;
        lock.notifyAll();
      }
    }

    private void systemGc(final long deadline) throws InterruptedException {
      synchronized (lock) {
        System.gc(); // NOPMD(DoNotCallGarbageCollectionExplicitly): needed to measure used memory.
        int previousGcCount = gcCount;
        while (gcCount == previousGcCount) {
          long timeout = deadline - System.currentTimeMillis();
          if (timeout > 0) {
            lock.wait(timeout);
          } else {
            throw new InterruptedException();
          }
        }
      }
    }
  }
}
