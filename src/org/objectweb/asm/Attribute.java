/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
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
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm;

/**
 * A non standard class, field, method or code attribute.
 */

public abstract class Attribute {

  /**
   * The type of this attribute.
   */

  public final String type;

  /**
   * The next attribute in this attribute list. May be <tt>null</tt>.
   */

  public Attribute next;

  /**
   * Constructs a new empty attribute.
   *
   * @param type the type of the attribute.
   */

  public Attribute (final String type) {
    this.type = type;
  }

  /**
   * Analyzes a {@link #type type} attribute and finds the corresponding labels.
   * This method must analyze the attribute in the given class reader, at the
   * given offset (whose type is equal to {@link #type type}), and must add to
   * given label array the labels that corresponds to the bytecode offsets
   * contained in the attribute (if there are such offsets, and if the
   * corresponding labels do not already exist). The default implementation of
   * this method, which is only called for code attributes, does nothing.
   *
   * @param cr the class that contains the attribute to be analyzed.
   * @param off index of the first byte of the attribute's content in {@link
   *      ClassReader#b cr.b}. The 6 attribute header bytes, containing the type
   *      and the length of the attribute, are not taken into account here.
   * @param len the length of the attribute's content.
   * @param labels the array that must be completed with the labels contained in
   *      the attribute.
   */

  protected void analyze (
    ClassReader cr,
    int off,
    int len,
    Label[] labels)
  {
    // does nothing
  }

  /**
   * Reads a {@link #type type} attribute. This method must return a <i>new</i>
   * {@link Attribute} object, of type {@link #type type}, corresponding to the
   * <tt>len</tt> bytes starting at the given offset, in the given class reader.
   *
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the attribute's content in {@link
   *      ClassReader#b cr.b}. The 6 attribute header bytes, containing the type
   *      and the length of the attribute, are not taken into account here.
   * @param len the length of the attribute's content.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass readClass} or {@link
   *      ClassReader#readConst readConst}..
   * @param labels the labels of the method's code, or <tt>null</tt> if the
   *      attribute to be read is not a code attribute.
   * @return a <i>new</i> {@link Attribute} object corresponding to the given
   *      bytes.
   */

  protected abstract Attribute read (
    ClassReader cr,
    int off,
    int len,
    char[] buf,
    Label[] labels);

  /**
   * Returns the byte array form of this attribute.
   *
   * @param cw the class to which this attribute must be added. This parameter
   *      can be used to add to the constant pool of this class the items that
   *      corresponds to this attribute.
   * @param code the bytecode of the method corresponding to this code
   *      attribute, or <tt>null</tt> if this attribute is not a code
   *      attributes.
   * @param len the length of the bytecode of the method corresponding to this
   *      code attribute, or <tt>null</tt> if this attribute is not a code
   *      attribute.
   * @return the byte array form of this attribute.
   */

  protected abstract ByteVector write (ClassWriter cw, byte[] code, int len);

  /**
   * Returns the length of the attribute list that begins with this attribute.
   *
   * @return the length of the attribute list that begins with this attribute.
   */

  final int getCount () {
    int count = 0;
    Attribute attr = this;
    while (attr != null) {
      count += 1;
      attr = attr.next;
    }
    return count;
  }

  /**
   * Returns the size of all the attributes in this attribute list.
   *
   * @param cw the class writer to be used to convert the attributes into byte
   *      arrays, with the {@link #write write} method.
   * @param code the bytecode of the method corresponding to these code
   *      attributes, or <tt>null</tt> if these attributes are not code
   *      attributes.
   * @param len the length of the bytecode of the method corresponding to these
   *      code attributes, or <tt>null</tt> if these attributes are not code
   *      attributes.
   * @return the size of all the attributes in this attribute list. This size
   *      includes the size of the attribute headers.
   */

  final int getSize (final ClassWriter cw, final byte[] code, final int len) {
    int size = 0;
    Attribute attr = this;
    while (attr != null) {
      cw.newUTF8(attr.type);
      size += attr.write(cw, code, len).length + 6;
      attr = attr.next;
    }
    return size;
  }

  /**
   * Writes all the attributes of this attribute list in the given byte vector.
   *
   * @param cw the class writer to be used to convert the attributes into byte
   *      arrays, with the {@link #write write} method.
   * @param code the bytecode of the method corresponding to these code
   *      attributes, or <tt>null</tt> if these attributes are not code
   *      attributes.
   * @param len the length of the bytecode of the method corresponding to these
   *      code attributes, or <tt>null</tt> if these attributes are not code
   *      attributes.
   * @param out where the attributes must be written.
   */

  final void put (
    final ClassWriter cw,
    final byte[] code,
    final int len,
    final ByteVector out)
  {
    Attribute attr = this;
    while (attr != null) {
      ByteVector b = attr.write(cw, code, len);
      out.putShort(cw.newUTF8(attr.type)).putInt(b.length);
      out.putByteArray(b.data, 0, b.length);
      attr = attr.next;
    }
  }
}
