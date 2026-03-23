package Pantailak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Klaseak.Erabiltzailea;
import Klaseak.Langilea;
import Klaseak.Lanpostua;
import services.ActionLogger;
import services.ErabiltzaileaService;
import services.LangileaService;
import services.SessionContext;
import DB.ApiClient;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class LangileakController {

    @FXML
    private TableView<Langilea> tableLangileak;
    @FXML
    private TableColumn<Langilea, Integer> colId;
    @FXML
    private TableColumn<Langilea, String> colIzena;
    @FXML
    private TableColumn<Langilea, String> colAbizena;
    @FXML
    private TableColumn<Langilea, String> colNAN;
    @FXML
    private TableColumn<Langilea, Integer> colKodea;
    @FXML
    private TableColumn<Langilea, String> colLanpostua;
    @FXML
    private TableColumn<Langilea, String> colErabiltzaileIzena;
    @FXML
    private TableColumn<Langilea, String> colHelbidea;

    @FXML
    private TextField txtIzena, txtAbizena, txtNAN, txtLangileKodea, txtHelbidea;
    @FXML
    private ComboBox<Lanpostua> comboLanpostu;
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPass;
    @FXML
    private Button btnSave, btnCancel, btnResetPass;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> lanpostuFilter;
    @FXML
    private ComboBox<String> ordenatuFilter;
    @FXML
    private Label langileKopuruaLabel;

    @FXML
    private Label totalLangileakLabel, sukaldariakLabel, zerbitzariakLabel, adminLabel;

    @FXML
    private Button btnAdd, btnEdit, btnDelete, atzeraBotoia, refreshButton;

    private ObservableList<Langilea> langileakLista;
    private FilteredList<Langilea> filteredData;
    private Langilea langileaEditatzen;

    @FXML
    public void initialize() {
        System.out.println("initialize() deituta");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colAbizena.setCellValueFactory(new PropertyValueFactory<>("abizena"));
        colNAN.setCellValueFactory(new PropertyValueFactory<>("nan"));
        colKodea.setCellValueFactory(new PropertyValueFactory<>("langileKodea"));
        colErabiltzaileIzena.setCellValueFactory(new PropertyValueFactory<>("erabiltzaileIzena"));
        colHelbidea.setCellValueFactory(new PropertyValueFactory<>("helbidea"));
        colLanpostua.setCellValueFactory(new PropertyValueFactory<>("lanpostuaName"));

        comboLanpostu.getItems().setAll(LangileaService.getLanpostuak());

        formularioaKonfiguratu();
        filtroakKonfiguratu();
        taulaBirkargatu();
        bilaketaKonfiguratu();
        botoiakKonfiguratu();
        taulaAukera();
        formularioaGarbitu();
    }

    private void formularioaKonfiguratu() {
        btnSave.setOnAction(e -> langileaGorde());
        btnCancel.setOnAction(e -> formularioaGarbitu());
        if (btnResetPass != null) {
            btnResetPass.setOnAction(e -> resetPasahitza());
        }
    }

    @FXML
    private void openChat(ActionEvent event) {
        StageManager.openChatWindow();
    }
    private void resetPasahitza() {
        Langilea selected = tableLangileak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Aukeratu langile bat pasahitza reset egiteko");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Pasahitza");
        dialog.setHeaderText("Pasahitza berria");
        dialog.setContentText("Sartu pasahitza berria:");

        dialog.showAndWait().ifPresent(newPass -> {
            String pass = newPass != null ? newPass.trim() : "";
            if (pass.isEmpty()) {
                mostrarAlerta("Pasahitza ezin da hutsik egon");
                return;
            }
            if (!pass.matches("\\d+")) {
                mostrarAlerta("Pasahitzak zenbaki dezimalez bakarrik konposatuta egon behar da");
                return;
            }

            Stage loadingStage = showLoadingStage("Pasahitza aldatzen...");
            setButtonsDisabled(true);

            new Thread(() -> {
                selected.setPasahitza(pass);
                boolean success = LangileaService.update(selected);
                if (success) {
                    ActionLogger.log(
                            SessionContext.getCurrentUser(),
                            "UPDATE",
                            "langileak",
                            "Pasahitza reset: " + selected.getIzena() + " (ID=" + selected.getId() + ")"
                    );
                }
                Platform.runLater(() -> {
                    try { loadingStage.close(); } catch (Exception ignored) {}
                    setButtonsDisabled(false);

                    if (success) {
                        txtPass.clear();
                        taulaBirkargatu();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Pasahitza ongi aldatu da");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Errorea pasahitza aldatzean (begiratu kontsola HTTP errorea ikusteko)");
                        alert.showAndWait();
                    }
                });
            }).start();
        });
    }

    private Stage showLoadingStage(String message) {
        Stage owner = (Stage) tableLangileak.getScene().getWindow();
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Mesedez itxaron");

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        Label label = new Label(message);

        VBox root = new VBox(12, indicator, label);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 320, 160));
        stage.setResizable(false);
        stage.show();
        stage.centerOnScreen();
        return stage;
    }

    private void setButtonsDisabled(boolean disabled) {
        if (btnSave != null) btnSave.setDisable(disabled);
        if (btnCancel != null) btnCancel.setDisable(disabled);
        if (btnResetPass != null) btnResetPass.setDisable(disabled);
        if (btnAdd != null) btnAdd.setDisable(disabled);
        if (btnDelete != null) btnDelete.setDisable(disabled);
        if (refreshButton != null) refreshButton.setDisable(disabled);
    }

    private void filtroakKonfiguratu() {
        lanpostuFilter.getItems().addAll("Guztiak", "Sukaldaria", "Zerbitzaria", "Administratzailea", "Gerentea");
        lanpostuFilter.setValue("Guztiak");

        ordenatuFilter.getItems().addAll("ID", "Izena", "Abizena", "Lanpostua");
        ordenatuFilter.setValue("ID");

        lanpostuFilter.setOnAction(e -> filtroakAplikatu());
        ordenatuFilter.setOnAction(e -> ordenaAplikatu());
    }

    private void bilaketaKonfiguratu() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtroakAplikatu();
            });
        }
    }

    private void botoiakKonfiguratu() {
        btnAdd.setOnAction(e -> {
            formularioaGarbitu();
            langileaEditatzen = null;
        });

        btnDelete.setOnAction(e -> deleteSelected());

        if (refreshButton != null) {
            refreshButton.setOnAction(e -> taulaBirkargatu());
        }
    }

    private void taulaAukera() {
        tableLangileak.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        kargatuFormularioa(newSelection);
                    }
                });
    }

    private void kargatuFormularioa(Langilea langilea) {
        langileaEditatzen = langilea;

        txtIzena.setText(langilea.getIzena());
        txtAbizena.setText(langilea.getAbizena());
        txtNAN.setText(langilea.getNan());
        txtLangileKodea.setText(String.valueOf(langilea.getLangileKodea()));
        txtHelbidea.setText(langilea.getHelbidea());
        txtUser.setText(langilea.getErabiltzaileIzena());
        txtPass.clear(); // Segurtasunagatik ez dugu pasahitza kargatzen

        comboLanpostu.setValue(langilea.getLanpostua());
    }

    private void formularioaGarbitu() {
        langileaEditatzen = null;

        txtIzena.clear();
        txtAbizena.clear();
        txtNAN.clear();
        txtLangileKodea.clear();
        txtHelbidea.clear();
        txtUser.clear();
        txtPass.clear();
        comboLanpostu.setValue(null);
    }

    private void langileaGorde() {
        try {
            String izena = txtIzena.getText();
            String abizena = txtAbizena.getText();
            String nan = txtNAN.getText();
            String kodeaStr = txtLangileKodea.getText();
            String helbidea = txtHelbidea.getText();
            String user = txtUser.getText();
            String pass = txtPass.getText();
            Lanpostua lanpostua = comboLanpostu.getValue();

            if (izena.isEmpty() || abizena.isEmpty() || nan.isEmpty() || kodeaStr.isEmpty() || lanpostua == null) {
                mostrarAlerta("Bete beharrezko eremu guztiak (*)");
                return;
            }

            if (user.isEmpty()) {
                mostrarAlerta("Erabiltzaile izena beharrezkoa da");
                return;
            }

            if (langileaEditatzen == null) {
                if (pass.isEmpty()) {
                    mostrarAlerta("Pasahitza beharrezkoa da langile berria sortzeko");
                    return;
                }
                if (!pass.matches("\\d+")) {
                    mostrarAlerta("Pasahitza zenbaki dezimalak bakarrik izan behar dira");
                    return;
                }
            } else if (!pass.isEmpty() && !pass.matches("\\d+")) {
                mostrarAlerta("Pasahitza zenbaki dezimalak bakarrik izan behar dira");
                return;
            }

            int kodea;
            try {
                kodea = Integer.parseInt(kodeaStr);
            } catch (NumberFormatException e) {
                mostrarAlerta("Kodea zenbakia izan behar da");
                return;
            }

            Langilea l = (langileaEditatzen != null) ? langileaEditatzen : new Langilea();
            l.setIzena(izena);
            l.setAbizena(abizena);
            l.setNan(nan);
            l.setLangileKodea(kodea);
            l.setHelbidea(helbidea);
            l.setErabiltzaileIzena(user);
            l.setLanpostua(lanpostua);
            if (!pass.isEmpty()) {
                l.setPasahitza(pass);
            }

            boolean isCreate = (langileaEditatzen == null);
            Stage loadingStage = showLoadingStage(isCreate ? "Langilea sortzen..." : "Langilea eguneratzen...");
            setButtonsDisabled(true);

            new Thread(() -> {
                boolean success;
                if (isCreate) {
                    success = LangileaService.create(l) != null;
                    if (success) ActionLogger.log(SessionContext.getCurrentUser(), "INSERT", "langileak", "Langile berria: " + l.getIzena());
                } else {
                    success = LangileaService.update(l);
                    if (success) ActionLogger.log(SessionContext.getCurrentUser(), "UPDATE", "langileak", "Langilea eguneratua: " + l.getIzena());
                }

                boolean successFinal = success;
                String err = successFinal ? "" : services.LangileaService.getLastUpdateError();
                String errFinal = (err == null || err.isEmpty()) ? "Errore ezezaguna" : err;

                Platform.runLater(() -> {
                    try { loadingStage.close(); } catch (Exception ignored) {}
                    setButtonsDisabled(false);

                    if (successFinal) {
                        taulaBirkargatu();
                        formularioaGarbitu();
                        mostrarAlerta(isCreate ? "Langile berria ongi sortu da" : "Langilea ongi eguneratu da");
                    } else {
                        mostrarAlerta("Errorea gordetzean:\n" + errFinal);
                    }
                });
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Errorea gordetzean: " + e.getMessage());
        }
    }

    private void deleteSelected() {
        Langilea selected = tableLangileak.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta("Aukeratu langile bat ezabatzeko");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ziur zaude " + selected.getIzena() + " ezabatu nahi duzula?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            Stage loadingStage = showLoadingStage("Erabiltzailea ezabatzen...");
            setButtonsDisabled(true);

            new Thread(() -> {
                boolean success = LangileaService.deleteLangile(selected.getId());
                if (success) ActionLogger.log(SessionContext.getCurrentUser(), "DELETE", "langileak", "Langilea ezabatua: " + selected.getIzena());

                Platform.runLater(() -> {
                    try { loadingStage.close(); } catch (Exception ignored) {}
                    setButtonsDisabled(false);

                    if (success) {
                        taulaBirkargatu();
                        formularioaGarbitu();
                        mostrarAlerta("Erabiltzailea ongi ezabatu da");
                    } else {
                        mostrarAlerta("Errorea ezabatzean");
                    }
                });
            }).start();
        }
    }

    private void taulaBirkargatu() {
        setButtonsDisabled(true);

        new Thread(() -> {
            List<Langilea> langileak = LangileaService.getAll();
            String errorMsg = null;

            if (langileak.isEmpty()) {
                try {
                    var res = ApiClient.get("/api/langileak");
                    if (res.statusCode() != 200) {
                        errorMsg = "Ezin izan dira langileak kargatu.\nAPI: " + ApiClient.getBaseUrl() + "\nHTTP: " + res.statusCode() + "\n" + (res.body() != null ? res.body() : "");
                    } else if (res.body() == null || !res.body().trim().startsWith("[")) {
                        String body = res.body() != null ? res.body() : "";
                        String snippet = body.length() > 300 ? body.substring(0, 300) + "..." : body;
                        errorMsg = "Ezin izan dira langileak kargatu.\nAPI: " + ApiClient.getBaseUrl() + "\nErantzuna ez da zerrenda bat.\n" + snippet;
                    } else {
                        String body = res.body() != null ? res.body() : "";
                        String snippet = body.length() > 300 ? body.substring(0, 300) + "..." : body;
                        String debug = LangileaService.getLastGetAllDebug();
                        if (debug.length() > 800) debug = debug.substring(0, 800) + "...";
                        errorMsg = "Langileak ez dira erakusten.\nAPI: " + ApiClient.getBaseUrl() + "\nHTTP: " + res.statusCode() + "\nParse:\n" + debug + "\nErantzuna:\n" + snippet;
                    }
                } catch (Exception e) {
                    errorMsg = "Ezin izan dira langileak kargatu.\nAPI: " + ApiClient.getBaseUrl() + "\n" + e.getMessage();
                }
            }

            String finalErrorMsg = errorMsg;
            Platform.runLater(() -> {
                setButtonsDisabled(false);

                if (finalErrorMsg != null) {
                    mostrarAlerta(finalErrorMsg);
                }

                langileakLista = FXCollections.observableArrayList(langileak);
                filteredData = new FilteredList<>(langileakLista, p -> true);

                SortedList<Langilea> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(tableLangileak.comparatorProperty());
                tableLangileak.setItems(sortedData);

                langileKopuruaLabel.setText(langileak.size() + " langile");
                filtroakAplikatu();
                estatistikakEguneratu();
            });
        }).start();
    }

    private void filtroakAplikatu() {
        String filter = (lanpostuFilter.getValue() != null) ? lanpostuFilter.getValue() : "Guztiak";
        String searchText = (searchField != null) ? searchField.getText().toLowerCase() : "";

        filteredData.setPredicate(langilea -> {
            String lanpostuName = langilea.getLanpostuaName();
            boolean matchesFilter = filter.equals("Guztiak") || (lanpostuName != null && lanpostuName.equals(filter));
            boolean matchesSearch = searchText.isEmpty() || 
                                   langilea.getIzena().toLowerCase().contains(searchText) ||
                                   langilea.getAbizena().toLowerCase().contains(searchText) ||
                                   langilea.getErabiltzaileIzena().toLowerCase().contains(searchText) ||
                                   langilea.getHelbidea().toLowerCase().contains(searchText) ||
                                   String.valueOf(langilea.getLangileKodea()).contains(searchText) ||
                                   langilea.getNan().toLowerCase().contains(searchText);
            return matchesFilter && matchesSearch;
        });
    }

    private void ordenaAplikatu() {
        String orden = ordenatuFilter.getValue();
        Comparator<Langilea> comparator;

        switch (orden) {
            case "Izena":
                comparator = Comparator.comparing(Langilea::getIzena);
                break;
            case "Abizena":
                comparator = Comparator.comparing(Langilea::getAbizena);
                break;
            case "Lanpostua":
                comparator = Comparator.comparing(Langilea::getLanpostuaName);
                break;
            default:
                comparator = Comparator.comparing(Langilea::getId);
        }

        langileakLista.sort(comparator);
    }

    private void estatistikakEguneratu() {
        if (totalLangileakLabel != null) {
            totalLangileakLabel.setText(String.valueOf(langileakLista.size()));
            sukaldariakLabel.setText(String.valueOf(langileakLista.stream().filter(l -> l.getLanpostuaName().equals("Sukaldaria")).count()));
            zerbitzariakLabel.setText(String.valueOf(langileakLista.stream().filter(l -> l.getLanpostuaName().equals("Zerbitzaria")).count()));
            adminLabel.setText(String.valueOf(langileakLista.stream().filter(l -> l.getLanpostuaName().equals("Administratzailea") || l.getLanpostuaName().equals("Gerentea")).count()));
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(mensaje);
        alert.show();
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
}
