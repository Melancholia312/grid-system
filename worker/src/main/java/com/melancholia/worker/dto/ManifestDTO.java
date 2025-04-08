package com.melancholia.worker.dto;

public class ManifestDTO {

    private String className;
    private String annotationName;

    public ManifestDTO() {}

    public ManifestDTO(String className, String annotationName) {
        this.className = className;
        this.annotationName = annotationName;
    }

    public String getClassName() {
        return className;
    }

    public String getAnnotationName() {
        return annotationName;
    }

}
