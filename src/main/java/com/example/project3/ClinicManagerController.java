package com.example.project3;

import project3.*;
import util.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ClinicManagerController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}