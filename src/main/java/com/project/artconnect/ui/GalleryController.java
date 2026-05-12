package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GalleryController {

    @FXML private TableView<Gallery>          galleryTable;
    @FXML private TableColumn<Gallery,String> nameColumn, locationColumn;
    @FXML private TableColumn<Gallery,Integer> capacityColumn;
    @FXML private TableColumn<Gallery,Double> ratingColumn;

    @FXML private Label detailName, detailLocation, detailCapacity,
                        detailEmail, detailPhone, detailRating;
    @FXML private ListView<String> exhibitionList;
    @FXML private VBox detailPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacity()).asObject());
        ratingColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRating()).asObject());

        ratingColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v + " ★");
                setStyle(v >= 4.5 ? "-fx-text-fill:#2e7d32;-fx-font-weight:bold;"
                        : v >= 4.0 ? "-fx-text-fill:#f57f17;" : "-fx-text-fill:#c62828;");
            }
        });

        galleryTable.setRowFactory(tv -> new TableRow<Gallery>() {
            @Override protected void updateItem(Gallery item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("");
                else if (isSelected()) setStyle("-fx-background-color:#f5d5d5;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:#fef8f8;" : "-fx-background-color:white;");
            }
        });
        galleryTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> galleryTable.refresh());

        if (detailPane != null) detailPane.setEffect(new DropShadow(8, 2, 2, Color.gray(0.3)));

        refreshTable();
    }

    @FXML public void handleTableClick() {
        Gallery g = galleryTable.getSelectionModel().getSelectedItem();
        if (g == null) return;
        detailName.setText(g.getName());
        detailLocation.setText("Adresse: " + nvl(g.getLocation()));
        detailCapacity.setText("Capacité: " + g.getCapacity());
        detailEmail.setText("Email: " + nvl(g.getContactEmail()));
        detailPhone.setText("Tél: " + nvl(g.getContactPhone()));
        detailRating.setText("Note: " + g.getRating() + " / 5.0");

        List<String> expos = g.getExhibitions().stream()
                .map(e -> "• " + e.getTitle()
                        + (e.getStartDate() != null
                           ? "  [" + e.getStartDate() + " → " + e.getEndDate() + "]" : "")
                        + " | " + e.getArtworks().size() + " œuvre(s)")
                .collect(Collectors.toList());
        exhibitionList.setItems(FXCollections.observableArrayList(
                expos.isEmpty() ? List.of("Aucune exposition") : expos));
    }

    @FXML private void handleAdd() {
        showGalleryDialog(null).ifPresent(g -> {
            try { galleryService.createGallery(g); refreshTable(); showInfo("\"" + g.getName() + "\" ajoutée."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        Gallery sel = galleryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez une galerie à modifier."); return; }
        showGalleryDialog(sel).ifPresent(g -> {
            try { galleryService.updateGallery(g); refreshTable(); showInfo("Galerie mise à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        Gallery sel = galleryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez une galerie à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + sel.getName() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { galleryService.deleteGallery(sel.getName()); refreshTable(); showInfo("Supprimée."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<Gallery> showGalleryDialog(Gallery ex) {
        boolean isEdit = ex != null;
        Dialog<Gallery> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier une galerie" : "Ajouter une galerie");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField tfName     = new TextField(isEdit ? ex.getName() : "");
        TextField tfLocation = new TextField(isEdit ? nvl(ex.getLocation()) : "");
        TextField tfCapacity = new TextField(isEdit ? String.valueOf(ex.getCapacity()) : "0");
        TextField tfEmail    = new TextField(isEdit ? nvl(ex.getContactEmail()) : "");
        TextField tfPhone    = new TextField(isEdit ? nvl(ex.getContactPhone()) : "");
        TextField tfRating   = new TextField(isEdit ? String.valueOf(ex.getRating()) : "0.0");
        tfName.setDisable(isEdit);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0, new Label("Nom *"),       tfName);
        g.addRow(1, new Label("Adresse"),     tfLocation);
        g.addRow(2, new Label("Capacité"),    tfCapacity);
        g.addRow(3, new Label("Email"),       tfEmail);
        g.addRow(4, new Label("Téléphone"),   tfPhone);
        g.addRow(5, new Label("Note (0-5)"),  tfRating);
        d.getDialogPane().setContent(g);

        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfName.textProperty().addListener((o,ov,nv) -> btn.setDisable(nv.trim().isEmpty()));

        d.setResultConverter(b -> {
            if (b != ok) return null;
            Gallery gallery = isEdit ? ex : new Gallery();
            if (!isEdit) gallery.setName(tfName.getText().trim());
            gallery.setLocation(tfLocation.getText().trim());
            gallery.setContactEmail(tfEmail.getText().trim());
            gallery.setContactPhone(tfPhone.getText().trim());
            try { gallery.setCapacity(Integer.parseInt(tfCapacity.getText().trim())); }
            catch (NumberFormatException ignored) { gallery.setCapacity(0); }
            try { gallery.setRating(Double.parseDouble(tfRating.getText().trim())); }
            catch (NumberFormatException ignored) { gallery.setRating(0.0); }
            return gallery;
        });
        return d.showAndWait();
    }

    private void refreshTable() {
        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m)  { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m)  { new Alert(Alert.AlertType.WARNING,     m, ButtonType.OK).showAndWait(); }
    private void showError(String m) { new Alert(Alert.AlertType.ERROR,       m, ButtonType.OK).showAndWait(); }
}
