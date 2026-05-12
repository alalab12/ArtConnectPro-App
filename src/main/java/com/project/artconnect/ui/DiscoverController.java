package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();

    @FXML
    public void initialize() {
        refresh();
    }

    public void refresh() {
        discoverPane.getChildren().clear();

        List<Exhibition> exhibitions = new ArrayList<>();
        for (Gallery g : galleryService.getAllGalleries()) {
            exhibitions.addAll(g.getExhibitions());
            if (exhibitions.size() >= 3) break;
        }
        exhibitions.stream().limit(3).forEach(this::addExhibitionCard);
        workshopService.getAllWorkshops().stream().limit(3).forEach(this::addWorkshopCard);
    }

    private VBox createCard(String accent) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color:white;" +
                "-fx-border-color:" + accent + ";" +
                "-fx-border-width:0 0 0 4;" +
                "-fx-border-radius:6;" +
                "-fx-background-radius:6;");
        DropShadow shadow = new DropShadow(10, 3, 3, Color.gray(0.25));
        card.setEffect(shadow);
        return card;
    }

    private Label badge(String text, String bg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;" +
                "-fx-font-size:10;-fx-font-weight:bold;" +
                "-fx-padding:2 6;-fx-background-radius:3;");
        return l;
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = createCard("#7c2d2d");
        Label title = new Label(e.getTitle());
        title.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-wrap-text:true;");
        title.setWrapText(true);
        String dates = (e.getStartDate() != null ? e.getStartDate() : "?")
                + " → " + (e.getEndDate() != null ? e.getEndDate() : "?");
        Label gallery = new Label("📍 " + (e.getGallery() != null ? e.getGallery().getName() : "—"));
        gallery.setStyle("-fx-text-fill:#555;");
        card.getChildren().addAll(badge("EXPOSITION", "#7c2d2d"), title, new Label(dates), gallery);
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = createCard("#43a047");
        Label title = new Label(w.getTitle());
        title.setStyle("-fx-font-weight:bold;-fx-font-size:14;-fx-wrap-text:true;");
        title.setWrapText(true);
        Label instructor = new Label("👤 " + (w.getInstructor() != null ? w.getInstructor().getName() : "—"));
        instructor.setStyle("-fx-text-fill:#555;");
        Label price = new Label("💶 " + w.getPrice() + " €");
        price.setStyle("-fx-font-weight:bold;-fx-text-fill:#43a047;");
        card.getChildren().addAll(badge("ATELIER", "#43a047"), title, instructor, price);
        discoverPane.getChildren().add(card);
    }
}
