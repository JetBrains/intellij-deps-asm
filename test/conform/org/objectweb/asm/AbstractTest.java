/***
 * ASM tests
 * Copyright (c) 2000-2011 INRIA, France Telecom
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Super class for test suites based on a jar file.
 * 
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public abstract class AbstractTest extends TestCase {

    protected String n;

    protected InputStream is;

    public AbstractTest() {
        super("test");
    }

    protected void init(final String n, final InputStream is) {
        this.n = n;
        this.is = is;
    }

    protected TestSuite getSuite() throws Exception {
        TestSuite suite = new TestSuite(getClass().getName());
        String files = System.getProperty("asm.test") + ",";
        String clazz = System.getProperty("asm.test.class");
        String partcount = System.getProperty("parts");
        String partid = System.getProperty("part");
        int parts = partcount == null ? 1 : Integer.parseInt(partcount);
        int part = partid == null ? 0 : Integer.parseInt(partid);
        int id = 0;
        while (files.indexOf(',') != -1) {
            String file = files.substring(0, files.indexOf(','));
            files = files.substring(files.indexOf(',') + 1);
            File f = new File(file);
            if (f.isDirectory()) {
                scanDirectory("", f, suite, clazz);
            } else {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    String n = e.getName();
                    String p = n.replace('/', '.');
                    if (n.endsWith(".class")
                            && (clazz == null || p.indexOf(clazz) != -1)) {
                        n = p.substring(0, p.length() - 6);
                        if (id % parts == part) {
                            InputStream is = zip.getInputStream(e);
                            AbstractTest t = getClass().newInstance();
                            t.init(n, is);
                            suite.addTest(t);
                        }
                        ++id;
                    }
                }
            }
        }
        return suite;
    }

    private void scanDirectory(final String path, final File f,
            final TestSuite suite, final String clazz) throws Exception {
        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; ++i) {
            String n = fs[i].getName();
            String qn = path.length() == 0 ? n : path + "." + n;
            if (fs[i].isDirectory()) {
                scanDirectory(qn, fs[i], suite, clazz);
            } else if (qn.endsWith(".class") && !qn.startsWith("invalid.")) {
                if (clazz == null || qn.startsWith("pkg.")
                        || qn.indexOf(clazz) != -1) {
                    qn = qn.substring(0, qn.length() - 6);
                    InputStream is = new FileInputStream(fs[i]);
                    AbstractTest t = getClass().newInstance();
                    t.init(qn, is);
                    suite.addTest(t);
                }
            }
        }
    }

    public abstract void test() throws Exception;

    public void assertEquals(final ClassReader cr1, final ClassReader cr2)
            throws Exception {
        assertEquals(cr1, cr2, null, null);
    }

    public void assertEquals(final ClassReader cr1, final ClassReader cr2,
            final ClassVisitor filter1, final ClassVisitor filter2)
            throws Exception {
        if (!Arrays.equals(cr1.b, cr2.b)) {
            ClassVisitor cv1 = new DumpClassVisitor();
            ClassVisitor cv2 = new DumpClassVisitor();
            if (filter1 != null) {
                filter1.cv = cv1;
            }
            if (filter2 != null) {
                filter2.cv = cv2;
            }
            cr1.accept(filter1 == null ? cv1 : filter1, 0);
            cr2.accept(filter2 == null ? cv2 : filter2, 0);
            assertEquals("different data", cv1.toString(), cv2.toString());
        }
    }

    @Override
    public String getName() {
        return super.getName() + ": " + n;
    }

    public static class VerifierTest extends TestCase {

        public VerifierTest() {
            super("testVerifier");
        }

        public void testVerifier() throws Exception {
            try {
                Class.forName("invalid.Invalid", true, getClass()
                        .getClassLoader());
                fail("The new JDK 7 verifier does not trigger!");
            } catch (VerifyError ve) {
                // This is expected since the class is invalid.
                ve.printStackTrace();
            }
        }
    }
    
    private static class DumpClassVisitor extends ClassVisitor {
     
        static class LabelId {
            int id;
            boolean used;
            @Override
            public int hashCode() {
                return id;
            }
            @Override
            public boolean equals(Object other) {
                return other instanceof LabelId && ((LabelId) other).id == id;
            }
            @Override
            public String toString() {
                return "<" + id + ">";
            }
        }

        static class VisitLabel {
            final LabelId id;
            VisitLabel(LabelId id) {
                this.id = id;
            }
            @Override
            public int hashCode() {
                return id.hashCode();
            }
            @Override
            public boolean equals(Object other) {
                return other instanceof VisitLabel && ((VisitLabel) other).id.equals(id);
            }
            @Override
            public String toString() {
                return "visitLabel " + id + "\n";
            }
        }

        final ArrayList<Object> tokens = new ArrayList<Object>();
        
        public DumpClassVisitor() {
            super(Opcodes.ASM5, null);
            // Make sure we don't forget to override a method of the super
            // class, because doing so could hide bugs in the tested classes.
            // This cyclic reference will cause stack overflow errors if a
            // method is not overridden.
            this.cv = this;
        }

        LabelId getLabelId(Label label, boolean use) {
            if (label.info == null) {
                label.info = new LabelId();
            }
            LabelId id = (LabelId) label.info; 
            if (use) {
                id.used = true;
            }
            return id;
        }

        void dump(Object... args) {
            tokens.add(args[0]);
            for (int i = 1; i < args.length; ++i) {
                tokens.add(' ');
                dump(args[i]);
            }
            tokens.add('\n');
        }
        
        void dump(Object arg) {
          if (arg instanceof Object[]) {
              Object[] objs = (Object[]) arg;
              tokens.add('[');
              for (int i = 0; i < objs.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(objs[i]);
              }
              tokens.add(']');
          } else if (arg instanceof int[]) {
              int[] values = (int[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof float[]) {
              float[] values = (float[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof long[]) {
              long[] values = (long[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof double[]) {
              double[] values = (double[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof short[]) {
              short[] values = (short[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof byte[]) {
              byte[] values = (byte[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof char[]) {
              char[] values = (char[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof boolean[]) {
              boolean[] values = (boolean[]) arg;
              tokens.add('[');
              for (int i = 0; i < values.length; ++i) {
                  if (i > 0) {
                      tokens.add(' ');
                  }
                  dump(values[i]);
              }
              tokens.add(']');
          } else if (arg instanceof Label) {
              tokens.add(getLabelId((Label) arg, /* use= */ true));
          } else if (arg instanceof Attribute) {
              tokens.add(((Attribute) arg).type + "Attribute");
          } else if (arg instanceof String) {
              tokens.add("\"" + arg + "\"");
          } else {
              tokens.add(arg);   
          }
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : tokens) {
                stringBuilder.append(o);
            }
            return stringBuilder.toString();
        }

        @Override
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            dump("visit", version, access, name, signature, superName, interfaces);
        }

        @Override
        public void visitSource(String source, String debug) {
            dump("visitSource", source, debug);
        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            dump("visitOuterClass", owner, name, desc);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            dump("visitAnnotation", desc, visible);
            return new DumpAnnotationVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                TypePath typePath, String desc, boolean visible) {
            dump("visitTypeAnnotation", typeRef, typePath, desc, visible);
            return new DumpAnnotationVisitor();
        }

        @Override
        public void visitAttribute(Attribute attr) {
            dump("visitAttribute", attr);
        }

        @Override
        public void visitInnerClass(String name, String outerName,
                String innerName, int access) {
            dump("visitInnerClass", name, outerName, innerName, access);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                String signature, Object value) {
            dump("visitField", access, name, desc, signature, value);
            return new DumpFieldVisitor();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            dump("visitMethod", access, name, desc, signature, exceptions);
            return new DumpMethodVisitor();
        }

        @Override
        public void visitEnd() {
            dump("visitEnd");
            // Removes the unused visitLabel tokens, and assign ids to the
            // remaining labels. Unused visitLabel calls can occur, due to the
            // way uninitialized frame types are handled (false positives may
            // occur, see the doc of ClassReader). These unused labels may
            // differ before / after a transformation, but this does not change
            // the method code itself.
            ListIterator<Object> iterator = tokens.listIterator();
            int nextId = 1;
            while (iterator.hasNext()) {
                Object token = iterator.next();
                LabelId id = null;
                if (token instanceof VisitLabel) {
                    if (!((VisitLabel) token).id.used) {
                        iterator.remove();
                    } else {
                        id = ((VisitLabel) token).id;
                    }
                } else if (token instanceof LabelId) {
                   id = (LabelId) token;
                }
                if (id != null && id.id == 0) {
                    id.id = nextId++;
                }
            }
        }
        
        private class DumpAnnotationVisitor extends AnnotationVisitor {
            
            public DumpAnnotationVisitor() {
                super(Opcodes.ASM5, null);
                // Make sure we don't forget to override a method of the super
                // class, because doing so could hide bugs in the tested classes.
                // This cyclic reference will cause stack overflow errors if a
                // method is not overridden.
                this.av = this;
            }

            @Override
            public void visit(String name, Object value) {
                dump("visit", name, value);
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
                dump("visitEnum", name, desc, value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                dump("visitAnnotation", name, desc);
                return new DumpAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                dump("visitArray", name);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitEnd() {
                dump("visitEnd");
            }
        }
        
        private class DumpFieldVisitor extends FieldVisitor {

            public DumpFieldVisitor() {
                super(Opcodes.ASM5, null);
                // Make sure we don't forget to override a method of the super
                // class, because doing so could hide bugs in the tested classes.
                // This cyclic reference will cause stack overflow errors if a
                // method is not overridden.
                this.fv = this;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc,
                    boolean visible) {
                dump("visitAnnotation", desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef,
                    TypePath typePath, String desc, boolean visible) {
                dump("visitTypeAnnotation", typeRef, typePath, desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitAttribute(Attribute attr) {
                dump("visitAttribute", attr);
            }

            @Override
            public void visitEnd() {
                dump("visitEnd");
            }
        }
        
        private class DumpMethodVisitor extends MethodVisitor {
            
            public DumpMethodVisitor() {
                super(Opcodes.ASM5, null);
                // Make sure we don't forget to override a method of the super
                // class, because doing so could hide bugs in the tested classes.
                // This cyclic reference will cause stack overflow errors if a
                // method is not overridden.
                this.mv = this;
            }

            @Override
            public void visitParameter(String name, int access) {
                dump("visitParameter", name, access);
            }

            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                dump("visitAnnotationDefault");
                return new DumpAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc,
                    boolean visible) {
                dump("visitAnnotation", desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef,
                    TypePath typePath, String desc, boolean visible) {
                dump("visitTypeAnnotation", typeRef, typePath, desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter,
                    String desc, boolean visible) {
                dump("visitParameterAnnotation", parameter, desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitAttribute(Attribute attr) {
                dump("visitAttribute", attr);
            }

            @Override
            public void visitCode() {
                dump("visitCode");
            }

            @Override
            public void visitFrame(int type, int nLocal, Object[] local,
                    int nStack, Object[] stack) {
                dump("visitFrame", type, nLocal, local, nStack, stack);
            }

            @Override
            public void visitInsn(int opcode) {
                dump("visitInsn", opcode);
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
                dump("visitIntInsn", opcode, operand);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                dump("visitVarInsn", opcode, var);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                dump("visitTypeInsn", opcode, type);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name,
                    String desc) {
                dump("visitFieldInsn", opcode, owner, name, desc);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name,
                    String desc, boolean itf) {
                dump("visitMethodInsn", opcode, owner, name, desc, itf);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc,
                    Handle bsm, Object... bsmArgs) {
                dump("visitInvokeDynamicInsn", name, desc, bsm, bsmArgs);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                dump("visitJumpInsn", opcode, label);
            }

            @Override
            public void visitLabel(Label label) {
                tokens.add(new VisitLabel(getLabelId(label, /* use = */ false)));
            }

            @Override
            public void visitLdcInsn(Object cst) {
                dump("visitLdcInsn", cst);
            }

            @Override
            public void visitIincInsn(int var, int increment) {
                dump("visitIincInsn", var, increment);
            }

            @Override
            public void visitTableSwitchInsn(int min, int max, Label dflt,
                    Label... labels) {
                dump("visitTableSwitchInsn", min, max, dflt, labels);
            }

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys,
                    Label[] labels) {
                dump("visitLookupSwitchInsn", dflt, keys, labels);
            }

            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                dump("visitMultiANewArrayInsn", desc, dims);
            }

            @Override
            public AnnotationVisitor visitInsnAnnotation(int typeRef,
                    TypePath typePath, String desc, boolean visible) {
                dump("visitInsnAnnotation", typeRef, typePath, desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitTryCatchBlock(Label start, Label end,
                    Label handler, String type) {
                dump("visitTryCatchBlock", start, end, handler, type);
            }

            @Override
            public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                    TypePath typePath, String desc, boolean visible) {
                dump("visitTryCatchAnnotation", typeRef, typePath, desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitLocalVariable(String name, String desc,
                    String signature, Label start, Label end, int index) {
                dump("visitLocalVariable", name, desc, signature, start, end, index);
            }

            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                    TypePath typePath, Label[] start, Label[] end, int[] index,
                    String desc, boolean visible) {
                dump("visitLocalVariableAnnotation", typeRef, typePath, start, end, index,
                        desc, visible);
                return new DumpAnnotationVisitor();
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                dump("visitLineNumber", line, start);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                dump("visitMaxs", maxStack, maxLocals);
            }

            @Override
            public void visitEnd() {
                dump("visitEnd");
            }
        }
    }
}
