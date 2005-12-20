package cases;

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
