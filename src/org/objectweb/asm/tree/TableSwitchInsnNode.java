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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A node that represents a TABLESWITCH instruction.
 */

public class TableSwitchInsnNode extends AbstractInsnNode {

  /**
   * The minimum key value.
   */

  public int min;

  /**
   * The maximum key value.
   */

  public int max;

  /**
   * Beginning of the default handler block.
   */

  public Label dflt;

  /**
   * Beginnings of the handler blocks. This list is a list of {@link Label
   * Label} objects.
   */

  public final List labels;

  /**
   * Constructs a new {@link TableSwitchInsnNode TableSwitchInsnNode}.
   *
   * @param min the minimum key value.
   * @param max the maximum key value.
   * @param dflt beginning of the default handler block.
   * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
   *      beginning of the handler block for the <tt>min + i</tt> key.
   */

  public TableSwitchInsnNode (
    final int min,
    final int max,
    final Label dflt,
    final Label[] labels)
  {
    super(Constants.TABLESWITCH);
    this.min = min;
    this.max = max;
    this.dflt = dflt;
    this.labels = new ArrayList();
    if (labels != null) {
      this.labels.addAll(Arrays.asList(labels));
    }
  }

  public void accept (final CodeVisitor cv) {
    Label[] labels = new Label[this.labels.size()];
    this.labels.toArray(labels);
    cv.visitTableSwitchInsn(min, max, dflt, labels);
  }
}
