import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.HashMap;

public class RT {
    public static CallSite cst(Lookup lookup, String name, MethodType type, Object constant) {
        return new ConstantCallSite(MethodHandles.constant(Object.class, constant));
    }
    
    public static CallSite unary(Lookup lookup, String name, MethodType type) {
        MethodHandle target;
        if (name.equals("asBoolean")) {
            target = MethodHandles.explicitCastArguments(
                    MethodHandles.identity(Object.class),
                    MethodType.methodType(boolean.class, Object.class));
        } else { // "not"
            target =  MethodHandles.explicitCastArguments(
                    NOT,
                    MethodType.methodType(Object.class, Object.class));
        }
        return new ConstantCallSite(target);
    }
    
    public static CallSite binary(Lookup lookup, String name, MethodType type) {
        BinaryOpCallSite callSite = new BinaryOpCallSite(name, type);
        callSite.setTarget(callSite.fallback);
        return callSite;
    }
    
    public static class UnayOps {
        public static Object not(boolean b) {
            return !b;
        }
    }
    
    private static final MethodHandle NOT;
    static {
        try {
            NOT = MethodHandles.publicLookup().findStatic(UnayOps.class, "not",
                    MethodType.methodType(Object.class, boolean.class));
        } catch (ReflectiveOperationException e) {
            throw new LinkageError(e.getMessage(), e);
        }
    }
    
    static class BinaryOpCallSite extends MutableCallSite {
        private final String opName;
        final MethodHandle fallback;
        
        public BinaryOpCallSite(String opName, MethodType type) {
            super(type);
            this.opName = opName;
            this.fallback = FALLBACK.bindTo(this);
        }
        
        Object fallback(Object v1, Object v2) throws Throwable {
            // when you debug with this message don't forget that && and || are lazy !!
            //System.out.println("fallback called with "+opName+'('+v1.getClass()+','+v2.getClass()+')');
            
            Class< ? extends Object> class1 = v1.getClass();
            Class< ? extends Object> class2 = v2.getClass();
            MethodHandle op = lookupBinaryOp(opName, class1, class2);
            
            // convert arguments
            MethodType type = type();
            MethodType opType = op.type();
            if (opType.parameterType(0) == String.class) {
                if (opType.parameterType(1) == String.class) {
                    op = MethodHandles.filterArguments(op, 0, TO_STRING, TO_STRING);
                } else {
                    op = MethodHandles.filterArguments(op, 0, TO_STRING);
                    op = MethodHandles.explicitCastArguments(op, type);
                }
            } else {
                if (opType.parameterType(1) == String.class) {
                    op = MethodHandles.filterArguments(op, 1, TO_STRING);
                }
                op = MethodHandles.explicitCastArguments(op, type);
            }
            
            // prepare guard
            MethodHandle guard = MethodHandles.guardWithTest(TEST1.bindTo(class1),
                    MethodHandles.guardWithTest(TEST2.bindTo(class2), op, fallback),
                    fallback); 
            
            // install the inlining cache
            setTarget(guard);
            return op.invokeWithArguments(v1, v2);
        }
        
        public static boolean test1(Class<?> v1Class, Object v1, Object v2) {
            return v1.getClass() == v1Class;
        }
        public static boolean test2(Class<?> v2Class, Object v1, Object v2) {
            return v2.getClass() == v2Class;
        }
        
        private static final MethodHandle TO_STRING, TEST1, TEST2, FALLBACK;
        static {
            Lookup lookup = MethodHandles.lookup();
            try {
                TO_STRING = lookup.findVirtual(Object.class, "toString",
                        MethodType.methodType(String.class));
                MethodType testType = MethodType.methodType(boolean.class, Class.class, Object.class, Object.class);
                TEST1 = lookup.findStatic(BinaryOpCallSite.class, "test1", testType);
                TEST2 = lookup.findStatic(BinaryOpCallSite.class, "test2", testType);
                FALLBACK = lookup.findVirtual(BinaryOpCallSite.class, "fallback",
                        MethodType.genericMethodType(2));
            } catch(ReflectiveOperationException e) {
                throw new LinkageError(e.getMessage(), e);
            }
        }
    }
    

    public static class BinaryOps {
        public static Object add(int v1, int v2) {
            return v1 + v2;
        }
        public static Object add(double v1, double v2) {
            return v1 + v2;
        }
        public static Object add(String v1, String v2) {
            return v1 + v2;
        }
        
        public static Object mul(int v1, int v2) {
            return v1 * v2;
        }
        public static Object mul(double v1, double v2) {
            return v1 * v2;
        }
        
        public static Object gt(int v1, int v2) {
            return v1 > v2;
        }
        public static Object gt(double v1, double v2) {
            return v1 > v2;
        }
        public static Object gt(String v1, String v2) {
            return v1.compareTo(v2) > 0;
        }
    }
    
    static MethodHandle lookupBinaryOp(String opName, Class<?> class1, Class<?> class2) {
        int rank = Math.max(RANK_MAP.get(class1), RANK_MAP.get(class2));
        String mangledName = opName+rank;
        MethodHandle mh = BINARY_CACHE.get(mangledName);
        if (mh != null) {
            return mh;
        }
        
        for(;rank < PRIMITIVE_ARRAY.length;) {
            Class<?> primitive = PRIMITIVE_ARRAY[rank];
            try {
                mh = MethodHandles.publicLookup().findStatic(BinaryOps.class, opName,
                        MethodType.methodType(Object.class, primitive, primitive));
            } catch (NoSuchMethodException e) {
                rank = rank + 1;
                continue;
            } catch (IllegalAccessException e) {
                throw new LinkageError(e.getMessage(), e);
            }
            
            BINARY_CACHE.put(mangledName, mh);
            return mh;
        }
        throw new LinkageError("unknown operation "+opName+" ("+class1.getName()+','+class2.getName()+')');
    }
    
    private static final HashMap<Class<?>, Integer> RANK_MAP;
    private static final Class<?>[] PRIMITIVE_ARRAY;
    private static final HashMap<String, MethodHandle> BINARY_CACHE;
    static {
        Class<?>[] primitives = new Class<?>[] {
                boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class, String.class       
        };
        Class<?>[] wrappers = new Class<?>[] {
                Boolean.class, Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class, String.class       
        };
        HashMap<Class<?>, Integer> rankMap = new HashMap<Class<?>, Integer>();
        for(int i=0; i<wrappers.length; i++) {
            rankMap.put(wrappers[i], i);
        }

        RANK_MAP = rankMap;
        PRIMITIVE_ARRAY = primitives;
        BINARY_CACHE = new HashMap<String, MethodHandle>();
    }
}