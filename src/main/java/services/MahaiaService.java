package services;

import Klaseak.Mahaia;
import DB.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MahaiaService {
    private final Gson gson = new GsonBuilder().create();

    public CompletableFuture<List<Mahaia>> getAllMahai() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("DEBUG: API dei egiten: /api/mahaiak");

                HttpResponse<String> response = ApiClient.get("/api/mahaiak");

                System.out.println("DEBUG: Egoera kodea: " + response.statusCode());

                if (response.statusCode() == 200) {
                    List<Mahaia> mahaiak = new ArrayList<>();
                    JsonArray arr = JsonParser.parseString(response.body()).getAsJsonArray();
                    for (JsonElement el : arr) {
                        mahaiak.add(gson.fromJson(el, Mahaia.class));
                    }
                    return mahaiak;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    public CompletableFuture<Mahaia> createMahai(Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = gson.toJson(mahai);
                HttpResponse<String> response = ApiClient.post("/api/mahaiak", jsonBody);

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    return gson.fromJson(response.body(), Mahaia.class);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> updateMahai(int id, Mahaia mahai) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = gson.toJson(mahai);
                HttpResponse<String> response = ApiClient.put("/api/mahaiak/" + id, jsonBody);
                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deleteMahai(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = ApiClient.delete("/api/mahaiak/" + id);
                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
