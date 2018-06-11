package org.objectweb.asm;

public final class MethodTooLargeException extends IndexOutOfBoundsException {
    private final String className;
    private final String methodName;
    private final String descriptor;
    private final int codeSize;

    /**
     * @return The name of the enclosing class, or <tt>null</tt> if not known
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The name of the method
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return The descriptor of the the method
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * @return The number of byte codes in the method.
     */
    public int getCodeSize() {
        return codeSize;
    }

    public MethodTooLargeException(String className, String methodName, String descriptor, int codeSize) {
        super("Method code too large: " + (className == null ? "" : className + ".") + methodName + " " + descriptor);
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.codeSize = codeSize;
    }
}
