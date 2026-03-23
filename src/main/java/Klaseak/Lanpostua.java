package Klaseak;

import com.google.gson.annotations.SerializedName;

public class Lanpostua {
    private int id;

    @SerializedName(value = "lanpostu_izena", alternate = { "izena", "lanpostua_izena", "lanpostuaIzena" })
    private String izena;

    public Lanpostua(int id, String izena) {
        this.id = id;
        this.izena = izena;
    }

    public Lanpostua() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIzena() {
        return izena != null ? izena : "";
    }

    @Override
    public String toString() {
        return izena;
    }


    public void setIzena(String izena) {
        this.izena = izena;
    }
}
