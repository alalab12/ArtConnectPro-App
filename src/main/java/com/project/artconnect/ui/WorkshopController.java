package com.project.artconnect.ui;

import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkshopController {

    @FXML private TableView<Workshop>           workshopTable;
    @FXML private TableColumn<Workshop,String>  titleColumn, instructorColumn, locationColumn, levelColumn;
    @FXML private TableColumn<Workshop,LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop,Double>  priceColumn;
    @FXML private TableColumn<Workshop,Integer> spotsColumn;

    @FXML private Label detailTitle, detailInstructor, detailDate, detailDuration;
    @FXML private Label detailLocation, detailLevel, detailPrice, detailSpots, detailDesc;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        spotsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMaxParticipants()).asObject());
        instructorColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getInstructor() != null ? c.getValue().getInstructor().getName() : "TBD"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(FMT));
            }
        });

        // Color level
        levelColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(switch (v) {
                    case "Beginner"    -> "-fx-text-fill:#2e7d32;-fx-font-weight:bold;";
                    case "Intermediate"-> "-fx-text-fill:#f57f17;-fx-font-weight:bold;";
                    case "Advanced"    -> "-fx-text-fill:#c62828;-fx-font-weight:bold;";
                    default            -> "";
                });
            }
        });

        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    @FXML public void handleTableClick() {
        Workshop w = workshopTable.getSelectionModel().getSelectedItem();
        if (w == null) return;
        detailTitle.setText(w.getTitle());
        detailInstructor.setText("Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "TBD")
                + (w.getInstructor() != null && w.getInstructor().getCity() != null ? " (" + w.getInstructor().getCity() + ")" : ""));
        detailDate.setText("Date: " + (w.getDate() != null ? w.getDate().format(FMT) : "—"));
        detailDuration.setText("Duration: " + w.getDurationMinutes() + " min");
        detailLocation.setText("Location: " + nvl(w.getLocation()));
        detailLevel.setText("Level: " + nvl(w.getLevel()));
        detailPrice.setText("Price: " + w.getPrice() + " €");
        detailSpots.setText("Max participants: " + w.getMaxParticipants());
        detailDesc.setText(w.getDescription() != null && !w.getDescription().isEmpty() ? w.getDescription() : "—");
    }

    private static String nvl(String s) { return s != null ? s : "—"; }
}
