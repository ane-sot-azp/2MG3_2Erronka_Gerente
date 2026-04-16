package Pantailak;

import Klaseak.Osagaia;
import Klaseak.Kategoria;
import Klaseak.Produktua;
import Klaseak.ProduktuaOsagaia;
import javafx.beans.property.SimpleStringProperty;
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
import services.ActionLogger;
import services.KategoriaService;
import services.OsagaiaService;
import services.ProduktuaService;
import services.SessionContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduktuakController {
    @FXML private TableView<Produktua> tableProduktuak;
    @FXML private TableColumn<Produktua, Integer> colId;
    @FXML private TableColumn<Produktua, String> colIzena;
    @FXML private TableColumn<Produktua, Double> colPrezioa;
    @FXML private TableColumn<Produktua, String> colMota;
    @FXML private TableColumn<Produktua, Integer> colStock;

    @FXML private TextField searchField;
    @FXML private Label produktuaKopuruaLabel;

    @FXML private TextField txtIzena;
    @FXML private TextField txtPrezioa;
    @FXML private TextField txtMotaId;
    @FXML private TextField txtStock;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnDelete;
    @FXML private Button btnNew;
    @FXML private Button refreshButton;

    @FXML private TableView<ProduktuaOsagaia> tableProduktuaOsagaiak;
    @FXML private TableColumn<ProduktuaOsagaia, String> colOsIzena;
    @FXML private TableColumn<ProduktuaOsagaia, Integer> colOsKant;
    @FXML private TableColumn<ProduktuaOsagaia, Integer> colOsStock;

    @FXML private ComboBox<Osagaia> comboOsagaiak;
    @FXML private TextField txtKantitatea;
    @FXML private Button btnGehituOsagaia;
    @FXML private Button btnEguneratuOsagaia;
    @FXML private Button btnKenduOsagaia;

    private ObservableList<Produktua> produktuakLista = FXCollections.observableArrayList();
    private FilteredList<Produktua> filteredData;
    private ObservableList<ProduktuaOsagaia> osagaiakProduktuan = FXCollections.observableArrayList();
    private ObservableList<Osagaia> osagaiakLista = FXCollections.observableArrayList();

    private Produktua produktuaEditatzen;
    private final KategoriaService kategoriaService = new KategoriaService();
    private final Map<Integer, String> motaIzenaById = new HashMap<>();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colPrezioa.setCellValueFactory(new PropertyValueFactory<>("prezioa"));
        colMota.setCellValueFactory(cell -> {
            Produktua p = cell.getValue();
            String label = p == null ? "" : motaIzenaById.getOrDefault(p.getMotaId(), String.valueOf(p.getMotaId()));
            return new SimpleStringProperty(label);
        });
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colOsIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colOsKant.setCellValueFactory(new PropertyValueFactory<>("kantitatea"));
        colOsStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        tableProduktuak.setItems(produktuakLista);
        tableProduktuaOsagaiak.setItems(osagaiakProduktuan);

        filteredData = new FilteredList<>(produktuakLista, p -> true);
        SortedList<Produktua> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(tableProduktuak.comparatorProperty());
        tableProduktuak.setItems(sorted);

        tableProduktuak.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadProduktua(newV);
        });

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));
        }

        if (refreshButton != null) refreshButton.setOnAction(e -> loadData());
        if (btnNew != null) btnNew.setOnAction(e -> clearForm());
        if (btnSave != null) btnSave.setOnAction(e -> saveProduktua());
        if (btnCancel != null) btnCancel.setOnAction(e -> clearForm());
        if (btnDelete != null) btnDelete.setOnAction(e -> deleteProduktua());

        if (btnGehituOsagaia != null) btnGehituOsagaia.setOnAction(e -> addOsagaia());
        if (btnEguneratuOsagaia != null) btnEguneratuOsagaia.setOnAction(e -> updateOsagaia());
        if (btnKenduOsagaia != null) btnKenduOsagaia.setOnAction(e -> removeOsagaia());

        if (comboOsagaiak != null) comboOsagaiak.setItems(osagaiakLista);

        loadData();
        clearForm();
    }

    private void loadData() {
        new Thread(() -> {
            List<Produktua> produktuak = ProduktuaService.getProduktuak();
            List<Osagaia> osagaiak = OsagaiaService.getOsagaiak();
            List<Kategoria> kategoriak = kategoriaService.getAllKategoriak().join();
            Platform.runLater(() -> {
                produktuakLista.setAll(produktuak);
                osagaiakLista.setAll(osagaiak);
                motaIzenaById.clear();
                for (Kategoria k : kategoriak) {
                    if (k != null) motaIzenaById.put(k.getId(), k.getIzena());
                }
                tableProduktuak.refresh();
                if (produktuaKopuruaLabel != null) produktuaKopuruaLabel.setText(produktuak.size() + " produktu");
                if (produktuaEditatzen != null) {
                    refreshProduktuaOsagaiak(produktuaEditatzen.getId());
                }
            });
        }).start();
    }

    private void applyFilter(String text) {
        String t = text == null ? "" : text.toLowerCase().trim();
        filteredData.setPredicate(p ->
                t.isEmpty() ||
                        (p.getIzena() != null && p.getIzena().toLowerCase().contains(t)) ||
                        String.valueOf(p.getId()).contains(t)
        );
    }

    private void loadProduktua(Produktua p) {
        produktuaEditatzen = p;
        txtIzena.setText(p.getIzena());
        txtPrezioa.setText(String.valueOf(p.getPrezioa()));
        txtMotaId.setText(String.valueOf(p.getMotaId()));
        txtStock.setText(String.valueOf(p.getStock()));
        refreshProduktuaOsagaiak(p.getId());
    }

    private void refreshProduktuaOsagaiak(int produktuaId) {
        new Thread(() -> {
            List<ProduktuaOsagaia> list = ProduktuaService.getOsagaiak(produktuaId);
            Platform.runLater(() -> osagaiakProduktuan.setAll(list));
        }).start();
    }

    private void clearForm() {
        produktuaEditatzen = null;
        txtIzena.clear();
        txtPrezioa.clear();
        txtMotaId.clear();
        txtStock.clear();
        osagaiakProduktuan.clear();
        if (comboOsagaiak != null) comboOsagaiak.getSelectionModel().clearSelection();
        if (txtKantitatea != null) txtKantitatea.clear();
        tableProduktuak.getSelectionModel().clearSelection();
    }

    private void saveProduktua() {
        try {
            String izena = txtIzena.getText() != null ? txtIzena.getText().trim() : "";
            if (izena.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Izena beharrezkoa da");
                return;
            }
            double prezioa = Double.parseDouble(txtPrezioa.getText().replace(',', '.'));
            int motaId = Integer.parseInt(txtMotaId.getText());
            int stock = Integer.parseInt(txtStock.getText());

            boolean isCreate = (produktuaEditatzen == null);
            Produktua p = isCreate ? new Produktua() : produktuaEditatzen;
            p.setIzena(izena);
            p.setPrezioa(prezioa);
            p.setMotaId(motaId);
            p.setStock(stock);

            new Thread(() -> {
                boolean success;
                if (isCreate) {
                    Produktua created = ProduktuaService.createProduktua(p);
                    success = created != null;
                } else {
                    success = ProduktuaService.updateProduktua(p);
                }
                boolean successFinal = success;
                Platform.runLater(() -> {
                    if (successFinal) {
                        ActionLogger.log(
                                SessionContext.getCurrentUser(),
                                isCreate ? "INSERT" : "UPDATE",
                                "produktuak",
                                (isCreate ? "Produktua sortu: " : "Produktua eguneratu: ") + izena
                        );
                        loadData();
                        if (isCreate) clearForm();
                        showAlert(Alert.AlertType.INFORMATION, isCreate ? "Produktua sortuta" : "Produktua eguneratuta");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Errorea produktua gordetzean");
                    }
                });
            }).start();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Zenbaki baliagarriak sartu behar dira (prezioa/motaId/stock)");
        }
    }

    private void deleteProduktua() {
        Produktua selected = tableProduktuak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu produktua ezabatzeko");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Ziur zaude produktua ezabatu nahi duzula?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        new Thread(() -> {
            boolean success = ProduktuaService.deleteProduktua(selected.getId());
            Platform.runLater(() -> {
                if (success) {
                    ActionLogger.log(SessionContext.getCurrentUser(), "DELETE", "produktuak", "Produktua ezabatuta: " + selected.getIzena());
                    loadData();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Produktua ezabatuta");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errorea produktua ezabatzean");
                }
            });
        }).start();
    }

    private void addOsagaia() {
        if (produktuaEditatzen == null) {
            showAlert(Alert.AlertType.WARNING, "Lehenengo produktua gorde");
            return;
        }
        Osagaia o = comboOsagaiak.getValue();
        if (o == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu osagaia");
            return;
        }
        int kant;
        try {
            kant = Integer.parseInt(txtKantitatea.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Kantitatea zenbakia izan behar da");
            return;
        }

        new Thread(() -> {
            boolean success = ProduktuaService.addOsagaia(produktuaEditatzen.getId(), o.getId(), kant);
            Platform.runLater(() -> {
                if (success) {
                    refreshProduktuaOsagaiak(produktuaEditatzen.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Osagaia gehituta");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errorea osagaia gehitzean");
                }
            });
        }).start();
    }

    private void updateOsagaia() {
        if (produktuaEditatzen == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu produktua");
            return;
        }
        ProduktuaOsagaia selected = tableProduktuaOsagaiak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu osagaia taulan");
            return;
        }
        int kant;
        try {
            kant = Integer.parseInt(txtKantitatea.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Kantitatea zenbakia izan behar da");
            return;
        }
        int osagaiaId = selected.getOsagaiaId();

        new Thread(() -> {
            boolean success = ProduktuaService.updateOsagaia(produktuaEditatzen.getId(), osagaiaId, kant);
            Platform.runLater(() -> {
                if (success) {
                    refreshProduktuaOsagaiak(produktuaEditatzen.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Osagaia eguneratuta");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errorea osagaia eguneratzean");
                }
            });
        }).start();
    }

    private void removeOsagaia() {
        if (produktuaEditatzen == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu produktua");
            return;
        }
        ProduktuaOsagaia selected = tableProduktuaOsagaiak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aukeratu osagaia taulan");
            return;
        }
        int osagaiaId = selected.getOsagaiaId();

        new Thread(() -> {
            boolean success = ProduktuaService.removeOsagaia(produktuaEditatzen.getId(), osagaiaId);
            Platform.runLater(() -> {
                if (success) {
                    refreshProduktuaOsagaiak(produktuaEditatzen.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Osagaia kenduta");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errorea osagaia kentzean");
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        if (type == Alert.AlertType.INFORMATION) {
            alert.show();
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(e -> alert.close());
            delay.play();
        } else {
            alert.showAndWait();
        }
    }

    @FXML
    private void atzeraBueltatu(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            StageManager.switchStage(currentStage, "menu-view.fxml", "Menu Nagusia", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openChat(ActionEvent event) {
        StageManager.openChatWindow();
    }

    @FXML
    private void openEguraldia(ActionEvent event) {
        StageManager.openEguraldiaWindow();
    }
}
