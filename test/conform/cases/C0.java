package cases;

import java.lang.annotation.Documented;

public @interface C0 {

    byte byteValue() default (byte) 1;

    char charValue() default (char) 1;

    boolean booleanValue() default true;

    int intValue() default 1;

    short shortValue() default (short) 1;

    long longValue() default 1L;

    float floatValue() default 1F;

    double doubleValue() default 1D;

    String stringValue() default "1";

    Class classValue() default C2.class;

    C1 enumValue() default C1.V1;

    Documented annotationValue() default @Documented;

    byte[] byteArrayValue() default { (byte) 0, (byte) 1 };

    char[] charArrayValue() default { '0', '1' };

    boolean[] booleanArrayValue() default { false, true };

    int[] intArrayValue() default { 0, 1 };

    short[] shortArrayValue() default { (short) 0, (short) 1 };

    long[] longArrayValue() default { 0L, 1L };

    float[] floatArrayValue() default { 0F, 1F };

    double[] doubleArrayValue() default { 0D, 1D };

    String[] stringArrayValue() default { "0", "1" };

    Class[] classArrayValue() default { C2.class, C2.class };

    C1[] enumArrayValue() default { C1.V0, C1.V1 };

    Documented[] annotationArrayValue() default { @Documented, @Documented };

}
