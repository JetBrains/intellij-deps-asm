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

package org.objectweb.asm.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class visitor that prints the classes it visits.
 */

public abstract class PrintClassVisitor implements ClassVisitor {

  /**
   * The text to be printed. Since the code of methods is not necessarily
   * visited in sequential order, one method after the other, but can be
   * interlaced (some instructions from method one, then some instructions from
   * method two, then some instructions from method one again...), it is not
   * possible to print the visited instructions directly to a sequential
   * stream. A class is therefore printed in a two steps process: a string tree
   * is constructed during the visit, and printed to a sequential stream at the
   * end of the visit. This string tree is stored in this field, as a string
   * list that can contain other string lists, which can themselves contain
   * other string lists, and so on.
   */

  protected final List text;

  /**
   * A buffer that can be used to create strings.
   */

  protected final StringBuffer buf;

  /**
   * The print writer to be used to print the class.
   */

  protected final PrintWriter pw;

  /**
   * Constructs a new {@link PrintClassVisitor PrintClassVisitor} object.
   *
   * @param pw the print writer to be used to print the class.
   */

  public PrintClassVisitor (final PrintWriter pw) {
    this.text = new ArrayList();
    this.buf = new StringBuffer();
    this.pw = pw;
  }

  public void visitEnd () {
    printList(text);
    pw.flush();
  }

  /**
   * Prints the given string tree to {@link pw pw}.
   *
   * @param l a string tree, i.e., a string list that can contain other string
   *      lists, and so on recursively.
   */

  private void printList (final List l) {
    for (int i = 0; i < l.size(); ++i) {
      Object o = l.get(i);
      if (o instanceof List) {
        printList((List)o);
      } else {
        pw.print(o.toString());
      }
    }
  }
}
