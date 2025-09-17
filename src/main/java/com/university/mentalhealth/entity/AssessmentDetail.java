package com.university.mentalhealth.entity;

/**
 * 测评详情实体类
 */
public class AssessmentDetail {
    private String questionText;
    private String options;
    private Integer answerValue;

    // Getter和Setter方法
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public Integer getAnswerValue() { return answerValue; }
    public void setAnswerValue(Integer answerValue) { this.answerValue = answerValue; }
}