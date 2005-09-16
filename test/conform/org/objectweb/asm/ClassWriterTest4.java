/***
 * ASM tests
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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;

import org.objectweb.asm.util.TraceClassVisitor;

import junit.framework.TestSuite;

public class ClassWriterTest4 extends AbstractTest {

    public static void premain(
        final String agentArgs,
        final Instrumentation inst)
    {
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(
                final ClassLoader loader,
                final String className,
                final Class classBeingRedefined,
                final ProtectionDomain domain,
                final byte[] classFileBuffer)
                    throws IllegalClassFormatException
            {
                String n = className.replace('/', '.');
                if (agentArgs.length() == 0 || n.indexOf(agentArgs) != -1) {
                    System.out.println("transform " + n);
                    return transformClass(classFileBuffer);
                } else {
                    return null;
                }
            }
        });
    }

    private static byte[] transformClass(byte[] clazz) {
        ClassReader cr = new ClassReader(clazz);
        if (cr.readInt(4) != Opcodes.V1_6) {
            return null;
        }
        ClassWriter cw = new ClassWriter(false, true, true) {
            protected String getCommonSuperClass(
                final String type1,
                final String type2)
            {
                ClassInfo c, d;
                try {
                    c = new ClassInfo(type1);
                    d = new ClassInfo(type2);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getType().getInternalName();
                }
            }
        };
        ClassAdapter ca = new ClassAdapter(cw) {
            public MethodVisitor visitMethod(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final String[] exceptions)
            {
                return new MethodAdapter(cv.visitMethod(access,
                        name,
                        desc,
                        signature,
                        exceptions))
                {
                    private HashSet labels = new HashSet();

                    private boolean transformed = false;

                    public void visitLabel(final Label label) {
                        super.visitLabel(label);
                        labels.add(label);
                    }

                    public void visitJumpInsn(
                        final int opcode,
                        final Label label)
                    {
                        super.visitJumpInsn(opcode, label);
                        if (!transformed && !labels.contains(label)) {
                            transformed = true;
                            for (int i = 0; i < 33000; ++i) {
                                mv.visitInsn(Opcodes.POP);
                            }
                        }
                    }
                };
            }
        };
        cr.accept(ca, false);
        return cw.toByteArray();
    }

    public static TestSuite suite() throws Exception {
        return new ClassWriterTest4().getSuite();
    }

    public void test() throws Exception {
        try {
            Class.forName(n, true, getClass().getClassLoader());
        } catch (ClassFormatError cfe) {
            fail(cfe.getMessage());
        } catch (VerifyError ve) {
            // String s = n.replace('.', '/') + ".class";
            // InputStream is =
            // getClass().getClassLoader().getResourceAsStream(s);
            // ClassReader cr = new ClassReader(is);
            // byte[] b = transformClass(cr.b);
            // StringWriter sw1 = new StringWriter();
            // StringWriter sw2 = new StringWriter();
            // sw2.write(ve.toString() + "\n");
            // ClassVisitor cv1 = new TraceClassVisitor(new PrintWriter(sw1));
            // ClassVisitor cv2 = new TraceClassVisitor(new PrintWriter(sw2));
            // cr.accept(new ClassFilter(cv1), false);
            // new ClassReader(b).accept(new ClassFilter(cv2), false);
            // String s1 = sw1.toString();
            // String s2 = sw2.toString();
            // assertEquals("different data", s1, s2);
            fail(ve.toString());
        }
    }
}
