/***
 * ASM examples: examples showing how asm can be used
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

public class ArraySet implements Set {

  private int[] values = new int[3];

  private int size;

  public boolean contains (int v) {
    for (int i = 0; i < size; ++i) {
      if (values[i] == v) {
        return true;
      }
    }
    return false;
  }

  public void add (int v) {
    if (!contains(v)) {
      if (size == values.length) {
        System.err.println("[enlarge]");
        int[] newValues = new int[values.length + 3];
        for (int i = 0; i < size; ++i) {
          newValues[i] = values[i];
        }
        values = newValues;
      }
      values[size++] = v;
    }
  }

  public void remove (int v) {
    int i = 0;
    int j = 0;
    while (i < size) {
      int u = values[i];
      if (u != v) {
        values[j++] = u;
      }
      ++i;
    }
    size = j;
  }
}
