package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommunityController {

    @FXML private TableView<CommunityMember>             memberTable;
    @FXML private TableColumn<CommunityMember,String>    nameColumn, emailColumn, cityColumn, membershipColumn;
    @FXML private TableColumn<CommunityMember,Integer>   yearColumn;

    @FXML private Label detailName, detailEmail, detailCity, detailPhone, detailYear, detailMembership;
    @FXML private ListView<String> reviewList;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        // Color membership badge
        membershipColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle("Premium".equalsIgnoreCase(v)
                        ? "-fx-text-fill:#7b1fa2;-fx-font-weight:bold;"
                        : "-fx-text-fill:#555;");
            }
        });

        refreshTable();
    }

    @FXML public void handleTableClick() {
        CommunityMember m = memberTable.getSelectionModel().getSelectedItem();
        if (m == null) return;
        detailName.setText(m.getName());
        detailEmail.setText("Email: " + nvl(m.getEmail()));
        detailCity.setText("City: " + nvl(m.getCity()));
        detailPhone.setText("Phone: " + nvl(m.getPhone()));
        detailYear.setText("Born: " + (m.getBirthYear() != null ? m.getBirthYear() : "—"));
        detailMembership.setText("Membership: " + nvl(m.getMembershipType()));

        List<String> reviews = communityService.getReviewsByMember(m).stream()
                .map(r -> "★".repeat(r.getRating()) + "  " + r.getArtwork().getTitle()
                        + (r.getComment() != null && !r.getComment().isEmpty() ? "\n  \"" + r.getComment() + "\"" : ""))
                .collect(Collectors.toList());
        reviewList.setItems(FXCollections.observableArrayList(
                reviews.isEmpty() ? List.of("No reviews yet") : reviews));
    }

    @FXML private void handleAdd() {
        showMemberDialog(null).ifPresent(m -> {
            try { communityService.createMember(m); refreshTable(); showInfo("\"" + m.getName() + "\" ajouté."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        CommunityMember s = memberTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez un membre à modifier."); return; }
        showMemberDialog(s).ifPresent(u -> {
            try { communityService.updateMember(u); refreshTable(); showInfo("Mis à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        CommunityMember s = memberTable.getSelectionModel().getSelectedItem();
        if (s == null) { showWarn("Sélectionnez un membre à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + s.getName() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { communityService.deleteMember(s.getName()); refreshTable(); showInfo("Supprimé."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<CommunityMember> showMemberDialog(CommunityMember ex) {
        boolean isEdit = ex != null;
        Dialog<CommunityMember> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier un membre" : "Ajouter un membre");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        TextField tfName  = new TextField(isEdit ? ex.getName()          : "");
        TextField tfEmail = new TextField(isEdit ? nvl(ex.getEmail())    : "");
        TextField tfCity  = new TextField(isEdit ? nvl(ex.getCity())     : "");
        TextField tfPhone = new TextField(isEdit ? nvl(ex.getPhone())    : "");
        TextField tfYear  = new TextField(isEdit && ex.getBirthYear() != null ? String.valueOf(ex.getBirthYear()) : "");
        ComboBox<String> cbMembership = new ComboBox<>(FXCollections.observableArrayList("free","Premium"));
        cbMembership.setValue(isEdit && ex.getMembershipType() != null ? ex.getMembershipType() : "free");
        tfName.setDisable(isEdit);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0,new Label("Nom *"),tfName); g.addRow(1,new Label("Email"),tfEmail);
        g.addRow(2,new Label("Ville"),tfCity); g.addRow(3,new Label("Tél."),tfPhone);
        g.addRow(4,new Label("Année"),tfYear); g.addRow(5,new Label("Membership"),cbMembership);
        d.getDialogPane().setContent(g);
        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfName.textProperty().addListener((o,ov,nv)->btn.setDisable(nv.trim().isEmpty()));
        d.setResultConverter(b -> {
            if (b != ok) return null;
            CommunityMember m = isEdit ? ex : new CommunityMember();
            if (!isEdit) m.setName(tfName.getText().trim());
            m.setEmail(tfEmail.getText().trim()); m.setCity(tfCity.getText().trim());
            m.setPhone(tfPhone.getText().trim()); m.setMembershipType(cbMembership.getValue());
            try { m.setBirthYear(tfYear.getText().trim().isEmpty() ? null : Integer.parseInt(tfYear.getText().trim())); }
            catch (NumberFormatException ignored) { m.setBirthYear(null); }
            return m;
        });
        return d.showAndWait();
    }

    private void refreshTable() { memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers())); }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m) { new Alert(Alert.AlertType.WARNING,     m, ButtonType.OK).showAndWait(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR,       m, ButtonType.OK).showAndWait(); }
}
