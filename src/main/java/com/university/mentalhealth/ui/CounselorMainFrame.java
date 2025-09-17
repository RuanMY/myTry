package com.university.mentalhealth.ui;

import com.university.mentalhealth.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CounselorMainFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public CounselorMainFrame() {
        setTitle("大学生心理护航系统 - 咨询师端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 添加各个功能选项卡
        tabbedPane.addTab("工作台", new CounselorDashboardPanel());
        tabbedPane.addTab("日程管理", new CounselorSchedulePanel());
        tabbedPane.addTab("预约处理", createAppointmentProcessingPanel());
        tabbedPane.addTab("个案管理", new CaseManagementPanel());
        tabbedPane.addTab("咨询记录", createRecordPanel());

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

        // 预约菜单
        JMenu appointmentMenu = new JMenu("预约");
        JMenuItem newAppointmentItem = new JMenuItem("新建预约");
        JMenuItem viewAppointmentsItem = new JMenuItem("查看预约");

        newAppointmentItem.addActionListener(e -> showNewAppointmentDialog());
        viewAppointmentsItem.addActionListener(e -> showAppointmentProcessingDialog());

        appointmentMenu.add(newAppointmentItem);
        appointmentMenu.add(viewAppointmentsItem);

        // 个案菜单
        JMenu caseMenu = new JMenu("个案");
        JMenuItem viewCasesItem = new JMenuItem("查看个案");
        JMenuItem addRecordItem = new JMenuItem("添加记录");

        viewCasesItem.addActionListener(e -> showCaseManagement());
        addRecordItem.addActionListener(e -> showAddRecordDialog());

        caseMenu.add(viewCasesItem);
        caseMenu.add(addRecordItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        JMenuItem manualItem = new JMenuItem("使用手册");

        aboutItem.addActionListener(e -> showAboutDialog());
        manualItem.addActionListener(e -> showManualDialog());

        helpMenu.add(aboutItem);
        helpMenu.add(manualItem);

        // 添加所有菜单到菜单栏
        menuBar.add(systemMenu);
        menuBar.add(appointmentMenu);
        menuBar.add(caseMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createAppointmentProcessingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton openProcessingButton = new JButton("打开预约处理界面");
        openProcessingButton.addActionListener(e -> {
            AppointmentProcessingDialog dialog = new AppointmentProcessingDialog(this);
            dialog.setVisible(true);
        });
        panel.add(openProcessingButton, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRecordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("咨询记录功能", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    // 菜单项动作方法
    private void showNewAppointmentDialog() {
        JOptionPane.showMessageDialog(this, "新建预约功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAppointmentProcessingDialog() {
        AppointmentProcessingDialog dialog = new AppointmentProcessingDialog(this);
        dialog.setVisible(true);
    }

    private void showCaseManagement() {
        // 切换到个案管理标签页
        tabbedPane.setSelectedIndex(3);
    }

    private void showAddRecordDialog() {
        JOptionPane.showMessageDialog(this, "添加咨询记录功能", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "大学生心理护航系统 v1.0\n\n" +
                        "咨询师端模块\n" +
                        "版权所有 © 2023 大学心理健康中心",
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showManualDialog() {
        JOptionPane.showMessageDialog(this,
                "咨询师端使用说明:\n\n" +
                        "1. 工作台: 查看今日预约和统计信息\n" +
                        "2. 日程管理: 管理您的工作时间安排\n" +
                        "3. 预约处理: 处理学生的预约请求\n" +
                        "4. 个案管理: 管理学生咨询个案\n" +
                        "5. 咨询记录: 查看和记录咨询内容",
                "使用手册", JOptionPane.INFORMATION_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CounselorMainFrame frame = new CounselorMainFrame();
            frame.setVisible(true);
        });
    }
}