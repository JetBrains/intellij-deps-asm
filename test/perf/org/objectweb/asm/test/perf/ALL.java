/***
 * ASM performance test: measures the performances of asm package
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

package org.objectweb.asm.test.perf;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class ALL extends ClassLoader  {

  private static ZipFile zip;

  private static ZipOutputStream dst;

  private static int mode;

  private static int total;

  private static int totalSize;

  private static double[][] perfs;

  static boolean compute;

  static boolean skipDebug;

  public static void main (String[] args) throws Exception {
    System.out.println("Comparing ASM, BCEL and SERP performances...");
    System.out.println("This may take 20 to 30 minutes\n");
    // measures performances
    System.out.println("ASM PERFORMANCES\n");
    new ASM().perfs(args);
    double[][] asmPerfs = perfs;
    System.out.println("\nBCEL PERFORMANCES\n");
    new BCEL().perfs(args);
    double[][] bcelPerfs = perfs;
    System.out.println("\nSERP PERFORMANCES\n");
    new SERP().perfs(args);
    double[][] serpPerfs = perfs;

    // prints results
    System.out.println("\nGLOBAL RESULTS");
    System.out.println("\nWITH DEBUG INFORMATION\n");
    for (int step = 0; step < 2; ++step) {
      for (mode = 0; mode < 4; ++mode) {
        switch (mode) {
          case 0: System.out.print("NO ADAPT:     "); break;
          case 1: System.out.print("NULL ADAPT:   "); break;
          case 2: System.out.print("COMPUTE MAXS: "); break;
          default: System.out.print("ADD COUNTER:  "); break;
        }
        System.out.print((float)asmPerfs[step][mode] + " ms");
        if (mode > 0) {
          System.out.print(" (*");
          System.out.print((float)(asmPerfs[step][mode]/asmPerfs[step][0]));
          System.out.print(")");
        }
        System.out.print(" ");
        System.out.print((float)bcelPerfs[step][mode] + " ms");
        if (mode > 0) {
          System.out.print(" (*");
          System.out.print((float)(bcelPerfs[step][mode]/bcelPerfs[step][0]));
          System.out.print(")");
        }
        System.out.print(" ");
        System.out.print((float)serpPerfs[step][mode] + " ms");
        if (mode > 0) {
          System.out.print(" (*");
          System.out.print((float)(serpPerfs[step][mode]/serpPerfs[step][0]));
          System.out.print(")");
        }
        System.out.println();
      }
      if (step == 0) {
        System.out.println("\nWITHOUT DEBUG INFORMATION\n");
      }
    }

    System.out.println("\nRELATIVE RESULTS");
    System.out.println("\nWITH DEBUG INFORMATION\n");
    for (int step = 0; step < 2; ++step) {
			System.err.println("[MEASURE      ASM       BCEL      SERP]");
      for (mode = 1; mode < 4; ++mode) {
        int base;
        switch (mode) {
          case 1: System.out.print("NULL ADAPT:   "); base = 0; break;
          case 2: System.out.print("COMPUTE MAXS: "); base = 1; break;
          default: System.out.print("ADD COUNTER:  "); base = 1; break;
        }
        double ref = asmPerfs[step][mode] - asmPerfs[step][base];
        System.out.print((float)ref + " ms ");
        double f = bcelPerfs[step][mode] - bcelPerfs[step][base];
        System.out.print((float)f + " ms (*");
        System.out.print((float)(f/ref));
        System.out.print(") ");
        double g = serpPerfs[step][mode] - serpPerfs[step][base];
        System.out.print((float)g + " ms (*");
        System.out.print((float)(g/ref));
        System.out.print(")");
        System.out.println();
      }
      if (step == 0) {
        System.out.println("\nWITHOUT DEBUG INFORMATION\n");
      }
    }
  }

  void perfs (final String[] args) throws Exception {
    // prepares zip files, if necessary
    if (!(new File(args[0] + "classes1.zip").exists())) {
      for (int step = 0; step < 2; ++step) {
        dst = new ZipOutputStream(
          new FileOutputStream(args[0] + "classes" + (step + 1) + ".zip"));
        mode = step == 0 ? 1 : 4;
        for (int i = 1; i < args.length; ++i) {
          ALL loader = newInstance();
          zip = new ZipFile(args[i]);
          Enumeration entries = zip.entries();
          double t = System.currentTimeMillis();
          while (entries.hasMoreElements()) {
            String s = ((ZipEntry)entries.nextElement()).getName();
            if (s.endsWith(".class")) {
              s = s.substring(0, s.length() - 6).replace('/', '.');
              loader.loadClass(s);
            }
          }
        }
        dst.close();
        dst = null;
      }
    }

    // measures performances
    perfs = new double[2][4];
    System.out.println("FIRST STEP: WITH DEBUG INFORMATION");
    for (int step = 0; step < 2; ++step) {
      zip = new ZipFile(args[0] + "classes" + (step + 1) + ".zip");
      for (mode = 0; mode < 4; ++mode) {
        for (int i = 0; i < 4; ++i) {
          ALL loader = newInstance();
          total = 0;
          totalSize = 0;
          Enumeration entries = zip.entries();
          double t = System.currentTimeMillis();
          while (entries.hasMoreElements()) {
            String s = ((ZipEntry)entries.nextElement()).getName();
            if (s.endsWith(".class")) {
              s = s.substring(0, s.length() - 6).replace('/', '.');
              loader.loadClass(s);
            }
          }
          t = System.currentTimeMillis() - t;
          if (i == 0) {
            perfs[step][mode] = t;
          } else {
            perfs[step][mode] = Math.min(perfs[step][mode], t);
          }
          switch (mode) {
            case 0: System.out.print("NO ADAPT:     "); break;
            case 1: System.out.print("NULL ADAPT:   "); break;
            case 2: System.out.print("COMPUTE MAXS: "); break;
            default: System.out.print("ADD COUNTER:  "); break;
          }
          System.out.print((float)t + " ms ");
          System.out.print("(" + total + " classes");
          System.out.println(", " + totalSize + " bytes)");
          loader = null;
          gc();
        }
      }
      if (step == 0) {
        System.out.println("SECOND STEP: WITHOUT DEBUG INFORMATION");
      }
    }

    // prints results
    System.out.println("\nRESULTS");
    System.out.println("\nWITH DEBUG INFORMATION\n");
    for (int step = 0; step < 2; ++step) {
      for (mode = 0; mode < 4; ++mode) {
        switch (mode) {
          case 0: System.out.print("NO ADAPT:     "); break;
          case 1: System.out.print("NULL ADAPT:   "); break;
          case 2: System.out.print("COMPUTE MAXS: "); break;
          default: System.out.print("ADD COUNTER:  "); break;
        }
        System.out.println((float)perfs[step][mode] + " ms");
      }
      if (step == 0) {
        System.out.println("\nWITHOUT DEBUG INFORMATION\n");
      }
    }
  }

  protected Class findClass (final String name) throws ClassNotFoundException {
    try {
      byte[] b;
      String fileName = name.replace('.', '/') + ".class";
      InputStream is = zip.getInputStream(zip.getEntry(fileName));
      switch (mode) {
        case 0:
          b = new byte[is.available()];
          int len = 0;
          while (true) {
            int n = is.read(b, len, b.length - len);
            if (n == -1) {
              if (len < b.length) {
                byte[] c = new byte[len];
                System.arraycopy(b, 0, c, 0, len);
                b = c;
              }
              break;
            } else {
              len += n;
              if (len == b.length) {
                byte[] c = new byte[b.length + 1000];
                System.arraycopy(b, 0, c, 0, len);
                b = c;
              }
            }
          }
          break;
        case 1:
          compute = false;
          skipDebug = false;
          b = nullAdaptClass(is, name);
          break;
        case 2:
          compute = true;
          skipDebug = false;
          b = nullAdaptClass(is, name);
          break;
        case 3:
          b = counterAdaptClass(is, name);
          break;
        //case 4:
        default:
          compute = false;
          skipDebug = true;
          b = nullAdaptClass(is, name);
          break;
      }
      if (dst != null) {
        dst.putNextEntry(new ZipEntry(fileName));
        dst.write(b, 0, b.length);
        dst.closeEntry();
      }
      total += 1;
      totalSize += b.length;
      return defineClass(name, b, 0, b.length);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ClassNotFoundException(name);
    }
  }

  private static void gc () {
  	try {
      Runtime.getRuntime().gc();
      Thread.currentThread().sleep(50);
      Runtime.getRuntime().gc();
      Thread.currentThread().sleep(50);
      Runtime.getRuntime().gc();
      Thread.currentThread().sleep(50);
    } catch (InterruptedException e) {
    }
  }

  abstract ALL newInstance ();

  abstract byte[] nullAdaptClass (final InputStream is, final String name)
    throws Exception;

  abstract byte[] counterAdaptClass (final InputStream is, final String name)
    throws Exception;
}
