
package annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation declaration with different values
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ValuesAnnotation {
  byte    byteValue();
  char    charValue();
  boolean booleanValue();
  int     intValue();
  short   shortValue();
  long    longValue();
  float   floatValue();
  double  doubleValue();
  String  stringValue();

  ValuesEnum enumValue();
  ValueAttrAnnotation annotationValue();
  Class classValue();

  byte[]    byteArrayValue();
  char[]    charArrayValue();
  boolean[] booleanArrayValue();
  int[]     intArrayValue();
  short[]   shortArrayValue();
  long[]    longArrayValue();
  float[]   floatArrayValue();
  double[]  doubleArrayValue();
  String[]  stringArrayValue();

  ValuesEnum[] enumArrayValue();
  ValueAttrAnnotation[] annotationArrayValue();
  Class[] classArrayValue();

}

