/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/
package de.loskutov.bco.asm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.loskutov.bco.BytecodeOutlinePlugin;

/**
 * Container for decompiled bytecode
 * @author Andrei
 */
public class DecompileResult {
    
    /**
     * start of source line marker
     */
    public static final String SOURCE_LINE_PREFIX = " ("; //$NON-NLS-1$
    /**
     * stop of source line marker
     */
    public static final String SOURCE_LINE_SUFFIX = ")\n"; //$NON-NLS-1$
    /**
     * " (345)\n" is a valid example for this pattern, 345 is stored as content of
     * matching group #1
     */
    private static final Pattern sourceLinePattern = Pattern
        .compile(" \\((\\d+)\\)\\n"); //$NON-NLS-1$

    private String decompiledCode;

    /**
     * @param decompiledCode full decompiled bytecode as string
     */
    public DecompileResult(String decompiledCode) {
        setDecompiledCode(decompiledCode);
    }

    /**
     * Returns source line from corresponding source code.
     * Based on text pattern search in decompiled bytecode - used pattern is
     * currently (\d) just before line end
     * @param byteCodeTextOffset text offset from decompiled code
     * @return -1 if source line is not found
     */
    public int getSourceLine(int byteCodeTextOffset) {
        int startLine = -1;
        // last character in the currrent line
        int stopSearch = decompiledCode.indexOf("\n", byteCodeTextOffset) + 1; //$NON-NLS-1$
        String subSequence = decompiledCode.substring(0, stopSearch);
        int startSearch = subSequence.lastIndexOf("//") + 1; //$NON-NLS-1$
        String lineNbrStr = getLastMatch(
            startSearch, subSequence.length(), decompiledCode);
        if (lineNbrStr == null) {
            return startLine;
        }
        try {
            startLine = Integer.parseInt(lineNbrStr);
        } catch (NumberFormatException e) {
            BytecodeOutlinePlugin.error(null, e);
        }
        return startLine;
    }

    private static final String getLastMatch(int startSearch, int stopSearch,
        String code) {
        Matcher matcher = sourceLinePattern.matcher(code.subSequence(
            startSearch, stopSearch));
        String lastGroup = null;
        while (matcher.find()) {
            lastGroup = matcher.group(1);
        }
        return lastGroup;
    }

    /**
     * @param decompiledCode The decompiledCode to set.
     */
    public void setDecompiledCode(String decompiledCode) {
        this.decompiledCode = decompiledCode;
    }

    /**
     * @return Returns the decompiledCode.
     */
    public String getDecompiledCode() {
        return decompiledCode;
    }
}