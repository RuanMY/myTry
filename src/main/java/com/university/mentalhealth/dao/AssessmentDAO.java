package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.Assessment;
import com.university.mentalhealth.entity.AssessmentQuestion;
import com.university.mentalhealth.entity.AssessmentSession;
import com.university.mentalhealth.entity.AssessmentAnswer;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssessmentDAO {
    private static final Logger logger = Logger.getLogger(AssessmentDAO.class.getName());

    // Assessment 相关方法
    public List<Assessment> getAllAssessments() {
        List<Assessment> assessments = new ArrayList<>();
        String sql = "SELECT * FROM assessments WHERE is_active = true ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            if (conn == null) {
                logger.severe("数据库连接为null");
                return assessments;
            }
            if (conn.isClosed()) {
                logger.severe("数据库连接已关闭");
                return assessments;
            }

            logger.info("执行SQL: " + sql);
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                Assessment assessment = extractAssessmentFromResultSet(rs);
                assessments.add(assessment);
                logger.info("成功加载量表: " + assessment.getName() +
                        ", ID: " + assessment.getId() +
                        ", 状态: " + rs.getBoolean("is_active"));
            }

            logger.info("数据库查询完成，找到 " + count + " 个量表");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "数据库查询失败", e);
            // 添加详细的错误信息
            logger.severe("SQLState: " + e.getSQLState());
            logger.severe("Error Code: " + e.getErrorCode());
            logger.severe("Message: " + e.getMessage());
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return assessments;
    }

    public Optional<Assessment> getAssessmentById(int id) {
        String sql = "SELECT * FROM assessments WHERE id = ? AND is_active = true";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Assessment assessment = extractAssessmentFromResultSet(rs);
                return Optional.of(assessment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID获取测评量表失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    // AssessmentQuestion 相关方法
    public List<AssessmentQuestion> getQuestionsByAssessmentId(int assessmentId) {
        List<AssessmentQuestion> questions = new ArrayList<>();
        String sql = "SELECT * FROM assessment_questions WHERE assessment_id = ? ORDER BY question_order";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, assessmentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                AssessmentQuestion question = extractQuestionFromResultSet(rs);
                questions.add(question);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取测评题目失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return questions;
    }

    // AssessmentSession 相关方法
    public boolean saveAssessmentSession(AssessmentSession session) {
        String sql = "INSERT INTO assessment_sessions (student_id, assessment_id, total_score, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, session.getStudentId());
            pstmt.setLong(2, session.getAssessmentId());
            pstmt.setInt(3, session.getTotalScore());
            pstmt.setTimestamp(4, Timestamp.valueOf(session.getStartTime()));
            pstmt.setTimestamp(5, Timestamp.valueOf(session.getEndTime()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setId(generatedKeys.getInt(1));
                        logger.info("测评会话保存成功: session_id=" + session.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存测评会话失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("保存测评会话失败");
        return false;
    }

    // AssessmentAnswer 相关方法
    public boolean saveAssessmentAnswers(List<AssessmentAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return true;
        }

        String sql = "INSERT INTO assessment_answers (session_id, question_id, answer_value) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (AssessmentAnswer answer : answers) {
                pstmt.setLong(1, answer.getSessionId());
                pstmt.setLong(2, answer.getQuestionId());
                pstmt.setInt(3, answer.getAnswerValue());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            logger.info("保存测评答案成功，共保存 " + results.length + " 个答案");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存测评答案失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return false;
    }

    // 获取学生测评历史
    public List<AssessmentSession> getStudentSessions(int studentId) {
        List<AssessmentSession> sessions = new ArrayList<>();
        String sql = "SELECT s.*, a.name as assessment_name " +
                "FROM assessment_sessions s " +
                "JOIN assessments a ON s.assessment_id = a.id " +
                "WHERE s.student_id = ? " +
                "ORDER BY s.end_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                AssessmentSession session = extractSessionFromResultSet(rs);
                sessions.add(session);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取学生测评历史失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return sessions;
    }

    // 提取方法
    private Assessment extractAssessmentFromResultSet(ResultSet rs) throws SQLException {
        Assessment assessment = new Assessment();

        // 将getLong改为getInt
        assessment.setId(rs.getInt("id"));  // ❌ getLong() → ✅ getInt()
        assessment.setName(rs.getString("name"));
        assessment.setDescription(rs.getString("description"));
        assessment.setTotalQuestions(rs.getInt("total_questions"));
        assessment.setRiskThreshold(rs.getInt("risk_threshold"));
        assessment.setInterpretationRules(rs.getString("interpretation_rules"));

        // 添加is_active字段提取
        try {
            boolean isActive = rs.getBoolean("is_active");
            // 如果需要，可以在Assessment实体类中添加这个字段
        } catch (SQLException e) {
            logger.warning("is_active字段不存在或无法访问: " + e.getMessage());
        }

        return assessment;
    }

    private AssessmentQuestion extractQuestionFromResultSet(ResultSet rs) throws SQLException {
        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(rs.getInt("id"));
        question.setAssessmentId(rs.getInt("assessment_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setQuestionOrder(rs.getInt("question_order"));
        question.setOptions(rs.getString("options"));
        return question;
    }

    private AssessmentSession extractSessionFromResultSet(ResultSet rs) throws SQLException {
        AssessmentSession session = new AssessmentSession();
        session.setId(rs.getInt("id"));
        session.setStudentId(rs.getInt("student_id"));
        session.setAssessmentId(rs.getInt("assessment_id"));
        session.setAssessmentName(rs.getString("assessment_name"));
        session.setTotalScore(rs.getInt("total_score"));
        session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        return session;
    }

    // 在AssessmentDAO中添加新方法

    /**
     * 获取学生的测评完成情况
     */
    public Map<Long, Integer> getStudentCompletionStatus(int studentId) {
        Map<Long, Integer> completionStatus = new HashMap<>();
        String sql = "SELECT assessment_id, COUNT(*) as completion_count " +
                "FROM assessment_sessions " +
                "WHERE student_id = ? " +
                "GROUP BY assessment_id";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Long assessmentId = rs.getLong("assessment_id");
                int count = rs.getInt("completion_count");
                completionStatus.put(assessmentId, count);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取学生测评完成情况失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return completionStatus;
    }

    /**
     * 获取测评的平均分
     */
    public double getAssessmentAverageScore(int assessmentId) {
        String sql = "SELECT AVG(total_score) as average_score " +
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
                return rs.getDouble("average_score");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取测评平均分失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return 0.0;
    }
}