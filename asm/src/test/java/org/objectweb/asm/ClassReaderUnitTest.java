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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * ClassReader unit tests.
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class ClassReaderUnitTest implements Opcodes {

  @Test
  public void testIllegalConstructorArgument() {
    assertThrows(IOException.class, () -> new ClassReader((InputStream) null));
  }

  @Test
  public void testGetItem() throws IOException {
    ClassReader classReader = new ClassReader(getClass().getName());
    int item = classReader.getItem(1);
    assertTrue(item >= 10);
    assertTrue(item < classReader.header);
  }

  @Test
  public void testReadByte() throws IOException {
    ClassReader classReader = new ClassReader(getClass().getName());
    assertEquals(classReader.b[0] & 0xFF, classReader.readByte(0));
  }

  @Test
  public void testGetAccess() throws Exception {
    String name = getClass().getName();
    assertEquals(Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, new ClassReader(name).getAccess());
  }

  @Test
  public void testGetClassName() throws Exception {
    String name = getClass().getName();
    assertEquals(name.replace('.', '/'), new ClassReader(name).getClassName());
  }

  @Test
  public void testGetSuperName() throws Exception {
    assertEquals(
        Object.class.getName().replace('.', '/'),
        new ClassReader(getClass().getName()).getSuperName());
    assertEquals(null, new ClassReader(Object.class.getName()).getSuperName());
  }

  @Test
  public void testGetInterfaces() throws Exception {
    String[] interfaces = new ClassReader(getClass().getName()).getInterfaces();
    assertNotNull(interfaces);
    assertEquals(1, interfaces.length);
    assertEquals(Opcodes.class.getName().replace('.', '/'), interfaces[0]);

    interfaces = new ClassReader(Opcodes.class.getName()).getInterfaces();
    assertNotNull(interfaces);
  }
}
