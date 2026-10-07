package com.duellite.model;

/** Modelo de una carta Monster obtenida desde la API. */
public class Card {
    private final String name;
    private final int atk;
    private final int def;
    private final String image; // URL de la imagen

    public Card(String name, int atk, int def, String image) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.image = image;
    }

    public String getName() { return name; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public String getImage() { return image; }

    @Override
    public String toString() {
        return name + " (ATK " + atk + " / DEF " + def + ")";
    }
}
