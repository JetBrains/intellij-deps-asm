/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.Constants;
import org.objectweb.asm.CodeVisitor;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A node that represents a LOOKUPSWITCH instruction.
 */

public class LookupSwitchInsnNode extends AbstractInsnNode {

  /**
   * Beginning of the default handler block.
   */

  public Label dflt;

  /**
   * The values of the keys. This list is a list a {@link java.lang.Integer
   * Integer} objects.
   */

  public final List keys;

  /**
   * Beginnings of the handler blocks. This list is a list of {@link Label
   * Label} objects.
   */

  public final List labels;

  /**
   * Constructs a new {@link LookupSwitchInsnNode LookupSwitchInsnNode} object.
   *
   * @param dflt beginning of the default handler block.
   * @param keys the values of the keys.
   * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
   *      beginning of the handler block for the <tt>keys[i]</tt> key.
   */

  public LookupSwitchInsnNode (
    final Label dflt,
    final int[] keys,
    final Label[] labels)
  {
    super(Constants.LOOKUPSWITCH);
    this.dflt = dflt;
    this.keys = new ArrayList();
    this.labels = new ArrayList();
    if (keys != null) {
      for (int i = 0; i < keys.length; ++i) {
        this.keys.add(new Integer(keys[i]));
      }
    }
    if (labels != null) {
      this.labels.addAll(Arrays.asList(labels));
    }
  }

  public void accept (final CodeVisitor cv) {
    int[] keys = new int[this.keys.size()];
    for (int i = 0; i < keys.length; ++i) {
      keys[i] = ((Integer)this.keys.get(i)).intValue();
    }
    Label[] labels = new Label[this.labels.size()];
    this.labels.toArray(labels);
    cv.visitLookupSwitchInsn(dflt, keys, labels);
  }
}
