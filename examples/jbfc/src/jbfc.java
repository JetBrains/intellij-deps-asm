
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.jbfc.BFCompiler;


public class jbfc {

  public static void main( String[] args) throws IOException {
    if( args.length<2) {
      System.out.println( "Usage: jbfc [-v] <bf program file> <java class name>");
      return;
    }

    boolean verbose = false;
    String fileName = null;
    String className = null;
    for( int i = 0; i<args.length; i++) {
      if( "-v".equals( args[ i])) {
        verbose = true;
      } else {
        fileName = args[ i];
        className = args[ i+1];
        break;
      }
    }
    
    FileReader r = new FileReader( fileName);

    ClassWriter cw = new ClassWriter( true);
    
    BFCompiler c = new BFCompiler();
    c.compile( r, className, fileName, verbose ? new TraceClassVisitor( cw, new PrintWriter( System.out)) : cw);

    r.close();
    
    FileOutputStream os = new FileOutputStream( className + ".class");
    os.write( cw.toByteArray());
    os.flush();
    os.close();
  }

}

