package com.university.mentalhealth.entity;

import java.time.LocalDateTime;

public class TimeSlot {
    private Integer id;
    private Integer counselorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // available, booked, cancelled
    private String counselorName;
    private String title;

    // 构造函数
    public TimeSlot() {}

    public TimeSlot(Integer counselorId, LocalDateTime startTime, LocalDateTime endTime) {
        this.counselorId = counselorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "available";
    }

    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCounselorId() { return counselorId; }
    public void setCounselorId(Integer counselorId) { this.counselorId = counselorId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", counselorId=" + counselorId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                '}';
    }
}