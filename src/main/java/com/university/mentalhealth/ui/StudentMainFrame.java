package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.*;
import com.university.mentalhealth.service.AssessmentService;
import com.university.mentalhealth.service.StudentService;
import com.university.mentalhealth.util.SessionManager;
import com.university.mentalhealth.ui.StudentAppointmentPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class StudentMainFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(StudentMainFrame.class.getName());

    private JTabbedPane tabbedPane;
    private StudentService studentService;
    private Student currentStudent;
    private AssessmentService assessmentService;

    // 个人信息面板的组件
    private JTextField studentIdField;
    private JTextField nameField;
    private JTextField departmentField;
    private JTextField contactPhoneField;
    private JTextField emergencyContactField;
    private JTextField emergencyPhoneField;

    public StudentMainFrame() {
        this.studentService = new StudentService();
        this.assessmentService = new AssessmentService();
        loadCurrentStudent();
        initUI();
    }

    private void loadCurrentStudent() {
        Optional<Student> studentOpt = studentService.getCurrentStudent();
        if (studentOpt.isPresent()) {
            currentStudent = studentOpt.get();
            logger.info("加载当前学生信息: " + currentStudent.getDisplayInfo());
        } else {
            logger.warning("无法加载当前学生信息");
            JOptionPane.showMessageDialog(this, "无法加载学生信息，请重新登录", "错误", JOptionPane.ERROR_MESSAGE);
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    private void initUI() {
        setTitle("大学生心理护航系统 - 学生端 - " + (currentStudent != null ? currentStudent.getName() : ""));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 添加各个功能选项卡
        tabbedPane.addTab("首页", createDashboardPanel());
        tabbedPane.addTab("个人信息", createPersonalInfoPanel());
        tabbedPane.addTab("心理测评", createAssessmentPanel());

        // 咨询预约选项卡
        tabbedPane.addTab("咨询预约", new StudentAppointmentPanel());

        tabbedPane.addTab("测评历史", createHistoryPanel());
        tabbedPane.addTab("心理知识", createKnowledgePanel());

        add(tabbedPane);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu systemMenu = new JMenu("系统");
        JMenuItem logoutItem = new JMenuItem("退出登录");
        JMenuItem exitItem = new JMenuItem("退出系统");

        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));

        systemMenu.add(logoutItem);
        systemMenu.addSeparator();
        systemMenu.add(exitItem);
        menuBar.add(systemMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 欢迎信息
        JLabel welcomeLabel = new JLabel("欢迎使用大学生心理护航系统", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // 学生基本信息
        if (currentStudent != null) {
            JPanel infoPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

            infoPanel.add(createInfoLabel("姓名：", currentStudent.getName()));
            infoPanel.add(createInfoLabel("学号：", currentStudent.getStudentId()));
            infoPanel.add(createInfoLabel("院系：", currentStudent.getDepartment()));
            infoPanel.add(createInfoLabel("联系电话：", currentStudent.getContactPhone()));

            panel.add(infoPanel, BorderLayout.CENTER);
        }

        // 功能快捷入口
        JPanel quickAccessPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickAccessPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] buttonNames = {"心理测评", "咨询预约", "查看历史", "个人信息", "心理知识", "系统设置"};
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            button.setPreferredSize(new Dimension(120, 60));
            button.addActionListener(e -> navigateToTab(name));
            quickAccessPanel.add(button);
        }

        panel.add(quickAccessPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInfoLabel(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        panel.add(nameLabel);
        panel.add(valueLabel);
        return panel;
    }

    private void navigateToTab(String tabName) {
        switch (tabName) {
            case "心理测评":
                tabbedPane.setSelectedIndex(2);
                break;
            case "咨询预约":
                tabbedPane.setSelectedIndex(3);
                break;
            case "查看历史":
                tabbedPane.setSelectedIndex(4);
                break;
            case "个人信息":
                tabbedPane.setSelectedIndex(1);
                break;
            case "心理知识":
                tabbedPane.setSelectedIndex(5);
                break;
        }
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("个人信息管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 学号（只读）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("学号:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        studentIdField = new JTextField(20);
        studentIdField.setEditable(false);
        formPanel.add(studentIdField, gbc);

        // 姓名（只读）
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("姓名:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(20);
        nameField.setEditable(false);
        formPanel.add(nameField, gbc);

        // 院系（只读）
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("院系:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        departmentField = new JTextField(20);
        departmentField.setEditable(false);
        formPanel.add(departmentField, gbc);

        // 联系电话
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("联系电话:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contactPhoneField = new JTextField(20);
        formPanel.add(contactPhoneField, gbc);

        // 紧急联系人
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("紧急联系人:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        emergencyContactField = new JTextField(20);
        formPanel.add(emergencyContactField, gbc);

        // 紧急联系电话
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("紧急联系电话:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        emergencyPhoneField = new JTextField(20);
        formPanel.add(emergencyPhoneField, gbc);

        // 加载当前学生数据
        loadStudentData();

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("保存修改");
        JButton refreshButton = new JButton("刷新");

        saveButton.addActionListener(e -> saveContactInfo());
        refreshButton.addActionListener(e -> loadStudentData());

        buttonPanel.add(saveButton);
        buttonPanel.add(refreshButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadStudentData() {
        if (currentStudent != null) {
            studentIdField.setText(currentStudent.getStudentId());
            nameField.setText(currentStudent.getName());
            departmentField.setText(currentStudent.getDepartment());
            contactPhoneField.setText(currentStudent.getContactPhone());
            emergencyContactField.setText(currentStudent.getEmergencyContact());
            emergencyPhoneField.setText(currentStudent.getEmergencyPhone());
        }
    }

    private void saveContactInfo() {
        String contactPhone = contactPhoneField.getText().trim();
        String emergencyContact = emergencyContactField.getText().trim();
        String emergencyPhone = emergencyPhoneField.getText().trim();

        // 简单验证
        if (contactPhone.isEmpty() || emergencyContact.isEmpty() || emergencyPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有字段都必须填写", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = studentService.updateContactInfo(contactPhone, emergencyContact, emergencyPhone);
        if (success) {
            JOptionPane.showMessageDialog(this, "联系信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 重新加载学生数据
            loadCurrentStudent();
        } else {
            JOptionPane.showMessageDialog(this, "联系信息更新失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 其他面板方法保持不变（心理测评、咨询预约、测评历史、心理知识）
    private JPanel createAssessmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 顶部：测评选择
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("选择测评量表:");
        JComboBox<Assessment> assessmentComboBox = new JComboBox<>();
        JButton startButton = new JButton("开始测评");
        JButton historyButton = new JButton("查看历史");

        // 加载测评量表
        loadAssessmentsToComboBox(assessmentComboBox);

        selectionPanel.add(label);
        selectionPanel.add(assessmentComboBox);
        selectionPanel.add(startButton);
        selectionPanel.add(historyButton);

        // 中部：测评说明
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descriptionArea.setText("请从下拉列表选择测评量表，点击\"开始测评\"进行测试。");

        // 添加选择监听器
        assessmentComboBox.addActionListener(e -> {
            Assessment selected = (Assessment) assessmentComboBox.getSelectedItem();
            if (selected != null) {
                descriptionArea.setText(selected.getDescription() +
                        "\n\n题目数量: " + selected.getTotalQuestions() +
                        "\n风险评估阈值: " + selected.getRiskThreshold());
            }
        });

        // 按钮事件
        startButton.addActionListener(e -> {
            Assessment selected = (Assessment) assessmentComboBox.getSelectedItem();
            if (selected != null) {
                startAssessment(selected);
            } else {
                JOptionPane.showMessageDialog(this, "请选择测评量表", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        historyButton.addActionListener(e -> showAssessmentHistory());

        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        return panel;
    }

    private void loadAssessmentsToComboBox(JComboBox<Assessment> comboBox) {
        comboBox.removeAllItems();
        List<Assessment> assessments = assessmentService.getAvailableAssessments();
        for (Assessment assessment : assessments) {
            comboBox.addItem(assessment);
        }
    }

    private void startAssessment(Assessment assessment) {
        // 检查是否已经完成过该测评
        if (assessmentService.hasCompletedAssessment(assessment.getId())) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "您已经完成过该测评，是否重新进行测评？",
                    "确认重新测评",
                    JOptionPane.YES_NO_OPTION
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // 创建测评对话框
        AssessmentDialog dialog = new AssessmentDialog(this, assessment);
        dialog.setVisible(true);
    }

    private void showAssessmentHistory() {
        List<AssessmentSession> history = assessmentService.getStudentAssessmentHistory();

        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无测评历史记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 使用新的详细历史对话框
        AssessmentHistoryDetailDialog historyDialog = new AssessmentHistoryDetailDialog(this, history.get(0));
        historyDialog.setVisible(true);
    }

    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("咨询预约功能开发中...", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("测评历史功能开发中...", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKnowledgePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("心理知识功能开发中...", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要退出登录吗?",
                "确认退出",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            // 回到登录界面
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }
}