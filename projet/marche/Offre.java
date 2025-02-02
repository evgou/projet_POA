package marche;

import jade.core.AID;

public class Offre {

    private AID vendeur;
    private String price;

    public Offre(AID vendeur, String price) {
        this.vendeur = vendeur;
        this.price = price;
    }

    public AID getVendeur() {
        return vendeur;
    }

    public void setVendeur(AID vendeur) {
        this.vendeur = vendeur;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
