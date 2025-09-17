package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.Student;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentDAO implements BaseDAO<Student> {
    private static final Logger logger = Logger.getLogger(StudentDAO.class.getName());

    @Override
    public Optional<Student> findById(int id) {
        String sql = "SELECT s.*, u.username, u.created_at as user_created_at " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE s.user_id = ? AND u.is_active = true";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = extractStudentFromResultSet(rs);
                return Optional.of(student);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID查询学生失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    public Optional<Student> findByStudentId(String studentId) {
        String sql = "SELECT s.*, u.username, u.created_at as user_created_at " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE s.student_id = ? AND u.is_active = true";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = extractStudentFromResultSet(rs);
                return Optional.of(student);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据学号查询学生失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    public List<Student> findByDepartment(String department) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username, u.created_at as user_created_at " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE s.department = ? AND u.is_active = true " +
                "ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, department);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = extractStudentFromResultSet(rs);
                students.add(student);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据院系查询学生失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return students;
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username, u.created_at as user_created_at " +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "WHERE u.is_active = true " +
                "ORDER BY s.student_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = extractStudentFromResultSet(rs);
                students.add(student);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有学生失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return students;
    }

    @Override
    public boolean save(Student student) {
        String sql = "INSERT INTO students (user_id, student_id, name, department, contact_phone, emergency_contact, emergency_phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, student.getId());
            pstmt.setString(2, student.getStudentId());
            pstmt.setString(3, student.getName());
            pstmt.setString(4, student.getDepartment());
            pstmt.setString(5, student.getContactPhone());
            pstmt.setString(6, student.getEmergencyContact());
            pstmt.setString(7, student.getEmergencyPhone());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("学生信息保存成功: " + student.getStudentId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存学生信息失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("学生信息保存失败: " + student.getStudentId());
        return false;
    }

    @Override
    public boolean update(Student student) {
        String sql = "UPDATE students SET student_id = ?, name = ?, department = ?, " +
                "contact_phone = ?, emergency_contact = ?, emergency_phone = ? " +
                "WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getDepartment());
            pstmt.setString(4, student.getContactPhone());
            pstmt.setString(5, student.getEmergencyContact());
            pstmt.setString(6, student.getEmergencyPhone());
            pstmt.setLong(7, student.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("学生信息更新成功: " + student.getStudentId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新学生信息失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("学生信息更新失败: " + student.getStudentId());
        return false;
    }

    @Override
    public boolean delete(int id) {
        // 学生信息通常不删除，只禁用用户账号
        logger.info("学生信息不建议删除，请通过禁用用户账号实现");
        return false;
    }

    public boolean updateContactInfo(int userId, String contactPhone, String emergencyContact, String emergencyPhone) {
        String sql = "UPDATE students SET contact_phone = ?, emergency_contact = ?, emergency_phone = ? " +
                "WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, contactPhone);
            pstmt.setString(2, emergencyContact);
            pstmt.setString(3, emergencyPhone);
            pstmt.setLong(4, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("学生联系信息更新成功: user_id=" + userId);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新学生联系信息失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("学生联系信息更新失败: user_id=" + userId);
        return false;
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String studentId = rs.getString("student_id");
        String name = rs.getString("name");
        String department = rs.getString("department");
        String contactPhone = rs.getString("contact_phone");
        String emergencyContact = rs.getString("emergency_contact");
        String emergencyPhone = rs.getString("emergency_phone");
        String username = rs.getString("username");
        Timestamp userCreatedAt = rs.getTimestamp("user_created_at");

        Student student = new Student();
        student.setId(userId);
        student.setUsername(username);
        student.setStudentId(studentId);
        student.setName(name);
        student.setDepartment(department);
        student.setContactPhone(contactPhone);
        student.setEmergencyContact(emergencyContact);
        student.setEmergencyPhone(emergencyPhone);

        return student;
    }
}
