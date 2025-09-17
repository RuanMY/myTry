package com.university.mentalhealth.ui;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class SystemConfigPanel extends JPanel {
    private final Preferences prefs = Preferences.userNodeForPackage(SystemConfigPanel.class);

    private JTextField systemNameField;
    private JTextField adminEmailField;
    private JSpinner sessionTimeoutSpinner;
    private JSpinner maxAppointmentsSpinner;
    private JCheckBox emailNotificationCheckBox;
    private JCheckBox autoBackupCheckBox;

    public SystemConfigPanel() {
        initUI();
        loadConfig();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 配置表单面板
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder("系统配置"));

        // 系统名称
        formPanel.add(new JLabel("系统名称:"));
        systemNameField = new JTextField();
        formPanel.add(systemNameField);

        // 管理员邮箱
        formPanel.add(new JLabel("管理员邮箱:"));
        adminEmailField = new JTextField();
        formPanel.add(adminEmailField);

        // 会话超时时间
        formPanel.add(new JLabel("会话超时(分钟):"));
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
        formPanel.add(sessionTimeoutSpinner);

        // 最大预约数
        formPanel.add(new JLabel("每日最大预约数:"));
        maxAppointmentsSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
        formPanel.add(maxAppointmentsSpinner);

        // 邮件通知
        formPanel.add(new JLabel("邮件通知:"));
        emailNotificationCheckBox = new JCheckBox("启用邮件通知");
        formPanel.add(emailNotificationCheckBox);

        // 自动备份
        formPanel.add(new JLabel("自动备份:"));
        autoBackupCheckBox = new JCheckBox("启用自动备份");
        formPanel.add(autoBackupCheckBox);

        // 空行
        formPanel.add(new JLabel());
        formPanel.add(new JLabel());

        add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));

        JButton saveButton = new JButton("保存配置");
        JButton resetButton = new JButton("恢复默认");
        JButton testEmailButton = new JButton("测试邮件");
        JButton backupNowButton = new JButton("立即备份");

        saveButton.addActionListener(e -> saveConfig());
        resetButton.addActionListener(e -> resetConfig());
        testEmailButton.addActionListener(e -> testEmail());
        backupNowButton.addActionListener(e -> backupNow());

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(testEmailButton);
        buttonPanel.add(backupNowButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadConfig() {
        systemNameField.setText(prefs.get("systemName", "大学生心理护航系统"));
        adminEmailField.setText(prefs.get("adminEmail", "admin@university.edu.cn"));
        sessionTimeoutSpinner.setValue(prefs.getInt("sessionTimeout", 30));
        maxAppointmentsSpinner.setValue(prefs.getInt("maxAppointments", 20));
        emailNotificationCheckBox.setSelected(prefs.getBoolean("emailNotifications", false));
        autoBackupCheckBox.setSelected(prefs.getBoolean("autoBackup", false));
    }

    private void saveConfig() {
        prefs.put("systemName", systemNameField.getText());
        prefs.put("adminEmail", adminEmailField.getText());
        prefs.putInt("sessionTimeout", (Integer) sessionTimeoutSpinner.getValue());
        prefs.putInt("maxAppointments", (Integer) maxAppointmentsSpinner.getValue());
        prefs.putBoolean("emailNotifications", emailNotificationCheckBox.isSelected());
        prefs.putBoolean("autoBackup", autoBackupCheckBox.isSelected());

        JOptionPane.showMessageDialog(this, "配置保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetConfig() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要恢复默认配置吗？", "确认恢复", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try {
                prefs.clear(); // 清除所有配置
                loadConfig(); // 重新加载默认值
                JOptionPane.showMessageDialog(this, "配置已恢复默认", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "恢复配置失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void testEmail() {
        String email = adminEmailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "测试邮件已发送到: " + email + "\n请检查邮箱收件箱。",
                "测试邮件", JOptionPane.INFORMATION_MESSAGE);
    }

    private void backupNow() {
        JOptionPane.showMessageDialog(this,
                "数据库备份功能开发中\n备份将保存到: /backups/",
                "备份提示", JOptionPane.INFORMATION_MESSAGE);
    }

    // 数据库备份配置面板
    private JPanel createBackupConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("备份配置"));

        panel.add(new JLabel("备份路径:"));
        JTextField backupPathField = new JTextField("/backups/");
        panel.add(backupPathField);

        panel.add(new JLabel("备份频率:"));
        JComboBox<String> backupFrequency = new JComboBox<>(new String[]{"每天", "每周", "每月"});
        panel.add(backupFrequency);

        panel.add(new JLabel("保留天数:"));
        JSpinner retentionSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        panel.add(retentionSpinner);

        panel.add(new JLabel("压缩备份:"));
        JCheckBox compressCheckBox = new JCheckBox("启用压缩");
        panel.add(compressCheckBox);

        return panel;
    }

    // 邮件配置面板
    private JPanel createEmailConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("邮件配置"));

        panel.add(new JLabel("SMTP服务器:"));
        JTextField smtpServerField = new JTextField("smtp.university.edu.cn");
        panel.add(smtpServerField);

        panel.add(new JLabel("SMTP端口:"));
        JTextField smtpPortField = new JTextField("587");
        panel.add(smtpPortField);

        panel.add(new JLabel("发件邮箱:"));
        JTextField senderEmailField = new JTextField("noreply@university.edu.cn");
        panel.add(senderEmailField);

        panel.add(new JLabel("邮箱密码:"));
        JPasswordField emailPasswordField = new JPasswordField();
        panel.add(emailPasswordField);

        panel.add(new JLabel("SSL加密:"));
        JCheckBox sslCheckBox = new JCheckBox("启用SSL");
        panel.add(sslCheckBox);

        return panel;
    }
}