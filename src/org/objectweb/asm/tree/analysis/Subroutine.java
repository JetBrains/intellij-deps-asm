/*
 * Copyright area
 */

package org.objectweb.asm.tree.analysis;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.JumpInsnNode;

class Subroutine {

  Label start;

  boolean[] access;

  List callers;

  private Subroutine () {
  }

  public Subroutine (final Label start, final int maxLocals, final JumpInsnNode caller) {
    this.start = start;
    this.access = new boolean[maxLocals];
    this.callers = new ArrayList();
    callers.add(caller);
  }

  public Subroutine copy () {
    Subroutine result = new Subroutine();
    result.start = start;
    result.access = new boolean[access.length];
    System.arraycopy(access, 0, result.access, 0, access.length);
    result.callers = new ArrayList(callers);
    return result;
  }

  public boolean merge (final Subroutine subroutine) throws AnalyzerException {
    if (subroutine.start != start) {
      throw new AnalyzerException("Overlapping sub routines");
    }
    boolean changes = false;
    for (int i = 0; i < access.length; ++i) {
      if (subroutine.access[i] && !access[i]) {
        access[i] = true;
        changes = true;
      }
    }
    for (int i = 0; i < subroutine.callers.size(); ++i) {
      Object caller = subroutine.callers.get(i);
      if (!callers.contains(caller)) {
        callers.add(caller);
        changes = true;
      }
    }
    return changes;
  }
}