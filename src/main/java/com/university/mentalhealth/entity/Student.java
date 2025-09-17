package com.university.mentalhealth.entity;

public class Student extends User {
    private String studentId;
    private String name;
    private String department;
    private String contactPhone;
    private String emergencyContact;
    private String emergencyPhone;

    public Student() {}

    public Student(int id, String username, String passwordHash, UserType type,
                   String studentId, String name, String department,
                   String contactPhone, String emergencyContact, String emergencyPhone) {
        super(id, username, passwordHash, type, null);
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.contactPhone = contactPhone;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
    }

    // Getter和Setter方法
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    /**
     * 获取简化的学生信息（用于显示）
     */
    public String getDisplayInfo() {
        return name + " (" + studentId + ") - " + department;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                ", emergencyPhone='" + emergencyPhone + '\'' +
                '}';
    }
}