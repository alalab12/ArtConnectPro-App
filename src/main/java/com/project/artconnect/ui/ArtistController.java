package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArtistController {

    @FXML private TextField            searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist>    artistTable;
    @FXML private TableColumn<Artist, String>  nameColumn;
    @FXML private TableColumn<Artist, String>  cityColumn;
    @FXML private TableColumn<Artist, String>  emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;
    @FXML private TableColumn<Artist, Boolean> activeColumn;


    @FXML private Label detailName, detailCity, detailEmail, detailPhone;
    @FXML private Label detailYear, detailDisciplines, detailBio;
    @FXML private ListView<String> detailArtworks;

    private final ArtistService  artistService  = ServiceProvider.getArtistService();
    private final ArtworkService artworkService = ServiceProvider.getArtworkService();

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        activeColumn.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActive()));
        activeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v ? "✓" : "✗"));
                setStyle(empty || v == null ? "" : (v ? "-fx-text-fill:green;" : "-fx-text-fill:red;"));
            }
        });
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML public void handleTableClick() {
        Artist a = artistTable.getSelectionModel().getSelectedItem();
        if (a == null) return;
        detailName.setText(a.getName());
        detailCity.setText("City: " + nvl(a.getCity()));
        detailEmail.setText("Email: " + nvl(a.getContactEmail()));
        detailPhone.setText("Phone: " + nvl(a.getPhone()));
        detailYear.setText("Born: " + (a.getBirthYear() != null ? a.getBirthYear() : "—"));
        String discs = a.getDisciplines().stream().map(Discipline::getName).collect(Collectors.joining(", "));
        detailDisciplines.setText("Disciplines: " + (discs.isEmpty() ? "—" : discs));
        detailBio.setText(a.getBio() != null && !a.getBio().isEmpty() ? a.getBio() : "No bio available.");
        List<String> works = artworkService.getArtworksByArtist(a).stream()
                .map(aw -> aw.getTitle() + " (" + (aw.getCreationYear() != null ? aw.getCreationYear() : "?") + ") — " + (aw.getStatus() != null ? aw.getStatus() : ""))
                .collect(Collectors.toList());
        detailArtworks.setItems(FXCollections.observableArrayList(works.isEmpty() ? List.of("No artworks found") : works));
    }

    @FXML private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, d != null ? d.getName() : null, null)));
    }

    @FXML private void handleReset() { searchField.clear(); disciplineFilter.setValue(null); refreshTable(); }

    @FXML private void handleAdd() {
        showArtistDialog(null).ifPresent(artist -> {
            try { artistService.createArtist(artist); refreshTable(); showInfo("\"" + artist.getName() + "\" ajouté."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        Artist s = artistTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez un artiste à modifier."); return; }
        showArtistDialog(s).ifPresent(u -> {
            try { artistService.updateArtist(u); refreshTable(); showInfo("\"" + u.getName() + "\" mis à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        Artist s = artistTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez un artiste à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + s.getName() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { artistService.deleteArtist(s.getName()); refreshTable(); showInfo("Supprimé."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<Artist> showArtistDialog(Artist ex) {
        boolean isEdit = ex != null;
        Dialog<Artist> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier un artiste" : "Ajouter un artiste");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        TextField tfName = new TextField(isEdit ? ex.getName() : "");
        TextField tfCity = new TextField(isEdit ? nvl(ex.getCity()) : "");
        TextField tfEmail= new TextField(isEdit ? nvl(ex.getContactEmail()) : "");
        TextField tfYear = new TextField(isEdit && ex.getBirthYear() != null ? String.valueOf(ex.getBirthYear()) : "");
        TextField tfPhone= new TextField(isEdit ? nvl(ex.getPhone()) : "");
        TextArea taBio   = new TextArea(isEdit ? nvl(ex.getBio()) : ""); taBio.setPrefRowCount(3); taBio.setWrapText(true);
        tfName.setDisable(isEdit);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0, new Label("Nom *"), tfName);  g.addRow(1, new Label("Ville"), tfCity);
        g.addRow(2, new Label("Email"), tfEmail); g.addRow(3, new Label("Année"), tfYear);
        g.addRow(4, new Label("Tél."),  tfPhone); g.addRow(5, new Label("Bio"),   taBio);
        d.getDialogPane().setContent(g);
        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfName.textProperty().addListener((o,ov,nv) -> btn.setDisable(nv.trim().isEmpty()));
        d.setResultConverter(b -> {
            if (b != ok) return null;
            Artist a = isEdit ? ex : new Artist();
            if (!isEdit) a.setName(tfName.getText().trim());
            a.setCity(tfCity.getText().trim()); a.setContactEmail(tfEmail.getText().trim());
            a.setPhone(tfPhone.getText().trim()); a.setBio(taBio.getText().trim()); a.setActive(true);
            try { a.setBirthYear(tfYear.getText().trim().isEmpty() ? null : Integer.parseInt(tfYear.getText().trim())); }
            catch (NumberFormatException ignored) { a.setBirthYear(null); }
            return a;
        });
        return d.showAndWait();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m) { new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR,   m, ButtonType.OK).showAndWait(); }
}
