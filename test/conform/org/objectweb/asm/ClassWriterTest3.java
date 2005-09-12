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

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import junit.framework.TestSuite;

/**
 * ClassWriter tests.
 * 
 * @author Eric Bruneton
 */
public class ClassWriterTest3 extends AbstractTest {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(
                final ClassLoader loader,
                final String className,
                final Class classBeingRedefined,
                final ProtectionDomain domain,
                final byte[] classFileBuffer)
                    throws IllegalClassFormatException
            {
                ClassReader cr = new ClassReader(classFileBuffer);
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
                cr.accept(cw, false);
                return cw.toByteArray();
            }
        });
    }

    public static TestSuite suite() throws Exception {
        return new ClassWriterTest3().getSuite();
    }

    public void test() throws Exception {
        try {
            Class.forName(n, true, getClass().getClassLoader());
        } catch (ClassFormatError cfe) {
            fail(cfe.getMessage());
        } catch (VerifyError cfe) {
            // StringWriter sw = new StringWriter();
            // ClassVisitor cv = new TraceClassVisitor(new PrintWriter(sw));
            // new ClassReader(b).accept(new ClassFilter(cv), false);
            // assertEquals(sw.toString(), cfe.getMessage());
            // fail(cfe.getMessage() + "\n" + sw);
            // assertEquals(cr, new ClassReader(b));
            fail(cfe.toString());
        }
    }
}

/**
 * @author Eugene Kuleshov
 */
class ClassInfo {

    private Type type;

    int access;

    String superClass;

    String[] interfaces;

    public ClassInfo(String type) {
        this.type = Type.getType("L" + type + ";");
        String s = type.replace('.', '/') + ".class";
        InputStream is = getClass().getClassLoader().getResourceAsStream(s);
        ClassReader cr;
        try {
            cr = new ClassReader(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cr.accept(new ClassVisitor() {

            public void visit(
                int version,
                int access,
                String name,
                String signature,
                String superName,
                String[] interfaces)
            {
                ClassInfo.this.access = access;
                ClassInfo.this.superClass = superName;
                ClassInfo.this.interfaces = interfaces;
            }

            public void visitSource(String source, String debug) {
            }

            public void visitOuterClass(String owner, String name, String desc)
            {
            }

            public AnnotationVisitor visitAnnotation(
                String desc,
                boolean visible)
            {
                return null;
            }

            public void visitAttribute(Attribute attr) {
            }

            public void visitInnerClass(
                String name,
                String outerName,
                String innerName,
                int access)
            {
            }

            public FieldVisitor visitField(
                int access,
                String name,
                String desc,
                String signature,
                Object value)
            {
                return null;
            }

            public MethodVisitor visitMethod(
                int access,
                String name,
                String desc,
                String signature,
                String[] exceptions)
            {
                return null;
            }

            public void visitEnd() {
            }
        }, true);
    }

    String getName() {
        return type.getInternalName();
    }

    Type getType() {
        return type;
    }

    int getModifiers() {
        return access;
    }

    ClassInfo getSuperclass() {
        if (superClass == null) {
            return null;
        }
        return new ClassInfo(superClass);
    }

    ClassInfo[] getInterfaces() {
        if (interfaces == null) {
            return new ClassInfo[0];
        }
        ClassInfo[] result = new ClassInfo[interfaces.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new ClassInfo(interfaces[i]);
        }
        return result;
    }

    boolean isInterface() {
        return (getModifiers() & Opcodes.ACC_INTERFACE) > 0;
    }

    private boolean implementsInterface(ClassInfo that) {
        for (ClassInfo c = this; c != null; c = c.getSuperclass()) {
            ClassInfo[] tis = c.getInterfaces();
            for (int i = 0; i < tis.length; ++i) {
                ClassInfo ti = tis[i];
                if (ti == that || ti.implementsInterface(that))
                    return true;
            }
        }
        return false;
    }

    private boolean isSubclassOf(ClassInfo that) {
        for (ClassInfo c = this; c != null; c = c.getSuperclass()) {
            if (c.getSuperclass() == that)
                return true;
        }
        return false;
    }

    public boolean isAssignableFrom(ClassInfo that) {
        if (this == that)
            return true;

        if (that.isSubclassOf(this))
            return true;

        if (that.implementsInterface(this))
            return true;

        if (that.isInterface()
                && this.getType().getDescriptor().equals("Ljava/lang/Object;"))
            return true;

        return false;
    }
}
