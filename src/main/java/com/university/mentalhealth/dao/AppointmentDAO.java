package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentDAO implements BaseDAO<Appointment> {
    private static final Logger logger = Logger.getLogger(AppointmentDAO.class.getName());

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT a.*, s.name as student_name, c.name as counselor_name, " +
                "ts.start_time, ts.end_time " +
                "FROM appointments a " +
                "LEFT JOIN students s ON a.student_id = s.user_id " +
                "LEFT JOIN counselors c ON a.counselor_id = c.user_id " +
                "LEFT JOIN counselor_time_slots ts ON a.time_slot_id = ts.id " +
                "WHERE a.id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                return Optional.of(appointment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID查询预约失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return Optional.empty();
    }

    public List<Appointment> findByStudentId(int studentId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, c.name as counselor_name, " +
                "ts.start_time, ts.end_time " +
                "FROM appointments a " +
                "LEFT JOIN students s ON a.student_id = s.user_id " +
                "LEFT JOIN counselors c ON a.counselor_id = c.user_id " +
                "LEFT JOIN counselor_time_slots ts ON a.time_slot_id = ts.id " +
                "WHERE a.student_id = ? ORDER BY ts.start_time DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据学生ID查询预约失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return appointments;
    }

    public List<Appointment> findByCounselorId(int counselorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, c.name as counselor_name, " +
                "ts.start_time, ts.end_time " +
                "FROM appointments a " +
                "LEFT JOIN students s ON a.student_id = s.user_id " +
                "LEFT JOIN counselors c ON a.counselor_id = c.user_id " +
                "LEFT JOIN counselor_time_slots ts ON a.time_slot_id = ts.id " +
                "WHERE a.counselor_id = ? ORDER BY ts.start_time DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, counselorId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据咨询师ID查询预约失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return appointments;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, c.name as counselor_name, " +
                "ts.start_time, ts.end_time " +
                "FROM appointments a " +
                "LEFT JOIN students s ON a.student_id = s.user_id " +
                "LEFT JOIN counselors c ON a.counselor_id = c.user_id " +
                "LEFT JOIN counselor_time_slots ts ON a.time_slot_id = ts.id " +
                "ORDER BY ts.start_time DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有预约失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return appointments;
    }

    @Override
    public boolean save(Appointment appointment) {
        String sql = "INSERT INTO appointments (student_id, counselor_id, time_slot_id, status, notes) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, appointment.getStudentId());
            pstmt.setInt(2, appointment.getCounselorId());
            pstmt.setInt(3, appointment.getTimeSlotId());
            pstmt.setString(4, appointment.getStatus());
            pstmt.setString(5, appointment.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointment.setId(generatedKeys.getInt(1));
                        logger.info("预约保存成功: appointment_id=" + appointment.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存预约失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET status = ?, notes = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, appointment.getStatus());
            pstmt.setString(2, appointment.getNotes());
            pstmt.setInt(3, appointment.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("预约更新成功: appointment_id=" + appointment.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新预约失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("预约状态更新成功: appointment_id=" + appointmentId + ", status=" + status);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新预约状态失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("预约删除成功: appointment_id=" + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "删除预约失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    public boolean isTimeSlotBooked(int timeSlotId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE time_slot_id = ? AND status IN ('pending', 'confirmed')";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, timeSlotId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "检查时间段是否被预约失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setStudentId(rs.getInt("student_id"));
        appointment.setCounselorId(rs.getInt("counselor_id"));
        appointment.setTimeSlotId(rs.getInt("time_slot_id"));
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        appointment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        // 附加信息
        appointment.setStudentName(rs.getString("student_name"));
        appointment.setCounselorName(rs.getString("counselor_name"));
        if (rs.getTimestamp("start_time") != null) {
            appointment.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            appointment.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }

        return appointment;
    }
}