/***
 * ASM performance test: measures the performances of asm package
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
package org.objectweb.asm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.verifier.structurals.ModifiedPass3bVerifier;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import serp.bytecode.BCClass;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Project;

/**
 * @author Eric Bruneton
 */
public abstract class ALLPerfTest extends ClassLoader {

    static boolean compute;

    static boolean computeFrames;

    static boolean skipDebug;

    static ClassPool pool;

    static Project p;

    static BCClass c;

    public static void main(final String[] args) throws Exception {
        String clazz = System.getProperty("asm.test.class");
        ZipFile zip = new ZipFile(System.getProperty("java.home")
                + "/lib/rt.jar");
        List classes = new ArrayList();

        Enumeration entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = (ZipEntry) entries.nextElement();
            String s = e.getName();
            if (s.endsWith(".class")) {
                s = s.substring(0, s.length() - 6).replace('/', '.');
                if (clazz == null || s.indexOf(clazz) != -1) {
                    InputStream is = zip.getInputStream(e);
                    classes.add(new ClassReader(is).b);
                }
            }
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassReader cr = new ClassReader(b);
                int access = cr.getAccess();
                String className = cr.getClassName();
                String superName = cr.getSuperName();
                String[] interfaces = cr.getInterfaces();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to get class info for " + classes.size()
                    + " classes = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                new ClassReader(b).accept(new EmptyVisitor(), 0);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize " + classes.size()
                    + " classes = " + t + " ms");
        }
        
        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(0);
                new ClassReader(b).accept(cw, 0);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassReader cr = new ClassReader(b);
                ClassWriter cw = new ClassWriter(cr, 0);
                cr.accept(cw, 0);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with copyPool = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                new ClassReader(b).accept(cw, 0);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with computeMaxs = " + t
                    + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            int errors = 0;
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                try {
                    new ClassReader(b).accept(cw, 0);
                } catch (Throwable e) {
                    ++errors;
                }
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with computeFrames = " + t
                    + " ms (" + errors + " errors)");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(0);
                new ClassReader(b).accept(new ClassAdapter(cw) {

                    public MethodVisitor visitMethod(
                        final int access,
                        final String name,
                        final String desc,
                        final String signature,
                        final String[] exceptions)
                    {
                        return new LocalVariablesSorter(access,
                                desc,
                                cv.visitMethod(access,
                                        name,
                                        desc,
                                        signature,
                                        exceptions));
                    }

                }, ClassReader.EXPAND_FRAMES);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with LocalVariablesSorter = "
                    + t + " ms");
        }

        System.out.println();

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                new ClassReader(b).accept(new ClassNode(), 0);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize " + classes.size()
                    + " classes with tree package = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(0);
                ClassNode cn = new ClassNode();
                new ClassReader(b).accept(cn, 0);
                cn.accept(cw);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with tree package = " + t
                    + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            int errors = 0;
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size() / 10; ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassReader cr = new ClassReader(b);
                ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_DEBUG);
                List methods = cn.methods;
                for (int k = 0; k < methods.size(); ++k) {
                    MethodNode method = (MethodNode) methods.get(k);
                    Analyzer a = new Analyzer(new SimpleVerifier());
                    try {
                        a.analyze(cn.name, method);
                    } catch (Throwable th) {
                        ++errors;
                    }
                }
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to analyze " + classes.size() / 10
                    + " classes with SimpleVerifier = " + t + " ms (" + errors
                    + " errors)");
        }
        System.out.println();

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullBCELAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with BCEL = " + t + " ms");
        }

        compute = true;
        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullBCELAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with BCEL and computeMaxs = "
                    + t + " ms");
        }

        compute = false;
        computeFrames = true;
        for (int i = 0; i < 10; ++i) {
            int errors = 0;
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                try {
                    nullBCELAdapt(b);
                } catch (Throwable e) {
                    ++errors;
                }
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size()
                    + " classes with BCEL and computeFrames = " + t + " ms ("
                    + errors + " errors)");
        }

        System.out.println();

        compute = false;
        computeFrames = false;
        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullAspectjBCELAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with Aspectj BCEL = " + t
                    + " ms");
        }

        compute = true;
        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullAspectjBCELAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size()
                    + " classes with Aspectj BCEL and computeMaxs = " + t
                    + " ms");
        }

        compute = false;
        computeFrames = true;
        for (int i = 0; i < 10; ++i) {
            int errors = 0;
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                try {
                    nullAspectjBCELAdapt(b);
                } catch (Throwable e) {
                    ++errors;
                }
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size()
                    + " classes with Aspectj BCEL and computeFrames = " + t
                    + " ms (" + errors + " errors)");
        }

        System.out.println();

        compute = false;
        computeFrames = false;
        for (int i = 0; i < 10; ++i) {
            pool = new ClassPool(null);
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullJavassistAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with Javassist = " + t + " ms");
        }

        System.out.println();

        for (int i = 0; i < 10; ++i) {
            p = new Project();
            c = null;
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                nullSERPAdapt(b);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with SERP = " + t + " ms");
        }

    }

    private static void nullBCELAdapt(final byte[] b) throws IOException {
        JavaClass jc = new ClassParser(new ByteArrayInputStream(b),
                "class-name").parse();
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cp = cg.getConstantPool();
        Method[] ms = cg.getMethods();
        for (int k = 0; k < ms.length; ++k) {
            MethodGen mg = new MethodGen(ms[k], cg.getClassName(), cp);
            boolean lv = ms[k].getLocalVariableTable() == null;
            boolean ln = ms[k].getLineNumberTable() == null;
            if (lv) {
                mg.removeLocalVariables();
            }
            if (ln) {
                mg.removeLineNumbers();
            }
            mg.stripAttributes(skipDebug);
            InstructionList il = mg.getInstructionList();
            if (il != null) {
                InstructionHandle ih = il.getStart();
                while (ih != null) {
                    ih = ih.getNext();
                }
                if (compute) {
                    mg.setMaxStack();
                    mg.setMaxLocals();
                }
                if (computeFrames) {
                    ModifiedPass3bVerifier verif;
                    verif = new ModifiedPass3bVerifier(jc, k);
                    verif.do_verify();
                }
            }
            cg.replaceMethod(ms[k], mg.getMethod());
        }
        cg.getJavaClass().getBytes();
    }

    private static void nullAspectjBCELAdapt(final byte[] b) throws IOException
    {
        org.aspectj.apache.bcel.classfile.JavaClass jc = new org.aspectj.apache.bcel.classfile.ClassParser(new ByteArrayInputStream(b),
                "class-name").parse();
        org.aspectj.apache.bcel.generic.ClassGen cg = new org.aspectj.apache.bcel.generic.ClassGen(jc);
        org.aspectj.apache.bcel.generic.ConstantPoolGen cp = cg.getConstantPool();
        org.aspectj.apache.bcel.classfile.Method[] ms = cg.getMethods();
        for (int k = 0; k < ms.length; ++k) {
            org.aspectj.apache.bcel.generic.MethodGen mg = new org.aspectj.apache.bcel.generic.MethodGen(ms[k],
                    cg.getClassName(),
                    cp);
            boolean lv = ms[k].getLocalVariableTable() == null;
            boolean ln = ms[k].getLineNumberTable() == null;
            if (lv) {
                mg.removeLocalVariables();
            }
            if (ln) {
                mg.removeLineNumbers();
            }
            mg.stripAttributes(skipDebug);
            org.aspectj.apache.bcel.generic.InstructionList il = mg.getInstructionList();
            if (il != null) {
                org.aspectj.apache.bcel.generic.InstructionHandle ih = il.getStart();
                while (ih != null) {
                    ih = ih.getNext();
                }
                if (compute) {
                    mg.setMaxStack();
                    mg.setMaxLocals();
                }
                if (computeFrames) {
                    org.aspectj.apache.bcel.verifier.structurals.ModifiedPass3bVerifier verif = new org.aspectj.apache.bcel.verifier.structurals.ModifiedPass3bVerifier(jc,
                            k);
                    verif.do_verify();
                }
            }
            cg.replaceMethod(ms[k], mg.getMethod());
        }
        cg.getJavaClass().getBytes();
    }

    private static void nullJavassistAdapt(final byte[] b) throws Exception {
        CtClass cc = pool.makeClass(new ByteArrayInputStream(b));
        CtMethod[] ms = cc.getDeclaredMethods();
        for (int j = 0; j < ms.length; ++j) {
            if (skipDebug) {
                // is there a mean to remove the debug attributes?
            }
            if (compute) {
                // how to force recomputation of maxStack and maxLocals?
            }
        }
        cc.toBytecode();
    }

    private static void nullSERPAdapt(final byte[] b) throws Exception {
        if (c != null) {
            p.removeClass(c);
        }
        c = p.loadClass(new ByteArrayInputStream(b));
        c.getDeclaredFields();
        BCMethod[] methods = c.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            Code code = methods[i].getCode(false);
            if (code != null) {
                while (code.hasNext()) {
                    code.next();
                }
                if (compute) {
                    code.calculateMaxStack();
                    code.calculateMaxLocals();
                }
            }
        }
        c.toByteArray();
    }
}
