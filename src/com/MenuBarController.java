package com;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import java.nio.file.Path;

public class MenuBarController implements Initializable {

    private VideoProject videoProject;

    @FXML
    public void saveProject() {
        // Implémentez la logique pour sauvegarder le projet ici
        System.out.println("Sauvegarde du projet...");
        // Mettre saveFilePath à la racine du projet
        Path saveFilePath = Paths.get(videoProject.getProjectName() + ".json");

        videoProject.saveProject(saveFilePath);
    }

    public void setVideoProject(VideoProject videoProject) {
        this.videoProject = videoProject;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
