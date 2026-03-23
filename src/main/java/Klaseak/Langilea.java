package Klaseak;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

public class Langilea {
    private int id;
    private String izena;
    private String abizena;
    @SerializedName(value = "nan", alternate = { "NAN" })
    private String nan;

    @SerializedName(value = "erabiltzaile_izena", alternate = { "erabiltzaileIzena" })
    private String erabiltzaileIzena;

    @SerializedName(value = "langile_kodea", alternate = { "langileKodea" })
    private int langileKodea;
    private String pasahitza;
    private String helbidea;
    private Lanpostua lanpostua;

    public Langilea(int id, String izena, String abizena, String nan, String erabiltzaileIzena, int langileKodea, String pasahitza, String helbidea, Lanpostua lanpostua) {
        this.id = id;
        this.izena = izena;
        this.abizena = abizena;
        this.nan = nan;
        this.erabiltzaileIzena = erabiltzaileIzena;
        this.langileKodea = langileKodea;
        this.pasahitza = pasahitza;
        this.helbidea = helbidea;
        this.lanpostua = lanpostua;
    }

    public Langilea() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena != null ? izena : ""; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getAbizena() { return abizena != null ? abizena : ""; }
    public void setAbizena(String abizena) { this.abizena = abizena; }

    public String getAbizena1() { return abizena; }
    public void setAbizena1(String abizena1) { this.abizena = abizena1; }

    public String getAbizena2() { return ""; }
    public void setAbizena2(String abizena2) { }

    public String getTelefonoa() { return ""; }
    public void setTelefonoa(String telefonoa) { }

    public String getNan() { return nan != null ? nan : ""; }
    public void setNan(String nan) { this.nan = nan; }

    public String getErabiltzaileIzena() { return erabiltzaileIzena != null ? erabiltzaileIzena : ""; }
    public void setErabiltzaileIzena(String erabiltzaileIzena) { this.erabiltzaileIzena = erabiltzaileIzena; }

    public int getLangileKodea() { return langileKodea; }
    public void setLangileKodea(int langileKodea) { this.langileKodea = langileKodea; }

    public String getPasahitza() { return pasahitza != null ? pasahitza : ""; }
    public void setPasahitza(String pasahitza) { this.pasahitza = pasahitza; }

    public String getPasahitzaDecimal() {
        String hex = getPasahitza();
        if (hex.isEmpty()) return "";
        try {
            return new BigInteger(hex, 16).toString(10);
        } catch (Exception e) {
            return "";
        }
    }

    public String getHelbidea() { return helbidea != null ? helbidea : ""; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }

    public Klaseak.Lanpostua getLanpostua() { return lanpostua; }
    public void setLanpostua(Lanpostua value) { this.lanpostua = value; }

    public String getLanpostuaName() {
        if (lanpostua == null) return "";
        String name = lanpostua.getIzena();
        return name != null ? name : "";
    }
}
