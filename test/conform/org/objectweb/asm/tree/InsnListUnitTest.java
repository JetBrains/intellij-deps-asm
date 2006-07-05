/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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
 */
package org.objectweb.asm.tree;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * InsnList unit tests.
 * 
 * @author Eric Bruneton
 */
public class InsnListUnitTest extends TestCase {

    InsnList l1;

    InsnList l2;

    InsnNode in1;

    InsnNode in2;

    protected void setUp() throws Exception {
        super.setUp();
        InsnList.check = true;
        l1 = new InsnList();
        l2 = new InsnList();
        in1 = new InsnNode(0);
        in2 = new InsnNode(0);
        l2.add(in1);
        l2.add(in2);
    }

    protected void assertEquals(
        final AbstractInsnNode[] expected,
        final AbstractInsnNode[] value)
    {
        assertEquals(expected.length, value.length);
        for (int i = 0; i < value.length; ++i) {
            assertEquals(expected[i], value[i]);
        }
    }

    public void testSize() {
        assertEquals(0, l1.size());
    }

    public void testGetFirst() {
        assertEquals(null, l1.getFirst());
    }

    public void testGetLast() {
        assertEquals(null, l1.getLast());
    }

    public void testInvalidGet() {
        try {
            l1.get(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testContains() {
        assertEquals(false, l1.contains(new InsnNode(0)));
    }

    public void testIterator() {
        InsnNode insn = new InsnNode(0);
        l2.add(insn);
        Iterator it = l2.iterator();
        assertEquals(true, it.hasNext());
        assertEquals(in1, it.next());
        assertEquals(true, it.hasNext());
        assertEquals(in2, it.next());
        assertEquals(true, it.hasNext());
        it.remove();
        assertEquals(true, it.hasNext());
        assertEquals(insn, it.next());
        assertEquals(false, it.hasNext());
        it = l2.iterator();
        assertEquals(true, it.hasNext());
        assertEquals(in1, it.next());
        assertEquals(true, it.hasNext());
        assertEquals(insn, it.next());
        assertEquals(false, it.hasNext());
    }

    public void testInvalidIndexOf() {
        try {
            l1.indexOf(new InsnNode(0));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testToArray() {
        assertEquals(0, l1.toArray().length);
    }

    public void testInvalidSet() {
        try {
            l1.set(new InsnNode(0), new InsnNode(0));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSet() {
        l1.add(new InsnNode(0));
        AbstractInsnNode insn = new InsnNode(0);
        l1.set(l1.get(0), insn);
        assertEquals(1, l1.size());
        assertEquals(insn, l1.getFirst());
    }

    public void testInvalidAdd() {
        try {
            l1.add(in1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAddEmpty() {
        InsnNode insn = new InsnNode(0);
        l1.add(insn);
        assertEquals(1, l1.size());
        assertEquals(insn, l1.getFirst());
        assertEquals(insn, l1.getLast());
        assertEquals(insn, l1.get(0));
        assertEquals(true, l1.contains(insn));
        assertEquals(0, l1.indexOf(insn));
        assertEquals(new AbstractInsnNode[] { insn }, l1.toArray());
        assertEquals(null, insn.getPrevious());
        assertEquals(null, insn.getNext());
    }

    public void testAddNonEmpty() {
        InsnNode insn = new InsnNode(0);
        l1.add(new InsnNode(0));
        l1.add(insn);
        assertEquals(2, l1.size());
        assertEquals(insn, l1.getLast());
        assertEquals(insn, l1.get(1));
        assertEquals(true, l1.contains(insn));
        assertEquals(1, l1.indexOf(insn));
    }

    public void testAddEmptyList() {
        l1.add(new InsnList());
        assertEquals(0, l1.size());
        assertEquals(null, l1.getFirst());
        assertEquals(null, l1.getLast());
        assertEquals(new AbstractInsnNode[0], l1.toArray());
    }

    public void testInvalidAddAll() {
        try {
            l1.add(l1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

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
        assertEquals(new AbstractInsnNode[] { in1, in2 }, l1.toArray());
    }

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
        assertEquals(new AbstractInsnNode[] { insn, in1, in2 }, l1.toArray());
    }

    public void testInvalidInsert() {
        try {
            l1.insert(in1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInsertEmpty() {
        InsnNode insn = new InsnNode(0);
        l1.insert(insn);
        assertEquals(1, l1.size());
        assertEquals(insn, l1.getFirst());
        assertEquals(insn, l1.getLast());
        assertEquals(insn, l1.get(0));
        assertEquals(true, l1.contains(insn));
        assertEquals(0, l1.indexOf(insn));
        assertEquals(new AbstractInsnNode[] { insn }, l1.toArray());
    }

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

    public void testInvalidInsertAll() {
        try {
            l1.insert(l1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInsertAllEmptyList() {
        l1.insert(new InsnList());
        assertEquals(0, l1.size());
        assertEquals(null, l1.getFirst());
        assertEquals(null, l1.getLast());
        assertEquals(new AbstractInsnNode[0], l1.toArray());
    }

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
        assertEquals(new AbstractInsnNode[] { in1, in2 }, l1.toArray());
    }

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
        assertEquals(new AbstractInsnNode[] { in1, in2, insn }, l1.toArray());
    }

    public void testInvalidInsert2() {
        try {
            l1.insert(new InsnNode(0), new InsnNode(0));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInsert2NotLast() {
        InsnNode insn = new InsnNode(0);
        l2.insert(in1, insn);
        assertEquals(3, l2.size());
        assertEquals(in1, l2.getFirst());
        assertEquals(in2, l2.getLast());
        assertEquals(in1, l2.get(0));
        assertEquals(true, l2.contains(insn));
        assertEquals(1, l2.indexOf(insn));
        assertEquals(new AbstractInsnNode[] { in1, insn, in2 }, l2.toArray());
    }

    public void testInsert2Last() {
        InsnNode insn = new InsnNode(0);
        l2.insert(in2, insn);
        assertEquals(3, l2.size());
        assertEquals(in1, l2.getFirst());
        assertEquals(insn, l2.getLast());
        assertEquals(in1, l2.get(0));
        assertEquals(true, l2.contains(insn));
        assertEquals(2, l2.indexOf(insn));
        assertEquals(new AbstractInsnNode[] { in1, in2, insn }, l2.toArray());
    }

    public void testInvalidInsertAll2() {
        try {
            l1.insert(new InsnNode(0), new InsnList());
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testInsertAll2EmptyList() {
        InsnNode insn = new InsnNode(0);
        l1.add(insn);
        l1.insert(insn, new InsnList());
        assertEquals(1, l1.size());
        assertEquals(insn, l1.getFirst());
        assertEquals(insn, l1.getLast());
        assertEquals(new AbstractInsnNode[] { insn }, l1.toArray());
    }

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
        assertEquals(new AbstractInsnNode[] { insn, in1, in2 }, l1.toArray());
    }

    public void testInvalidRemove() {
        try {
            l1.remove(new InsnNode(0));
        } catch (IllegalArgumentException e) {
        }
    }

    public void testRemoveSingle() {
        InsnNode insn = new InsnNode(0);
        l1.add(insn);
        l1.remove(insn);
        assertEquals(0, l1.size());
        assertEquals(null, l1.getFirst());
        assertEquals(null, l1.getLast());
        assertEquals(false, l1.contains(insn));
        assertEquals(new AbstractInsnNode[0], l1.toArray());
        assertEquals(null, insn.getPrevious());
        assertEquals(null, insn.getNext());
    }

    public void testRemoveFirst() {
        InsnNode insn = new InsnNode(0);
        l1.add(insn);
        l1.add(new InsnNode(0));
        l1.remove(insn);
        assertEquals(false, l1.contains(insn));
        assertEquals(null, insn.getPrevious());
        assertEquals(null, insn.getNext());
    }

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

    public void testRemoveLast() {
        InsnNode insn = new InsnNode(0);
        l1.add(new InsnNode(0));
        l1.add(insn);
        l1.remove(insn);
        assertEquals(false, l1.contains(insn));
        assertEquals(null, insn.getPrevious());
        assertEquals(null, insn.getNext());
    }

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
        assertEquals(new AbstractInsnNode[0], l1.toArray());
        assertEquals(null, insn.getPrevious());
        assertEquals(null, insn.getNext());
    }
}
