package cases;

/* compiled with source=target=1.5 */
/* covers visible annotations, deprecated access flag, */
/* inner classes and synthetic members, */
/* method signatures with array types */

/**
 * @deprecated
 */
@Deprecated
public class C3
{

    /**
     * @deprecated
     */
    @Deprecated
    private int i;

    /**
     * @deprecated
     */
    @Deprecated
    void m()
    {
        new C3() {
            public String toString() {
                return C3.this + " " + i;
            }
        };
    }

    class C {

        void m(
            @Deprecated
            boolean[] z,
            @Deprecated
            byte[] b,
            char[] c,
            short[] s,
            int[] i,
            float[] f,
            long[] l,
            double[] d,
            C[] e)
        {
        }

        void m(
            boolean[][] z,
            byte[][] b,
            char[][] c,
            short[][] s,
            int[][] i,
            float[][] f,
            long[][] l,
            double[][] d,
            C[][] e)
        {
        }
    }
}
