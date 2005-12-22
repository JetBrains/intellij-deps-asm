package cases;

import java.lang.annotation.Documented;

/* compiled with source=target=1.5 */
/* covers invisible annotations, almost all instructions, */
/* almost all access flags, all primitive array and reference type */
/* some signatures, unicode characters */

@C0(byteValue = 0, charValue = '0', booleanValue = false, intValue = 0)
public class C2<E> implements I1<E>
{

    @C0(shortValue = 0, longValue = 0L, floatValue = 0f, doubleValue = 0d)
    private final boolean z = true;

    @C0(stringValue = "0", enumValue = C1.V0, annotationValue = @Documented)
    protected byte b = 1;

    @C0(classValue = C0.class)
    public char c = '1';

    @C0(byteArrayValue = { 1, 0 }, charArrayValue = { '1', 0 })
    static short s = 1;

    @C0(booleanArrayValue = { true, false }, intArrayValue = { 1, 0 })
    transient int i = 1;

    @C0(shortArrayValue = { (short) 1, (short) 0 }, longArrayValue = { 1L, 0L })
    volatile long l = 1l;

    @C0(floatArrayValue = { 1f, 0f }, doubleArrayValue = { 1d, 0d })
    float f = 1f;

    @C0(stringArrayValue = { "1", "0" }, enumArrayValue = { C1.V1, C1.V2 })
    double d = 1d;

    @C0(annotationArrayValue = {}, classArrayValue = {})
    String str = "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111" +
            "\u0111\u0011\u0001";

    @C0(enumValue = C1.V0)
    E e;

    @C0(annotationValue = @Documented)
    public C2()
    {
    }

    int i() {
        return i;
    }

    long l() {
        return l;
    }

    float f() {
        return f;
    }

    double d() {
        return d;
    }

    @C0
    synchronized public E m(
        boolean _z,
        byte _b,
        char _c,
        short _s,
        int _i,
        float _f,
        long _l,
        double _d,
        @C0
        E _e)
    {
        _e = null;
        i = -1;
        i = 0;
        i = 1;
        i = 2;
        i = 3;
        i = 4;
        i = 5;
        l = 0l;
        l = 1l;
        f = 0f;
        f = 1f;
        f = 2f;
        d = 0d;
        d = 1d;
        i = 128;
        i = 256;
        i = 65536;
        l = 128l;
        f = 128f;
        d = 128d;
        str = "str";

        _i = (new boolean[] { false, true }[0]) ? 1 : 0;
        _b = new byte[] { 0, 1 }[0];
        _c = new char[] { '0', '1' }[0];
        _s = new short[] { 0, 1 }[0];
        _i = new int[] { 0, 1 }[0];
        _l = new long[] { 0, 1 }[0];
        _f = new float[] { 0, 1 }[0];
        _d = new double[] { 0, 1 }[0];
        _e = null;
        str = new String[] { new String("0"), new String("1") }[0];
        (new boolean[1])[0] = _z;
        (new byte[1])[0] = _b;
        (new char[1])[0] = _c;
        (new short[1])[0] = _s;
        (new int[1])[0] = _i;
        (new long[1])[0] = _l;
        (new float[1])[0] = _f;
        (new double[1])[0] = _d;
        (new String[1])[0] = str;
        E e = _e;

        new Float(0).floatValue();
        new Double(0).doubleValue();
        float f = 0;
        double d = 0;
        float[] fs = new float[1];
        double[] ds = new double[1];
        f = fs[0] = --f;
        d = ds[0] = --d;
        new Integer(i++);
        new Long(l++);
        // SWAP?

        _i = _i * 2 + i % 2 - i / 2 + (i << 1) + (i >> 1) + (i >>> 1);
        _l = _l * 2 + l % 2 - l / 2 + (l << 1) + (l >> 1) + (l >>> 1);
        _f = _f * 2 + f % 2 - f / 2;
        _d = _d * 2 + d % 2 - d / 2;
        i = -i;
        l = -l;
        f = -f;
        d = -d;

        for (int k = 0; k < 3; ++k) {
            i = (i & (i | ~i)) ^ i;
            l = (l & (l | ~l)) ^ l;
        }

        b = (byte) i;
        c = (char) i;
        s = (short) i;
        i = (int) l + (int) f + (int) d;
        l = i + (long) f + (long) d;
        f = (float) i + (float) l + (float) d;
        d = (double) i + (double) l + f;

        if (i == 0) {
            if (i != 0) {
                if (i > 0) {
                    if (i < 0) {
                        if (i >= 0) {
                            if (i <= 0) {
                                i = 1;
                            }
                        }
                    }
                }
            }
        }
        if (i == 1) {
            if (i != 1) {
                if (i > 1) {
                    if (i < 1) {
                        if (i >= 1) {
                            if (i <= 1) {
                                i = 1;
                            }
                        }
                    }
                }
            }
        }
        if (l == 1) {
            if (f == 1f) {
                if (f > 1f) {
                    if (f < 1f) {
                        if (d == 1d) {
                            if (d > 1d) {
                                if (d < 1d) {
                                }
                            }
                        }
                    }
                }
            }
        }
        if (str == "str") {
            if (str != "str") {
                str = "\n\r\t\"\\";
            }
        }
        if (str == null) {
            if (str != null) {
                str = "1";
            }
        }

        switch (i) {
            case 0:
                i = 1;
                break;
            case 1:
                i = 2;
                break;
            case 2:
                i = 3;
                break;
            default:
                break;

        }
        switch (i) {
            case 0:
                i = 1;
                break;
            case 10000:
                i = 2;
                break;
            case 20000:
                i = 2;
                break;
            default:
                break;

        }

        try {
            try {
                if (z) {
                    throw new RuntimeException();
                }
            } finally {
                I1<E> i1 = null;
                i1.m(z, b, c, s, i, f, l, d, _e);
            }
        } catch (Exception ex) {
            if (e instanceof String) {
                synchronized (this) {
                    str = (String) e;
                }
            }
        }

        (new int[1][2][3])[0] = null;

        return null;
    }

    public strictfp void n(Object... args) throws Exception {
    }
}
