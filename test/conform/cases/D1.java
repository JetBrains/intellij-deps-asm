package cases;

public class D1 {

    public void m(boolean z) {
        try {
            try {
                z = true;
            } finally {
                z = false;
            }
        } catch (Exception ex) {
            z = true;
        }
    }
}
