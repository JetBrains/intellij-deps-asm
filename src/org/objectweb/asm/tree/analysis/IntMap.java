/*
 * Copyright area
 */

package org.objectweb.asm.tree.analysis;

class IntMap {
  
  private int size;
  
  private Object[] keys;
  
  private int[] values;
  
  public IntMap (final int size) {
    this.size = size;
    this.keys = new Object[size];
    this.values = new int[size];
  }
  
  public int get (final Object key) {
    int n = size;
    int i = (key.hashCode() & 0x7FFFFFFF)%n;
    while (keys[i] != key) {
      i = (i+1)%n;
    }
    return values[i];
  }
  
  public void put (final Object key, final int value) {
    int n = size;
    int i = (key.hashCode() & 0x7FFFFFFF)%n;
    while (keys[i] != null) {
      i = (i+1)%n;
    }
    keys[i] = key;
    values[i] = value;
  }
}