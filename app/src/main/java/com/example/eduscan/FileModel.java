package com.example.eduscan;

import java.util.Objects;

public class FileModel {
    private String fileName;
    private String filePath;

    public FileModel(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object obj) {
        // Verificăm dacă obiectul dat este același cu acest obiect
        if (this == obj) {
            return true;
        }
        // Verificăm dacă obiectul dat este null sau nu este o instanță a clasei FileModel
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        // Converitm obiectul dat la FileModel pentru a putea accesa proprietățile
        FileModel otherFile = (FileModel) obj;
        // Verificăm dacă toate proprietățile sunt egale
        return Objects.equals(this.fileName, otherFile.fileName) &&
                Objects.equals(this.filePath, otherFile.filePath);
    }

}
