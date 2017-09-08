/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.tools;

import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import aQute.bnd.header.Attrs;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.EmbeddedResource;
import aQute.bnd.osgi.Jar;
import aQute.bnd.service.AnalyzerPlugin;

public class ModuleInfoBndPlugin implements AnalyzerPlugin {
  private static final String MODULE_NAME = "Module-Name";
  private static final String MODULE_VERSION = "Module-Version";
  private static final String MODULE_REQUIRES = "Module-Requires";
  private static final String MODULE_EXPORTS = "Module-Exports";
  //private static final String MODULE_PROVIDES = "Module-Provides";
  //private static final String MODULE_USES = "Module-Uses";
  
  public boolean analyzeJar(Analyzer analyzer) throws Exception {
    String moduleName = analyzer.getProperty(MODULE_NAME, analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME));
    String moduleVersion =  analyzer.getProperty(MODULE_VERSION, analyzer.getProperty(Constants.BUNDLE_VERSION));
    String requireModules = analyzer.getProperty(MODULE_REQUIRES);
    String exportPackages = analyzer.getProperty(MODULE_EXPORTS, analyzer.getProperty(Constants.EXPORT_PACKAGE));
    
    //System.out.println(moduleName);
    //System.out.println(moduleVersion);
    //System.out.println(requireModules);
    //System.out.println(exportPackages);
    
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
    
    ModuleVisitor mv = writer.visitModule(moduleName, Opcodes.ACC_OPEN, moduleVersion);
    
    // requires
    mv.visitRequire("java.base", Opcodes.ACC_MANDATED, null);
    if (requireModules != null) {
      Parameters requireParams = analyzer.parseHeader(requireModules);
      for (String requireName : requireParams.keySet()) {
        Attrs attrs = requireParams.get(requireName);
        boolean isTransitive = attrs.containsKey("transitive");
        boolean isStatic = attrs.containsKey("static");
        mv.visitRequire(requireName, (isTransitive ? Opcodes.ACC_TRANSITIVE : 0) | (isStatic ? Opcodes.ACC_STATIC_PHASE : 0), null);
      }
    }
    
    // exports
    if (exportPackages != null) {
      Parameters exportParams = analyzer.parseHeader(exportPackages);
      for (String packageName : exportParams.keySet()) {
        if (packageName.endsWith("*")) {
            throw new IllegalStateException("unsupported wildcard packages " + packageName);
        }
        mv.visitExport(packageName.replace('.', '/'), 0);
      }
    }
    
    mv.visitEnd();
    
    writer.visitEnd();
    byte[] bytecode = writer.toByteArray();
    
    // debug
    //ClassReader reader = new ClassReader(bytecode);
    //reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    
    Jar jar = analyzer.getJar();
    EmbeddedResource moduleInfo = new EmbeddedResource(bytecode, System.currentTimeMillis());
    jar.putResource("module-info.class", moduleInfo);
    
    return false;
  }
}
