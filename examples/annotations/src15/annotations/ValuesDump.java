package annotations;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;

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
attrann0.add( "byteValue", new Byte((byte)1));
attrann0.add( "charValue", new Character((char)65));
attrann0.add( "booleanValue", new Boolean(true));
attrann0.add( "intValue", new Integer(1));
attrann0.add( "shortValue", new Short((short)1));
attrann0.add( "longValue", new Long(1L));
attrann0.add( "floatValue", new Float(1.0f));
attrann0.add( "doubleValue", new Double(1.0d));
attrann0.add( "stringValue", "A");
attrann0.add( "enumValue", new Annotation.EnumConstValue("Lannotations/ValuesEnum;", "ONE"));
Annotation attrann0val10 = new Annotation("Lannotations/ValueAttrAnnotation;");
attrann0val10.add( "value", "annotation");
attrann0.add( "annotationValue", attrann0val10);
attrann0.add( "classValue", Type.getType("Lannotations/Values;"));
attrann0.add( "byteArrayValue", new byte[] {1, 2});
attrann0.add( "charArrayValue", new char[] {(char)99, (char)98, (char)3});
attrann0.add( "booleanArrayValue", new boolean[] {true, false});
attrann0.add( "intArrayValue", new int[] {1, 2});
attrann0.add( "shortArrayValue", new short[] {(short)1, (short)2});
attrann0.add( "longArrayValue", new long[] {1L, 2L});
attrann0.add( "floatArrayValue", new float[] {1.0f, 2.0f});
attrann0.add( "doubleArrayValue", new double[] {1.0d, 2.0d});
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
