package services;

public class EguraldiInfo {
    private final String hiria;
    private final String egunaTestua;
    private final String azkenEguneraketa;
    private final int tenpMin;
    private final int tenpMax;
    private final String deskribapenaEu;
    private final String sinboloKodea;

    public EguraldiInfo(
            String hiria,
            String egunaTestua,
            String azkenEguneraketa,
            int tenpMin,
            int tenpMax,
            String deskribapenaEu,
            String sinboloKodea
    ) {
        this.hiria = hiria;
        this.egunaTestua = egunaTestua;
        this.azkenEguneraketa = azkenEguneraketa;
        this.tenpMin = tenpMin;
        this.tenpMax = tenpMax;
        this.deskribapenaEu = deskribapenaEu;
        this.sinboloKodea = sinboloKodea;
    }

    public String getHiria() { return hiria; }
    public String getEgunaTestua() { return egunaTestua; }
    public String getAzkenEguneraketa() { return azkenEguneraketa; }
    public int getTenpMin() { return tenpMin; }
    public int getTenpMax() { return tenpMax; }
    public String getDeskribapenaEu() { return deskribapenaEu; }
    public String getSinboloKodea() { return sinboloKodea; }
}
