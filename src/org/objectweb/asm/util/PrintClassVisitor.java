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

  protected final ArrayList dump;

  protected final PrintWriter pw;

  /**
   * Constructs a new {@link PrintClassVisitor PrintClassVisitor} object.
   *
   * @param pw the print writer to be used to print the trace.
   */

  public PrintClassVisitor (final PrintWriter pw) {
    this.dump = new ArrayList();
    this.pw = pw;
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    PrintCodeVisitor pcv = printMethod(access, name, desc, exceptions);
    dump.add(pcv.getText());
    return pcv;
  }

  public void visitEnd () {
    printList(dump);
    pw.flush();
  }

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

  public abstract PrintCodeVisitor printMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions);
}
