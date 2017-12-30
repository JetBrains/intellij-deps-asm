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

/**
 * An empty class adapter, which reads and writes Java classes with no intermediate transformation.
 *
 * @author Eric Bruneton
 */
public abstract class Adapter {

  /** The access flags, name, super class and interfaces of a class. */
  public static class ClassInfo {

    int access;
    String name;
    String superClass;
    String[] interfaces;

    public ClassInfo(
        final int access, final String name, final String superClass, final String[] interfaces) {
      this.access = access;
      this.name = name;
      this.superClass = superClass;
      this.interfaces = interfaces;
    }
  }

  /** @return the version of this class adapter, or an empty string if there is no version */
  public String getVersion() {
    return "";
  }

  /**
   * @param classFile a JVMS ClassFile structure
   * @return access flags, name, super class and interfaces of the given class.
   */
  public ClassInfo getClassInfo(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns an in-memory, object representation of the given class.
   *
   * @param classFile a JVMS ClassFile structure
   * @return an in-memory, object representation of the given class.
   */
  public Object getClassObjectModel(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads a class and returns the number of 'elements' it contains.
   *
   * @param classFile a JVMS ClassFile structure
   * @return the number of 'elements' found in the given class.
   */
  public int read(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads a class and writes and returns an equivalent one.
   *
   * @param classFile a JVMS ClassFile structure
   * @param computeMaxs whether to recompute the maximum stack size and maximum number of local
   *     variables for each method.
   * @return the rebuilt class.
   */
  public byte[] readAndWrite(final byte[] classFile, final boolean computeMaxs) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads a class and writes and returns an equivalent one with all its stack map frames
   * recomputed.
   *
   * @param classFile a JVMS ClassFile structure
   * @return the rebuilt class.
   */
  public byte[] readAndWriteWithComputeFrames(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads a class and writes and returns an equivalent one, sharing the same constant pool.
   *
   * @param classFile a JVMS ClassFile structure
   * @return the rebuilt class.
   */
  public byte[] readAndWriteWithCopyPool(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reads a class and writes and returns an equivalent one, via the construction of in-memory,
   * object representation of the class.
   *
   * @param classFile a JVMS ClassFile structure
   * @return the rebuilt class.
   */
  public byte[] readAndWriteWithObjectModel(final byte[] classFile) {
    throw new UnsupportedOperationException();
  }
}
