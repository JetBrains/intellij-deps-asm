
package annotations;

import java.lang.annotation.Annotation;

import junit.framework.TestCase;


public class ValuesTest extends TestCase {
  
  private ValuesAnnotation a;

  protected void setUp() throws Exception {
    TestClassLoader cl = new TestClassLoader( "annotations.Values", getClass().getClassLoader());
    Class c = cl.loadClass( "annotations.Values");
    Annotation[] annotations = c.getAnnotations();
    a = ( ValuesAnnotation) annotations[ 0];
  }


  public void testByteValue() {
    assertEquals( 1, a.byteValue());
  }

  public void testCharValue() {
    assertEquals( 'A', a.charValue());
  }

  public void testBooleanValue() {
    assertEquals( true, a.booleanValue());
  }

  public void testIntValue() {
    assertEquals( 1, a.intValue());
  }

  public void testShortValue() {
    assertEquals( 1, a.shortValue());
  }

  public void testLongValue() {
    assertEquals( 1L, a.longValue());
  }

  public void testFloatValue() {
    assertEquals( 1.0f, a.floatValue(), 0.1f);
  }

  public void testDoubleValue() {
    assertEquals( 1.0d, a.doubleValue(), 0.1d);
  }

  public void testStringValue() {
    assertEquals( "A", a.stringValue());
  }

  public void testAnnotationValue() {
    ValueAttrAnnotation ann = a.annotationValue();
    assertEquals( "annotation", ann.value());
  }

  public void testEnumValue() {
    ValuesEnum en = a.enumValue();
    assertEquals( ValuesEnum.ONE, en);
  }
  
  public void testClassValue() {
    Class c = a.classValue();
    assertEquals( Values.class.getName(), c.getName());
  }
  
  
  public void testByteArrayValue() {
    byte[] bs = a.byteArrayValue();
    assertEquals( 1, bs[0]);
    assertEquals( -1, bs[1]);
  }

  public void testCharArrayValue() {
    char[] bs = a.charArrayValue();
    assertEquals( 'c', bs[0]);
    assertEquals( 'b', bs[1]);
    assertEquals((char) -1, bs[2]);
  }

  public void testBooleanArrayValue() {
    boolean[] bs = a.booleanArrayValue();
    assertEquals( true, bs[0]);
    assertEquals( false, bs[1]);
  }

  public void testIntArrayValue() {
    int[] bs = a.intArrayValue();
    assertEquals( 1, bs[0]);
    assertEquals( -1, bs[1]);
  }

  public void testShortArrayValue() {
    short[] bs = a.shortArrayValue();
    assertEquals( 1, bs[0]);
    assertEquals( -1, bs[1]);
  }

  public void testLongArrayValue() {
    long[] bs = a.longArrayValue();
    assertEquals( 1L, bs[0]);
    assertEquals( -1L, bs[1]);
  }

  public void testFloatArrayValue() {
    float[] bs = a.floatArrayValue();
    assertEquals( 1.0f, bs[0], 0.1f);
    assertEquals( -1.0f, bs[1], 0.1f);
  }

  public void testDoubleArrayValue() {
    double[] bs = a.doubleArrayValue();
    assertEquals( 1.0d, bs[0], 0.1d);
    assertEquals( -1.0d, bs[1], 0.1d);
  }

  public void testStringArrayValue() {
    String[] s = a.stringArrayValue();
    assertEquals( "aa", s[0]);
    assertEquals( "bb", s[1]);
  }

  public void testAnnotationArrayValue() {
    ValueAttrAnnotation[] ann = a.annotationArrayValue();
    assertEquals( "annotation1", ann[0].value());
    assertEquals( "annotation2", ann[1].value());
  }

  public void testEnumArrayValue() {
    ValuesEnum[] en = a.enumArrayValue();
    assertEquals( ValuesEnum.ONE, en[0]);
    assertEquals( ValuesEnum.TWO, en[1]);
  }
  
  public void testClassArrayValue() {
    Class[] c = a.classArrayValue();
    assertEquals( Values.class.getName(), c[0].getName());
    assertEquals( Values.class.getName(), c[1].getName());
  }
  
  
  private static final class TestClassLoader extends ClassLoader {
    private final String className;
    private final ClassLoader loader;

    public TestClassLoader(String className, ClassLoader loader) {
      super();
      this.className = className;
      this.loader = loader;
    }

    public Class loadClass( String name) throws ClassNotFoundException {
      if( className.equals( name)) {
        try {
          byte[] bytecode = ValuesDump.dump();
          return super.defineClass( className, bytecode, 0, bytecode.length);            
        
        } catch( Exception ex) {
          ex.printStackTrace();
          throw new ClassNotFoundException( "Load error: "+ex.toString(), ex);
        
        }        
      }
      
      return loader.loadClass( name);
    }

  }
  
}

