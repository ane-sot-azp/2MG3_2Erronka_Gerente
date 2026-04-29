package services;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

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

    @SerializedName("xehetasunak")
    private final List<EguraldiTarteInfo> xehetasunak;

    public EguraldiInfo(
            String egunaTestua,
            String zeruEgoera,
            String tenpMin,
            String tenpMax,
            String prezipitazioa,
            List<EguraldiTarteInfo> xehetasunak
    ) {
        this.egunaTestua = egunaTestua;
        this.zeruEgoera = zeruEgoera;
        this.tenpMin = tenpMin;
        this.tenpMax = tenpMax;
        this.prezipitazioa = prezipitazioa;
        this.xehetasunak = xehetasunak == null ? new ArrayList<>() : xehetasunak;
    }

    public String getEgunaTestua() { return egunaTestua; }
    public String getZeruEgoera() { return zeruEgoera; }
    public String getTenpMin() { return tenpMin; }
    public String getTenpMax() { return tenpMax; }
    public String getPrezipitazioa() { return prezipitazioa; }
    public List<EguraldiTarteInfo> getXehetasunak() { return xehetasunak; }
}
