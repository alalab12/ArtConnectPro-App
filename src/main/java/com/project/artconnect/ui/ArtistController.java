package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ArtistController {

    @FXML private TextField            searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist>    artistTable;
    @FXML private TableColumn<Artist, String>  nameColumn;
    @FXML private TableColumn<Artist, String>  cityColumn;
    @FXML private TableColumn<Artist, String>  emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        showArtistDialog(null).ifPresent(artist -> {
            try {
                artistService.createArtist(artist);
                refreshTable();
                showInfo("Artiste ajouté", "\"" + artist.getName() + "\" a été ajouté.");
            } catch (Exception e) {
                showError("Erreur ajout", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez un artiste à modifier."); return; }
        showArtistDialog(selected).ifPresent(updated -> {
            try {
                artistService.updateArtist(updated);
                refreshTable();
                showInfo("Modifié", "\"" + updated.getName() + "\" mis à jour.");
            } catch (Exception e) {
                showError("Erreur modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez un artiste à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selected.getName() + "\" ? Cette action est irréversible.", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    artistService.deleteArtist(selected.getName());
                    refreshTable();
                    showInfo("Supprimé", "\"" + selected.getName() + "\" supprimé.");
                } catch (Exception e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    private Optional<Artist> showArtistDialog(Artist existing) {
        boolean isEdit = existing != null;
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier un artiste" : "Ajouter un artiste");
        dialog.setHeaderText(isEdit ? "Modifier les informations de l'artiste"
                                    : "Renseignez les informations du nouvel artiste");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField tfName  = new TextField(isEdit ? existing.getName()  : "");
        TextField tfCity  = new TextField(isEdit ? nvl(existing.getCity())  : "");
        TextField tfEmail = new TextField(isEdit ? nvl(existing.getContactEmail()) : "");
        TextField tfYear  = new TextField(isEdit && existing.getBirthYear() != null
                ? String.valueOf(existing.getBirthYear()) : "");
        TextField tfPhone = new TextField(isEdit ? nvl(existing.getPhone()) : "");
        TextArea  taBio   = new TextArea(isEdit ? nvl(existing.getBio()) : "");
        taBio.setPrefRowCount(3); taBio.setWrapText(true);
        tfName.setDisable(isEdit); // nom = clé métier, non modifiable

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 80, 10, 10));
        grid.add(new Label("Nom *"),       0, 0); grid.add(tfName,  1, 0);
        grid.add(new Label("Ville"),       0, 1); grid.add(tfCity,  1, 1);
        grid.add(new Label("Email"),       0, 2); grid.add(tfEmail, 1, 2);
        grid.add(new Label("Année nais."), 0, 3); grid.add(tfYear,  1, 3);
        grid.add(new Label("Téléphone"),   0, 4); grid.add(tfPhone, 1, 4);
        grid.add(new Label("Bio"),         0, 5); grid.add(taBio,   1, 5);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveBtn);
        saveNode.setDisable(!isEdit);
        tfName.textProperty().addListener((obs, o, n) -> saveNode.setDisable(n.trim().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            Artist a = isEdit ? existing : new Artist();
            if (!isEdit) a.setName(tfName.getText().trim());
            a.setCity(tfCity.getText().trim());
            a.setContactEmail(tfEmail.getText().trim());
            a.setPhone(tfPhone.getText().trim());
            a.setBio(taBio.getText().trim());
            try { a.setBirthYear(tfYear.getText().trim().isEmpty() ? null : Integer.parseInt(tfYear.getText().trim())); }
            catch (NumberFormatException ex) { a.setBirthYear(null); }
            a.setActive(true);
            return a;
        });
        return dialog.showAndWait();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String t, String m)    { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarning(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR,   m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
}
