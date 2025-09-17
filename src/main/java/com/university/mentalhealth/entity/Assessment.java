package com.university.mentalhealth.entity;

import java.time.LocalDateTime;

public class Assessment {
    private Integer id;
    private String name;
    private String description;
    private Integer totalQuestions;
    private Integer riskThreshold;
    private String interpretationRules;
    private LocalDateTime createdAt;

    // Getter和Setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getRiskThreshold() { return riskThreshold; }
    public void setRiskThreshold(Integer riskThreshold) { this.riskThreshold = riskThreshold; }

    public String getInterpretationRules() { return interpretationRules; }
    public void setInterpretationRules(String interpretationRules) { this.interpretationRules = interpretationRules; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Assessment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalQuestions=" + totalQuestions +
                ", riskThreshold=" + riskThreshold +
                '}';
    }
}
