package Klaseak;

import com.google.gson.annotations.SerializedName;

public class Mahaia {
    @SerializedName("id")
    private int id;

    @SerializedName("zenbakia")
    private int zenbakia;

    @SerializedName("pertsonaKopurua")
    private int pertsonaKopurua;

    @SerializedName("kokapena")
    private String kokapena;

    public Mahaia() {}

    public Mahaia(int id, int zenbakia, int pertsonaKopurua, String kokapena) {
        this.id = id;
        this.zenbakia = zenbakia;
        this.pertsonaKopurua = pertsonaKopurua;
        this.kokapena = kokapena;
    }

    // Getters
    public int getId() { return id; }
    public int getZenbakia() { return zenbakia; }
    public int getPertsonaKopurua() { return pertsonaKopurua; }
    public String getKokapena() { return kokapena != null ? kokapena : ""; }

    public int getPertsonaMax() { return getPertsonaKopurua(); }

    public boolean isOccupied() { return false; }
    public boolean isOkupatuta() { return isOccupied(); }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setZenbakia(int zenbakia) { this.zenbakia = zenbakia; }
    public void setPertsonaKopurua(int pertsonaKopurua) { this.pertsonaKopurua = pertsonaKopurua; }
    public void setKokapena(String kokapena) { this.kokapena = kokapena; }

    public void setPertsonaMax(int pertsonaMax) { setPertsonaKopurua(pertsonaMax); }

    public void setOccupied(boolean occupied) { }

    @Override
    public String toString() {
        return "Mahai " + zenbakia + " (" + getKokapena() + ")";
    }
}
