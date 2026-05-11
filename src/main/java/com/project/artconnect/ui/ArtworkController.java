package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArtworkController {

    @FXML private ComboBox<String>          typeFilter, statusFilter;
    @FXML private TableView<Artwork>        artworkTable;
    @FXML private TableColumn<Artwork,String>  titleColumn, artistColumn, typeColumn, mediumColumn, statusColumn;
    @FXML private TableColumn<Artwork,Integer> yearColumn;
    @FXML private TableColumn<Artwork,Double>  priceColumn;


    @FXML private Label detailTitle, detailArtist, detailType, detailMedium;
    @FXML private Label detailDimensions, detailYear, detailPrice, detailStatus, detailDesc;
    @FXML private ListView<String> detailExhibitions;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService  artistService  = ServiceProvider.getArtistService();
    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        mediumColumn.setCellValueFactory(new PropertyValueFactory<>("medium"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus() != null ? c.getValue().getStatus().name() : ""));
        artistColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArtist() != null ? c.getValue().getArtist().getName() : "Unknown"));


        List<String> types = artworkService.getAllArtworks().stream()
                .map(Artwork::getType).filter(t -> t != null).distinct().sorted().collect(Collectors.toList());
        types.add(0, "All types");
        typeFilter.setItems(FXCollections.observableArrayList(types));
        typeFilter.setValue("All types");

        statusFilter.setItems(FXCollections.observableArrayList(
                "All statuses", "FOR_SALE", "SOLD", "EXHIBITED"));
        statusFilter.setValue("All statuses");

        refreshTable();
    }

    @FXML public void handleTableClick() {
        Artwork a = artworkTable.getSelectionModel().getSelectedItem();
        if (a == null) return;
        detailTitle.setText(a.getTitle());
        detailArtist.setText("Artist: " + (a.getArtist() != null ? a.getArtist().getName() : "Unknown"));
        detailType.setText("Type: " + nvl(a.getType()));
        detailMedium.setText("Medium: " + nvl(a.getMedium()));
        detailDimensions.setText("Dimensions: " + nvl(a.getDimensions()));
        detailYear.setText("Year: " + (a.getCreationYear() != null ? a.getCreationYear() : "—"));
        detailPrice.setText("Price: " + a.getPrice() + " €");
        detailStatus.setText("Status: " + (a.getStatus() != null ? a.getStatus().name() : "—"));
        detailDesc.setText(a.getDescription() != null && !a.getDescription().isEmpty() ? a.getDescription() : "—");


        List<String> expos = galleryService.getAllGalleries().stream()
                .flatMap(g -> g.getExhibitions().stream())
                .filter(e -> e.getArtworks().stream().anyMatch(aw -> aw.getTitle().equals(a.getTitle())))
                .map(e -> e.getTitle() + " @ " + (e.getGallery() != null ? e.getGallery().getName() : "?"))
                .collect(Collectors.toList());
        detailExhibitions.setItems(FXCollections.observableArrayList(expos.isEmpty() ? List.of("Not in any exhibition") : expos));
    }

    @FXML public void handleFilter() {
        String type   = typeFilter.getValue();
        String status = statusFilter.getValue();
        List<Artwork> filtered = artworkService.getAllArtworks().stream()
                .filter(a -> type == null   || type.startsWith("All")   || type.equals(a.getType()))
                .filter(a -> status == null || status.startsWith("All") || status.equals(a.getStatus() != null ? a.getStatus().name() : ""))
                .collect(Collectors.toList());
        artworkTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML public void handleReset() {
        typeFilter.setValue("All types"); statusFilter.setValue("All statuses"); refreshTable();
    }

    @FXML private void handleAdd() {
        showArtworkDialog(null).ifPresent(a -> {
            try { artworkService.createArtwork(a); refreshTable(); showInfo("\"" + a.getTitle() + "\" ajouté."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        Artwork s = artworkTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez une œuvre à modifier."); return; }
        showArtworkDialog(s).ifPresent(u -> {
            try { artworkService.updateArtwork(u); refreshTable(); showInfo("Mis à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        Artwork s = artworkTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez une œuvre à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + s.getTitle() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { artworkService.deleteArtwork(s.getTitle()); refreshTable(); showInfo("Supprimé."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<Artwork> showArtworkDialog(Artwork ex) {
        boolean isEdit = ex != null;
        Dialog<Artwork> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier une œuvre" : "Ajouter une œuvre");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        TextField tfTitle  = new TextField(isEdit ? ex.getTitle() : "");
        TextField tfType   = new TextField(isEdit ? nvl(ex.getType())   : "");
        TextField tfMedium = new TextField(isEdit ? nvl(ex.getMedium()) : "");
        TextField tfDims   = new TextField(isEdit ? nvl(ex.getDimensions()) : "");
        TextField tfPrice  = new TextField(isEdit ? String.valueOf(ex.getPrice()) : "0");
        TextField tfYear   = new TextField(isEdit && ex.getCreationYear() != null ? String.valueOf(ex.getCreationYear()) : "");
        TextArea taDesc    = new TextArea(isEdit ? nvl(ex.getDescription()) : ""); taDesc.setPrefRowCount(3); taDesc.setWrapText(true);
        ComboBox<Artwork.Status> cbStatus = new ComboBox<>(FXCollections.observableArrayList(Artwork.Status.values()));
        cbStatus.setValue(isEdit && ex.getStatus() != null ? ex.getStatus() : Artwork.Status.FOR_SALE);
        ComboBox<Artist> cbArtist = new ComboBox<>(FXCollections.observableArrayList(artistService.getAllArtists()));
        if (isEdit && ex.getArtist() != null) cbArtist.setValue(ex.getArtist());
        tfTitle.setDisable(isEdit);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0,new Label("Titre *"),tfTitle); g.addRow(1,new Label("Type"),tfType);
        g.addRow(2,new Label("Medium"),tfMedium); g.addRow(3,new Label("Dimensions"),tfDims);
        g.addRow(4,new Label("Prix (€)"),tfPrice); g.addRow(5,new Label("Année"),tfYear);
        g.addRow(6,new Label("Statut"),cbStatus); g.addRow(7,new Label("Artiste"),cbArtist);
        g.addRow(8,new Label("Description"),taDesc);
        d.getDialogPane().setContent(g);
        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfTitle.textProperty().addListener((o,ov,nv)->btn.setDisable(nv.trim().isEmpty()));
        d.setResultConverter(b -> {
            if (b != ok) return null;
            Artwork a = isEdit ? ex : new Artwork();
            if (!isEdit) a.setTitle(tfTitle.getText().trim());
            a.setType(tfType.getText().trim()); a.setMedium(tfMedium.getText().trim());
            a.setDimensions(tfDims.getText().trim()); a.setDescription(taDesc.getText().trim());
            a.setStatus(cbStatus.getValue()); a.setArtist(cbArtist.getValue());
            try { a.setPrice(Double.parseDouble(tfPrice.getText().trim())); } catch (NumberFormatException ignored) { a.setPrice(0); }
            try { a.setCreationYear(tfYear.getText().trim().isEmpty() ? null : Integer.parseInt(tfYear.getText().trim())); } catch (NumberFormatException ignored) {}
            return a;
        });
        return d.showAndWait();
    }

    private void refreshTable() { artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks())); }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m) { new Alert(Alert.AlertType.WARNING,     m, ButtonType.OK).showAndWait(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR,       m, ButtonType.OK).showAndWait(); }
}
