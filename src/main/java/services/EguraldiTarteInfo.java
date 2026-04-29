package services;

import com.google.gson.annotations.SerializedName;

public class EguraldiTarteInfo {
    @SerializedName("aldia")
    private final String aldia;

    @SerializedName("ordua")
    private final String ordua;

    @SerializedName("tenperatura")
    private final String tenperatura;

    @SerializedName("zeruEgoera")
    private final String zeruEgoera;

    public EguraldiTarteInfo(
            String aldia,
            String ordua,
            String tenperatura,
            String zeruEgoera
    ) {
        this.aldia = aldia;
        this.ordua = ordua;
        this.tenperatura = tenperatura;
        this.zeruEgoera = zeruEgoera;
    }

    public String getAldia() { return aldia; }
    public String getOrdua() { return ordua; }
    public String getTenperatura() { return tenperatura; }
    public String getZeruEgoera() { return zeruEgoera; }
}
