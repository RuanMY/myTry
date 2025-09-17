package com.university.mentalhealth.entity;

public enum UserType {
    student("学生"),
    counselor("咨询师"),
    admin("管理员");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}