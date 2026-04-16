package services;

import DB.ApiClient;
import com.google.gson.Gson;
import java.net.http.HttpResponse;

public class EguraldiaService {
    private static final Gson GSON = new Gson();

    public static EguraldiaErantzuna loadEguraldia() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/eguraldia/azkena");
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("API errorea: " + response.statusCode());
            }

            EguraldiaErantzuna erantzuna = GSON.fromJson(response.body(), EguraldiaErantzuna.class);
            if (erantzuna == null) {
                throw new IllegalStateException("Eguraldiaren erantzuna hutsik dago.");
            }

            return erantzuna;
        } catch (Exception e) {
            throw new IllegalStateException("Errorea eguraldia API-tik kargatzean: " + e.getMessage(), e);
        }
    }
}
