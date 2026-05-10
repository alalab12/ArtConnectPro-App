package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ArtworkController {

    @FXML private TableView<Artwork>             artworkTable;
    @FXML private TableColumn<Artwork, String>   titleColumn;
    @FXML private TableColumn<Artwork, String>   typeColumn;
    @FXML private TableColumn<Artwork, Double>   priceColumn;
    @FXML private TableColumn<Artwork, String>   statusColumn;
    @FXML private TableColumn<Artwork, String>   artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService  artistService  = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null
                        ? cellData.getValue().getArtist().getName() : "Unknown"));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtworkDialog(null).ifPresent(artwork -> {
            try {
                artworkService.createArtwork(artwork);
                refreshTable();
                showInfo("Œuvre ajoutée", "\"" + artwork.getTitle() + "\" a été ajoutée.");
            } catch (Exception e) {
                showError("Erreur ajout", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez une œuvre à modifier."); return; }
        showArtworkDialog(selected).ifPresent(updated -> {
            try {
                artworkService.updateArtwork(updated);
                refreshTable();
                showInfo("Modifié", "\"" + updated.getTitle() + "\" mis à jour.");
            } catch (Exception e) {
                showError("Erreur modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez une œuvre à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selected.getTitle() + "\" ? Action irréversible.", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    artworkService.deleteArtwork(selected.getTitle());
                    refreshTable();
                    showInfo("Supprimé", "\"" + selected.getTitle() + "\" supprimé.");
                } catch (Exception e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    private Optional<Artwork> showArtworkDialog(Artwork existing) {
        boolean isEdit = existing != null;
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier une œuvre" : "Ajouter une œuvre");
        dialog.setHeaderText(isEdit ? "Modifier les informations de l'œuvre"
                                    : "Renseignez les informations de la nouvelle œuvre");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField tfTitle  = new TextField(isEdit ? existing.getTitle() : "");
        TextField tfType   = new TextField(isEdit ? nvl(existing.getType()) : "");
        TextField tfMedium = new TextField(isEdit ? nvl(existing.getMedium()) : "");
        TextField tfDims   = new TextField(isEdit ? nvl(existing.getDimensions()) : "");
        TextField tfPrice  = new TextField(isEdit ? String.valueOf(existing.getPrice()) : "0");
        TextField tfYear   = new TextField(isEdit && existing.getCreationYear() != null
                ? String.valueOf(existing.getCreationYear()) : "");
        TextArea  taDesc   = new TextArea(isEdit ? nvl(existing.getDescription()) : "");
        taDesc.setPrefRowCount(3); taDesc.setWrapText(true);

        ComboBox<Artwork.Status> cbStatus = new ComboBox<>(
                FXCollections.observableArrayList(Artwork.Status.values()));
        cbStatus.setValue(isEdit && existing.getStatus() != null
                ? existing.getStatus() : Artwork.Status.FOR_SALE);

        ComboBox<Artist> cbArtist = new ComboBox<>(
                FXCollections.observableArrayList(artistService.getAllArtists()));
        if (isEdit && existing.getArtist() != null) cbArtist.setValue(existing.getArtist());

        tfTitle.setDisable(isEdit); // titre = clé métier

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 80, 10, 10));
        grid.add(new Label("Titre *"),    0, 0); grid.add(tfTitle,  1, 0);
        grid.add(new Label("Type"),       0, 1); grid.add(tfType,   1, 1);
        grid.add(new Label("Medium"),     0, 2); grid.add(tfMedium, 1, 2);
        grid.add(new Label("Dimensions"), 0, 3); grid.add(tfDims,   1, 3);
        grid.add(new Label("Prix (€)"),   0, 4); grid.add(tfPrice,  1, 4);
        grid.add(new Label("Année"),      0, 5); grid.add(tfYear,   1, 5);
        grid.add(new Label("Statut"),     0, 6); grid.add(cbStatus, 1, 6);
        grid.add(new Label("Artiste"),    0, 7); grid.add(cbArtist, 1, 7);
        grid.add(new Label("Description"),0, 8); grid.add(taDesc,   1, 8);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveBtn);
        saveNode.setDisable(!isEdit);
        tfTitle.textProperty().addListener((obs, o, n) -> saveNode.setDisable(n.trim().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            Artwork a = isEdit ? existing : new Artwork();
            if (!isEdit) a.setTitle(tfTitle.getText().trim());
            a.setType(tfType.getText().trim());
            a.setMedium(tfMedium.getText().trim());
            a.setDimensions(tfDims.getText().trim());
            a.setDescription(taDesc.getText().trim());
            a.setStatus(cbStatus.getValue());
            a.setArtist(cbArtist.getValue());
            try { a.setPrice(Double.parseDouble(tfPrice.getText().trim())); }
            catch (NumberFormatException ex) { a.setPrice(0); }
            try { a.setCreationYear(tfYear.getText().trim().isEmpty() ? null : Integer.parseInt(tfYear.getText().trim())); }
            catch (NumberFormatException ex) { a.setCreationYear(null); }
            return a;
        });
        return dialog.showAndWait();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String t, String m)    { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarning(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR,   m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
}
