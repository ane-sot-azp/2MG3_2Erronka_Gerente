package Klaseak;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Hornitzailea {
    @SerializedName("id")
    private int id;

    @SerializedName("izena")
    private String izena;

    @SerializedName("kontaktua")
    private String kontaktua;

    @SerializedName("helbidea")
    private String helbidea;

    public Hornitzailea() {
    }

    public Hornitzailea(int id, String izena, String kontaktua, String helbidea) {
        this.id = id;
        this.izena = izena;
        this.kontaktua = kontaktua;
        this.helbidea = helbidea;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getKontaktua() { return kontaktua; }
    public void setKontaktua(String kontaktua) { this.kontaktua = kontaktua; }

    public String getHelbidea() { return helbidea != null ? helbidea : ""; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }

    @Override
    public String toString() {
        return izena;
    }
}