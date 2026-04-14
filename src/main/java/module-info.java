module org.example._erronka_java {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.desktop;
    requires java.net.http;
    requires java.sql;

    requires com.google.gson;
    requires net.synedra.validatorfx;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires org.apache.pdfbox;
    requires tools.jackson.databind;
    requires unirest.java;

    opens Pantailak to javafx.fxml;
    opens Klaseak to javafx.base, com.google.gson;
    opens css;
    opens services to com.google.gson;
    opens icons;

    exports Klaseak;
    exports Pantailak;
    exports services;
    exports DB;

}
