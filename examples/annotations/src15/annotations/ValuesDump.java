package annotations;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.RuntimeVisibleAnnotations;


public class ValuesDump implements Constants {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(false);
CodeVisitor cv;

cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/Values", "java/lang/Object", null, "Values.java");

{
cv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
cv.visitVarInsn(ALOAD, 0);
cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
cv.visitInsn(RETURN);
cv.visitMaxs(1, 1);
}
{
// CLASS ATRIBUTE
RuntimeVisibleAnnotations attr = new RuntimeVisibleAnnotations();
{
Annotation attrann0 = new Annotation("Lannotations/ValuesAnnotation;");
attrann0.add( "byteValue", new Integer((int)1));
attrann0.add( "charValue", new Integer((int)65));
attrann0.add( "booleanValue", new Integer((int)1));
attrann0.add( "intValue", new Integer((int)1));
attrann0.add( "shortValue", new Integer((int)1));
attrann0.add( "longValue", new Long((long)1));
attrann0.add( "floatValue", new Float((float)1.0));
attrann0.add( "doubleValue", new Double((double)1.0));
attrann0.add( "stringValue", "A");
attrann0.add( "enumValue", new Annotation.EnumConstValue("Lannotations/ValuesEnum;", "ONE"));
Annotation attrann0val10 = new Annotation("Lannotations/ValueAttrAnnotation;");
attrann0val10.add( "value", "annotation");
attrann0.add( "annotationValue", attrann0val10);
attrann0.add( "classValue", Type.getType("Lannotations/Values;"));
Object[] attrann0val12 = new Object[2];
attrann0val12[0] = new Integer((int)1);
attrann0val12[1] = new Integer((int)2);
attrann0.add( "byteArrayValue", attrann0val12);
Object[] attrann0val13 = new Object[3];
attrann0val13[0] = new Integer((int)99);
attrann0val13[1] = new Integer((int)98);
attrann0val13[2] = new Integer((int)3);
attrann0.add( "charArrayValue", attrann0val13);
Object[] attrann0val14 = new Object[2];
attrann0val14[0] = new Integer((int)1);
attrann0val14[1] = new Integer((int)0);
attrann0.add( "booleanArrayValue", attrann0val14);
Object[] attrann0val15 = new Object[2];
attrann0val15[0] = new Integer((int)1);
attrann0val15[1] = new Integer((int)2);
attrann0.add( "intArrayValue", attrann0val15);
Object[] attrann0val16 = new Object[2];
attrann0val16[0] = new Integer((int)1);
attrann0val16[1] = new Integer((int)2);
attrann0.add( "shortArrayValue", attrann0val16);
Object[] attrann0val17 = new Object[2];
attrann0val17[0] = new Long((long)1);
attrann0val17[1] = new Long((long)2);
attrann0.add( "longArrayValue", attrann0val17);
Object[] attrann0val18 = new Object[2];
attrann0val18[0] = new Float((float)1.0);
attrann0val18[1] = new Float((float)2.0);
attrann0.add( "floatArrayValue", attrann0val18);
Object[] attrann0val19 = new Object[2];
attrann0val19[0] = new Double((double)1.0);
attrann0val19[1] = new Double((double)2.0);
attrann0.add( "doubleArrayValue", attrann0val19);
Object[] attrann0val20 = new Object[2];
attrann0val20[0] = "aa";
attrann0val20[1] = "bb";
attrann0.add( "stringArrayValue", attrann0val20);
Object[] attrann0val21 = new Object[2];
attrann0val21[0] = new Annotation.EnumConstValue("Lannotations/ValuesEnum;", "ONE");
attrann0val21[1] = new Annotation.EnumConstValue("Lannotations/ValuesEnum;", "TWO");
attrann0.add( "enumArrayValue", attrann0val21);
Object[] attrann0val22 = new Object[2];
Annotation attrann0val22Arr0 = new Annotation("Lannotations/ValueAttrAnnotation;");
attrann0val22Arr0.add( "value", "annotation1");
attrann0val22[0] = attrann0val22Arr0;
Annotation attrann0val22Arr1 = new Annotation("Lannotations/ValueAttrAnnotation;");
attrann0val22Arr1.add( "value", "annotation2");
attrann0val22[1] = attrann0val22Arr1;
attrann0.add( "annotationArrayValue", attrann0val22);
Object[] attrann0val23 = new Object[2];
attrann0val23[0] = Type.getType("Lannotations/Values;");
attrann0val23[1] = Type.getType("Lannotations/Values;");
attrann0.add( "classArrayValue", attrann0val23);
attr.annotations.add( attrann0);
}
cw.visitAttribute(attr);
}
cw.visitEnd();

return cw.toByteArray();
}
}
