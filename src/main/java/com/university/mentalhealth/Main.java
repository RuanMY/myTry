package com.university.mentalhealth;

import com.university.mentalhealth.ui.LoginFrame;
import com.university.mentalhealth.util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 配置日志级别
        Logger.getLogger("").setLevel(Level.ALL);

        // 控制台处理器
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        Logger.getLogger("").addHandler(handler);

        logger.info("应用程序启动...");
        logger.info("Java版本: " + System.getProperty("java.version"));
        logger.info("当前目录: " + System.getProperty("user.dir"));

        // 设置Swing外观为系统默认
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("Swing外观设置成功");
        } catch (Exception e) {
            logger.warning("设置系统外观失败: " + e.getMessage());
        }

        // 测试数据库连接
        testDatabaseConnection();

        setupGlobalStyles();

        // 使用SwingUtilities确保GUI创建在事件分派线程中
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logger.info("创建登录界面...");
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                logger.info("登录界面显示完成");
            }
        });
    }

    private static void setupGlobalStyles() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // 设置全局字体和颜色
            Font chineseFont = new Font("微软雅黑", Font.PLAIN, 14);

            UIManager.put("Button.font", chineseFont);
            UIManager.put("Button.foreground", Color.BLACK);

            UIManager.put("Label.font", chineseFont);
            UIManager.put("Label.foreground", Color.BLACK);

            UIManager.put("TextField.font", chineseFont);
            UIManager.put("TextField.foreground", Color.BLACK);

            UIManager.put("ComboBox.font", chineseFont);
            UIManager.put("ComboBox.foreground", Color.BLACK);

            UIManager.put("RadioButton.font", chineseFont);
            UIManager.put("RadioButton.foreground", Color.BLACK);

        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).warning("设置全局样式失败: " + e.getMessage());
        }
    }

    private static void testDatabaseConnection() {
        logger.info("测试数据库连接...");
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                logger.info("数据库连接测试成功");

                // 测试用户表是否存在数据
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
                if (rs.next()) {
                    int count = rs.getInt("count");
                    logger.info("用户表记录数: " + count);
                }
                rs.close();
                stmt.close();
            } else {
                logger.severe("数据库连接测试失败: 连接为null或已关闭");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "数据库连接测试异常", e);
        } finally {
            DatabaseUtil.closeConnection(conn);
        }
    }
}
