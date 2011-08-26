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
 * A {@link ClassVisitor} that prints the ASM code that generates the classes it
 * visits. This class visitor can be used to quickly write ASM code to generate
 * some given bytecode: <ul> <li>write the Java source code equivalent to the
 * bytecode you want to generate;</li> <li>compile it with <tt>javac</tt>;</li>
 * <li>make a {@link ASMifierClassVisitor} visit this compiled class (see the
 * {@link #main main} method);</li> <li>edit the generated source code, if
 * necessary.</li> </ul> The source code printed when visiting the
 * <tt>Hello</tt> class is the following: <p> <blockquote>
 * 
 * <pre>
 * import org.objectweb.asm.*;
 *
 * public class HelloDump implements Opcodes {
 *
 *     public static byte[] dump() throws Exception {
 *
 *         ClassWriter cw = new ClassWriter(0);
 *         FieldVisitor fv;
 *         MethodVisitor mv;
 *         AnnotationVisitor av0;
 *
 *         cw.visit(49,
 *                 ACC_PUBLIC + ACC_SUPER,
 *                 &quot;Hello&quot;,
 *                 null,
 *                 &quot;java/lang/Object&quot;,
 *                 null);
 *
 *         cw.visitSource(&quot;Hello.java&quot;, null);
 *
 *         {
 *             mv = cw.visitMethod(ACC_PUBLIC, &quot;&lt;init&gt;&quot;, &quot;()V&quot;, null, null);
 *             mv.visitVarInsn(ALOAD, 0);
 *             mv.visitMethodInsn(INVOKESPECIAL,
 *                     &quot;java/lang/Object&quot;,
 *                     &quot;&lt;init&gt;&quot;,
 *                     &quot;()V&quot;);
 *             mv.visitInsn(RETURN);
 *             mv.visitMaxs(1, 1);
 *             mv.visitEnd();
 *         }
 *         {
 *             mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
 *                     &quot;main&quot;,
 *                     &quot;([Ljava/lang/String;)V&quot;,
 *                     null,
 *                     null);
 *             mv.visitFieldInsn(GETSTATIC,
 *                     &quot;java/lang/System&quot;,
 *                     &quot;out&quot;,
 *                     &quot;Ljava/io/PrintStream;&quot;);
 *             mv.visitLdcInsn(&quot;hello&quot;);
 *             mv.visitMethodInsn(INVOKEVIRTUAL,
 *                     &quot;java/io/PrintStream&quot;,
 *                     &quot;println&quot;,
 *                     &quot;(Ljava/lang/String;)V&quot;);
 *             mv.visitInsn(RETURN);
 *             mv.visitMaxs(2, 1);
 *             mv.visitEnd();
 *         }
 *         cw.visitEnd();
 *
 *         return cw.toByteArray();
 *     }
 * }
 *
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
public final class ASMifierClassVisitor extends ClassVisitor {

    /**
     * The print writer to be used to print the class.
     */
    final PrintWriter pw;

    /**
     * The visitor that actually converts visit events into source code.
     */
    final ASMifierVisitor sv;
    
    /**
     * Prints the ASM source code to generate the given class to the standard
     * output. <p> Usage: ASMifierClassVisitor [-debug] &lt;binary
     * class name or class file name&gt;
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
            System.err.println("Prints the ASM code to generate the given class.");
            System.err.println("Usage: ASMifierClassVisitor [-debug] "
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
        cr.accept(new ASMifierClassVisitor(new PrintWriter(System.out)),
                AbstractVisitor.getDefaultAttributes(),
                flags);
    }

    /**
     * Constructs a new {@link ASMifierClassVisitor} object.
     * 
     * @param pw the print writer to be used to print the class.
     */
    public ASMifierClassVisitor(final PrintWriter pw) {
        this(new ASMifierVisitor(), pw);
    }

    /**
     * Constructs a new {@link ASMifierClassVisitor} object.
     * 
     * @param sv the visitor that actually converts visit events into source
     *        code.
     * @param pw the print writer to be used to print the class.
     */
    public ASMifierClassVisitor(final ASMifierVisitor sv, final PrintWriter pw) {
        super(Opcodes.ASM4);
        this.sv = sv;
        this.pw = pw;
    }

    // ------------------------------------------------------------------------
    // Implementation of the ClassVisitor interface
    // ------------------------------------------------------------------------

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces)
    {
        sv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(final String file, final String debug) {
        sv.visitSource(file, debug);
    }

    @Override
    public void visitOuterClass(
        final String owner,
        final String name,
        final String desc)
    {
        sv.visitOuterClass(owner, name, desc);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
        return sv.visitClassAnnotation(desc, visible);
    }
    
    @Override
    public void visitAttribute(Attribute attr) {
        sv.visitClassAttribute(attr);
    }

    @Override
    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access)
    {
        sv.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
        return sv.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        return sv.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        sv.visitClassEnd();
        sv.print(pw);
        pw.flush();
    }
}
