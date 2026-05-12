package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExhibitionController {

    @FXML private TableView<Exhibition>           exhibitionTable;
    @FXML private TableColumn<Exhibition,String>  titleColumn, galleryColumn, descColumn;
    @FXML private TableColumn<Exhibition,LocalDate> startColumn, endColumn;

    @FXML private Label detailTitle, detailGallery, detailDates, detailDesc;
    @FXML private ListView<String> artworkList;
    @FXML private VBox detailPane;

    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        galleryColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getGallery() != null ? c.getValue().getGallery().getName() : "—"));


        exhibitionTable.setRowFactory(tv -> new TableRow<Exhibition>() {
            @Override protected void updateItem(Exhibition item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("");
                else if (isSelected()) setStyle("-fx-background-color:#f5d5d5;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:#fef8f8;" : "-fx-background-color:white;");
            }
        });
        exhibitionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> exhibitionTable.refresh());

        if (detailPane != null) detailPane.setEffect(new DropShadow(8, 2, 2, Color.gray(0.3)));

        refreshTable();
    }

    @FXML public void handleTableClick() {
        Exhibition e = exhibitionTable.getSelectionModel().getSelectedItem();
        if (e == null) return;
        detailTitle.setText(e.getTitle());
        detailGallery.setText("Galerie: " + (e.getGallery() != null ? e.getGallery().getName() : "—"));
        String dates = (e.getStartDate() != null ? e.getStartDate().toString() : "?")
                + " → " + (e.getEndDate() != null ? e.getEndDate().toString() : "?");
        detailDates.setText("Dates: " + dates);
        detailDesc.setText(e.getDescription() != null && !e.getDescription().isEmpty()
                ? e.getDescription() : "—");
        List<String> works = e.getArtworks().stream()
                .map(a -> "• " + a.getTitle())
                .collect(Collectors.toList());
        artworkList.setItems(FXCollections.observableArrayList(
                works.isEmpty() ? List.of("Aucune œuvre listée") : works));
    }

    @FXML private void handleAdd() {
        showExhibitionDialog(null).ifPresent(ex -> {
            try { exhibitionService.createExhibition(ex); refreshTable(); showInfo("\"" + ex.getTitle() + "\" ajoutée."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        Exhibition sel = exhibitionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez une exposition à modifier."); return; }
        showExhibitionDialog(sel).ifPresent(ex -> {
            try { exhibitionService.updateExhibition(ex); refreshTable(); showInfo("Exposition mise à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        Exhibition sel = exhibitionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez une exposition à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + sel.getTitle() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { exhibitionService.deleteExhibition(sel.getTitle()); refreshTable(); showInfo("Supprimée."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<Exhibition> showExhibitionDialog(Exhibition ex) {
        boolean isEdit = ex != null;
        Dialog<Exhibition> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier une exposition" : "Ajouter une exposition");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField tfTitle    = new TextField(isEdit ? ex.getTitle() : "");
        DatePicker dpStart   = new DatePicker(isEdit ? ex.getStartDate() : LocalDate.now());
        DatePicker dpEnd     = new DatePicker(isEdit ? ex.getEndDate()   : LocalDate.now().plusMonths(1));
        TextArea   taDesc    = new TextArea(isEdit ? nvl(ex.getDescription()) : "");
        taDesc.setPrefRowCount(3); taDesc.setWrapText(true);

        List<String> galleryNames = exhibitionService.getAllGalleryNames();
        ComboBox<String> cbGallery = new ComboBox<>(FXCollections.observableArrayList(galleryNames));
        if (isEdit && ex.getGallery() != null) cbGallery.setValue(ex.getGallery().getName());
        else if (!galleryNames.isEmpty())       cbGallery.setValue(galleryNames.get(0));
        tfTitle.setDisable(isEdit);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0, new Label("Titre *"),      tfTitle);
        g.addRow(1, new Label("Galerie *"),    cbGallery);
        g.addRow(2, new Label("Début"),        dpStart);
        g.addRow(3, new Label("Fin"),          dpEnd);
        g.addRow(4, new Label("Description"),  taDesc);
        d.getDialogPane().setContent(g);

        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfTitle.textProperty().addListener((o,ov,nv) -> btn.setDisable(nv.trim().isEmpty()));

        d.setResultConverter(b -> {
            if (b != ok) return null;
            Exhibition exhibition = isEdit ? ex : new Exhibition();
            if (!isEdit) exhibition.setTitle(tfTitle.getText().trim());
            exhibition.setStartDate(dpStart.getValue());
            exhibition.setEndDate(dpEnd.getValue());
            exhibition.setDescription(taDesc.getText().trim());
            if (cbGallery.getValue() != null) {
                Gallery gallery = new Gallery();
                gallery.setName(cbGallery.getValue());
                exhibition.setGallery(gallery);
            }
            return exhibition;
        });
        return d.showAndWait();
    }

    private void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.getAllExhibitions()));
    }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m)  { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m)  { new Alert(Alert.AlertType.WARNING,     m, ButtonType.OK).showAndWait(); }
    private void showError(String m) { new Alert(Alert.AlertType.ERROR,       m, ButtonType.OK).showAndWait(); }
}
