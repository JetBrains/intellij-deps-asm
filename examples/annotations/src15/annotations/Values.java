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

  byteArrayValue = { 1, -1},
  charArrayValue = { 'c', 'b', (char)-1},
  booleanArrayValue = {true, false},
  intArrayValue = { 1, -1},
  shortArrayValue = { (short)1, (short)-1},
  longArrayValue = { 1L, -1L},
  floatArrayValue = { 1.0f, -1.0f},
  doubleArrayValue = { 1.0d, -1.0d},
  stringArrayValue = { "aa", "bb"},

  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},
  annotationArrayValue = {@ValueAttrAnnotation( "annotation1"), @ValueAttrAnnotation( "annotation2")},
  classArrayValue = {Values.class, Values.class}
)
public class Values {
}

