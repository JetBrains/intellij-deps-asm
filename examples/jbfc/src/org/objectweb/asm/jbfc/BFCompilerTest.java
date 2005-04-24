
package org.objectweb.asm.jbfc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.objectweb.asm.ClassWriter;


public class BFCompilerTest extends TestCase {
  private BFCompiler bc;
  private ClassWriter cw;

  
  protected void setUp() throws Exception {
    super.setUp();
    bc = new BFCompiler();
    cw = new ClassWriter( true);
  }
  
  
  public void testCompileHelloWorld() throws Throwable {
    assertEquals( "Hello World!\n", execute( "Hello", 
        ">+++++++++[<++++++++>-]<.>+++++++[<++++>-]<+.+++++++..+++.[-]>++++++++[<++++>-]" + 
        "<.#>+++++++++++[<+++++>-]<.>++++++++[<+++>-]<.+++.------.--------.[-]>++++++++[" + 
        "<++++>-]<+.[-]++++++++++.", ""));
  }

  public void testCompileEcho() throws Throwable {
    assertEquals( "AAA", execute( "Echo", ",+[-.,+]", "AAA"));
  }

  public void testCompileYaPi() throws Throwable {
    assertEquals( "3.1415926\n", execute( "YaPi",
        ">+++++[<+++++++++>-]>>>>>>\r\n" + 
        "\r\n" + 
        "+++++ +++ (7 digits)\r\n" + 
        "\r\n" + 
        "[<<+>++++++++++>-]<<+>>+++<[->>+<-[>>>]>[[<+>-]>+>>]<<<<<]>[-]>[-]>[<+>-]<[>+<[-\r\n" + 
        ">>>>>>>+<<<<<<<]>[->+>>>>>>+<<<<<<<]>>>>++>>-]>[-]<<<[<<<<<<<]<[->>>>>[>>>>>>>]<\r\n" + 
        "<<<<<<[>>>>[-]>>>>>>>[-<<<<<<<+>>>>>>>]<<<<<<<<[<<++++++++++>>-]>[<<<<[>+>>+<<<-\r\n" + 
        "]>>>[<<<+>>>-]>-]<<<<[>>++>+<<<-]>>->[<<<+>>>-]>[-]<<<[->>+<-[>>>]>[[<+>-]>+>>]<\r\n" + 
        "<<<<]>[-]<<<<<<<<<]>+>>>>>>->>>>[<<<<<<<<+>>>>>>>>-]<<<<<<<[-]++++++++++<[->>+<-\r\n" + 
        "[>>>]>[[<+>-]>+>>]<<<<<]>[-]>[>>>>>+<<<<<-]>[<+>>+<-]>[<+>-]<<<+<+>>[-[-[-[-[-[-\r\n" + 
        "[-[-[-<->[-<+<->>[<<+>>[-]]]]]]]]]]]]<[+++++[<<<<++++++++>>>>>++++++++<-]>+<<<<-\r\n" + 
        ">>[>+>-<<<<<+++++++++>>>-]<<<<[>>>>>>+<<<<<<-]<[>>>>>>>.<<<<<<<<[+.[-]]>>]>[<]<+\r\n" + 
        ">>>[<.>-]<[-]>>>>>[-]<[>>[<<<<<<<+>>>>>>>-]<<-]]>>[-]>+<<<<[-]<]++++++++++.", ""));
  }

  public void testCompileTest1() throws Throwable {
    assertEquals( "H\n", execute( "Test1",
        "[]++++++++++[>++++++++++++++++++>+++++++>+<<<-]A;?@![#>>+<<]>[>++<[-]]>.>.", ""));
  }

  
  private String execute( String name, String code, String input) throws Throwable {
    bc.compile( new StringReader( code), name, name, cw);

    /*
    ClassReader cr = new ClassReader( cw.toByteArray());
    cr.accept( new TraceClassVisitor( null, new PrintWriter( System.err)), true);
    */
    
    /*
    File tmp = File.createTempFile( name, ".class");
    System.err.println( tmp.getAbsolutePath());
    FileOutputStream fos = new FileOutputStream( tmp);
    fos.write( cw.toByteArray());
    fos.flush();
    fos.close();
    */
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    InputStream is = System.in;
    PrintStream os = System.out;
    System.setIn( new ByteArrayInputStream( input.getBytes()));
    System.setOut( new PrintStream( bos));

    try {
      TestClassLoader cl = new TestClassLoader( getClass().getClassLoader(), name, cw.toByteArray());
      Class c = cl.loadClass( name);
      Method m = c.getDeclaredMethod( "main", new Class[] { String[].class});
      m.invoke( null, new Object[] { new String[ 0]});
    
    } catch (InvocationTargetException ex) {
      throw ex.getCause();
      
    } finally {
      System.setIn( is);
      System.setOut( os);
    
    }
    return new String( bos.toByteArray(), "ASCII");
  }

  
  private static final class TestClassLoader extends ClassLoader {
    private final String className;
    private final ClassLoader cl;
    private final byte[] bytecode;

    public TestClassLoader(ClassLoader cl, String className, byte[] bytecode) {
      super();
      this.cl = cl;
      this.className = className;
      this.bytecode = bytecode;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
      if( className.equals(name)) {
        return super.defineClass( className, bytecode, 0, bytecode.length);            
      }
      return cl.loadClass( name);
    }
    
  }
  
}

