package Pantailak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Klaseak.Hornitzailea;
import Klaseak.Osagaia;
import services.ActionLogger;
import services.HornitzaileaService;
import services.OsagaiaService;
import services.SessionContext;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HornitzaileakController {

    @FXML private TableView<Hornitzailea> tableHornitzaileak;
    @FXML private TableColumn<Hornitzailea, Integer> colHornitzaileId;
    @FXML private TableColumn<Hornitzailea, String> colHornitzaileIzena;
    @FXML private TableColumn<Hornitzailea, String> colHornitzaileKontaktua;
    @FXML private TableColumn<Hornitzailea, String> colHornitzaileHelbidea;

    @FXML private TextField txtIzena, txtKontaktua, txtHelbidea;
    @FXML private Button btnSaveHornitzailea, btnCancelHornitzailea;

    @FXML private TableView<Osagaia> tableHornitzailearenOsagaiak;
    @FXML private TableColumn<Osagaia, String> colOsagaiIzena;
    @FXML private TableColumn<Osagaia, Double> colOsagaiPrezioa;
    @FXML private TableColumn<Osagaia, Integer> colOsagaiStock;

    @FXML private ComboBox<Osagaia> comboOsagaiak;
    @FXML private Button btnGehituOsagaia, btnKenduOsagaia;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterOrdenatu;
    @FXML private Button btnAddHornitzailea, btnDeleteHornitzailea, atzeraBotoia, refreshButton;

    @FXML private Label hornitzaileKopuruaLabel, osagaiKopuruaLabel;

    private ObservableList<Hornitzailea> hornitzaileakLista;
    private ObservableList<Osagaia> hornitzailearenOsagaiakLista;
    private ObservableList<Osagaia> osagaiakDisponibleLista;
    private FilteredList<Hornitzailea> filteredHornitzaileak;

    private Hornitzailea hornitzaileaEditatzen;

    private HornitzaileaService hornitzaileaService;
    private OsagaiaService osagaiaService;
    private static final Logger LOGGER = Logger.getLogger(HornitzaileakController.class.getName());

    @FXML
    public void initialize() {
        try {
            LOGGER.info("HornitzaileakController inicializando...");

            hornitzaileaService = new HornitzaileaService();
            osagaiaService = new OsagaiaService();

            hornitzaileakLista = FXCollections.observableArrayList();
            hornitzailearenOsagaiakLista = FXCollections.observableArrayList();
            osagaiakDisponibleLista = FXCollections.observableArrayList();

            colHornitzaileId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colHornitzaileIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
            colHornitzaileKontaktua.setCellValueFactory(new PropertyValueFactory<>("kontaktua"));
            colHornitzaileHelbidea.setCellValueFactory(new PropertyValueFactory<>("helbidea"));

            colOsagaiIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
            colOsagaiPrezioa.setCellValueFactory(new PropertyValueFactory<>("prezioa"));
            colOsagaiStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

            comboOsagaiak.setItems(osagaiakDisponibleLista);

            tableHornitzaileak.setItems(hornitzaileakLista);
            tableHornitzailearenOsagaiak.setItems(hornitzailearenOsagaiakLista);

            formularioakKonfiguratu();
            filtroakKonfiguratu();
            datuakKargatu();
            bilaketaKonfiguratu();
            botoiakKonfiguratu();
            taulaAukerak();
            formularioHornitzaileaGarbitu();

            LOGGER.info("HornitzaileakController inicializado correctamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errorea initialize-n: " + e.getMessage(), e);
        }
    }

    private void formularioakKonfiguratu() {
        btnSaveHornitzailea.setOnAction(e -> hornitzaileaGorde());
        btnCancelHornitzailea.setOnAction(e -> formularioHornitzaileaGarbitu());
    }

    private void filtroakKonfiguratu() {
        filterOrdenatu.getItems().addAll("ID", "Izena");
        filterOrdenatu.setValue("ID");
        filterOrdenatu.setOnAction(e -> ordenaAplikatu());
    }

    private void bilaketaKonfiguratu() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtroakAplikatu();
            });
        }
    }

    private void botoiakKonfiguratu() {
        btnAddHornitzailea.setOnAction(e -> {
            formularioHornitzaileaGarbitu();
            hornitzaileaEditatzen = null;
        });

        btnDeleteHornitzailea.setOnAction(e -> deleteHornitzailea());

        btnGehituOsagaia.setOnAction(e -> gehituOsagaiaHornitzaileari());

        btnKenduOsagaia.setOnAction(e -> kenduOsagaiaHornitzaileatik());

        if (refreshButton != null) {
            refreshButton.setOnAction(e -> datuakKargatu());
        }
    }

    private void taulaAukerak() {
        tableHornitzaileak.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                kargatuHornitzailea(newVal);
            }
        });
    }

    private void kargatuHornitzailea(Hornitzailea h) {
        hornitzaileaEditatzen = h;
        txtIzena.setText(h.getIzena());
        txtKontaktua.setText(h.getKontaktua());
        txtHelbidea.setText(h.getHelbidea());

        kargatuOsagaiak(h.getId());
    }

    private void kargatuOsagaiak(int hornitzaileaId) {
        new Thread(() -> {
            List<Osagaia> osagaiak = hornitzaileaService.getOsagaiakByHornitzailea(hornitzaileaId);
            Platform.runLater(() -> {
                hornitzailearenOsagaiakLista.setAll(osagaiak);
                osagaiKopuruaLabel.setText(String.valueOf(osagaiak.size()));
            });
        }).start();
    }

    private void formularioHornitzaileaGarbitu() {
        txtIzena.clear();
        txtKontaktua.clear();
        txtHelbidea.clear();
        hornitzaileaEditatzen = null;
        hornitzailearenOsagaiakLista.clear();
    }

    private void hornitzaileaGorde() {
        String izena = txtIzena.getText();
        String kontaktua = txtKontaktua.getText();
        String helbidea = txtHelbidea.getText();

        if (izena.isEmpty() || kontaktua.isEmpty()) {
            mostrarError("Izena eta kontaktua bete behar dira.");
            return;
        }

        Hornitzailea h = (hornitzaileaEditatzen != null) ? hornitzaileaEditatzen : new Hornitzailea();
        h.setIzena(izena);
        h.setKontaktua(kontaktua);
        h.setHelbidea(helbidea);

        boolean isCreate = hornitzaileaEditatzen == null;
        new Thread(() -> {
            boolean success = isCreate
                    ? hornitzaileaService.createHornitzailea(h)
                    : hornitzaileaService.updateHornitzailea(h);
            Platform.runLater(() -> {
                if (success) {
                    datuakKargatu();
                    formularioHornitzaileaGarbitu();
                    mostrarInfo(isCreate ? "Hornitzailea ongi sortu da" : "Hornitzailea ongi eguneratu da");
                } else {
                    mostrarError("Errorea gordetzean.");
                }
            });
        }).start();
    }

    private void deleteHornitzailea() {
        Hornitzailea selected = tableHornitzaileak.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new Thread(() -> {
                boolean success = hornitzaileaService.deleteHornitzailea(selected.getId());
                Platform.runLater(() -> {
                    if (success) {
                        datuakKargatu();
                        formularioHornitzaileaGarbitu();
                        mostrarInfo("Hornitzailea ongi ezabatuta");
                    } else {
                        mostrarError("Errorea ezabatzean.");
                    }
                });
            }).start();
        }
    }

    private void datuakKargatu() {
        new Thread(() -> {
            List<Hornitzailea> h = hornitzaileaService.getHornitzaileak();
            List<Osagaia> o = OsagaiaService.getOsagaiak();
            Platform.runLater(() -> {
                hornitzaileakLista.setAll(h);
                osagaiakDisponibleLista.setAll(o);
                hornitzaileKopuruaLabel.setText(String.valueOf(h.size()));
                filtroakAplikatu();
            });
        }).start();
    }

    private void filtroakAplikatu() {
        String text = (searchField != null) ? searchField.getText().toLowerCase() : "";
        filteredHornitzaileak = new FilteredList<>(hornitzaileakLista);
        filteredHornitzaileak.setPredicate(h -> 
            h.getIzena().toLowerCase().contains(text) || h.getKontaktua().toLowerCase().contains(text)
        );
        tableHornitzaileak.setItems(filteredHornitzaileak);
    }

    private void ordenaAplikatu() {
        String order = filterOrdenatu.getValue();
        if ("Izena".equals(order)) {
            hornitzaileakLista.sort(Comparator.comparing(Hornitzailea::getIzena));
        } else {
            hornitzaileakLista.sort(Comparator.comparing(Hornitzailea::getId));
        }
    }

    private void gehituOsagaiaHornitzaileari() {
        Osagaia o = comboOsagaiak.getValue();
        if (o != null && hornitzaileaEditatzen != null) {
            if (hornitzaileaService.addOsagaiaToHornitzailea(hornitzaileaEditatzen.getId(), o.getId())) {
                kargatuOsagaiak(hornitzaileaEditatzen.getId());
                mostrarInfo("Osagaia gehitu da hornitzaileari");
            }
        }
    }

    private void kenduOsagaiaHornitzaileatik() {
        Osagaia o = tableHornitzailearenOsagaiak.getSelectionModel().getSelectedItem();
        if (o != null && hornitzaileaEditatzen != null) {
            if (hornitzaileaService.removeOsagaiaFromHornitzailea(hornitzaileaEditatzen.getId(), o.getId())) {
                kargatuOsagaiak(hornitzaileaEditatzen.getId());
                mostrarInfo("Osagaia kendu da hornitzailetik");
            }
        }
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        delay.setOnFinished(e -> alert.close());
        delay.play();
    }

    @FXML
    private void atzeraBueltatu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Pantailak/menu-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openChat(ActionEvent event) {
        StageManager.openChatWindow();
    }
}
