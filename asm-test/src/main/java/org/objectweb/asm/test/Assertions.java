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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import org.junit.jupiter.api.function.Executable;

/**
 * Provides convenient assertions to check that an executable succeeds or throws an exception,
 * depending on some condition.
 *
 * @author Eric Bruneton
 */
public final class Assertions {

  private Assertions() {}

  public static ExecutableSubject assertThat(final Executable executable) {
    return new ExecutableSubject(executable);
  }

  public static class ExecutableSubject {
    private final Executable executable;

    ExecutableSubject(final Executable executable) {
      this.executable = executable;
    }

    public <T extends Throwable> ExecutableOutcomeSubject<T> succeedsOrThrows(
        final Class<T> expectedType) {
      return new ExecutableOutcomeSubject<>(executable, expectedType);
    }
  }

  public static class ExecutableOutcomeSubject<T extends Throwable> {
    private final Executable executable;
    private final Class<T> expectedType;

    ExecutableOutcomeSubject(final Executable executable, final Class<T> expectedType) {
      this.executable = executable;
      this.expectedType = expectedType;
    }

    public void when(final boolean condition) {
      if (condition) {
        assertThrows(expectedType, executable);
      } else {
        assumingThat(true, executable);
      }
    }
  }
}
