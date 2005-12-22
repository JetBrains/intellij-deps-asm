package cases;

/* compiled with source=target=1.4 */
/* covers V1_4 version */

public class D4 {

    private int i;

    void m() {
        new D4() {
            public String toString() {
                return D4.this + " " + i;
            }
        };
    }
}
