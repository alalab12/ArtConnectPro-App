package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryController {

    @FXML private TableView<Gallery>           galleryTable;
    @FXML private TableColumn<Gallery,String>  nameColumn, addressColumn, ownerColumn;
    @FXML private TableColumn<Gallery,Double>  ratingColumn;

    @FXML private Label detailName, detailAddress, detailOwner, detailHours, detailPhone, detailRating, detailWebsite;
    @FXML private ListView<String> exhibitionList;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        ratingColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRating()).asObject());

        // Color-code rating
        ratingColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v + " ★");
                setStyle(v >= 4.5 ? "-fx-text-fill:#2e7d32;-fx-font-weight:bold;"
                        : v >= 4.0 ? "-fx-text-fill:#f57f17;" : "-fx-text-fill:#c62828;");
            }
        });

        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    @FXML public void handleTableClick() {
        Gallery g = galleryTable.getSelectionModel().getSelectedItem();
        if (g == null) return;
        detailName.setText(g.getName());
        detailAddress.setText("Address: " + nvl(g.getAddress()));
        detailOwner.setText("Owner: " + nvl(g.getOwnerName()));
        detailHours.setText("Hours: " + nvl(g.getOpeningHours()));
        detailPhone.setText("Phone: " + nvl(g.getContactPhone()));
        detailRating.setText("Rating: " + g.getRating() + " / 5.0");
        detailWebsite.setText("Website: " + nvl(g.getWebsite()));

        List<String> expos = g.getExhibitions().stream()
                .map(e -> "• " + e.getTitle()
                        + (e.getStartDate() != null ? "  [" + e.getStartDate() + " → " + e.getEndDate() + "]" : "")
                        + "\n  Theme: " + nvl(e.getTheme())
                        + " | Curator: " + nvl(e.getCuratorName())
                        + " | " + e.getArtworks().size() + " artwork(s)")
                .collect(Collectors.toList());
        exhibitionList.setItems(FXCollections.observableArrayList(
                expos.isEmpty() ? List.of("No exhibitions") : expos));
    }

    private static String nvl(String s) { return s != null ? s : "—"; }
}
