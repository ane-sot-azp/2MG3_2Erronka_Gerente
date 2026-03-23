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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterOrdenatu;
    @FXML private Button btnAddHornitzailea, btnDeleteHornitzailea, atzeraBotoia, refreshButton;

    @FXML private Label hornitzaileKopuruaLabel;
    @FXML private Label lblOsagaiKopurua;
    @FXML private Label lblStockTotala;
    @FXML private Label lblInbentarioBalioa;
    @FXML private Label lblStockGutxiKop;
    @FXML private ListView<Osagaia> listStockGutxi;

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

            tableHornitzaileak.setItems(hornitzaileakLista);
            tableHornitzailearenOsagaiak.setItems(hornitzailearenOsagaiakLista);
            tableHornitzaileak.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tableHornitzailearenOsagaiak.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
            List<Osagaia> osagaiakGuztiak = OsagaiaService.getOsagaiak();
            List<Osagaia> osagaiak = new java.util.ArrayList<>();
            for (Osagaia o : osagaiakGuztiak) {
                if (o != null && o.getHornitzaileakId() == hornitzaileaId) {
                    osagaiak.add(o);
                }
            }
            int stockTotala = 0;
            double balioaTotala = 0;
            java.util.List<Osagaia> stockGutxi = new java.util.ArrayList<>();
            for (Osagaia o : osagaiak) {
                stockTotala += o.getStock();
                balioaTotala += o.getStock() * o.getPrezioa();
                if (o.erosiBeharDa()) {
                    stockGutxi.add(o);
                }
            }
            int stockTotalaFinal = stockTotala;
            double balioaTotalaFinal = balioaTotala;
            List<Osagaia> osagaiakFinal = osagaiak;
            List<Osagaia> stockGutxiFinal = stockGutxi;
            Platform.runLater(() -> {
                hornitzailearenOsagaiakLista.setAll(osagaiakFinal);
                if (lblOsagaiKopurua != null) lblOsagaiKopurua.setText(osagaiakFinal.size() + " osagai");
                if (lblStockTotala != null) lblStockTotala.setText(String.valueOf(stockTotalaFinal));
                if (lblInbentarioBalioa != null) lblInbentarioBalioa.setText(String.format(java.util.Locale.US, "%.2f€", balioaTotalaFinal));
                if (lblStockGutxiKop != null) lblStockGutxiKop.setText(String.valueOf(stockGutxiFinal.size()));
                if (listStockGutxi != null) listStockGutxi.getItems().setAll(stockGutxiFinal);
            });
        }).start();
    }

    private void formularioHornitzaileaGarbitu() {
        txtIzena.clear();
        txtKontaktua.clear();
        txtHelbidea.clear();
        hornitzaileaEditatzen = null;
        hornitzailearenOsagaiakLista.clear();
        if (lblOsagaiKopurua != null) lblOsagaiKopurua.setText("0 osagai");
        if (lblStockTotala != null) lblStockTotala.setText("0");
        if (lblInbentarioBalioa != null) lblInbentarioBalioa.setText("0.00€");
        if (lblStockGutxiKop != null) lblStockGutxiKop.setText("0");
        if (listStockGutxi != null) listStockGutxi.getItems().clear();
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
                if (hornitzaileaEditatzen != null) {
                    kargatuOsagaiak(hornitzaileaEditatzen.getId());
                }
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
