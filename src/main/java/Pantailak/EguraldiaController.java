package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.EguraldiInfo;
import services.EguraldiaService;

public class EguraldiaController {
    @FXML private Label lblHiria;
    @FXML private Label lblEguna;
    @FXML private Label lblDeskribapena;
    @FXML private Label lblMin;
    @FXML private Label lblMax;
    @FXML private Label lblAzkenEguneraketa;

    @FXML
    public void initialize() {
        new Thread(() -> {
            EguraldiInfo info = EguraldiaService.loadDonostiaGaur();
            Platform.runLater(() -> render(info));
        }).start();
    }

    private void render(EguraldiInfo info) {
        if (info == null) return;
        if (lblHiria != null) lblHiria.setText(info.getHiria());
        if (lblEguna != null) lblEguna.setText(info.getEgunaTestua());
        if (lblDeskribapena != null) lblDeskribapena.setText(info.getDeskribapenaEu());
        if (lblMin != null) lblMin.setText(info.getTenpMin() + "°");
        if (lblMax != null) lblMax.setText(info.getTenpMax() + "°");
        if (lblAzkenEguneraketa != null) {
            String text = info.getAzkenEguneraketa();
            lblAzkenEguneraketa.setText(text == null || text.isBlank() ? "" : "Azken eguneraketa: " + text);
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblHiria.getScene().getWindow();
        stage.close();
    }
}
