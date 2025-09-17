package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.TimeSlot;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeSlotDAO implements BaseDAO<TimeSlot> {
    private static final Logger logger = Logger.getLogger(TimeSlotDAO.class.getName());

    @Override
    public Optional<TimeSlot> findById(int id) {
        String sql = "SELECT ts.*, c.name as counselor_name, c.title " +
                "FROM counselor_time_slots ts " +
                "LEFT JOIN counselors c ON ts.counselor_id = c.user_id " +
                "WHERE ts.id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                TimeSlot timeSlot = extractTimeSlotFromResultSet(rs);
                return Optional.of(timeSlot);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID查询时间段失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return Optional.empty();
    }

    public List<TimeSlot> findByCounselorId(int counselorId) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        String sql = "SELECT ts.*, c.name as counselor_name, c.title " +
                "FROM counselor_time_slots ts " +
                "LEFT JOIN counselors c ON ts.counselor_id = c.user_id " +
                "WHERE ts.counselor_id = ? ORDER BY ts.start_time";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, counselorId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TimeSlot timeSlot = extractTimeSlotFromResultSet(rs);
                timeSlots.add(timeSlot);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据咨询师ID查询时间段失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return timeSlots;
    }

    public List<TimeSlot> findAvailableTimeSlots(int counselorId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        String sql = "SELECT ts.*, c.name as counselor_name, c.title " +
                "FROM counselor_time_slots ts " +
                "LEFT JOIN counselors c ON ts.counselor_id = c.user_id " +
                "WHERE ts.counselor_id = ? AND ts.status = 'available' " +
                "AND ts.start_time >= ? AND ts.end_time <= ? " +
                "ORDER BY ts.start_time";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, counselorId);
            pstmt.setTimestamp(2, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(3, Timestamp.valueOf(endDate)); // 这里修正了参数索引
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TimeSlot timeSlot = extractTimeSlotFromResultSet(rs);
                timeSlots.add(timeSlot);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询可用时间段失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return timeSlots;
    }

    public List<TimeSlot> findAvailableTimeSlots(LocalDateTime startDate, LocalDateTime endDate) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        String sql = "SELECT ts.*, c.name as counselor_name, c.title " +
                "FROM counselor_time_slots ts " +
                "LEFT JOIN counselors c ON ts.counselor_id = c.user_id " +
                "WHERE ts.status = 'available' AND ts.start_time >= ? AND ts.end_time <= ? " +
                "ORDER BY ts.counselor_id, ts.start_time";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate)); // 修正参数索引
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));   // 修正参数索引
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TimeSlot timeSlot = extractTimeSlotFromResultSet(rs);
                timeSlots.add(timeSlot);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有可用时间段失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return timeSlots;
    }

    @Override
    public List<TimeSlot> findAll() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        String sql = "SELECT ts.*, c.name as counselor_name, c.title " +
                "FROM counselor_time_slots ts " +
                "LEFT JOIN counselors c ON ts.counselor_id = c.user_id " +
                "ORDER BY ts.start_time DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TimeSlot timeSlot = extractTimeSlotFromResultSet(rs);
                timeSlots.add(timeSlot);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有时间段失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return timeSlots;
    }

    @Override
    public boolean save(TimeSlot timeSlot) {
        String sql = "INSERT INTO counselor_time_slots (counselor_id, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, timeSlot.getCounselorId());
            pstmt.setTimestamp(2, Timestamp.valueOf(timeSlot.getStartTime()));
            pstmt.setTimestamp(3, Timestamp.valueOf(timeSlot.getEndTime()));
            pstmt.setString(4, timeSlot.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        timeSlot.setId(generatedKeys.getInt(1));
                        logger.info("时间段保存成功: time_slot_id=" + timeSlot.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存时间段失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean update(TimeSlot timeSlot) {
        String sql = "UPDATE counselor_time_slots SET status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, timeSlot.getStatus());
            pstmt.setInt(2, timeSlot.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("时间段状态更新成功: time_slot_id=" + timeSlot.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新时间段状态失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    public boolean updateStatus(int timeSlotId, String status) {
        String sql = "UPDATE counselor_time_slots SET status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, timeSlotId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("时间段状态更新成功: time_slot_id=" + timeSlotId + ", status=" + status);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新时间段状态失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM counselor_time_slots WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("时间段删除成功: time_slot_id=" + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "删除时间段失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    public boolean hasTimeConflict(int counselorId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT COUNT(*) FROM counselor_time_slots " +
                "WHERE counselor_id = ? AND status != 'cancelled' " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, counselorId);
            pstmt.setTimestamp(2, Timestamp.valueOf(endTime));     // 修正参数索引
            pstmt.setTimestamp(3, Timestamp.valueOf(startTime));   // 修正参数索引
            pstmt.setTimestamp(4, Timestamp.valueOf(endTime));     // 修正参数索引
            pstmt.setTimestamp(5, Timestamp.valueOf(startTime));   // 修正参数索引
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "检查时间冲突失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    private TimeSlot extractTimeSlotFromResultSet(ResultSet rs) throws SQLException {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(rs.getInt("id"));
        timeSlot.setCounselorId(rs.getInt("counselor_id"));
        timeSlot.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        timeSlot.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        timeSlot.setStatus(rs.getString("status"));

        // 附加信息
        timeSlot.setCounselorName(rs.getString("counselor_name"));
        timeSlot.setTitle(rs.getString("title"));

        return timeSlot;
    }
}