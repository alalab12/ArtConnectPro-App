package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopController {

    @FXML private TableView<Workshop>           workshopTable;
    @FXML private TableColumn<Workshop,String>  titleColumn, instructorColumn;
    @FXML private TableColumn<Workshop,LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop,Double>  priceColumn;
    @FXML private TableColumn<Workshop,Integer> spotsColumn, durationColumn;

    @FXML private Label detailTitle, detailInstructor, detailDate, detailDuration;
    @FXML private Label detailPrice, detailSpots, detailDesc;
    @FXML private VBox detailPane;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService   artistService   = ServiceProvider.getArtistService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        durationColumn.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getDurationHours()).asObject());
        spotsColumn.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getMaxParticipants()).asObject());
        instructorColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getInstructor() != null ? c.getValue().getInstructor().getName() : "—"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(FMT));
            }
        });

        workshopTable.setRowFactory(tv -> new TableRow<Workshop>() {
            @Override protected void updateItem(Workshop item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("");
                else if (isSelected()) setStyle("-fx-background-color:#f5d5d5;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:#fef8f8;" : "-fx-background-color:white;");
            }
        });
        workshopTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> workshopTable.refresh());

        if (detailPane != null) detailPane.setEffect(new DropShadow(8, 2, 2, Color.gray(0.3)));

        refreshTable();
    }

    @FXML public void handleTableClick() {
        Workshop w = workshopTable.getSelectionModel().getSelectedItem();
        if (w == null) return;
        detailTitle.setText(w.getTitle());
        detailInstructor.setText("Instructeur: " + (w.getInstructor() != null
                ? w.getInstructor().getName()
                  + (w.getInstructor().getCity() != null ? " (" + w.getInstructor().getCity() + ")" : "")
                : "—"));
        detailDate.setText("Date: " + (w.getDate() != null ? w.getDate().format(FMT) : "—"));
        detailDuration.setText("Durée: " + w.getDurationHours() + " h");
        detailPrice.setText("Prix: " + w.getPrice() + " €");
        detailSpots.setText("Participants max: " + w.getMaxParticipants());
        detailDesc.setText(w.getDescription() != null && !w.getDescription().isEmpty()
                ? w.getDescription() : "—");
    }

    @FXML private void handleAdd() {
        showWorkshopDialog(null).ifPresent(w -> {
            try { workshopService.createWorkshop(w); refreshTable(); showInfo("\"" + w.getTitle() + "\" ajouté."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleEdit() {
        Workshop sel = workshopTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez un atelier à modifier."); return; }
        showWorkshopDialog(sel).ifPresent(w -> {
            try { workshopService.updateWorkshop(w); refreshTable(); showInfo("Atelier mis à jour."); }
            catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void handleDelete() {
        Workshop sel = workshopTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélectionnez un atelier à supprimer."); return; }
        new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + sel.getTitle() + "\" ?", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait().ifPresent(b -> { if (b == ButtonType.OK) {
                try { workshopService.deleteWorkshop(sel.getTitle()); refreshTable(); showInfo("Supprimé."); }
                catch (Exception e) { showError(e.getMessage()); }
            }});
    }

    private Optional<Workshop> showWorkshopDialog(Workshop ex) {
        boolean isEdit = ex != null;
        Dialog<Workshop> d = new Dialog<>();
        d.setTitle(isEdit ? "Modifier un atelier" : "Ajouter un atelier");
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField tfTitle    = new TextField(isEdit ? ex.getTitle() : "");
        DatePicker dpDate    = new DatePicker(isEdit && ex.getDate() != null
                ? ex.getDate().toLocalDate() : java.time.LocalDate.now());
        TextField tfTime     = new TextField(isEdit && ex.getDate() != null
                ? ex.getDate().toLocalTime().toString() : "09:00");
        TextField tfDuration = new TextField(isEdit ? String.valueOf(ex.getDurationHours()) : "2");
        TextField tfSpots    = new TextField(isEdit ? String.valueOf(ex.getMaxParticipants()) : "10");
        TextField tfPrice    = new TextField(isEdit ? String.valueOf(ex.getPrice()) : "0.0");
        TextArea  taDesc     = new TextArea(isEdit ? nvl(ex.getDescription()) : "");
        taDesc.setPrefRowCount(3); taDesc.setWrapText(true);

        List<String> artistNames = artistService.getAllArtists().stream()
                .map(Artist::getName).collect(Collectors.toList());
        ComboBox<String> cbInstructor = new ComboBox<>(FXCollections.observableArrayList(artistNames));
        if (isEdit && ex.getInstructor() != null) cbInstructor.setValue(ex.getInstructor().getName());
        else if (!artistNames.isEmpty())           cbInstructor.setValue(artistNames.get(0));
        tfTitle.setDisable(isEdit);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16,80,10,10));
        g.addRow(0, new Label("Titre *"),          tfTitle);
        g.addRow(1, new Label("Instructeur"),      cbInstructor);
        g.addRow(2, new Label("Date"),             dpDate);
        g.addRow(3, new Label("Heure (HH:mm)"),   tfTime);
        g.addRow(4, new Label("Durée (heures)"),   tfDuration);
        g.addRow(5, new Label("Places max"),       tfSpots);
        g.addRow(6, new Label("Prix (€)"),         tfPrice);
        g.addRow(7, new Label("Description"),      taDesc);
        d.getDialogPane().setContent(g);

        javafx.scene.Node btn = d.getDialogPane().lookupButton(ok);
        btn.setDisable(!isEdit);
        tfTitle.textProperty().addListener((o,ov,nv) -> btn.setDisable(nv.trim().isEmpty()));

        d.setResultConverter(b -> {
            if (b != ok) return null;
            Workshop w = isEdit ? ex : new Workshop();
            if (!isEdit) w.setTitle(tfTitle.getText().trim());
            w.setDescription(taDesc.getText().trim());
            try {
                w.setDurationHours(Integer.parseInt(tfDuration.getText().trim()));
            } catch (NumberFormatException ignored) { w.setDurationHours(0); }
            try { w.setMaxParticipants(Integer.parseInt(tfSpots.getText().trim())); }
            catch (NumberFormatException ignored) { w.setMaxParticipants(0); }
            try { w.setPrice(Double.parseDouble(tfPrice.getText().trim())); }
            catch (NumberFormatException ignored) { w.setPrice(0.0); }
            try {
                String timeStr = tfTime.getText().trim();
                if (timeStr.matches("\\d{1,2}:\\d{2}")) {
                    w.setDate(dpDate.getValue().atTime(
                            java.time.LocalTime.parse(timeStr)));
                } else {
                    w.setDate(dpDate.getValue().atStartOfDay());
                }
            } catch (Exception ignored) { w.setDate(dpDate.getValue().atStartOfDay()); }
            if (cbInstructor.getValue() != null) {
                Artist a = new Artist();
                a.setName(cbInstructor.getValue());
                w.setInstructor(a);
            }
            return w;
        });
        return d.showAndWait();
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }
    private static String nvl(String s) { return s != null ? s : ""; }
    private void showInfo(String m)  { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showWarn(String m)  { new Alert(Alert.AlertType.WARNING,     m, ButtonType.OK).showAndWait(); }
    private void showError(String m) { new Alert(Alert.AlertType.ERROR,       m, ButtonType.OK).showAndWait(); }
}
