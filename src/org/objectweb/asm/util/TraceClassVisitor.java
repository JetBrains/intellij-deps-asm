/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
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
package org.objectweb.asm.util;

import java.io.FileInputStream;
import java.io.PrintWriter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A {@link ClassVisitor} that prints a disassembled view of the classes it
 * visits. This class visitor can be used alone (see the {@link #main main}
 * method) to disassemble a class. It can also be used in the middle of class
 * visitor chain to trace the class that is visited at a given point in this
 * chain. This may be uselful for debugging purposes. <p> The trace printed when
 * visiting the <tt>Hello</tt> class is the following: <p> <blockquote>
 * 
 * <pre>
 * // class version 49.0 (49)
 * // access flags 0x21
 * public class Hello {
 *
 *  // compiled from: Hello.java
 *
 *   // access flags 0x1
 *   public &lt;init&gt; ()V
 *     ALOAD 0
 *     INVOKESPECIAL java/lang/Object &lt;init&gt; ()V
 *     RETURN
 *     MAXSTACK = 1
 *     MAXLOCALS = 1
 *
 *   // access flags 0x9
 *   public static main ([Ljava/lang/String;)V
 *     GETSTATIC java/lang/System out Ljava/io/PrintStream;
 *     LDC &quot;hello&quot;
 *     INVOKEVIRTUAL java/io/PrintStream println (Ljava/lang/String;)V
 *     RETURN
 *     MAXSTACK = 2
 *     MAXLOCALS = 1
 * }
 * </pre>
 * 
 * </blockquote> where <tt>Hello</tt> is defined by: <p> <blockquote>
 * 
 * <pre>
 * public class Hello {
 *
 *     public static void main(String[] args) {
 *         System.out.println(&quot;hello&quot;);
 *     }
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public final class TraceClassVisitor extends ClassVisitor {

    /**
     * The print writer to be used to print the class. May be null.
     */
    protected final PrintWriter pw;

    /**
     * The visitor that actually converts visit events into text.
     */
    private final TraceVisitor tv;

    /**
     * Prints a disassembled view of the given class to the standard output. <p>
     * Usage: TraceClassVisitor [-debug] &lt;binary class name or class
     * file name &gt;
     * 
     * @param args the command line arguments.
     * 
     * @throws Exception if the class cannot be found, or if an IO exception
     *         occurs.
     */
    public static void main(final String[] args) throws Exception {
        int i = 0;
        int flags = ClassReader.SKIP_DEBUG;

        boolean ok = true;
        if (args.length < 1 || args.length > 2) {
            ok = false;
        }
        if (ok && "-debug".equals(args[0])) {
            i = 1;
            flags = 0;
            if (args.length != 2) {
                ok = false;
            }
        }
        if (!ok) {
            System.err.println("Prints a disassembled view of the given class.");
            System.err.println("Usage: TraceClassVisitor [-debug] "
                    + "<fully qualified class name or class file name>");
            return;
        }
        ClassReader cr;
        if (args[i].endsWith(".class") || args[i].indexOf('\\') > -1
                || args[i].indexOf('/') > -1)
        {
            cr = new ClassReader(new FileInputStream(args[i]));
        } else {
            cr = new ClassReader(args[i]);
        }
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)),
                AbstractVisitor.getDefaultAttributes(),
                flags);
    }

    /**
     * Constructs a new {@link TraceClassVisitor}.
     * 
     * @param pw the print writer to be used to print the class.
     */
    public TraceClassVisitor(final PrintWriter pw) {
        this(null, pw);
    }

    /**
     * Constructs a new {@link TraceClassVisitor}.
     * 
     * @param cv the {@link ClassVisitor} to which this visitor delegates calls.
     *        May be <tt>null</tt>.
     * @param pw the print writer to be used to print the class.
     */
    public TraceClassVisitor(final ClassVisitor cv, final PrintWriter pw) {
        this(cv, new TraceVisitor(), pw);
    }

    /**
     * Constructs a new {@link TraceClassVisitor}.
     * 
     * @param cv the {@link ClassVisitor} to which this visitor delegates calls.
     *        May be <tt>null</tt>.
     * @param tv the visitor that actually converts visit events into text.
     * @param pw the print writer to be used to print the class. May be null if
     *        you simply want to use the result via
     *        {@link AbstractVisitor#getText()}, instead of printing it.
     */
    public TraceClassVisitor(
        final ClassVisitor cv,
        final TraceVisitor tv,
        final PrintWriter pw)
    {
        super(Opcodes.ASM4, cv);
        this.pw = pw;
        this.tv = tv;
    }

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces)
    {
        tv.visit(version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(final String file, final String debug) {
        tv.visitSource(file, debug);
        super.visitSource(file, debug);
    }

    @Override
    public void visitOuterClass(
        final String owner,
        final String name,
        final String desc)
    {
        tv.visitOuterClass(owner, name, desc);
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
        TraceVisitor tv = this.tv.visitClassAnnotation(desc, visible);
        AnnotationVisitor av = cv == null ? null : cv.visitAnnotation(desc,
                visible);
        return new TraceAnnotationVisitor(av, tv);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        tv.visitClassAttribute(attr);
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access)
    {
        tv.visitInnerClass(name, outerName, innerName, access);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
        TraceVisitor tv = this.tv.visitField(access,
                name,
                desc,
                signature,
                value);
        FieldVisitor fv = cv == null ? null : cv.visitField(access,
                name,
                desc,
                signature,
                value);
        return new TraceFieldVisitor(fv, tv);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        TraceVisitor tv = this.tv.visitMethod(access,
                name,
                desc,
                signature,
                exceptions);
        MethodVisitor mv = cv == null ? null : cv.visitMethod(access,
                name,
                desc,
                signature,
                exceptions);
        return new TraceMethodVisitor(mv, tv);
    }

    @Override
    public void visitEnd() {
        tv.visitClassEnd();
        if (pw != null) {
            tv.print(pw);
            pw.flush();
        }
        super.visitEnd();
    }
}
