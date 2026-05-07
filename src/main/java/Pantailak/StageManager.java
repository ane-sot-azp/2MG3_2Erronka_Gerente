package Pantailak;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import services.SessionContext;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StageManager {

    
    private static final Image APP_ICON =
            new Image(StageManager.class.getResourceAsStream("/icons/app_icon.png"));

    
    private static Image CHAT_ICON = loadChatIcon();

    private static final String APP_CSS =
            StageManager.class.getResource("/css/osis-suite.css").toExternalForm();

    
    private static final String COLOR_FUEGO = "#F3863A";
    private static final String COLOR_OZEANOA = "#1D505B";
    private static final String COLOR_BEIGE = "#C19A6B";
    private static final String COLOR_ZURIA = "#F5F5F5";
    private static final String COLOR_GORRIA = "#5B1C1C";
    private static final String CHAT_SERVER_HOST = resolveChatHost();
    private static final int CHAT_SERVER_PORT = resolveChatPort();
    private static final String CHAT_SHARED_KEY = "OSIS_TXAT_GAKO_2026";
    private static final String ENCRYPTION_PREFIX = "ENC|";
    private static final String FILE_START_PREFIX = "FILE_START|";
    private static final String FILE_CHUNK_PREFIX = "FILE_CHUNK|";
    private static final String FILE_END_PREFIX = "FILE_END|";
    private static final String FILE_CANCEL_PREFIX = "FILE_CANCEL|";
    private static final String FILE_MESSAGE_PREFIX = "FILE_READY|";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int FILE_CHUNK_SIZE = 8 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png", ".txt");

    
    private static Stage floatingStage = null;
    private static StackPane mainContainer = null;
    private static ImageView chatIconView = null;
    private static Circle notificationBadge = null;
    private static Label notificationCount = null;
    private static String erabiltzaileIzena = null;
    private static Stage chatWindow = null;
    private static Stage eguraldiWindow = null;
    private static TxatController currentChatController = null; 
    private static List<String> unreadMessages = new ArrayList<>();
    private static List<String> sessionMessages = new ArrayList<>();
    private static Socket chatSocket = null;
    private static BufferedReader chatReader = null;
    private static PrintWriter chatWriter = null;
    private static boolean isChatServerConnected = false;
    private static boolean isFirstConnection = true;
    private static boolean useFloatingChatButton = true;
    private static final Object chatWriteLock = new Object();
    private static final Map<String, IncomingFileTransfer> incomingFileTransfers =
            Collections.synchronizedMap(new HashMap<>());
    private static final Set<String> outgoingFileTransfers =
            Collections.synchronizedSet(new HashSet<>());

    
    private static boolean isDragging = false;
    private static double dragStartX, dragStartY;
    private static final double DRAG_THRESHOLD = 5.0;

    private StageManager() {}

    

    private static String resolveChatHost() {
        String fromProp = System.getProperty("CHAT_SERVER_HOST");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp.trim();
        }

        String fromEnv = System.getenv("CHAT_SERVER_HOST");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        try {
            String baseUrl = DB.ApiClient.getBaseUrl();
            URI uri = URI.create(baseUrl);
            String host = uri.getHost();
            if (host != null && !host.isBlank()) {
                return host;
            }
        } catch (Exception ignored) {
        }

        return "127.0.0.1";
    }

    private static int resolveChatPort() {
        String fromProp = System.getProperty("CHAT_SERVER_PORT");
        if (fromProp != null && !fromProp.isBlank()) {
            try {
                return Integer.parseInt(fromProp.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String fromEnv = System.getenv("CHAT_SERVER_PORT");
        if (fromEnv != null && !fromEnv.isBlank()) {
            try {
                return Integer.parseInt(fromEnv.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        return 5555;
    }

    private static Image loadChatIcon() {
        try {
            
            Image svgIcon = new Image(StageManager.class.getResourceAsStream("/icons/CHAT.svg"));
            if (!svgIcon.isError()) {
                System.out.println("DEBUG: SVG kargatuta");
                return svgIcon;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Errorea SVG kargatzean: " + e.getMessage());
        }

        try {
            
            Image pngIcon = new Image(StageManager.class.getResourceAsStream("/icons/chat_icon.png"));
            if (!pngIcon.isError()) {
                System.out.println("DEBUG: PNG kargatuta");
                return pngIcon;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Errorea PNG kargatzean: " + e.getMessage());
        }

        
        System.out.println("DEBUG: Ezin izan da ikonoa kargatu, ez dago eskuragarri, emotikonoa kargatuko da ordezkatzeko.");
        return null;
    }

    

    public static void switchToLogin(Stage currentStage) throws IOException {
        hideFloatingChatButton();
        disconnectChatServer();
        sessionMessages.clear();
        unreadMessages.clear();
        currentChatController = null; 
        isFirstConnection = true;
        switchStage(currentStage, "login-view.fxml", "Saioa Hasi", false);
    }

    public static void switchStage(Stage currentStage, String fxml, String title, boolean maximized)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxml));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.getIcons().add(APP_ICON);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(APP_CSS);
        newStage.setScene(scene);

        if (maximized) {
            newStage.setMaximized(true);
        } else {
            newStage.centerOnScreen();
        }

        newStage.setOnCloseRequest(e -> {
            hideFloatingChatButton();
            disconnectChatServer();
            Platform.exit();
            System.exit(0);
        });

        currentStage.close();
        newStage.show();

        if (useFloatingChatButton && erabiltzaileIzena != null) {
            updateFloatingButtonPosition();
        }
    }

    public static Stage openStage(String fxml, String title, boolean maximized, int width, int height)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxml));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.getIcons().add(APP_ICON);

        Scene scene;
        if (maximized) {
            scene = new Scene(root);
            stage.setMaximized(true);
        } else {
            scene = new Scene(root, width, height);
        }

        scene.getStylesheets().add(APP_CSS);
        stage.setScene(scene);
        stage.centerOnScreen();

        return stage;
    }

    

    public static void showFloatingChatButton(String username) {
        erabiltzaileIzena = username;
        useFloatingChatButton = true;

        Platform.runLater(() -> {
            try {
                if (floatingStage != null && floatingStage.isShowing()) {
                    updateFloatingButtonPosition();
                    return;
                }

                createFloatingButton();
                connectToChatServer();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    public static void hideFloatingChatButton() {
        Platform.runLater(() -> {
            if (floatingStage != null) {
                floatingStage.hide();
            }
        });
    }

    public static void showFloatingChatButtonIfHidden() {
        if (useFloatingChatButton && floatingStage != null && !floatingStage.isShowing()) {
            Platform.runLater(() -> {
                floatingStage.show();
                updateFloatingButtonPosition();
            });
        }
    }

    public static void enableHeaderChat(String username) {
        erabiltzaileIzena = username;
        useFloatingChatButton = false;
        Platform.runLater(() -> {
            hideFloatingChatButton();
            connectToChatServer();
        });
    }
    private static void updateFloatingButtonPosition() {
        if (floatingStage != null && floatingStage.isShowing()) {
            Platform.runLater(() -> {
                try {
                    double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                    floatingStage.setX((screenWidth - floatingStage.getWidth()) / 2);
                    floatingStage.setY(20);
                } catch (Exception e) {
                    System.err.println("Errorea kokapena eguneratzean: " + e.getMessage());
                }
            });
        }
    }

    

    private static void connectToChatServer() {
        new Thread(() -> {
            try {
                chatSocket = new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT);
                chatReader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
                chatWriter = new PrintWriter(chatSocket.getOutputStream(), true);
                isChatServerConnected = true;

                
                synchronized (chatWriteLock) {
                    chatWriter.println(erabiltzaileIzena);
                }
                System.out.println("DEBUG: Erabiltzailea bidalita: " + erabiltzaileIzena);

                
                loadSessionMessages();

                
                if (isFirstConnection) {
                    String welcomeMessage = "SISTEMA: " + erabiltzaileIzena + " konektatu da";
                    
                    saveMessageToSession(welcomeMessage);
                    isFirstConnection = false;

                    
                    if (currentChatController != null) {
                        Platform.runLater(() -> {
                            currentChatController.addStyledMessageToContainer(welcomeMessage);
                        });
                    }
                }

                listenToChatServer();

            } catch (IOException e) {
                System.err.println("Errorea zerbitzarira konektaztean: " + e.getMessage());
                isChatServerConnected = false;

                
                String errorMessage = "SISTEMA: Ezin da zerbitzarira konektatu";
                saveMessageToSession(errorMessage);

                
                if (currentChatController != null) {
                    Platform.runLater(() -> {
                        currentChatController.addStyledMessageToContainer(errorMessage);
                    });
                }
            }
        }).start();
    }

    
    private static void listenToChatServer() {
        try {
            String message;
            while (isChatServerConnected && chatSocket != null && chatSocket.isConnected() &&
                    (message = chatReader.readLine()) != null) {

                if (handleIncomingFileMessage(message)) {
                    continue;
                }

                //final String finalMessage = message;

                //publishIncomingMessage(finalMessage);

                final String finalMessage = decryptIncomingMessage(message);
                System.out.println("DEBUG: Mezua jasota: " + finalMessage);

                publishIncomingMessage(finalMessage);
            }
        } catch (IOException e) {
            System.err.println("DEBUG: Zerbitzariarekin konexioa itxita: " + e.getMessage());
        } finally {
            isChatServerConnected = false;
        }
    }
    private static void disconnectChatServer() {
        isChatServerConnected = false;
        try {
            if (chatWriter != null && chatSocket != null && chatSocket.isConnected()) {
                
                
            }
            if (chatWriter != null) chatWriter.close();
            if (chatReader != null) chatReader.close();
            if (chatSocket != null) chatSocket.close();
        } catch (IOException e) {
            
        }
        synchronized (incomingFileTransfers) {
            for (IncomingFileTransfer transfer : incomingFileTransfers.values()) {
                transfer.dispose();
            }
            incomingFileTransfers.clear();
        }
        outgoingFileTransfers.clear();
        saveSessionToFile();
        currentChatController = null; 
    }

    

    public static void sendChatMessage(String message) {
        if (chatWriter != null && isChatServerConnected) {
            try {
                String encryptedMessage = encryptChatMessage(message);
                System.out.println("DEBUG: Mezua bidaltzen: " + encryptedMessage);
                synchronized (chatWriteLock) {
                    chatWriter.println(encryptedMessage);
                }
            } catch (Exception e) {
                String errorMessage = message + " (ezin bidali - zifratze errorea)";
                saveMessageToSession(errorMessage);

                if (currentChatController != null) {
                    Platform.runLater(() -> currentChatController.addStyledMessageToContainer(errorMessage));
                }
            }
        } else {
            
            String errorMessage = message + " (ezin bidali - ez dago konexiorik)";
            saveMessageToSession(errorMessage);

            
            if (currentChatController != null) {
                Platform.runLater(() -> {
                    currentChatController.addStyledMessageToContainer(errorMessage);
                });
            }
        }
    }

    public static void sendSystemMessage(String message) {
        String systemMessage = "SISTEMA: " + message;
        saveMessageToSession(systemMessage);

        Platform.runLater(() -> {
            if (currentChatController != null) {
                currentChatController.addStyledMessageToContainer(systemMessage);
            }
        });
    }

    public static void sendChatFile(File file) {
        if (file == null) {
            return;
        }

        new Thread(() -> sendChatFileInternal(file)).start();
    }

    public static List<String> getSessionMessages() {
        return new ArrayList<>(sessionMessages);
    }

    public static void clearSessionMessages() {
        sessionMessages.clear();
        File sessionFile = new File(getSessionFilePath());
        if (sessionFile.exists()) {
            sessionFile.delete();
        }

        
        if (currentChatController != null) {
            Platform.runLater(() -> {
                currentChatController.messagesContainer.getChildren().clear();
            });
        }
    }

    private static void saveMessageToSession(String message) {
        
        if (!sessionMessages.isEmpty()) {
            String lastMessage = sessionMessages.get(sessionMessages.size() - 1);
            if (lastMessage.equals(message)) {
                return;
            }
        }
        sessionMessages.add(message);
        if (sessionMessages.size() > 1000) {
            sessionMessages.remove(0);
        }
        saveSessionToFile();
    }

    private static void loadSessionMessages() {
        File sessionFile = new File(getSessionFilePath());
        if (sessionFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(sessionFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sessionMessages.add(line);
                }
                System.out.println("DEBUG: " + sessionMessages.size() + " mezu kargatuta sesio fitxategitik");
            } catch (IOException e) {
                System.err.println("Errorea sesioko mezuak kargatzean: " + e.getMessage());
            }
        }
    }

    private static void saveSessionToFile() {
        if (!sessionMessages.isEmpty()) {
            File sessionFile = new File(getSessionFilePath());
            try (PrintWriter writer = new PrintWriter(new FileWriter(sessionFile))) {
                for (String message : sessionMessages) {
                    writer.println(message);
                }
            } catch (IOException e) {
                System.err.println("Errorea mezuak gordetzean: " + e.getMessage());
            }
        }
    }

    private static String getSessionFilePath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        String safeUsername = erabiltzaileIzena != null ?
                erabiltzaileIzena.replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
        return tempDir + "osis_chat_" + safeUsername + ".session";
    }

    private static void publishIncomingMessage(String message) {
        Platform.runLater(() -> {
            saveMessageToSession(message);

            boolean isSystemMessage = message.toLowerCase().contains(" sartu da") ||
                    message.toLowerCase().contains(" atera egin da") ||
                    message.toLowerCase().contains(" konektatu da") ||
                    message.toLowerCase().contains(" deskonektatu da");

            boolean isOwnMessage = erabiltzaileIzena != null && message.startsWith(erabiltzaileIzena + ": ");

            if (!isOwnMessage && !isSystemMessage) {
                if (chatWindow == null || !chatWindow.isShowing()) {
                    addUnreadMessage(message);
                }
            }

            if (currentChatController != null) {
                currentChatController.addStyledMessageToContainer(message);
            }
        });
    }

    private static void publishLocalMessage(String message) {
        saveMessageToSession(message);
        Platform.runLater(() -> {
            if (currentChatController != null) {
                currentChatController.addStyledMessageToContainer(message);
            }
        });
    }

    private static void sendChatFileInternal(File file) {
        if (chatWriter == null || !isChatServerConnected) {
            publishLocalMessage(file.getName() + " (ezin bidali - ez dago konexiorik)");
            return;
        }

        String sanitizedName = sanitizeFileName(file.getName());
        String validationError = validateFile(sanitizedName, file.length());
        if (validationError != null) {
            publishLocalMessage("[Fitxategia baztertua] " + sanitizedName + " (" + validationError + ")");
            return;
        }

        String fileId = UUID.randomUUID().toString().replace("-", "");
        outgoingFileTransfers.add(fileId);

        try (InputStream inputStream = new FileInputStream(file)) {
            synchronized (chatWriteLock) {
                chatWriter.println(FILE_START_PREFIX + fileId + "|" + sanitizedName + "|" + file.length() + "|" + getMimeType(sanitizedName));
            }

            byte[] buffer = new byte[FILE_CHUNK_SIZE];
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                byte[] chunk = bytesRead == buffer.length ? buffer.clone() : java.util.Arrays.copyOf(buffer, bytesRead);
                String chunkBase64 = Base64.getEncoder().encodeToString(chunk);
                String encryptedChunk = encryptChatMessage(chunkBase64);

                synchronized (chatWriteLock) {
                    chatWriter.println(FILE_CHUNK_PREFIX + fileId + "|" + chunkIndex + "|" + encryptedChunk);
                }

                chunkIndex++;
            }

            synchronized (chatWriteLock) {
                chatWriter.println(FILE_END_PREFIX + fileId);
            }

            publishLocalMessage(buildFileChatMessage(erabiltzaileIzena, sanitizedName, formatSize(file.length()), file.getAbsolutePath()));
        } catch (Exception e) {
            try {
                synchronized (chatWriteLock) {
                    if (chatWriter != null) {
                        chatWriter.println(FILE_CANCEL_PREFIX + fileId);
                    }
                }
            } catch (Exception ignored) {
            }

            publishLocalMessage("[Errorea] Ezin izan da fitxategia bidali: " + sanitizedName);
        } finally {
            outgoingFileTransfers.remove(fileId);
        }
    }

    private static boolean handleIncomingFileMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        if (message.startsWith(FILE_START_PREFIX)) {
            return handleIncomingFileStart(message);
        }

        if (message.startsWith(FILE_CHUNK_PREFIX)) {
            return handleIncomingFileChunk(message);
        }

        if (message.startsWith(FILE_END_PREFIX)) {
            return handleIncomingFileEnd(message);
        }

        if (message.startsWith(FILE_CANCEL_PREFIX)) {
            return handleIncomingFileCancel(message);
        }

        return false;
    }

    private static boolean handleIncomingFileStart(String message) {
        String[] parts = message.split("\\|", 6);
        if (parts.length != 6) {
            return true;
        }

        String sender = parts[1];
        String fileId = parts[2];
        String fileName = sanitizeFileName(parts[3]);

        if (outgoingFileTransfers.contains(fileId)) {
            return true;
        }

        long size;
        try {
            size = Long.parseLong(parts[4]);
        } catch (NumberFormatException e) {
            publishIncomingMessage(sender + ": [Fitxategia baztertua] Tamaina baliogabea.");
            return true;
        }

        String validationError = validateFile(fileName, size);
        if (validationError != null) {
            publishIncomingMessage(sender + ": [Fitxategia baztertua] " + fileName + " (" + validationError + ")");
            return true;
        }

        synchronized (incomingFileTransfers) {
            IncomingFileTransfer previous = incomingFileTransfers.remove(fileId);
            if (previous != null) {
                previous.dispose();
            }

            incomingFileTransfers.put(fileId, new IncomingFileTransfer(sender, fileId, fileName, size, parts[5]));
        }

        publishIncomingMessage(sender + ": [Fitxategia jasotzen] " + fileName + " (" + formatSize(size) + ")");
        return true;
    }

    private static boolean handleIncomingFileChunk(String message) {
        String[] parts = message.split("\\|", 5);
        if (parts.length != 5) {
            return true;
        }

        String sender = parts[1];
        String fileId = parts[2];

        if (outgoingFileTransfers.contains(fileId)) {
            return true;
        }

        IncomingFileTransfer transfer;
        synchronized (incomingFileTransfers) {
            transfer = incomingFileTransfers.get(fileId);
        }

        if (transfer == null) {
            return true;
        }

        try {
            String chunkBase64 = decryptChatMessage(parts[4]);
            byte[] chunkBytes = Base64.getDecoder().decode(chunkBase64);
            transfer.write(chunkBytes);

            if (transfer.size() > MAX_FILE_SIZE || transfer.size() > transfer.expectedSize()) {
                throw new IllegalStateException("Fitxategiaren tamaina mugaz kanpo dago");
            }
        } catch (Exception e) {
            removeIncomingTransfer(fileId);
            publishIncomingMessage(sender + ": [Errorea] Ezin izan da fitxategia jaso.");
        }

        return true;
    }

    private static boolean handleIncomingFileEnd(String message) {
        String[] parts = message.split("\\|", 3);
        if (parts.length != 3) {
            return true;
        }

        String sender = parts[1];
        String fileId = parts[2];

        if (outgoingFileTransfers.remove(fileId)) {
            return true;
        }

        IncomingFileTransfer transfer = removeIncomingTransfer(fileId);
        if (transfer == null) {
            return true;
        }

        try {
            if (transfer.size() != transfer.expectedSize()) {
                throw new IllegalStateException("Tamaina ez dator bat");
            }

            File savedFile = saveIncomingFile(transfer);
            publishIncomingMessage(buildFileChatMessage(sender, transfer.fileName(), formatSize(transfer.expectedSize()), savedFile.getAbsolutePath()));
        } catch (Exception e) {
            publishIncomingMessage(sender + ": [Errorea] Ezin izan da fitxategia gorde.");
        } finally {
            transfer.dispose();
        }

        return true;
    }

    private static boolean handleIncomingFileCancel(String message) {
        String[] parts = message.split("\\|", 3);
        if (parts.length != 3) {
            return true;
        }

        if (outgoingFileTransfers.remove(parts[2])) {
            return true;
        }

        IncomingFileTransfer transfer = removeIncomingTransfer(parts[2]);
        if (transfer != null) {
            transfer.dispose();
        }

        publishIncomingMessage(parts[1] + ": [Fitxategia ezeztatuta]");
        return true;
    }

    private static IncomingFileTransfer removeIncomingTransfer(String fileId) {
        synchronized (incomingFileTransfers) {
            return incomingFileTransfers.remove(fileId);
        }
    }

    private static String validateFile(String fileName, long size) {
        String extension = getExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "Fitxategi mota hau ez dago baimenduta.";
        }

        if (size <= 0) {
            return "Fitxategia hutsik dago.";
        }

        if (size > MAX_FILE_SIZE) {
            return "Fitxategiak " + formatSize(MAX_FILE_SIZE) + " baino gehiago dauka.";
        }

        return null;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "fitxategia";
        }

        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index).toLowerCase();
    }

    private static String getMimeType(String fileName) {
        return switch (getExtension(fileName)) {
            case ".pdf" -> "application/pdf";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private static String formatSize(long size) {
        if (size >= 1024L * 1024L) {
            return String.format(java.util.Locale.US, "%.2f MB", size / 1024d / 1024d);
        }
        if (size >= 1024L) {
            return String.format(java.util.Locale.US, "%.2f KB", size / 1024d);
        }
        return size + " B";
    }

    private static String buildFileChatMessage(String sender, String fileName, String size, String path) {
        return sender + ": " + FILE_MESSAGE_PREFIX + fileName + "|" + size + "|" + path;
    }

    private static File saveIncomingFile(IncomingFileTransfer transfer) throws IOException {
        File directory = new File(System.getProperty("user.home"), "Documents/OSIS/TxatFitxategiak");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Ezin izan da karpeta sortu");
        }

        File target = createUniqueFile(directory, transfer.fileName());
        try (OutputStream outputStream = new FileOutputStream(target)) {
            transfer.writeTo(outputStream);
        }
        return target;
    }

    private static File createUniqueFile(File directory, String fileName) {
        File target = new File(directory, fileName);
        if (!target.exists()) {
            return target;
        }

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        for (int i = 1; i <= 999; i++) {
            File candidate = new File(directory, baseName + "_" + i + extension);
            if (!candidate.exists()) {
                return candidate;
            }
        }

        return new File(directory, baseName + "_" + UUID.randomUUID() + extension);
    }


    private static void createFloatingButton() {
        try {
            
            mainContainer = new StackPane();
            mainContainer.setPickOnBounds(false);
            mainContainer.setStyle("-fx-background-color: transparent;");
            mainContainer.setPrefSize(70, 70);
            mainContainer.setMaxSize(70, 70);
            mainContainer.setMinSize(70, 70);

            
            StackPane buttonCircle = new StackPane();
            buttonCircle.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 30;" +
                            "-fx-border-color: " + COLOR_FUEGO + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 30;" +
                            "-fx-pref-width: 60;" +
                            "-fx-pref-height: 60;" +
                            "-fx-min-width: 60;" +
                            "-fx-min-height: 60;" +
                            "-fx-max-width: 60;" +
                            "-fx-max-height: 60;"
            );

            
            if (CHAT_ICON != null && !CHAT_ICON.isError()) {
                chatIconView = new ImageView(CHAT_ICON);
                chatIconView.setFitWidth(30);
                chatIconView.setFitHeight(30);
                chatIconView.setPreserveRatio(true);
                chatIconView.setSmooth(true);

                if (CHAT_ICON.getUrl() != null && CHAT_ICON.getUrl().toLowerCase().endsWith(".svg")) {
                    chatIconView.setStyle(
                            "-fx-effect: dropshadow(gaussian, " + COLOR_FUEGO + ", 1, 0.5, 0, 0);"
                    );
                }

                buttonCircle.getChildren().add(chatIconView);
            } else {
                Label fallbackLabel = new Label("💬");
                fallbackLabel.setStyle(
                        "-fx-text-fill: " + COLOR_FUEGO + ";" +
                                "-fx-font-size: 26px;" +
                                "-fx-font-family: 'Segoe UI';"
                );
                buttonCircle.getChildren().add(fallbackLabel);
            }

            
            StackPane notificationContainer = new StackPane();
            notificationContainer.setStyle("-fx-background-color: transparent;");
            notificationContainer.setPrefSize(24, 24);
            notificationContainer.setMaxSize(24, 24);
            notificationContainer.setMinSize(24, 24);

            
            notificationBadge = new Circle(10);
            notificationBadge.setFill(Color.web(COLOR_GORRIA));
            notificationBadge.setStroke(Color.WHITE);
            notificationBadge.setStrokeWidth(2);
            notificationBadge.setVisible(false);

            
            notificationCount = new Label();
            notificationCount.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 10px;" +
                            "-fx-padding: 0;" +
                            "-fx-alignment: center;"
            );
            notificationCount.setVisible(false);

            
            notificationContainer.getChildren().addAll(notificationBadge, notificationCount);
            StackPane.setAlignment(notificationBadge, Pos.CENTER);
            StackPane.setAlignment(notificationCount, Pos.CENTER);

            
            mainContainer.getChildren().addAll(buttonCircle, notificationContainer);

            
            StackPane.setAlignment(notificationContainer, Pos.TOP_RIGHT);
            StackPane.setMargin(notificationContainer, new Insets(-1, -1, 0, 0));

            
            setupMouseEvents(mainContainer, buttonCircle, notificationContainer);

            
            Tooltip tooltip = new Tooltip("Klik: Ireki txata\nArrastratu: Mugitu");
            tooltip.setStyle(
                    "-fx-background-color: " + COLOR_OZEANOA + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-font-size: 11px;"
            );
            tooltip.setShowDelay(Duration.millis(300));
            Tooltip.install(mainContainer, tooltip);

            
            floatingStage = new Stage();
            floatingStage.initStyle(StageStyle.TRANSPARENT);
            floatingStage.setAlwaysOnTop(true);
            floatingStage.setResizable(false);
            floatingStage.setWidth(75);
            floatingStage.setHeight(75);

            Scene scene = new Scene(mainContainer);
            scene.setFill(null);

            floatingStage.setScene(scene);

            
            updateFloatingButtonPosition();

            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            
            floatingStage.show();
            fadeIn.play();

            
            updateNotificationBadge();

        } catch (Exception e) {
            System.err.println("Errorea botoia sortzean: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupMouseEvents(StackPane container, StackPane buttonCircle, StackPane notificationContainer) {
        container.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            dragStartX = event.getScreenX();
            dragStartY = event.getScreenY();
            isDragging = false;
            applyPressedStyle(buttonCircle);
        });

        container.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double deltaX = Math.abs(event.getScreenX() - dragStartX);
            double deltaY = Math.abs(event.getScreenY() - dragStartY);

            if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                isDragging = true;
                floatingStage.setX(event.getScreenX() - (floatingStage.getWidth() / 2));
                floatingStage.setY(event.getScreenY() - (floatingStage.getHeight() / 2));
                container.setCursor(javafx.scene.Cursor.MOVE);
                applyDraggingStyle(buttonCircle);
            }

            event.consume();
        });

        container.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            applyNormalStyle(buttonCircle);
            container.setCursor(javafx.scene.Cursor.HAND);

            if (!isDragging) {
                openChatWindow();
            }

            isDragging = false;
            adjustToSafePosition();
            event.consume();
        });

        container.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!isDragging) {
                applyHoverStyle(buttonCircle);
                container.setCursor(javafx.scene.Cursor.HAND);
            }
        });

        container.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (!isDragging) {
                applyNormalStyle(buttonCircle);
                container.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        container.setCursor(javafx.scene.Cursor.HAND);
    }

    private static void applyNormalStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + COLOR_FUEGO + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;"
        );
    }

    private static void applyHoverStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.1);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + COLOR_FUEGO + ";" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(243, 134, 58, 0.5), 8, 0, 0, 0);"
        );
    }

    private static void applyPressedStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.2);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #E67E22;" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;"
        );
    }

    private static void applyDraggingStyle(StackPane circle) {
        circle.setStyle(
                "-fx-background-color: rgba(243, 134, 58, 0.15);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #E67E22;" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 30;" +
                        "-fx-pref-width: 60;" +
                        "-fx-pref-height: 60;" +
                        "-fx-min-width: 60;" +
                        "-fx-min-height: 60;" +
                        "-fx-max-width: 60;" +
                        "-fx-max-height: 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231, 126, 34, 0.6), 10, 0, 0, 0);"
        );
    }

    

    public static void openChatWindow() {
        Platform.runLater(() -> {
            try {
                if (!SessionContext.isChatAllowed()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setContentText("Ez duzu txaterako sarbiderik.");
                    alert.showAndWait();
                    return;
                }
                hideFloatingChatButton();

                if (chatWindow != null && chatWindow.isShowing()) {
                    chatWindow.requestFocus();
                    chatWindow.toFront();
                    markAllMessagesAsRead();
                    return;
                }

                FXMLLoader loader = new FXMLLoader(StageManager.class.getResource("txat-view.fxml"));
                Parent root = loader.load();

                TxatController controller = loader.getController();
                currentChatController = controller; 

                controller.initializeWithData(
                        erabiltzaileIzena,
                        getSessionMessages(),
                        message -> sendChatMessage(message),
                        file -> sendChatFile(file)
                );

                chatWindow = new Stage();
                chatWindow.setTitle("OSIS Txat - " + erabiltzaileIzena);
                chatWindow.getIcons().add(APP_ICON);

                Scene scene = new Scene(root);
                scene.getStylesheets().add(APP_CSS);
                chatWindow.setScene(scene);

                chatWindow.setMinWidth(550);
                chatWindow.setMinHeight(450);
                chatWindow.setWidth(600);
                chatWindow.setHeight(500);

                chatWindow.centerOnScreen();

                chatWindow.setOnHiding(e -> {
                    currentChatController = null; 
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            Platform.runLater(() -> {
                                if (useFloatingChatButton) {
                                    showFloatingChatButtonIfHidden();
                                }
                            });
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });

                chatWindow.setOnCloseRequest(e -> {
                    currentChatController = null; 
                    chatWindow = null;
                });

                chatWindow.show();
                markAllMessagesAsRead();

            } catch (Exception e) {
                System.err.println("Errorea txata irekitzen: " + e.getMessage());
                e.printStackTrace();
                if (useFloatingChatButton) {
                    showFloatingChatButtonIfHidden();
                }
            }
        });
    }

    public static void openEguraldiaWindow() {
        Platform.runLater(() -> {
            try {
                if (eguraldiWindow != null && eguraldiWindow.isShowing()) {
                    eguraldiWindow.requestFocus();
                    eguraldiWindow.toFront();
                    return;
                }

                FXMLLoader loader = new FXMLLoader(StageManager.class.getResource("eguraldia-view.fxml"));
                Parent root = loader.load();

                eguraldiWindow = new Stage();
                eguraldiWindow.setTitle("Eguraldia");
                eguraldiWindow.getIcons().add(APP_ICON);

                Scene scene = new Scene(root);
                scene.getStylesheets().add(APP_CSS);
                eguraldiWindow.setScene(scene);

                double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
                eguraldiWindow.setMinWidth(1200);
                eguraldiWindow.setMinHeight(700);
                eguraldiWindow.setWidth(screenWidth * 0.92);
                eguraldiWindow.setHeight(screenHeight * 0.9);
                eguraldiWindow.setResizable(true);
                eguraldiWindow.centerOnScreen();

                eguraldiWindow.setOnCloseRequest(e -> eguraldiWindow = null);
                eguraldiWindow.show();
            } catch (Exception e) {
                System.err.println("Errorea eguraldia irekitzen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    

    public static void addUnreadMessage(String message) {
        
        boolean isOwnMessage = message.startsWith(erabiltzaileIzena + ": ");
        boolean isOwnSystemMessage = message.startsWith("SISTEMA: " + erabiltzaileIzena);

        if (!isOwnMessage && !isOwnSystemMessage) {
            unreadMessages.add(message);
            updateNotificationBadge();

            if (chatWindow == null || !chatWindow.isShowing()) {
                animateNotification();
            }
        }
    }

    public static void markAllMessagesAsRead() {
        unreadMessages.clear();
        updateNotificationBadge();
    }

    private static String decryptIncomingMessage(String message) {
        if (message == null || message.isBlank() || !message.contains(": ")) {
            return message;
        }

        int separatorIndex = message.indexOf(": ");
        String sender = message.substring(0, separatorIndex);
        String payload = message.substring(separatorIndex + 2);

        if (!payload.startsWith(ENCRYPTION_PREFIX)) {
            return message;
        }

        try {
            return sender + ": " + decryptChatMessage(payload);
        } catch (Exception e) {
            return sender + ": [Ezin izan da mezua deszifratu]";
        }
    }

    private static String encryptChatMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        java.security.SecureRandom.getInstanceStrong().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, getSharedKey(), new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return ENCRYPTION_PREFIX
                + Base64.getEncoder().encodeToString(iv)
                + "|"
                + Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decryptChatMessage(String payload) throws Exception {
        String[] parts = payload.split("\\|", 3);
        if (parts.length != 3 || !"ENC".equals(parts[0])) {
            throw new IllegalArgumentException("Mezu zifratua ez da baliozkoa");
        }

        byte[] iv = Base64.getDecoder().decode(parts[1]);
        byte[] encrypted = Base64.getDecoder().decode(parts[2]);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getSharedKey(), new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec getSharedKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(CHAT_SHARED_KEY.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }


    private static void updateNotificationBadge() {
        Platform.runLater(() -> {
            int count = unreadMessages.size();
            boolean hasNotifications = count > 0;

            if (notificationBadge != null && notificationCount != null) {
                notificationBadge.setVisible(hasNotifications);
                notificationCount.setVisible(hasNotifications);

                if (hasNotifications) {
                    String text = count > 9 ? "9+" : String.valueOf(count);
                    notificationCount.setText(text);

                    if (count > 9) {
                        notificationBadge.setRadius(11);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 9px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    } else if (count > 1) {
                        notificationBadge.setRadius(10);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 10px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    } else {
                        notificationBadge.setRadius(10);
                        notificationCount.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-family: 'Segoe UI';" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        );
                    }
                }
            }
        });
    }

    private static void animateNotification() {
        if (notificationBadge != null) {
            Platform.runLater(() -> {
                ScaleTransition pulse1 = new ScaleTransition(Duration.millis(100), notificationBadge);
                pulse1.setToX(1.3);
                pulse1.setToY(1.3);

                ScaleTransition pulse2 = new ScaleTransition(Duration.millis(100), notificationBadge);
                pulse2.setToX(1.0);
                pulse2.setToY(1.0);

                SequentialTransition pulse = new SequentialTransition(pulse1, pulse2);
                pulse.setCycleCount(1);
                pulse.play();
            });
        }
    }

    private static void adjustToSafePosition() {
        if (floatingStage == null) return;

        Platform.runLater(() -> {
            try {
                double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

                double x = floatingStage.getX();
                double y = floatingStage.getY();
                double width = floatingStage.getWidth();
                double height = floatingStage.getHeight();

                if (x < 10) x = 10;
                if (x > screenWidth - width - 10) x = screenWidth - width - 10;
                if (y < 10) y = 10;
                if (y > screenHeight - height - 10) y = screenHeight - height - 10;

                floatingStage.setX(x);
                floatingStage.setY(y);

            } catch (Exception e) {
                System.err.println("Errorea kokapena ezartzen: " + e.getMessage());
            }
        });
    }

    private static final class IncomingFileTransfer {
        private final String sender;
        private final String fileId;
        private final String fileName;
        private final long expectedSize;
        private final String mimeType;
        private final ByteArrayOutputStream buffer;

        private IncomingFileTransfer(String sender, String fileId, String fileName, long expectedSize, String mimeType) {
            this.sender = sender;
            this.fileId = fileId;
            this.fileName = fileName;
            this.expectedSize = expectedSize;
            this.mimeType = mimeType;
            this.buffer = new ByteArrayOutputStream((int) expectedSize);
        }

        private synchronized void write(byte[] chunk) throws IOException {
            buffer.write(chunk);
        }

        private synchronized void writeTo(OutputStream outputStream) throws IOException {
            buffer.writeTo(outputStream);
        }

        private synchronized long size() {
            return buffer.size();
        }

        private long expectedSize() {
            return expectedSize;
        }

        private String fileName() {
            return fileName;
        }

        private void dispose() {
            try {
                buffer.close();
            } catch (IOException ignored) {
            }
        }
    }
}
