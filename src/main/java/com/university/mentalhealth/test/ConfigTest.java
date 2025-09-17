package com.university.mentalhealth.test;

import com.university.mentalhealth.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class ConfigTest {
    private static final Logger logger = Logger.getLogger(ConfigTest.class.getName());

    public static void main(String[] args) {
        testDatabaseConnection();
        testLogger();
    }

    private static void testDatabaseConnection() {
        logger.info("开始测试数据库连接...");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                logger.info("数据库连接成功!");

                // 测试查询
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");

                if (rs.next()) {
                    int count = rs.getInt("count");
                    logger.info("用户表记录数: " + count);
                }
            }
        } catch (Exception e) {
            logger.severe("数据库连接测试失败: " + e.getMessage());
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(stmt);
            DatabaseUtil.closeConnection(conn);
        }
    }

    private static void testLogger() {
        logger.info("开始测试日志系统...");

        logger.fine("这是一条FINE级别的日志");
        logger.info("这是一条INFO级别的日志");
        logger.warning("这是一条WARNING级别的日志");
        logger.severe("这是一条SEVERE级别的日志");

        logger.info("日志系统测试完成!");
    }
}
