package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Klaseak.Langilea;
import Klaseak.Lanpostua;
import DB.ApiClient;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LangileaService {

    private static final Gson gson = new Gson();
    private static volatile String lastGetAllDebug = "";
    private static volatile String lastUpdateError = "";

    public static String getLastGetAllDebug() {
        return lastGetAllDebug != null ? lastGetAllDebug : "";
    }
    public static String getLastUpdateError() {
        return lastUpdateError != null ? lastUpdateError : "";
    }

    public static List<Langilea> getAll() {
        try {
            var res = ApiClient.get("/api/langileak");
            if (res.statusCode() != 200) {
                lastGetAllDebug = "HTTP " + res.statusCode() + " - " + (res.body() != null ? res.body() : "");
                System.err.println("ERROR getAll Langileak (" + ApiClient.getBaseUrl() + "): " + lastGetAllDebug);
                return List.of();
            }
            JsonArray arr = JsonParser.parseString(res.body()).getAsJsonArray();
            List<Langilea> out = new ArrayList<>();
            StringBuilder parseErrors = new StringBuilder();
            for (JsonElement el : arr) {
                try {
                    out.add(gson.fromJson(el, Langilea.class));
                } catch (Exception elementEx) {
                    String snippet = el != null ? el.toString() : "null";
                    if (snippet.length() > 300) snippet = snippet.substring(0, 300) + "...";
                    String msg = "ERROR parse Langilea element: " + elementEx.getMessage() + " - " + snippet;
                    System.err.println(msg);
                    if (parseErrors.length() < 1500) {
                        parseErrors.append(msg).append('\n');
                    }
                }
            }
            lastGetAllDebug = "Parsed=" + out.size() + " / " + arr.size() + "\n" + parseErrors;
            return out;
        } catch (Exception e) {
            lastGetAllDebug = "EXCEPTION: " + e.getMessage();
            System.err.println("ERROR getAll Langileak (" + ApiClient.getBaseUrl() + "): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public static Langilea create(Langilea l) {
        try {
            var json = gson.toJson(l);
            var res = ApiClient.post("/api/langileak", json);
            if (res.statusCode() == 200 || res.statusCode() == 201) {
                return gson.fromJson(res.body(), Langilea.class);
            }
            System.err.println("ERROR create Langilea: HTTP " + res.statusCode() + " - " + res.body());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean update(Langilea l) {
        try {
            var json = gson.toJson(l);
            var res = ApiClient.put("/api/langileak/" + l.getId(), json);
            if (res.statusCode() == 200 || res.statusCode() == 204) {
                lastUpdateError = "";
                return true;
            }
            lastUpdateError = "HTTP " + res.statusCode() + " - " + (res.body() != null ? res.body() : "");
            System.err.println("ERROR update Langilea: " + lastUpdateError);
            return false;
        } catch (Exception e) {
            lastUpdateError = "EXCEPTION: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLangile(int id) {
        try {
            var res = ApiClient.delete("/api/langileak/" + id);
            return res.statusCode() == 200 || res.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Lanpostua> getLanpostuak() {
        try {
            var res = ApiClient.get("/api/lanpostuak");

            if (res == null || res.body() == null || res.body().isEmpty()) {
                return List.of();
            }

            Type listType = new TypeToken<List<Lanpostua>>(){}.getType();
            List<Lanpostua> lista = gson.fromJson(res.body(), listType);

            return lista != null ? lista : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}
