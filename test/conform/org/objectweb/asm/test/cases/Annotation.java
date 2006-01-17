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
package org.objectweb.asm.test.cases;

import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates an annotation class with values of all types.
 * 
 * @author Eric Bruneton
 */
public class Annotation extends Generator {

    public void generate(final String dir) throws IOException {
        generate(dir, "pkg/Annotation.class", dump());
    }

    public byte[] dump() {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        AnnotationVisitor av0, av1, av2;

        cw.visit(V1_5,
                ACC_PUBLIC + ACC_ANNOTATION + ACC_ABSTRACT + ACC_INTERFACE,
                "pkg/Annotation",
                null,
                "java/lang/Object",
                new String[] { "java/lang/annotation/Annotation" });

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "byteValue",
                "()B",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Byte((byte) 1));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "charValue",
                "()C",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Character((char) 1));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "booleanValue",
                "()Z",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Boolean(true));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "intValue",
                "()I",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Integer(1));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "shortValue",
                "()S",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Short((short) 1));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "longValue",
                "()J",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Long(1L));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "floatValue",
                "()F",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Float("1.0"));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "doubleValue",
                "()D",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new Double("1.0"));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "stringValue",
                "()Ljava/lang/String;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, "1");
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "classValue",
                "()Ljava/lang/Class;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, Type.getType("Lpkg/Annotation;"));
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "enumValue",
                "()Lpkg/Enum;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visitEnum(null, "Lpkg/Enum;", "V1");
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "annotationValue",
                "()Ljava/lang/annotation/Documented;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av1 = av0.visitAnnotation(null, "Ljava/lang/annotation/Documented;");
        av1.visitEnd();
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "byteArrayValue",
                "()[B",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new byte[] { 0, 1 });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "charArrayValue",
                "()[C",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new char[] { '0', '1' });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "booleanArrayValue",
                "()[Z",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new boolean[] { false, true });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "intArrayValue",
                "()[I",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new int[] { 0, 1 });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "shortArrayValue",
                "()[S",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new short[] { (short) 0, (short) 1 });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "longArrayValue",
                "()[J",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new long[] { 0L, 1L });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "floatArrayValue",
                "()[F",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new float[] { 0.0f, 1.0f });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "doubleArrayValue",
                "()[D",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av0.visit(null, new double[] { 0.0d, 1.0d });
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "stringArrayValue",
                "()[Ljava/lang/String;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av1 = av0.visitArray(null);
        av1.visit(null, "0");
        av1.visit(null, "1");
        av1.visitEnd();
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "classArrayValue",
                "()[Ljava/lang/Class;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av1 = av0.visitArray(null);
        av1.visit(null, Type.getType("Lpkg/Annotation;"));
        av1.visit(null, Type.getType("Lpkg/Annotation;"));
        av1.visitEnd();
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "enumArrayValue",
                "()[Lpkg/Enum;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av1 = av0.visitArray(null);
        av1.visitEnum(null, "Lpkg/Enum;", "V0");
        av1.visitEnum(null, "Lpkg/Enum;", "V1");
        av1.visitEnd();
        av0.visitEnd();
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT,
                "annotationArrayValue",
                "()[Ljava/lang/annotation/Documented;",
                null,
                null);
        av0 = mv.visitAnnotationDefault();
        av1 = av0.visitArray(null);
        av2 = av1.visitAnnotation(null, "Ljava/lang/annotation/Documented;");
        av2.visitEnd();
        av2 = av1.visitAnnotation(null, "Ljava/lang/annotation/Documented;");
        av2.visitEnd();
        av1.visitEnd();
        av0.visitEnd();
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}
