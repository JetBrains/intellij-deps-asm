package org.objectweb.asm;

public final class ClassTooLargeException extends IndexOutOfBoundsException {
    private final String className;
    private final int constantPoolCount;

    /**
     * @return The name of the class, or <tt>null</tt> if not known
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The number of byte codes in the method.
     */
    public int getConstantPoolCount() {
        return constantPoolCount;
    }

    public ClassTooLargeException(String className, int constantPoolCount) {
        super("Class file too large" + (className == null ? "" : ": " + className));
        this.className = className;
        this.constantPoolCount = constantPoolCount;
    }
}
