package com.university.mentalhealth.dao;

import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.entity.UserType;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO implements BaseDAO<User> {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    // 添加密码加密验证（简单示例，实际应该使用BCrypt等加密方式）
    private String encryptPassword(String password) {
        // 这里使用简单加密，实际项目中应该使用BCryptPasswordEncoder
        return password; // 暂时不加密，直接存储明文（仅用于演示）
    }

    // 修改authenticate方法中使用加密  pstmt.setString(2, encryptPassword(password));


    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ? AND is_active = true";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID查询用户失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = true";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据用户名查询用户失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 检查用户状态
                boolean isActive = rs.getBoolean("is_active");
                String storedPassword = rs.getString("password_hash");
                String userType = rs.getString("type");

                logger.info("用户状态检查 - 活跃: " + isActive);
                logger.info("用户类型: " + userType);
                logger.info("密码比较 - 存储的: '" + storedPassword + "' vs 输入的: '" + password + "'");

                if (!isActive) {
                    logger.warning("用户账户未激活: " + username);
                    return Optional.empty();
                }

                // 直接比较明文密码
                if (storedPassword.equals(password)) {
                    User user = extractUserFromResultSet(rs);
                    logger.info("密码匹配成功，返回用户对象");
                    return Optional.of(user);
                } else {
                    logger.warning("密码不匹配");
                }
            } else {
                logger.warning("用户不存在: " + username);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "用户认证失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = true ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询所有用户失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return users;
    }

    public List<User> findByType(UserType userType) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE type = ? AND is_active = true ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userType.name());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据类型查询用户失败", e);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        return users;
    }

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password_hash, type, created_at, is_active) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, encryptPassword(user.getPasswordHash()));
            pstmt.setString(3, user.getType().name());
            pstmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setBoolean(5, true);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        logger.info("用户保存成功: " + user.getUsername());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存用户失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("用户保存失败: " + user.getUsername());
        return false;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, type = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, encryptPassword(user.getPasswordHash()));
            pstmt.setString(3, user.getType().name());
            pstmt.setLong(4, user.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("用户更新成功: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新用户失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("用户更新失败: " + user.getUsername());
        return false;
    }

    public boolean updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isActive);
            pstmt.setLong(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("用户状态更新成功: ID=" + userId + ", 状态=" + isActive);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新用户状态失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("用户状态更新失败: ID=" + userId);
        return false;
    }

    @Override
    public boolean delete(int id) {
        // 逻辑删除，不是物理删除
        return updateUserStatus(id, false);
    }

    public boolean permanentlyDelete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("用户永久删除成功: ID=" + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "永久删除用户失败", e);
        } finally {
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }

        logger.warning("用户永久删除失败: ID=" + id);
        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        UserType type = UserType.valueOf(rs.getString("type"));
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        boolean isActive = rs.getBoolean("is_active");

        User user = new User(id, username, passwordHash, type, createdAt);
        return user;
    }


}