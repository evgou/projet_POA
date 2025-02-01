package misc;

import java.io.Serializable;

public class Prix implements Serializable {
    private static final long serialVersionUID = 1L;
    private int prix;

    public Prix(int prix) {
        this.prix = prix;
    }

    public int getPrix() {
        return prix;
    }
}