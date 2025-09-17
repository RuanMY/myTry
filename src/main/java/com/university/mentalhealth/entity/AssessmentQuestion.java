package com.university.mentalhealth.entity;

public class AssessmentQuestion {
    private int id;
    private int assessmentId;
    private String questionText;
    private Integer questionOrder;
    private String options; // JSON格式存储选项

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    @Override
    public String toString() {
        return "AssessmentQuestion{" +
                "id=" + id +
                ", assessmentId=" + assessmentId +
                ", questionText='" + questionText + '\'' +
                ", questionOrder=" + questionOrder +
                '}';
    }
}