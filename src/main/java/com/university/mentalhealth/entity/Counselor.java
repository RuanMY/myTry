package com.university.mentalhealth.entity;

public class Counselor extends User {
    private String name;
    private String title;
    private String specialization;
    private Boolean isAvailable;

    public Counselor() {}

    public Counselor(int id, String username, String passwordHash, UserType type,
                     String name, String title, String specialization, Boolean isAvailable) {
        super(id, username, passwordHash, type, null);
        this.name = name;
        this.title = title;
        this.specialization = specialization;
        this.isAvailable = isAvailable;
    }

    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    @Override
    public String toString() {
        return "Counselor{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", specialization='" + specialization + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }

    public String getDisplayInfo() {
        return name + " (" + title + ") - " + specialization;
    }
}