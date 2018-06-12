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

package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * SignatureRemapper tests.
 *
 * @author Eric Bruneton
 */
public class SignatureRemapperTest {

  @Test
  public void testRemappingParentOnlyNestedClassExtends() {
    Remapper remapper = new SimpleRemapper(Collections.singletonMap("Outer", "RenamedOuter"));
    assertEquals(
        "LRenamedOuter<Ljava/lang/Object;>.Inner;",
        remapper.mapSignature("LOuter<Ljava/lang/Object;>.Inner;", false));
  }

  @Test
  public void testRemappingChildOnlyNestedClassExtends() {
    Remapper remapper =
        new SimpleRemapper(Collections.singletonMap("Outer$Inner", "Outer$RenamedInner"));
    assertEquals(
        "LOuter<Ljava/lang/Object;>.RenamedInner;",
        remapper.mapSignature("LOuter<Ljava/lang/Object;>.Inner;", false));
  }

  @Test
  public void testRemappingChildOnlyNestedClassExtends_IdentifiersWith$() {
    Remapper remapper =
        new SimpleRemapper(Collections.singletonMap("Outer$Inner$1", "Outer$RenamedInner$1"));
    assertEquals(
        "LOuter<Ljava/lang/Object;>.RenamedInner$1;",
        remapper.mapSignature("LOuter<Ljava/lang/Object;>.Inner$1;", false));
  }

  @Test
  public void testRemappingBothParentAndChildNestedClassExtends() {
    Map<String, String> mapping = new HashMap<String, String>();
    mapping.put("Outer", "RenamedOuter");
    mapping.put("Outer$Inner", "RenamedOuter$RenamedInner");
    Remapper remapper = new SimpleRemapper(mapping);
    assertEquals(
        "LRenamedOuter<Ljava/lang/Object;>.RenamedInner;",
        remapper.mapSignature("LOuter<Ljava/lang/Object;>.Inner;", false));
  }
}
