package Pantailak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.EguraldiaErantzuna;
import services.EguraldiInfo;
import services.EguraldiTarteInfo;
import services.EguraldiaService;

public class EguraldiaController {
    @FXML private Label lblHiria;
    @FXML private VBox pnlGaur;
    @FXML private FlowPane pnlHurrengoEgunak;

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

        if (pnlGaur != null) {
            pnlGaur.getChildren().clear();
        }

        if (pnlHurrengoEgunak != null) {
            pnlHurrengoEgunak.getChildren().clear();
        }

        if (info.getEgunak() == null || info.getEgunak().isEmpty()) {
            return;
        }

        EguraldiInfo gaur = info.getEgunak().get(0);
        if (pnlGaur != null) {
            VBox gaurTxartela = sortuGaurTxartela(gaur);
            VBox.setVgrow(gaurTxartela, Priority.ALWAYS);
            pnlGaur.getChildren().add(gaurTxartela);
        }

        if (pnlHurrengoEgunak != null) {
            for (int i = 1; i < info.getEgunak().size(); i++) {
                pnlHurrengoEgunak.getChildren().add(sortuHurrengoEgunTxartela(info.getEgunak().get(i)));
            }
        }
    }

    private VBox sortuGaurTxartela(EguraldiInfo eguna) {
        Label lblEguna = new Label(eguna.getEgunaTestua());
        lblEguna.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        Label lblDeskribapena = new Label(eguna.getZeruEgoera());
        lblDeskribapena.setWrapText(true);
        lblDeskribapena.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        Label lblXehetasunak = new Label(String.join("\n",
                "Tenp. min: " + eguna.getTenpMin() + " C",
                "Tenp. max: " + eguna.getTenpMax() + " C",
                "Prezipitazioa: " + eguna.getPrezipitazioa() + "%"));
        lblXehetasunak.setStyle("-fx-font-size: 15; -fx-text-fill: #2d3748;");
        lblXehetasunak.setWrapText(true);

        Label lblTarteak = new Label("Ordutegiaren arabera");
        lblTarteak.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        FlowPane pnlTarteak = new FlowPane();
        pnlTarteak.setHgap(12);
        pnlTarteak.setVgap(12);
        pnlTarteak.setPrefWrapLength(900);

        if (eguna.getXehetasunak() != null) {
            for (EguraldiTarteInfo tartea : eguna.getXehetasunak()) {
                pnlTarteak.getChildren().add(sortuTarteTxartela(tartea));
            }
        }

        VBox txartela = new VBox(14, lblEguna, lblDeskribapena, lblXehetasunak, lblTarteak, pnlTarteak);
        txartela.setPadding(new Insets(0));
        txartela.setFillWidth(true);

        return txartela;
    }

    private VBox sortuHurrengoEgunTxartela(EguraldiInfo eguna) {
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
        lblXehetasunak.setWrapText(true);

        VBox txartela = new VBox(14, lblEguna, lblDeskribapena, lblXehetasunak);
        txartela.setPadding(new Insets(18));
        txartela.setPrefWidth(230);
        txartela.setMinHeight(220);
        txartela.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d9d9d9;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");

        return txartela;
    }

    private VBox sortuTarteTxartela(EguraldiTarteInfo tartea) {
        String ordua = tartea.getOrdua() == null || tartea.getOrdua().isBlank()
                ? tartea.getAldia()
                : tartea.getOrdua();

        Label lblOrdua = new Label(ordua);
        lblOrdua.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1D505B;");

        String tenperatura = tartea.getTenperatura() == null || tartea.getTenperatura().isBlank()
                ? ""
                : "\n" + tartea.getTenperatura() + " C";
        Label lblInfo = new Label(tartea.getZeruEgoera() + tenperatura);
        lblInfo.setWrapText(true);
        lblInfo.setStyle("-fx-font-size: 14; -fx-text-fill: #2d3748;");

        VBox txartela = new VBox(8, lblOrdua, lblInfo);
        txartela.setPadding(new Insets(12));
        txartela.setPrefWidth(250);
        txartela.setMinHeight(90);
        txartela.setStyle(
                "-fx-background-color: #F5F5F5;" +
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
