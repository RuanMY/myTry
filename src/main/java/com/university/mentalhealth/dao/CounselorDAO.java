package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.Counselor;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CounselorDAO implements BaseDAO<Counselor> {
    private static final Logger logger = Logger.getLogger(CounselorDAO.class.getName());

    @Override
    public Optional<Counselor> findById(int id) {
        String sql = "SELECT u.*, c.name, c.title, c.specialization, c.is_available " +
                "FROM users u " +
                "JOIN counselors c ON u.id = c.user_id " +
                "WHERE u.id = ? AND u.is_active = true";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Counselor counselor = extractCounselorFromResultSet(rs);
                return Optional.of(counselor);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID查询咨询师失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<Counselor> findByUserId(int userId) {
        String sql = "SELECT u.*, c.name, c.title, c.specialization, c.is_available " +
                "FROM users u " +
                "JOIN counselors c ON u.id = c.user_id " +
                "WHERE u.id = ? AND u.is_active = true";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Counselor counselor = extractCounselorFromResultSet(rs);
                return Optional.of(counselor);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据用户ID查询咨询师失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Counselor> findAll() {
        List<Counselor> counselors = new ArrayList<>();
        String sql = "SELECT u.*, c.name, c.title, c.specialization, c.is_available " +
                "FROM users u " +
                "JOIN counselors c ON u.id = c.user_id " +
                "WHERE u.is_active = true AND c.is_available = true " +
                "ORDER BY c.name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Counselor counselor = extractCounselorFromResultSet(rs);
                counselors.add(counselor);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有咨询师失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return counselors;
    }

    public List<Counselor> findAvailableCounselors() {
        List<Counselor> counselors = new ArrayList<>();
        String sql = "SELECT u.*, c.name, c.title, c.specialization, c.is_available " +
                "FROM users u " +
                "JOIN counselors c ON u.id = c.user_id " +
                "WHERE u.is_active = true AND c.is_available = true " +
                "ORDER BY c.name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Counselor counselor = extractCounselorFromResultSet(rs);
                counselors.add(counselor);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询可用咨询师失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return counselors;
    }

    @Override
    public boolean save(Counselor counselor) {
        // 咨询师信息通常在用户注册时创建，这里主要处理更新
        return update(counselor);
    }

    @Override
    public boolean update(Counselor counselor) {
        String sql = "UPDATE counselors SET name = ?, title = ?, specialization = ?, is_available = ? " +
                "WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, counselor.getName());
            pstmt.setString(2, counselor.getTitle());
            pstmt.setString(3, counselor.getSpecialization());
            pstmt.setBoolean(4, counselor.getIsAvailable());
            pstmt.setInt(5, counselor.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("咨询师信息更新成功: user_id=" + counselor.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新咨询师信息失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    public boolean updateAvailability(int counselorId, boolean isAvailable) {
        String sql = "UPDATE counselors SET is_available = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, counselorId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("咨询师可用状态更新成功: user_id=" + counselorId + ", is_available=" + isAvailable);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新咨询师可用状态失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        // 逻辑删除，通过禁用用户实现
        String sql = "UPDATE users SET is_active = false WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("咨询师禁用成功: user_id=" + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "禁用咨询师失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
        return false;
    }

    private Counselor extractCounselorFromResultSet(ResultSet rs) throws SQLException {
        Counselor counselor = new Counselor();
        counselor.setId(rs.getInt("id"));
        counselor.setUsername(rs.getString("username"));
        counselor.setPasswordHash(rs.getString("password_hash"));
        counselor.setType(com.university.mentalhealth.entity.UserType.valueOf(rs.getString("type").toLowerCase()));
        counselor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        counselor.setName(rs.getString("name"));
        counselor.setTitle(rs.getString("title"));
        counselor.setSpecialization(rs.getString("specialization"));
        counselor.setIsAvailable(rs.getBoolean("is_available"));

        return counselor;
    }
}