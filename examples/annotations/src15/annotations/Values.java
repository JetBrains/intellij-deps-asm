package annotations;


@ValuesAnnotation(
  byteValue = 1,
  charValue = 'A',
  booleanValue = true,
  intValue = 1,
  shortValue = 1,
  longValue = 1L,
  floatValue = 1.0f,
  doubleValue = 1.0d,
  stringValue = "A",

  enumValue = ValuesEnum.ONE,
  annotationValue = @ValueAttrAnnotation( "annotation"),
  classValue = Values.class,

  byteArrayValue = { 1, 2},
  charArrayValue = { 'c', 'b', (char)3},
  booleanArrayValue = {true, false},
  intArrayValue = { 1, 2},
  shortArrayValue = { (short)1, (short)2},
  longArrayValue = { 1L, 2L},
  floatArrayValue = { 1.0f, 2.0f},
  doubleArrayValue = { 1.0d, 2.0d},
  stringArrayValue = { "aa", "bb"},

  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},
  annotationArrayValue = {@ValueAttrAnnotation( "annotation1"), @ValueAttrAnnotation( "annotation2")},
  classArrayValue = {Values.class, Values.class}
)
public class Values {
}

