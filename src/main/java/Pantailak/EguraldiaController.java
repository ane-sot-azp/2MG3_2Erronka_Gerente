package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.EguraldiaErantzuna;
import services.EguraldiInfo;
import services.EguraldiaService;

public class EguraldiaController {
    @FXML private Label lblHiria;
    @FXML private FlowPane pnlEgunak;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                EguraldiaErantzuna info = EguraldiaService.loadEguraldia();
                Platform.runLater(() -> render(info));
            } catch (Exception e) {
                Platform.runLater(() -> erroreaErakutsi(e.getMessage()));
            }
        }).start();
    }

    private void render(EguraldiaErantzuna info) {
        if (info == null) return;

        if (lblHiria != null) {
            lblHiria.setText(info.getUdalerria() + " - " + info.getProbintzia());
        }

        if (pnlEgunak != null) {
            pnlEgunak.getChildren().clear();
            for (EguraldiInfo eguna : info.getEgunak()) {
                pnlEgunak.getChildren().add(sortuEgunTxartela(eguna));
            }
        }
    }

    private VBox sortuEgunTxartela(EguraldiInfo eguna) {
        Label lblEguna = new Label(eguna.getEgunaTestua());
        lblEguna.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        Label lblDeskribapena = new Label(eguna.getZeruEgoera());
        lblDeskribapena.setWrapText(true);
        lblDeskribapena.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        Label lblXehetasunak = new Label(String.join("\n",
                "Tenp. min: " + eguna.getTenpMin() + " C",
                "Tenp. max: " + eguna.getTenpMax() + " C",
                "Prezipitazioa: " + eguna.getPrezipitazioa() + "%"));
        lblXehetasunak.setStyle("-fx-font-size: 15; -fx-text-fill: #2d3748;");

        VBox txartela = new VBox(14, lblEguna, lblDeskribapena, lblXehetasunak);
        txartela.setPadding(new Insets(18));
        txartela.setPrefWidth(230);
        txartela.setPrefHeight(280);
        txartela.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d9d9d9;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");

        return txartela;
    }

    private void erroreaErakutsi(String mezua) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eguraldia");
        alert.setHeaderText("Errorea eguraldia kargatzean");
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblHiria.getScene().getWindow();
        stage.close();
    }
}
