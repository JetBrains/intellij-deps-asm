/*
 * Copyright area
 */

package org.objectweb.asm.commons;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import junit.framework.TestSuite;

import org.objectweb.asm.AbstractTest;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

public class LocalVariablesSorterTest2 extends AbstractTest {

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
                return transformClass(classFileBuffer);
            }
        });
    }

    private static byte[] transformClass(byte[] clazz) {
        ClassReader cr = new ClassReader(clazz);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassAdapter(cw) {

            public void visit(
                int version,
                int access,
                String name,
                String signature,
                String superName,
                String[] interfaces)
            {
                super.visit(Opcodes.V1_6,
                        access,
                        name,
                        signature,
                        superName,
                        interfaces);
            }

            public MethodVisitor visitMethod(
                int access,
                String name,
                String desc,
                String signature,
                String[] exceptions)
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
        return cw.toByteArray();
    }

    public static TestSuite suite() throws Exception {
        return new LocalVariablesSorterTest2().getSuite();
    }

    public void test() throws Exception {
        try {
            Class.forName(n, true, getClass().getClassLoader());
        } catch (NoClassDefFoundError ncdfe) {
            // ignored
        } catch (UnsatisfiedLinkError ule) {
            // ignored
        } catch (ClassFormatError cfe) {
            fail(cfe.getMessage());
        } catch (VerifyError ve) {
            String s = n.replace('.', '/') + ".class";
            InputStream is = getClass().getClassLoader().getResourceAsStream(s);
            ClassReader cr = new ClassReader(is);
            byte[] b = transformClass(cr.b);
            StringWriter sw1 = new StringWriter();
            StringWriter sw2 = new StringWriter();
            sw2.write(ve.toString() + "\n");
            ClassVisitor cv1 = new TraceClassVisitor(new PrintWriter(sw1));
            ClassVisitor cv2 = new TraceClassVisitor(new PrintWriter(sw2));
            cr.accept(cv1, 0);
            new ClassReader(b).accept(cv2, 0);
            String s1 = sw1.toString();
            String s2 = sw2.toString();
            assertEquals("different data", s1, s2);
        }
    }

}
