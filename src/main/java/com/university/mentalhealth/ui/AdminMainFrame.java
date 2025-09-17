package com.university.mentalhealth.ui;

import com.university.mentalhealth.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminMainFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public AdminMainFrame() {
        setTitle("大学生心理护航系统 - 管理员端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 添加各个功能选项卡
        tabbedPane.addTab("系统概览", createDashboardPanel());
        tabbedPane.addTab("用户管理", new UserManagementPanel());
        tabbedPane.addTab("预约监控", new AppointmentMonitorPanel());
        tabbedPane.addTab("系统配置", new SystemConfigPanel());
        tabbedPane.addTab("数据统计", createStatisticsPanel());

        add(tabbedPane);
    }

    // ✅ 新增 createMenuBar() 方法
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 系统菜单
        JMenu systemMenu = new JMenu("系统");
        JMenuItem logoutItem = new JMenuItem("退出登录");
        JMenuItem exitItem = new JMenuItem("退出系统");

        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));

        systemMenu.add(logoutItem);
        systemMenu.addSeparator();
        systemMenu.add(exitItem);

        // 用户管理菜单
        JMenu userMenu = new JMenu("用户管理");
        JMenuItem addUserItem = new JMenuItem("添加用户");
        JMenuItem userListItem = new JMenuItem("用户列表");
        JMenuItem userStatsItem = new JMenuItem("用户统计");

        addUserItem.addActionListener(e -> showAddUserDialog());
        userListItem.addActionListener(e -> showUserManagement());
        userStatsItem.addActionListener(e -> showUserStatistics());

        userMenu.add(addUserItem);
        userMenu.add(userListItem);
        userMenu.add(userStatsItem);

        // 数据管理菜单
        JMenu dataMenu = new JMenu("数据管理");
        JMenuItem appointmentItem = new JMenuItem("预约数据");
        JMenuItem backupItem = new JMenuItem("数据备份");
        JMenuItem exportItem = new JMenuItem("数据导出");

        appointmentItem.addActionListener(e -> showAppointmentMonitor());
        backupItem.addActionListener(e -> backupData());
        exportItem.addActionListener(e -> exportData());

        dataMenu.add(appointmentItem);
        dataMenu.add(backupItem);
        dataMenu.add(exportItem);

        // 系统设置菜单
        JMenu settingsMenu = new JMenu("系统设置");
        JMenuItem configItem = new JMenuItem("系统配置");
        JMenuItem emailItem = new JMenuItem("邮件设置");
        JMenuItem securityItem = new JMenuItem("安全设置");

        configItem.addActionListener(e -> showSystemConfig());
        emailItem.addActionListener(e -> showEmailConfig());
        securityItem.addActionListener(e -> showSecurityConfig());

        settingsMenu.add(configItem);
        settingsMenu.add(emailItem);
        settingsMenu.add(securityItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        JMenuItem manualItem = new JMenuItem("使用手册");
        JMenuItem supportItem = new JMenuItem("技术支持");

        aboutItem.addActionListener(e -> showAboutDialog());
        manualItem.addActionListener(e -> showManualDialog());
        supportItem.addActionListener(e -> showSupportDialog());

        helpMenu.add(aboutItem);
        helpMenu.add(manualItem);
        helpMenu.add(supportItem);

        // 添加所有菜单到菜单栏
        menuBar.add(systemMenu);
        menuBar.add(userMenu);
        menuBar.add(dataMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    // 菜单项动作方法
    private void showAddUserDialog() {
        // 切换到用户管理标签页并触发添加操作
        tabbedPane.setSelectedIndex(1);
        JOptionPane.showMessageDialog(this, "请点击用户管理界面中的'添加用户'按钮", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUserManagement() {
        tabbedPane.setSelectedIndex(1); // 用户管理标签页
    }

    private void showUserStatistics() {
        // 可以在这里实现用户统计功能
        JOptionPane.showMessageDialog(this, "用户统计功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAppointmentMonitor() {
        tabbedPane.setSelectedIndex(2); // 预约监控标签页
    }

    private void backupData() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要立即备份数据库吗？", "确认备份", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "数据库备份功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportData() {
        JOptionPane.showMessageDialog(this, "数据导出功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSystemConfig() {
        tabbedPane.setSelectedIndex(3); // 系统配置标签页
    }

    private void showEmailConfig() {
        // 可以在这里实现专门的邮件配置界面
        JOptionPane.showMessageDialog(this, "邮件设置功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSecurityConfig() {
        // 安全设置功能
        JOptionPane.showMessageDialog(this, "安全设置功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "大学生心理护航系统 v1.0\n\n" +
                        "管理员端模块\n" +
                        "版权所有 © 2023 大学心理健康中心\n" +
                        "系统版本: 1.0.0\n" +
                        "数据库版本: 1.0.0",
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showManualDialog() {
        JOptionPane.showMessageDialog(this,
                "管理员端使用说明:\n\n" +
                        "1. 系统概览: 查看系统整体运行状态\n" +
                        "2. 用户管理: 管理所有系统用户账户\n" +
                        "3. 预约监控: 监控和管理所有预约数据\n" +
                        "4. 系统配置: 配置系统参数和设置\n" +
                        "5. 数据统计: 查看系统数据统计分析",
                "使用手册", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSupportDialog() {
        JOptionPane.showMessageDialog(this,
                "技术支持信息:\n\n" +
                        "技术支持邮箱: support@university.edu.cn\n" +
                        "联系电话: 010-12345678\n" +
                        "服务时间: 工作日 9:00-18:00\n\n" +
                        "常见问题解决方案请参考使用手册",
                "技术支持", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要退出登录吗?",
                "确认退出",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    // 原有的其他方法保持不变
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("系统管理概览", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // 系统统计信息
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] stats = {
                "总用户数: 156",
                "学生数: 150",
                "咨询师数: 5",
                "今日活跃: 23",
                "总咨询次数: 89",
                "系统运行: 30天"
        };

        for (String stat : stats) {
            JLabel statLabel = new JLabel(stat, SwingConstants.CENTER);
            statLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            statLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            statsPanel.add(statLabel);
        }

        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("数据统计分析", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 统计选项卡
        JTabbedPane statsTabbedPane = new JTabbedPane();
        statsTabbedPane.addTab("用户统计", createUserStatsPanel());
        statsTabbedPane.addTab("咨询统计", createCounselingStatsPanel());
        statsTabbedPane.addTab("测评统计", createAssessmentStatsPanel());

        panel.add(statsTabbedPane, BorderLayout.CENTER);

        // 导出按钮
        JButton exportButton = new JButton("导出报表");
        panel.add(exportButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUserStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 院系分布表格
        String[] columns = {"院系", "学生人数", "占比"};
        Object[][] data = {
                {"计算机学院", "45", "30%"},
                {"心理学院", "38", "25.3%"},
                {"经济学院", "25", "16.7%"},
                {"文学院", "20", "13.3%"},
                {"其他", "22", "14.7%"}
        };

        JTable statsTable = new JTable(data, columns);
        panel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCounselingStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 咨询统计表格
        String[] columns = {"咨询师", "咨询次数", "完成率", "平均评分"};
        Object[][] data = {
                {"张老师", "25", "92%", "4.8"},
                {"李老师", "18", "89%", "4.6"},
                {"王老师", "15", "93%", "4.7"}
        };

        JTable statsTable = new JTable(data, columns);
        panel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAssessmentStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 测评统计表格
        String[] columns = {"测评类型", "完成次数", "平均分数", "高危比例"};
        Object[][] data = {
                {"PHQ-9抑郁症", "89", "12.5", "15.7%"},
                {"GAD-7焦虑症", "76", "9.8", "10.5%"},
                {"UCLA孤独感", "45", "38.2", "22.2%"}
        };

        JTable statsTable = new JTable(data, columns);
        panel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminMainFrame frame = new AdminMainFrame();
            frame.setVisible(true);
        });
    }
}