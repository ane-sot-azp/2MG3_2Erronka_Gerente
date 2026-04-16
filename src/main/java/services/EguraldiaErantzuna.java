package services;

import java.util.ArrayList;
import java.util.List;

public class EguraldiaErantzuna {
    private String udalerria = "";
    private String probintzia = "";
    private List<EguraldiInfo> egunak = new ArrayList<>();

    public String getUdalerria() {
        return udalerria;
    }

    public String getProbintzia() {
        return probintzia;
    }

    public List<EguraldiInfo> getEgunak() {
        return egunak;
    }
}
