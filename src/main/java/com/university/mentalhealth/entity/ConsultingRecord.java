package com.university.mentalhealth.entity;

import java.time.LocalDateTime;

public class ConsultingRecord {
    private Integer id;
    private Integer appointmentId;
    private String content;
    private String suggestions;
    private LocalDateTime recordTime;
    private String studentName;
    private String counselorName;

    // 构造函数
    public ConsultingRecord() {}

    public ConsultingRecord(Integer appointmentId, String content, String suggestions) {
        this.appointmentId = appointmentId;
        this.content = content;
        this.suggestions = suggestions;
        this.recordTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }

    @Override
    public String toString() {
        return "ConsultingRecord{" +
                "id=" + id +
                ", appointmentId=" + appointmentId +
                ", recordTime=" + recordTime +
                '}';
    }
}