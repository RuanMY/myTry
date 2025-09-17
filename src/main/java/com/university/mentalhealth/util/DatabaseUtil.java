package com.university.mentalhealth.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {
    private static final Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
    private static Properties prop = new Properties();

    static {
        loadProperties();
        loadDriver();
    }

    private static void loadProperties() {
        try {
            // 临时使用文件路径
            String configPath = "src/main/resources/db.properties";
            try (InputStream input = new FileInputStream(configPath)) {
                prop.load(input);
                logger.info("数据库配置文件加载成功");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "加载数据库配置失败", e);
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(prop.getProperty("db.driver"));
            logger.info("数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "加载数据库驱动失败", e);
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    prop.getProperty("db.url"),
                    prop.getProperty("db.user"),
                    prop.getProperty("db.password")
            );
            logger.info("数据库连接成功");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取数据库连接失败", e);
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.info("数据库连接已关闭");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "关闭数据库连接失败", e);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "关闭Statement失败", e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "关闭ResultSet失败", e);
            }
        }
    }
}