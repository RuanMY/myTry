package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.AssessmentDAO;
import com.university.mentalhealth.entity.*;
import com.university.mentalhealth.util.DatabaseUtil;
import com.university.mentalhealth.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssessmentService {
    private static final Logger logger = Logger.getLogger(AssessmentService.class.getName());
    private final AssessmentDAO assessmentDAO;

    public AssessmentService() {
        this.assessmentDAO = new AssessmentDAO();
    }

    /**
     * 获取所有可用的测评量表
     */
    public List<Assessment> getAvailableAssessments() {
        return assessmentDAO.getAllAssessments();
    }

    /**
     * 根据ID获取测评量表
     */
    public Optional<Assessment> getAssessmentById(int assessmentId) {
        return assessmentDAO.getAssessmentById(assessmentId);
    }

    /**
     * 获取测评量表的题目
     */
    public List<AssessmentQuestion> getAssessmentQuestions(int assessmentId) {
        return assessmentDAO.getQuestionsByAssessmentId(assessmentId);
    }

    /**
     * 开始新的测评会话
     */
    public AssessmentSession startAssessmentSession(int assessmentId) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("只有学生可以开始测评");
            return null;
        }

        AssessmentSession session = new AssessmentSession();
        session.setStudentId(SessionManager.currentUser.getId());
        session.setAssessmentId(assessmentId);
        session.setStartTime(LocalDateTime.now());
        session.setTotalScore(0);

        return session;
    }

    /**
     * 提交测评答案并计算分数
     */
    public AssessmentSession submitAssessment(AssessmentSession session, List<AssessmentAnswer> answers) {
        if (session == null || answers == null || answers.isEmpty()) {
            logger.warning("测评数据不完整");
            return null;
        }

        try {
            // 计算总分
            int totalScore = calculateTotalScore(answers);
            session.setTotalScore(totalScore);
            session.setEndTime(LocalDateTime.now());

            // 保存测评会话
            boolean sessionSaved = assessmentDAO.saveAssessmentSession(session);
            if (!sessionSaved) {
                logger.severe("保存测评会话失败");
                return null;
            }

            // 设置答案的sessionId并保存
            for (AssessmentAnswer answer : answers) {
                answer.setSessionId(session.getId());
            }

            boolean answersSaved = assessmentDAO.saveAssessmentAnswers(answers);
            if (!answersSaved) {
                logger.severe("保存测评答案失败");
                return null;
            }

            logger.info("测评提交成功: session_id=" + session.getId() + ", 分数=" + totalScore);
            return session;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "提交测评过程中发生错误", e);
            return null;
        }
    }

    /**
     * 计算测评总分
     */
    private int calculateTotalScore(List<AssessmentAnswer> answers) {
        int totalScore = 0;
        for (AssessmentAnswer answer : answers) {
            totalScore += answer.getAnswerValue();
        }
        return totalScore;
    }

    /**
     * 获取测评结果解释
     */
    public String getAssessmentResult(Assessment assessment, int totalScore) {
        if (assessment == null) {
            return "无法获取测评结果";
        }

        // 这里可以根据interpretationRules解析结果
        // 简化处理：直接根据分数和阈值判断
        if (totalScore >= assessment.getRiskThreshold()) {
            return "测评结果显示可能存在心理健康风险，建议咨询专业人士";
        } else {
            return "测评结果正常，建议保持健康的生活方式";
        }
    }

    /**
     * 获取当前学生的测评历史
     */
    public List<AssessmentSession> getStudentAssessmentHistory() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("只有学生可以查看测评历史");
            return new ArrayList<>();
        }

        int studentId = SessionManager.currentUser.getId();
        return assessmentDAO.getStudentSessions(studentId);
    }

    /**
     * 检查学生是否已完成某个测评
     */
    public boolean hasCompletedAssessment(int assessmentId) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            return false;
        }

        int studentId = SessionManager.currentUser.getId();
        List<AssessmentSession> sessions = assessmentDAO.getStudentSessions(studentId);

        for (AssessmentSession session : sessions) {
            if (session.getAssessmentId() == assessmentId) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取风险评估级别
     */
    public String getRiskLevel(Assessment assessment, int totalScore) {
        if (assessment == null) {
            return "未知";
        }

        int threshold = assessment.getRiskThreshold();

        if (totalScore >= threshold + 10) {
            return "高风险";
        } else if (totalScore >= threshold) {
            return "中风险";
        } else if (totalScore >= threshold - 5) {
            return "低风险";
        } else {
            return "正常";
        }
    }

    /**
     * 获取详细的测评结果解释
     */
    public String getDetailedAssessmentResult(Assessment assessment, int totalScore) {
        if (assessment == null) {
            return "无法获取测评结果";
        }

        String assessmentName = assessment.getName();

        if ("PHQ-9抑郁症筛查".equals(assessmentName)) {
            return getPHQ9Result(totalScore);
        } else if ("GAD-7焦虑症筛查".equals(assessmentName)) {
            return getGAD7Result(totalScore);
        } else if ("压力感知量表".equals(assessmentName)) {
            return getStressAssessmentResult(totalScore);
        }

        return getGeneralAssessmentResult(assessment, totalScore);
    }

    /**
     * 获取压力测评建议
     */
    private String getStressAdvice(int score) {
        if (score <= 10) {
            return "继续保持健康的生活方式和积极的心态，定期进行压力自我评估。";
        } else if (score <= 20) {
            return "建议学习压力管理技巧，如时间管理、放松训练和适当运动。";
        } else {
            return "强烈建议寻求专业帮助，学习压力应对策略，必要时考虑心理咨询。";
        }
    }

    /**
     * PHQ-9抑郁症筛查结果解释
     */
    private String getPHQ9Result(int score) {
        StringBuilder result = new StringBuilder();
        result.append("PHQ-9抑郁症筛查结果分析：\n\n");
        result.append("您的得分：").append(score).append("分（总分27分）\n\n");

        if (score >= 0 && score <= 4) {
            result.append("✅ 结果：无抑郁症状\n");
            result.append("说明：您的情绪状态良好，没有明显的抑郁症状。建议保持健康的生活方式。");
        } else if (score >= 5 && score <= 9) {
            result.append("⚠️ 结果：轻度抑郁\n");
            result.append("说明：您可能有轻微的抑郁症状。建议关注情绪变化，适当进行放松和调节。");
        } else if (score >= 10 && score <= 14) {
            result.append("⚠️ 结果：中度抑郁\n");
            result.append("说明：您有明显的抑郁症状。建议寻求朋友或家人的支持，考虑咨询专业人士。");
        } else if (score >= 15 && score <= 19) {
            result.append("❌ 结果：中重度抑郁\n");
            result.append("说明：您的抑郁症状比较严重。强烈建议咨询心理医生或专业咨询师。");
        } else {
            result.append("❌ 结果：重度抑郁\n");
            result.append("说明：您的抑郁症状非常严重。请立即寻求专业帮助，联系心理咨询师或精神科医生。");
        }

        result.append("\n\n建议：").append(getPHQ9Advice(score));
        return result.toString();
    }

    /**
     * GAD-7焦虑症筛查结果解释
     */
    private String getGAD7Result(int score) {
        StringBuilder result = new StringBuilder();
        result.append("GAD-7焦虑症筛查结果分析：\n\n");
        result.append("您的得分：").append(score).append("分（总分21分）\n\n");

        if (score >= 0 && score <= 4) {
            result.append("✅ 结果：无焦虑症状\n");
            result.append("说明：您的情绪状态良好，没有明显的焦虑症状。");
        } else if (score >= 5 && score <= 9) {
            result.append("⚠️ 结果：轻度焦虑\n");
            result.append("说明：您可能有轻微的焦虑症状。建议学习放松技巧，适当运动。");
        } else if (score >= 10 && score <= 14) {
            result.append("⚠️ 结果：中度焦虑\n");
            result.append("说明：您有明显的焦虑症状。建议寻求支持，考虑咨询专业人士。");
        } else {
            result.append("❌ 结果：重度焦虑\n");
            result.append("说明：您的焦虑症状比较严重。建议咨询心理医生或专业咨询师。");
        }

        result.append("\n\n建议：").append(getGAD7Advice(score));
        return result.toString();
    }

    /**
     * 获取PHQ-9建议
     */
    private String getPHQ9Advice(int score) {
        if (score <= 4) {
            return "保持规律作息，多参与社交活动，保持积极心态。";
        } else if (score <= 9) {
            return "尝试每天进行30分钟有氧运动，练习深呼吸和放松技巧。";
        } else if (score <= 14) {
            return "建议与信任的人倾诉，考虑预约校园心理咨询服务。";
        } else if (score <= 19) {
            return "强烈建议预约专业心理咨询，保持规律就医。";
        } else {
            return "请立即联系心理咨询中心或前往医院精神科就诊，不要独自承受。";
        }
    }

    /**
     * 获取GAD-7建议
     */
    private String getGAD7Advice(int score) {
        if (score <= 4) {
            return "继续保持健康的生活方式，学习压力管理技巧。";
        } else if (score <= 9) {
            return "练习正念冥想，减少咖啡因摄入，保持充足睡眠。";
        } else if (score <= 14) {
            return "建议学习认知行为疗法技巧，考虑专业咨询。";
        } else {
            return "请预约专业心理咨询，学习焦虑管理策略，必要时考虑药物治疗。";
        }
    }

    /**
     * 通用测评结果解释
     */
    private String getGeneralAssessmentResult(Assessment assessment, int score) {
        int threshold = assessment.getRiskThreshold();

        StringBuilder result = new StringBuilder();
        result.append(assessment.getName()).append("结果分析：\n\n");
        result.append("您的得分：").append(score).append("分\n");
        result.append("风险评估阈值：").append(threshold).append("分\n\n");

        if (score < threshold) {
            result.append("✅ 结果：正常范围\n");
            result.append("说明：您的测评结果在正常范围内。");
        } else {
            result.append("⚠️ 结果：需要关注\n");
            result.append("说明：您的测评结果超出正常范围，建议关注相关症状。");
        }

        return result.toString();
    }

    /**
     * 获取推荐测评
     */
    public List<Assessment> getRecommendedAssessments() {
        List<Assessment> allAssessments = assessmentDAO.getAllAssessments();
        List<Assessment> recommended = new ArrayList<>();

        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            return recommended;
        }

        int studentId = SessionManager.currentUser.getId();

        // 简单推荐逻辑：推荐未完成或最近未做的测评
        for (Assessment assessment : allAssessments) {
            if (!hasCompletedAssessment(assessment.getId())) {
                recommended.add(assessment);
            }
        }

        return recommended;
    }

    /**
     * 压力测评结果解释
     */
    private String getStressAssessmentResult(int score) {
        StringBuilder result = new StringBuilder();
        result.append("压力感知量表结果分析：\n\n");
        result.append("您的得分：").append(score).append("分（总分30分）\n\n");

        if (score >= 0 && score <= 10) {
            result.append("✅ 结果：压力水平较低\n");
            result.append("说明：您目前的压力水平在正常范围内，能够很好地应对生活中的挑战。");
        } else if (score >= 11 && score <= 20) {
            result.append("⚠️ 结果：压力水平中等\n");
            result.append("说明：您感受到一定的压力，但仍在可管理范围内。建议关注压力源并适当调整。");
        } else if (score >= 21 && score <= 30) {
            result.append("❌ 结果：压力水平较高\n");
            result.append("说明：您正经历较高的压力水平，可能会影响身心健康。建议寻求适当的减压方法。");
        } else {
            result.append("❓ 结果：分数异常\n");
            result.append("说明：测评分数超出正常范围，请重新进行测评。");
        }

        result.append("\n\n建议：").append(getStressAdvice(score));
        return result.toString();
    }

    /**
     * 获取详细的测评历史记录（包含题目和答案）
     */
    public List<AssessmentDetail> getAssessmentDetails(int sessionId) {
        List<AssessmentDetail> details = new ArrayList<>();
        String sql = "SELECT aq.question_text, aq.options, aa.answer_value " +
                "FROM assessment_answers aa " +
                "JOIN assessment_questions aq ON aa.question_id = aq.id " +
                "WHERE aa.session_id = ? " +
                "ORDER BY aq.question_order";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, sessionId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                AssessmentDetail detail = new AssessmentDetail();
                detail.setQuestionText(rs.getString("question_text"));
                detail.setOptions(rs.getString("options"));
                detail.setAnswerValue(rs.getInt("answer_value"));
                details.add(detail);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取测评详情失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return details;
    }

    /**
     * 获取测评统计数据
     */
    public Map<String, Object> getAssessmentStatistics(int assessmentId) {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT " +
                "COUNT(*) as total_completions, " +
                "AVG(total_score) as average_score, " +
                "MAX(total_score) as max_score, " +
                "MIN(total_score) as min_score " +
                "FROM assessment_sessions " +
                "WHERE assessment_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, assessmentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("totalCompletions", rs.getInt("total_completions"));
                stats.put("averageScore", rs.getDouble("average_score"));
                stats.put("maxScore", rs.getInt("max_score"));
                stats.put("minScore", rs.getInt("min_score"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取测评统计失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return stats;
    }

    /**
     * 生成测评报告文本
     */
    public String generateReportText(AssessmentSession session, Assessment assessment,
                                     List<AssessmentDetail> details) {
        StringBuilder report = new StringBuilder();

        // 替换：report.append("=").append("=".repeat(50)).append("\n\n");
        report.append("=").append(repeatString("=", 50)).append("\n\n");

        report.append("测评名称: ").append(assessment.getName()).append("\n");
        report.append("测评时间: ").append(session.getEndTime()).append("\n");
        report.append("总得分: ").append(session.getTotalScore()).append("/");
        report.append(assessment.getTotalQuestions() * 3).append("\n\n");

        report.append("详细结果分析:\n");
        // 替换：report.append("-".repeat(50)).append("\n");
        report.append(repeatString("-", 50)).append("\n");
        report.append(getDetailedAssessmentResult(assessment, session.getTotalScore())).append("\n\n");

        report.append("题目作答情况:\n");
        // 替换：report.append("-".repeat(50)).append("\n");
        report.append(repeatString("-", 50)).append("\n");
        for (int i = 0; i < details.size(); i++) {
            AssessmentDetail detail = details.get(i);
            report.append(i + 1).append(". ").append(detail.getQuestionText()).append("\n");
            report.append("   答案: ").append(getAnswerText(detail.getAnswerValue())).append("\n");
        }

        return report.toString();
    }

    private String repeatString(String str, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }

    /**
     * 重载方法用于字符重复
     */
    private String repeatString(char ch, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(ch);
        }
        return result.toString();
    }

    private String getAnswerText(int value) {
        switch (value) {
            case 0: return "完全没有";
            case 1: return "有几天";
            case 2: return "一半以上时间";
            case 3: return "几乎每天";
            default: return "未知";
        }
    }
}

/**
 * 测评统计信息类
 */
class AssessmentStatistics {
    private int completionCount;
    private int averageScore;
    private LocalDateTime lastCompletionDate;

    // Getter和Setter方法
    public int getCompletionCount() { return completionCount; }
    public void setCompletionCount(int completionCount) { this.completionCount = completionCount; }

    public int getAverageScore() { return averageScore; }
    public void setAverageScore(int averageScore) { this.averageScore = averageScore; }

    public LocalDateTime getLastCompletionDate() { return lastCompletionDate; }
    public void setLastCompletionDate(LocalDateTime lastCompletionDate) { this.lastCompletionDate = lastCompletionDate; }
}