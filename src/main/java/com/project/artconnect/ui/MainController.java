package com.project.artconnect.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.application.Platform;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private DiscoverController discoverController;

    @FXML
    public void initialize() {
        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx.intValue() == 0 && discoverController != null) {
                discoverController.refresh();
            }
        });
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
