package com.university.mentalhealth.test;

import com.university.mentalhealth.dao.UserDAO;
import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.logging.Logger;

public class DetailedLoginTest {
    private static final Logger logger = Logger.getLogger(DetailedLoginTest.class.getName());

    public static void main(String[] args) {
        testDatabaseConnection();
        testUserDetailedInfo("stu001");
        testAuthenticationProcess("stu001", "123456");
    }

    private static void testDatabaseConnection() {
        logger.info("=== 数据库连接测试 ===");
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                logger.info("数据库连接成功");
            } else {
                logger.severe("数据库连接失败");
            }
        } catch (Exception e) {
            logger.severe("数据库连接异常: " + e.getMessage());
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }

    private static void testUserDetailedInfo(String username) {
        logger.info("\n=== 用户详细信息检查 ===");
        String sql = "SELECT username, password_hash, type, is_active FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                logger.info("用户名: " + rs.getString("username"));
                logger.info("密码: '" + rs.getString("password_hash") + "'");
                logger.info("类型: " + rs.getString("type"));
                logger.info("是否激活: " + rs.getBoolean("is_active"));
            } else {
                logger.warning("用户不存在: " + username);
            }
        } catch (Exception e) {
            logger.severe("查询用户信息失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
    }

    private static void testAuthenticationProcess(String username, String password) {
        logger.info("\n=== 认证过程测试 ===");
        UserDAO userDAO = new UserDAO();

        // 测试认证
        Optional<User> userOpt = userDAO.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("认证成功");
            logger.info("用户ID: " + user.getId());
            logger.info("用户名: " + user.getUsername());
            logger.info("用户类型: " + user.getType());
        } else {
            logger.warning("认证失败");
        }
    }
}
