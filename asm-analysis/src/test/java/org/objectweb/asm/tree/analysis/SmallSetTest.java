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
package org.objectweb.asm.tree.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * SmallSet tests.
 *
 * @author Eric Bruneton
 */
public class SmallSetTest {

  private static final Object ELEMENT1 = new Object();
  private static final Object ELEMENT2 = new Object();
  private static final Object ELEMENT3 = new Object();
  private static final Object ELEMENT4 = new Object();

  @Test
  public void testUnion1() {
    SmallSet<Object> set1 = new SmallSet<Object>(ELEMENT1);
    SmallSet<Object> set2 = new SmallSet<Object>(ELEMENT1);
    Set<Object> union1 = set1.union(set2);
    Set<Object> union2 = set2.union(set1);
    assertEquals(union1, union2);
    assertEquals(union1, new HashSet<Object>(Arrays.asList(ELEMENT1)));
  }

  @Test
  public void testUnion2() {
    SmallSet<Object> set1 = newSmallSet(ELEMENT1, ELEMENT2);
    SmallSet<Object> set2 = new SmallSet<Object>(ELEMENT1);
    Set<Object> union1 = set1.union(set2);
    Set<Object> union2 = set2.union(set1);
    assertEquals(union1, union2);
    assertEquals(union1, new HashSet<Object>(Arrays.asList(ELEMENT1, ELEMENT2)));
  }

  @Test
  public void testUnion2EqualSets() {
    SmallSet<Object> set1 = newSmallSet(ELEMENT1, ELEMENT2);
    SmallSet<Object> set2 = newSmallSet(ELEMENT2, ELEMENT1);
    Set<Object> union1 = set1.union(set2);
    Set<Object> union2 = set2.union(set1);
    assertEquals(union1, union2);
    assertEquals(union1, new HashSet<Object>(Arrays.asList(ELEMENT1, ELEMENT2)));
  }

  @Test
  public void testUnion3() {
    SmallSet<Object> set1 = newSmallSet(ELEMENT1, ELEMENT2);
    SmallSet<Object> set2 = new SmallSet<Object>(ELEMENT3);
    Set<Object> union1 = set1.union(set2);
    Set<Object> union2 = set2.union(set1);
    assertEquals(union1, union2);
    assertEquals(union1, new HashSet<Object>(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3)));
  }

  @Test
  public void testUnion4() {
    SmallSet<Object> set1 = newSmallSet(ELEMENT1, ELEMENT2);
    SmallSet<Object> set2 = newSmallSet(ELEMENT3, ELEMENT4);
    Set<Object> union1 = set1.union(set2);
    Set<Object> union2 = set2.union(set1);
    assertEquals(union1, union2);
    assertEquals(
        union1, new HashSet<Object>(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3, ELEMENT4)));
  }

  @Test
  public void testIterator() {
    Iterator<Object> iterator = newSmallSet(ELEMENT1, ELEMENT2).iterator();
    assertTrue(iterator.hasNext());
    assertEquals(ELEMENT1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(ELEMENT2, iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, () -> iterator.next());
    assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
  }

  private SmallSet<Object> newSmallSet(Object element1, Object element2) {
    return (SmallSet<Object>) new SmallSet<Object>(element1).union(new SmallSet<Object>(element2));
  }
}
