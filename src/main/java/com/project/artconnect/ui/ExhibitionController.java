package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExhibitionController {

    @FXML private TableView<Exhibition>           exhibitionTable;
    @FXML private TableColumn<Exhibition,String>  titleColumn, galleryColumn, curatorColumn, themeColumn;
    @FXML private TableColumn<Exhibition,LocalDate> startColumn, endColumn;

    @FXML private Label detailTitle, detailGallery, detailCurator, detailTheme, detailDates, detailDesc;
    @FXML private ListView<String> artworkList;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        curatorColumn.setCellValueFactory(new PropertyValueFactory<>("curatorName"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        galleryColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGallery() != null ? c.getValue().getGallery().getName() : "Unknown"));

        // Color ongoing vs future exhibitions
        titleColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                Exhibition e = getTableView().getItems().get(getIndex());
                LocalDate now = LocalDate.now();
                if (e.getStartDate() != null && e.getEndDate() != null) {
                    if (now.isAfter(e.getStartDate()) && now.isBefore(e.getEndDate()))
                        setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;"); // ongoing = green
                    else if (now.isBefore(e.getStartDate()))
                        setStyle("-fx-text-fill:#1565c0;"); // future = blue
                    else setStyle("-fx-text-fill:#999;"); // past = grey
                }
            }
        });

        refreshData();
    }

    @FXML public void handleTableClick() {
        Exhibition e = exhibitionTable.getSelectionModel().getSelectedItem();
        if (e == null) return;
        detailTitle.setText(e.getTitle());
        detailGallery.setText("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "—"));
        detailCurator.setText("Curator: " + nvl(e.getCuratorName()));
        detailTheme.setText("Theme: " + nvl(e.getTheme()));
        String dates = (e.getStartDate() != null ? e.getStartDate().toString() : "?")
                + " → " + (e.getEndDate() != null ? e.getEndDate().toString() : "?");
        detailDates.setText("Dates: " + dates);
        detailDesc.setText(e.getDescription() != null && !e.getDescription().isEmpty() ? e.getDescription() : "—");
        List<String> works = e.getArtworks().stream()
                .map(a -> "• " + a.getTitle() + (a.getArtist() != null ? " by " + a.getArtist().getName() : ""))
                .collect(Collectors.toList());
        artworkList.setItems(FXCollections.observableArrayList(works.isEmpty() ? List.of("No artworks listed") : works));
    }

    private void refreshData() {
        List<Exhibition> all = new ArrayList<>();
        for (Gallery g : galleryService.getAllGalleries()) all.addAll(g.getExhibitions());
        exhibitionTable.setItems(FXCollections.observableArrayList(all));
    }

    private static String nvl(String s) { return s != null ? s : "—"; }
}
