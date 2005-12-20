package cases;

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

    class C4 {

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
            C4[] e)
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
            C4[][] e)
        {
        }
    }
}
