package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class CommunityController {

    @FXML private TableView<CommunityMember>             memberTable;
    @FXML private TableColumn<CommunityMember, String>   nameColumn;
    @FXML private TableColumn<CommunityMember, String>   emailColumn;
    @FXML private TableColumn<CommunityMember, String>   cityColumn;
    @FXML private TableColumn<CommunityMember, String>   membershipColumn;
    @FXML private TableColumn<CommunityMember, Integer>  yearColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        refreshTable();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        showMemberDialog(null).ifPresent(member -> {
            try {
                communityService.createMember(member);
                refreshTable();
                showInfo("Membre ajouté", "\"" + member.getName() + "\" a été ajouté.");
            } catch (Exception e) {
                showError("Erreur ajout", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez un membre à modifier."); return; }
        showMemberDialog(selected).ifPresent(updated -> {
            try {
                communityService.updateMember(updated);
                refreshTable();
                showInfo("Modifié", "\"" + updated.getName() + "\" mis à jour.");
            } catch (Exception e) {
                showError("Erreur modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Aucune sélection", "Sélectionnez un membre à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selected.getName() + "\" ? Action irréversible.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    communityService.deleteMember(selected.getName());
                    refreshTable();
                    showInfo("Supprimé", "\"" + selected.getName() + "\" supprimé.");
                } catch (Exception e) {
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    // ── Dialogue de saisie ────────────────────────────────────────────────────

    private Optional<CommunityMember> showMemberDialog(CommunityMember existing) {
        boolean isEdit = existing != null;
        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier un membre" : "Ajouter un membre");
        dialog.setHeaderText(isEdit ? "Modifier les informations du membre"
                                    : "Renseignez les informations du nouveau membre");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField tfName  = new TextField(isEdit ? existing.getName()  : "");
        TextField tfEmail = new TextField(isEdit ? nvl(existing.getEmail()) : "");
        TextField tfCity  = new TextField(isEdit ? nvl(existing.getCity())  : "");
        TextField tfPhone = new TextField(isEdit ? nvl(existing.getPhone()) : "");
        TextField tfYear  = new TextField(isEdit && existing.getBirthYear() != null
                ? String.valueOf(existing.getBirthYear()) : "");

        ComboBox<String> cbMembership = new ComboBox<>(
                FXCollections.observableArrayList("free", "Premium"));
        cbMembership.setValue(isEdit && existing.getMembershipType() != null
                ? existing.getMembershipType() : "free");

        tfName.setDisable(isEdit); // nom = clé métier

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 80, 10, 10));
        grid.add(new Label("Nom *"),       0, 0); grid.add(tfName,       1, 0);
        grid.add(new Label("Email"),       0, 1); grid.add(tfEmail,      1, 1);
        grid.add(new Label("Ville"),       0, 2); grid.add(tfCity,       1, 2);
        grid.add(new Label("Téléphone"),   0, 3); grid.add(tfPhone,      1, 3);
        grid.add(new Label("Année nais."), 0, 4); grid.add(tfYear,       1, 4);
        grid.add(new Label("Membership"),  0, 5); grid.add(cbMembership, 1, 5);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveBtn);
        saveNode.setDisable(!isEdit);
        tfName.textProperty().addListener((obs, o, n) -> saveNode.setDisable(n.trim().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            CommunityMember m = isEdit ? existing : new CommunityMember();
            if (!isEdit) m.setName(tfName.getText().trim());
            m.setEmail(tfEmail.getText().trim());
            m.setCity(tfCity.getText().trim());
            m.setPhone(tfPhone.getText().trim());
            m.setMembershipType(cbMembership.getValue());
            try { m.setBirthYear(tfYear.getText().trim().isEmpty()
                    ? null : Integer.parseInt(tfYear.getText().trim())); }
            catch (NumberFormatException ex) { m.setBirthYear(null); }
            return m;
        });
        return dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshTable() {
        memberTable.setItems(
                FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String t, String m)    { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarning(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR,   m, ButtonType.OK); a.setTitle(t); a.showAndWait(); }
}
