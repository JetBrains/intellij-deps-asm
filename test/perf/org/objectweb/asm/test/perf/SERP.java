package org.objectweb.asm.test.perf;

import serp.bytecode.BCClass;
import serp.bytecode.BCField;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Instruction;
import serp.bytecode.Project;

import java.io.InputStream;

public class SERP extends ALL {

  private static Project p = new Project();

  private static BCClass c;

  public static void main (final String args[]) throws Exception {
    System.out.println("SERP PERFORMANCES\n");
    new SERP().perfs(args);
  }

  ALL newInstance () {
    return new SERP();
  }

  byte[] nullAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    if (c != null) {
      p.removeClass(c);
    }
    c = p.loadClass(is);
    BCField[] fields = c.getDeclaredFields();
    BCMethod[] methods = c.getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {
      Code code = methods[i].getCode(false);
      if (code != null) {
        while (code.hasNext()) {
          Instruction ins = code.next();
        }
        if (compute) {
          code.calculateMaxStack();
          code.calculateMaxLocals();
        }
      }
    }
    return c.toByteArray();
  }

  byte[] counterAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    if (c != null) {
      p.removeClass(c);
    }
    c = p.loadClass(is);
    BCField[] fields = c.getDeclaredFields();
    if (!c.isInterface()) {
      c.declareField("_counter", "I");
    }
    BCMethod[] methods = c.getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {
      BCMethod m = methods[i];
      if (!m.getName().equals("<init>") &&
          !m.isStatic() && !m.isAbstract() && !m.isNative())
      {
        Code code = m.getCode(false);
        if (code != null) {
          code.aload().setLocal(0);
          code.aload().setLocal(0);
          code.getfield().setField(name, "_counter", "I");
          code.constant().setValue(1);
          code.iadd();
          code.putfield().setField(name, "_counter", "I");
          code.setMaxStack(Math.max(code.getMaxStack(), 2));
        }
      }
    }
    return c.toByteArray();
  }
}
