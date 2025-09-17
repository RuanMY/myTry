package com.university.mentalhealth.entity;

import java.time.LocalDateTime;

public class AssessmentSession {
    private int id;
    private int studentId;
    private int assessmentId;
    private String assessmentName;
    private Integer totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "AssessmentSession{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", assessmentId=" + assessmentId +
                ", totalScore=" + totalScore +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
