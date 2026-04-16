package services;

import com.google.gson.annotations.SerializedName;

public class EguraldiInfo {
    @SerializedName("astekoEguna")
    private final String egunaTestua;

    @SerializedName("zeruEgoera")
    private final String zeruEgoera;

    @SerializedName("tenperaturaMinimoa")
    private final String tenpMin;

    @SerializedName("tenperaturaMaximoa")
    private final String tenpMax;

    @SerializedName("prezipitazioProbabilitatea")
    private final String prezipitazioa;

    public EguraldiInfo(
            String egunaTestua,
            String zeruEgoera,
            String tenpMin,
            String tenpMax,
            String prezipitazioa
    ) {
        this.egunaTestua = egunaTestua;
        this.zeruEgoera = zeruEgoera;
        this.tenpMin = tenpMin;
        this.tenpMax = tenpMax;
        this.prezipitazioa = prezipitazioa;
    }

    public String getEgunaTestua() { return egunaTestua; }
    public String getZeruEgoera() { return zeruEgoera; }
    public String getTenpMin() { return tenpMin; }
    public String getTenpMax() { return tenpMax; }
    public String getPrezipitazioa() { return prezipitazioa; }
}
