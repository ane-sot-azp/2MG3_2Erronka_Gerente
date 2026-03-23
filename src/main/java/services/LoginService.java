package services;

import DB.ApiClient;
import java.net.http.HttpResponse;

public class LoginService {

    public static String login(String erabiltzailea, String pasahitza) {

        try {
            // erabiltzailea int bat dela suposatzen dugu (langile_kodea)
            int langileKodea = Integer.parseInt(erabiltzailea);
            String json = "{ \"langile_kodea\": " + langileKodea + ", \"pasahitza\": \"" + pasahitza + "\" }";

            HttpResponse<String> response = ApiClient.post("/api/login/admin", json);

            int code = response.statusCode();

            if (code == 200) {
                return "OK";
            }

            if (code == 401) {
                return "BAD_CREDENTIALS";
            }

            if (code == 403) {
                return "NO_PERMISSION";
            }

            return "ERROR";

        } catch (NumberFormatException e) {
            return "BAD_CREDENTIALS"; // Kodea ez bada zenbakia
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
