package com.university.mentalhealth.entity;

public class AssessmentAnswer {
    private int id;
    private int sessionId;
    private int questionId;
    private Integer answerValue;

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public Integer getAnswerValue() { return answerValue; }
    public void setAnswerValue(Integer answerValue) { this.answerValue = answerValue; }

    @Override
    public String toString() {
        return "AssessmentAnswer{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", questionId=" + questionId +
                ", answerValue=" + answerValue +
                '}';
    }
}