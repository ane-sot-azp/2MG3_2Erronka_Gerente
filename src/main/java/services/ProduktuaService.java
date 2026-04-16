package services;

import DB.ApiClient;
import Klaseak.Produktua;
import Klaseak.ProduktuaOsagaia;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class ProduktuaService {
    private static final Gson gson = new Gson();

    public static List<Produktua> getProduktuak() {
        try {
            HttpResponse<String> response = ApiClient.get("/api/Produktuak");
            if (response.statusCode() != 200) return Collections.emptyList();
            Type listType = new TypeToken<List<Produktua>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static Produktua createProduktua(Produktua p) {
        try {
            String json = gson.toJson(p);
            HttpResponse<String> response = ApiClient.post("/api/Produktuak", json);
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Produktua.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateProduktua(Produktua p) {
        try {
            String json = gson.toJson(p);
            HttpResponse<String> response = ApiClient.put("/api/Produktuak/" + p.getId(), json);
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteProduktua(int id) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/Produktuak/" + id);
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ProduktuaOsagaia> getOsagaiak(int produktuaId) {
        try {
            HttpResponse<String> response = ApiClient.get("/api/Produktuak/" + produktuaId + "/osagaiak");
            if (response.statusCode() != 200) return Collections.emptyList();
            Type listType = new TypeToken<List<ProduktuaOsagaia>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static boolean addOsagaia(int produktuaId, int osagaiaId, int kantitatea) {
        try {
            String json = gson.toJson(new AddUpdateOsagaiaRequest(osagaiaId, kantitatea));
            HttpResponse<String> response = ApiClient.post("/api/Produktuak/" + produktuaId + "/osagaiak", json);
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOsagaia(int produktuaId, int osagaiaId, int kantitatea) {
        try {
            String json = gson.toJson(new AddUpdateOsagaiaRequest(osagaiaId, kantitatea));
            HttpResponse<String> response = ApiClient.put("/api/Produktuak/" + produktuaId + "/osagaiak/" + osagaiaId, json);
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeOsagaia(int produktuaId, int osagaiaId) {
        try {
            HttpResponse<String> response = ApiClient.delete("/api/Produktuak/" + produktuaId + "/osagaiak/" + osagaiaId);
            return response.statusCode() == 204 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class AddUpdateOsagaiaRequest {
        private final int osagaiaId;
        private final int kantitatea;

        private AddUpdateOsagaiaRequest(int osagaiaId, int kantitatea) {
            this.osagaiaId = osagaiaId;
            this.kantitatea = kantitatea;
        }
    }
}
