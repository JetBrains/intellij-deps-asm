
package annotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;

import junit.framework.TestCase;


public class ValuesTest extends TestCase {
  
  private ValuesAnnotation a;

  protected void setUp() throws Exception {
    Annotation[] annotations = getValuesClass().getAnnotations();
    a = ( ValuesAnnotation) annotations[ 0];
  }

  private Class getValuesClass() throws Exception {
    try {
      TestClassLoader cl = new TestClassLoader( "annotations.Values", getClass().getClassLoader());
      return cl.loadClass( "annotations.Values");
      
    } catch( Exception e) {
      e.printStackTrace();
      throw e;
    }
    // return Values.class;
  }

  public void testByteValue() {
    assertEquals( 1, a.byteValue());
  }

  public void testByteArrayValue() {
    byte[] bs = a.byteArrayValue();
    assertEquals( 1, bs[0]);
    assertEquals( 2, bs[1]);
  }

  public void testAnnotationValue() {
    ValueAttrAnnotation ann = a.annotationValue();
    assertEquals( "annotation", ann.value());
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
          System.err.println("LOADED "+name);
          return super.defineClass( className, bytecode, 0, bytecode.length);            
        
        } catch( Exception ex) {
          ex.printStackTrace();
          throw new ClassNotFoundException( "Load error: "+ex.toString(), ex);
        
        }
        
      }
      
      return loader.loadClass( name);
    }

    
    public static byte[] getCode( InputStream is) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buff = new byte[ 1024];
      int n = -1;
      while(( n = is.read( buff))>-1) bos.write( buff, 0, n);
      return bos.toByteArray();
    }
    
  }
  
  
}

