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
package org.objectweb.asm.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * InsnList unit tests.
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class InsnListUnitTest {

  InsnList l1;

  InsnList l2;

  InsnNode in1;

  InsnNode in2;

  @BeforeEach
  public void setUp() throws Exception {
    l1 = new CheckedInsnList();
    l2 = new CheckedInsnList();
    in1 = new InsnNode(0);
    in2 = new InsnNode(0);
    l2.add(in1);
    l2.add(in2);
  }

  void assertInsnEquals(final AbstractInsnNode[] expected, final AbstractInsnNode[] value) {
    assertEquals(expected.length, value.length);
    for (int i = 0; i < value.length; ++i) {
      assertEquals(expected[i], value[i]);
    }
  }

  @Test
  public void testSize() {
    assertEquals(0, l1.size());
  }

  @Test
  public void testGetFirst() {
    assertEquals(null, l1.getFirst());
  }

  @Test
  public void testGetLast() {
    assertEquals(null, l1.getLast());
  }

  @Test
  public void testInvalidGet() {
    assertThrows(IndexOutOfBoundsException.class, () -> l1.get(0));
  }

  @Test
  public void testContains() {
    assertEquals(false, l1.contains(new InsnNode(0)));
  }

  @Test
  public void testIterator() {
    InsnNode insn = new InsnNode(0);

    // iteration
    ListIterator<AbstractInsnNode> it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    assertTrue(it.hasNext());
    assertEquals(in2, it.next());
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    assertEquals(in2, it.previous());
    assertTrue(it.hasPrevious());
    assertEquals(in1, it.previous());
    assertFalse(it.hasPrevious());

    l2.add(insn);

    // remove()
    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    assertTrue(it.hasNext());
    assertEquals(in2, it.next());
    assertTrue(it.hasNext());
    it.remove(); // remove in2
    assertTrue(it.hasPrevious());
    assertTrue(it.hasNext());
    assertEquals(insn, it.next());
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());

    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    assertTrue(it.hasNext());
    assertEquals(insn, it.next());
    assertFalse(it.hasNext());

    l2.remove(insn);
    l2.insert(in1, in2);

    // add() then next()
    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    it.add(insn);
    assertEquals(in2, it.next());

    l2.remove(insn);

    // add() then previous()
    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    it.add(insn);
    assertEquals(insn, it.previous());
    assertEquals(insn, it.next());
    assertTrue(it.hasNext());
    assertEquals(in2, it.next());
    assertFalse(it.hasNext());

    l2.remove(insn);

    // set() then previous()
    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    it.set(insn);
    assertEquals(insn, it.previous());
    assertEquals(insn, it.next());
    assertTrue(it.hasNext());

    l2.remove(insn);
    l2.insertBefore(in2, in1);

    // add() then next()
    it = l2.iterator();
    assertTrue(it.hasNext());
    assertEquals(in1, it.next());
    it.set(insn);
    assertEquals(in2, it.next());
  }

  @Test
  public void testIterator2() {
    ListIterator<AbstractInsnNode> it = l2.iterator(l2.size());

    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    assertEquals(1, it.previousIndex());
    assertEquals(in2, it.previous());
    assertTrue(it.hasPrevious());
    assertEquals(0, it.previousIndex());
    assertEquals(in1, it.previous());
    assertFalse(it.hasPrevious());

    assertEquals(-1, it.previousIndex());

    assertTrue(it.hasNext());
    assertEquals(0, it.nextIndex());
    assertEquals(in1, it.next());
    assertTrue(it.hasNext());
    assertEquals(1, it.nextIndex());

    InsnNode insn = new InsnNode(0);
    it.add(insn);

    assertEquals(2, it.nextIndex());
    assertEquals(in2, it.next());
    assertFalse(it.hasNext());
    assertEquals(3, it.nextIndex());
  }

  @Test
  public void testIterator3() {
    InsnNode insn = new InsnNode(0);
    l2.add(insn);

    // reverse iteration
    ListIterator<AbstractInsnNode> it = l2.iterator(3);
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    assertEquals(insn, it.previous());
    assertTrue(it.hasPrevious());
    assertEquals(in2, it.previous());
    assertTrue(it.hasPrevious());
    assertEquals(in1, it.previous());
    assertFalse(it.hasPrevious());

    // reverse iteration with remove()
    it = l2.iterator(3);
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    assertEquals(insn, it.previous());
    it.remove(); // remove insn
    assertTrue(it.hasPrevious());
    assertEquals(in2, it.previous());
    it.remove(); // remove in2
    assertTrue(it.hasPrevious());
    assertEquals(in1, it.previous());
    assertFalse(it.hasPrevious());

    assertEquals(1, l2.size());
    assertEquals(in1, l2.getFirst());
  }

  @Test
  public void testIterator4() {
    assertThrows(NoSuchElementException.class, () -> new InsnList().iterator().next());
  }

  @Test
  public void testIterator5() {
    // Call add() on empty list
    ListIterator<AbstractInsnNode> it = l1.iterator();
    it.add(new InsnNode(0));
    assertEquals(1, l1.size());

    // Call set() at end of list
    it = l1.iterator();
    it.next();
    it.set(new InsnNode(1));
    assertEquals(1, l1.size());
    assertEquals(1, l1.get(0).opcode);

    // Call add() at end of list
    it = l2.iterator();
    it.next();
    it.next();
    it.add(new InsnNode(0));
    assertEquals(3, l2.size());

    // next() after set(), itself after previous()
    it = l2.iterator();
    it.next();
    it.next();
    it.previous();
    it.set(new InsnNode(1));
    assertEquals(1, it.next().opcode);
  }

  @Test
  public void testInvalidIndexOf() {
    assertThrows(IllegalArgumentException.class, () -> l1.indexOf(new InsnNode(0)));
  }

  @Test
  public void testToArray() {
    assertEquals(0, l1.toArray().length);
  }

  @Test
  public void testInvalidSet() {
    assertThrows(IllegalArgumentException.class, () -> l1.set(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testSet() {
    l1.add(new InsnNode(0));
    AbstractInsnNode insn = new InsnNode(0);
    l1.set(l1.getFirst(), insn);
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());

    l1.remove(insn);
    l1.add(new InsnNode(0));

    l1.set(l1.get(0), insn);
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());

    l1.remove(insn);
    l1.add(new InsnNode(0));
    l1.add(new InsnNode(0));

    l1.set(l1.get(1), insn);
    assertEquals(2, l1.size());
    assertEquals(insn, l1.get(1));
  }

  @Test
  public void testInvalidAdd() {
    assertThrows(IllegalArgumentException.class, () -> l1.add(in1));
  }

  @Test
  public void testAddEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(0, l1.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {insn}, l1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testAddNonEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.add(insn);
    assertEquals(2, l1.size());
    assertEquals(insn, l1.getLast());
    assertEquals(1, l1.indexOf(insn));
    assertEquals(insn, l1.get(1));
    assertEquals(true, l1.contains(insn));
  }

  @Test
  public void testAddEmptyList() {
    l1.add(new InsnList());
    assertEquals(0, l1.size());
    assertEquals(null, l1.getFirst());
    assertEquals(null, l1.getLast());
    assertInsnEquals(new AbstractInsnNode[0], l1.toArray());
  }

  @Test
  public void testInvalidAddAll() {
    assertThrows(IllegalArgumentException.class, () -> l1.add(l1));
  }

  @Test
  public void testAddAllEmpty() {
    l1.add(l2);
    assertEquals(2, l1.size());
    assertEquals(in1, l1.getFirst());
    assertEquals(in2, l1.getLast());
    assertEquals(in1, l1.get(0));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(in1));
    assertEquals(1, l1.indexOf(in2));
    assertInsnEquals(new AbstractInsnNode[] {in1, in2}, l1.toArray());
  }

  @Test
  public void testAddAllNonEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.add(l2);
    assertEquals(3, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(in2, l1.getLast());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(insn));
    assertEquals(1, l1.indexOf(in1));
    assertEquals(2, l1.indexOf(in2));
    assertInsnEquals(new AbstractInsnNode[] {insn, in1, in2}, l1.toArray());
  }

  @Test
  public void testInvalidInsert() {
    assertThrows(IllegalArgumentException.class, () -> l1.insert(in1));
  }

  @Test
  public void testInsertEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.insert(insn);
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(0, l1.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {insn}, l1.toArray());
  }

  @Test
  public void testInsertNonEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.insert(insn);
    assertEquals(2, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(0, l1.indexOf(insn));
  }

  @Test
  public void testInvalidInsertAll() {
    assertThrows(IllegalArgumentException.class, () -> l1.insert(l1));
  }

  @Test
  public void testInsertAllEmptyList() {
    l1.insert(new InsnList());
    assertEquals(0, l1.size());
    assertEquals(null, l1.getFirst());
    assertEquals(null, l1.getLast());
    assertInsnEquals(new AbstractInsnNode[0], l1.toArray());
  }

  @Test
  public void testInsertAllEmpty() {
    l1.insert(l2);
    assertEquals(2, l1.size(), 2);
    assertEquals(in1, l1.getFirst());
    assertEquals(in2, l1.getLast());
    assertEquals(in1, l1.get(0));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(in1));
    assertEquals(1, l1.indexOf(in2));
    assertInsnEquals(new AbstractInsnNode[] {in1, in2}, l1.toArray());
  }

  @Test
  public void testInsertAllNonEmpty() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.insert(l2);
    assertEquals(3, l1.size());
    assertEquals(in1, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertEquals(in1, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(in1));
    assertEquals(1, l1.indexOf(in2));
    assertEquals(2, l1.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {in1, in2, insn}, l1.toArray());
  }

  @Test
  public void testInvalidInsert2() {
    assertThrows(IllegalArgumentException.class, () -> l1.insert(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testInsert2NotLast() {
    InsnNode insn = new InsnNode(0);
    l2.insert(in1, insn);
    assertEquals(3, l2.size());
    assertEquals(in1, l2.getFirst());
    assertEquals(in2, l2.getLast());
    assertEquals(in1, l2.get(0));
    assertEquals(true, l2.contains(insn));
    assertEquals(1, l2.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {in1, insn, in2}, l2.toArray());
  }

  @Test
  public void testInsert2Last() {
    InsnNode insn = new InsnNode(0);
    l2.insert(in2, insn);
    assertEquals(3, l2.size());
    assertEquals(in1, l2.getFirst());
    assertEquals(insn, l2.getLast());
    assertEquals(in1, l2.get(0));
    assertEquals(true, l2.contains(insn));
    assertEquals(2, l2.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {in1, in2, insn}, l2.toArray());
  }

  @Test
  public void testInsertBefore() {
    InsnNode insn = new InsnNode(0);
    l2.insertBefore(in2, insn);
    assertEquals(3, l2.size());
    assertEquals(in1, l2.getFirst());
    assertEquals(in2, l2.getLast());
    assertEquals(insn, l2.get(1));
    assertEquals(true, l2.contains(insn));
    assertEquals(1, l2.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {in1, insn, in2}, l2.toArray());
  }

  @Test
  public void testInsertBeforeFirst() {
    InsnNode insn = new InsnNode(0);
    l2.insertBefore(in1, insn);
    assertEquals(3, l2.size());
    assertEquals(insn, l2.getFirst());
    assertEquals(in2, l2.getLast());
    assertEquals(insn, l2.get(0));
    assertEquals(true, l2.contains(insn));
    assertEquals(0, l2.indexOf(insn));
    assertInsnEquals(new AbstractInsnNode[] {insn, in1, in2}, l2.toArray());
  }

  @Test
  public void testInvalidInsertBefore() {
    assertThrows(
        IllegalArgumentException.class, () -> l1.insertBefore(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testInvalidInsertAll2() {
    assertThrows(IllegalArgumentException.class, () -> l1.insert(new InsnNode(0), new InsnList()));
  }

  @Test
  public void testInsertAll2EmptyList() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.insert(insn, new InsnList());
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertInsnEquals(new AbstractInsnNode[] {insn}, l1.toArray());
  }

  @Test
  public void testInsertAll2NotLast() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.add(new InsnNode(0));
    l1.insert(insn, l2);
    assertEquals(4, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(insn));
    assertEquals(1, l1.indexOf(in1));
    assertEquals(2, l1.indexOf(in2));
  }

  @Test
  public void testInsertAll2Last() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.insert(insn, l2);
    assertEquals(3, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(in2, l1.getLast());
    assertEquals(insn, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(0, l1.indexOf(insn));
    assertEquals(1, l1.indexOf(in1));
    assertEquals(2, l1.indexOf(in2));
    assertInsnEquals(new AbstractInsnNode[] {insn, in1, in2}, l1.toArray());
  }

  @Test
  public void testInvalidInsertBeforeAll() {
    assertThrows(
        IllegalArgumentException.class, () -> l1.insertBefore(new InsnNode(0), new InsnList()));
  }

  @Test
  public void testInsertBeforeAll2EmptyList() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.insertBefore(insn, new InsnList());
    assertEquals(1, l1.size());
    assertEquals(insn, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertInsnEquals(new AbstractInsnNode[] {insn}, l1.toArray());
  }

  @Test
  public void testInsertBeforeAll2NotLast() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.add(insn);
    l1.insertBefore(insn, l2);
    assertEquals(4, l1.size());
    assertEquals(in1, l1.get(1));
    assertEquals(in2, l1.get(2));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(3, l1.indexOf(insn));
    assertEquals(1, l1.indexOf(in1));
    assertEquals(2, l1.indexOf(in2));
  }

  @Test
  public void testInsertBeforeAll2First() {
    InsnNode insn = new InsnNode(0);
    l1.insert(insn);
    l1.insertBefore(insn, l2);
    assertEquals(3, l1.size());
    assertEquals(in1, l1.getFirst());
    assertEquals(insn, l1.getLast());
    assertEquals(in1, l1.get(0));
    assertEquals(true, l1.contains(insn));
    assertEquals(true, l1.contains(in1));
    assertEquals(true, l1.contains(in2));
    assertEquals(2, l1.indexOf(insn));
    assertEquals(0, l1.indexOf(in1));
    assertEquals(1, l1.indexOf(in2));
    assertInsnEquals(new AbstractInsnNode[] {in1, in2, insn}, l1.toArray());
  }

  @Test
  public void testInvalidRemove() {
    try {
      l1.remove(new InsnNode(0));
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testRemoveSingle() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.remove(insn);
    assertEquals(0, l1.size());
    assertEquals(null, l1.getFirst());
    assertEquals(null, l1.getLast());
    assertEquals(false, l1.contains(insn));
    assertInsnEquals(new AbstractInsnNode[0], l1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveFirst() {
    InsnNode insn = new InsnNode(0);
    l1.add(insn);
    l1.add(new InsnNode(0));
    l1.remove(insn);
    assertEquals(false, l1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveMiddle() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.add(insn);
    l1.add(new InsnNode(0));
    l1.remove(insn);
    assertEquals(false, l1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveLast() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.add(insn);
    l1.remove(insn);
    assertEquals(false, l1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testClear() {
    InsnNode insn = new InsnNode(0);
    l1.add(new InsnNode(0));
    l1.add(insn);
    l1.add(new InsnNode(0));
    l1.clear();
    assertEquals(0, l1.size());
    assertEquals(null, l1.getFirst());
    assertEquals(null, l1.getLast());
    assertEquals(false, l1.contains(insn));
    assertInsnEquals(new AbstractInsnNode[0], l1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testAcceptor1() {
    l1.add(new InsnNode(55));
    l1.add(new InsnNode(77));

    final InsnList lst = new InsnList();
    l1.accept(
        new MethodVisitor(Opcodes.ASM5) {
          @Override
          public void visitInsn(int opcode) {
            lst.add(new InsnNode(opcode));
          }
        });

    assertEquals(55, lst.get(0).opcode);
    assertEquals(77, lst.get(1).opcode);
  }

  @Test
  public void testResetLabels() throws Exception {
    LabelNode labelNode = new LabelNode();

    l1.add(new InsnNode(55));
    l1.add(labelNode);
    l1.add(new InsnNode(55));

    Label label = labelNode.getLabel();
    assertNotNull(label);

    l1.resetLabels();

    assertNotSame(label, labelNode.getLabel());
  }
}

class CheckedInsnList extends InsnList {

  @Override
  public int indexOf(final AbstractInsnNode insn) {
    if (!contains(insn)) {
      throw new IllegalArgumentException();
    }
    return super.indexOf(insn);
  }

  @Override
  public void set(final AbstractInsnNode location, final AbstractInsnNode insn) {
    if (!(contains(location) && insn.index == -1)) {
      throw new IllegalArgumentException();
    }
    super.set(location, insn);
  }

  @Override
  public void add(final AbstractInsnNode insn) {
    if (insn.index != -1) {
      throw new IllegalArgumentException();
    }
    super.add(insn);
  }

  @Override
  public void add(final InsnList insns) {
    if (insns == this) {
      throw new IllegalArgumentException();
    }
    super.add(insns);
  }

  @Override
  public void insert(final AbstractInsnNode insn) {
    if (insn.index != -1) {
      throw new IllegalArgumentException();
    }
    super.insert(insn);
  }

  @Override
  public void insert(final InsnList insns) {
    if (insns == this) {
      throw new IllegalArgumentException();
    }
    super.insert(insns);
  }

  @Override
  public void insert(final AbstractInsnNode location, final AbstractInsnNode insn) {
    if (!(contains(location) && insn.index == -1)) {
      throw new IllegalArgumentException();
    }
    super.insert(location, insn);
  }

  @Override
  public void insert(final AbstractInsnNode location, final InsnList insns) {
    if (!(contains(location) && insns != this)) {
      throw new IllegalArgumentException();
    }
    super.insert(location, insns);
  }

  @Override
  public void insertBefore(final AbstractInsnNode location, final AbstractInsnNode insn) {
    if (!(contains(location) && insn.index == -1)) {
      throw new IllegalArgumentException();
    }
    super.insertBefore(location, insn);
  }

  @Override
  public void insertBefore(final AbstractInsnNode location, final InsnList insns) {
    if (!(contains(location) && insns != this)) {
      throw new IllegalArgumentException();
    }
    super.insertBefore(location, insns);
  }

  @Override
  public void remove(final AbstractInsnNode insn) {
    if (!contains(insn)) {
      throw new IllegalArgumentException();
    }
    super.remove(insn);
  }

  @Override
  public void clear() {
    super.removeAll(true);
  }
}
