package marche;

import jade.core.AID;

import java.io.Serializable;

public class Prix implements Serializable {

    //private AID vendeur;
    //private String lot;
    private int price;

    public Prix(int price) {
        //this.vendeur = vendeur;
        //this.lot = lot;
        this.price = price;
    }
/*
    public AID getVendeur() {
        return vendeur;
    }

    public void setVendeur(AID vendeur) {
        //this.vendeur = vendeur;
    }

    public String getLot() {
        //return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

 */

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.valueOf(price);

    }
}
