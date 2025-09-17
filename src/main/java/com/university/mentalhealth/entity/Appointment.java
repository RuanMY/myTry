package com.university.mentalhealth.entity;

import java.time.LocalDateTime;

public class Appointment {
    private Integer id;
    private Integer studentId;
    private Integer counselorId;
    private Integer timeSlotId;
    private String status; // pending, confirmed, completed, cancelled
    private LocalDateTime createdAt;
    private String notes;
    private String studentName;
    private String counselorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 构造函数
    public Appointment() {}

    public Appointment(Integer studentId, Integer counselorId, Integer timeSlotId, String notes) {
        this.studentId = studentId;
        this.counselorId = counselorId;
        this.timeSlotId = timeSlotId;
        this.notes = notes;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCounselorId() { return counselorId; }
    public void setCounselorId(Integer counselorId) { this.counselorId = counselorId; }

    public Integer getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Integer timeSlotId) { this.timeSlotId = timeSlotId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", counselorId=" + counselorId +
                ", status='" + status + '\'' +
                ", timeSlotId=" + timeSlotId +
                '}';
    }
}