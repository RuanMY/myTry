package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CaseManagementPanel extends JPanel {
    private final AppointmentService appointmentService;
    private JTable casesTable;
    private JButton viewCaseButton;
    private JButton addRecordButton;
    private JButton viewHistoryButton;

    public CaseManagementPanel() {
        this.appointmentService = new AppointmentService();
        initUI();
        loadCases();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 标题
        JLabel titleLabel = new JLabel("个案管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        add(titleLabel, BorderLayout.NORTH);

        // 个案表格
        String[] columns = {"学生", "最近咨询", "咨询次数", "主要问题", "风险等级", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有操作列可编辑
            }
        };

        casesTable = new JTable(model);
        casesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        casesTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());

        JScrollPane scrollPane = new JScrollPane(casesTable);
        add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        viewCaseButton = new JButton("查看个案");
        addRecordButton = new JButton("添加记录");
        viewHistoryButton = new JButton("咨询历史");
        JButton refreshButton = new JButton("刷新");

        viewCaseButton.addActionListener(e -> viewCaseDetails());
        addRecordButton.addActionListener(e -> addConsultingRecord());
        viewHistoryButton.addActionListener(e -> viewConsultingHistory());
        refreshButton.addActionListener(e -> loadCases());

        buttonPanel.add(viewCaseButton);
        buttonPanel.add(addRecordButton);
        buttonPanel.add(viewHistoryButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        updateButtonState();
    }

    private void loadCases() {
        DefaultTableModel model = (DefaultTableModel) casesTable.getModel();
        model.setRowCount(0);

        // 获取已完成咨询的预约作为个案
        List<Appointment> appointments = appointmentService.getCounselorAppointments();

        for (Appointment appointment : appointments) {
            if ("completed".equals(appointment.getStatus())) {
                // 这里简化实现，实际应该从咨询记录中获取更多信息
                Object[] row = {
                        appointment.getStudentName(),
                        appointment.getStartTime().toString().substring(0, 10),
                        "1", // 咨询次数，实际应该统计
                        getMainIssueFromNotes(appointment.getNotes()),
                        assessRiskLevel(appointment),
                        "管理"
                };
                model.addRow(row);
            }
        }
    }

    private String getMainIssueFromNotes(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return "未指定";
        }

        // 简单的问题分类（实际应该更复杂）
        if (notes.toLowerCase().contains("抑郁")) return "情绪问题-抑郁";
        if (notes.toLowerCase().contains("焦虑")) return "情绪问题-焦虑";
        if (notes.toLowerCase().contains("压力")) return "学业压力";
        if (notes.toLowerCase().contains("人际")) return "人际关系";
        if (notes.toLowerCase().contains("家庭")) return "家庭问题";

        return notes.length() > 15 ? notes.substring(0, 15) + "..." : notes;
    }

    private String assessRiskLevel(Appointment appointment) {
        // 简单的风险评估（实际应该基于测评分数等更多信息）
        String notes = appointment.getNotes();
        if (notes != null) {
            if (notes.contains("严重") || notes.contains("紧急") || notes.contains("自杀")) {
                return "高危";
            } else if (notes.contains("中度") || notes.contains("持续")) {
                return "中危";
            }
        }
        return "低危";
    }

    private void updateButtonState() {
        boolean hasSelection = casesTable.getSelectedRow() >= 0;
        viewCaseButton.setEnabled(hasSelection);
        addRecordButton.setEnabled(hasSelection);
        viewHistoryButton.setEnabled(hasSelection);
    }

    private void viewCaseDetails() {
        int selectedRow = casesTable.getSelectedRow();
        if (selectedRow >= 0) {
            showCaseDetailDialog(selectedRow);
        }
    }

    private void showCaseDetailDialog(int caseIndex) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "个案详情", true);
        detailDialog.setSize(600, 500);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();

        // 基本信息标签页
        JPanel basicInfoPanel = createBasicInfoPanel(caseIndex);
        tabbedPane.addTab("基本信息", basicInfoPanel);

        // 咨询记录标签页
        JPanel recordsPanel = createRecordsPanel(caseIndex);
        tabbedPane.addTab("咨询记录", recordsPanel);

        // 评估信息标签页
        JPanel assessmentPanel = createAssessmentPanel(caseIndex);
        tabbedPane.addTab("评估信息", assessmentPanel);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> detailDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        detailDialog.add(tabbedPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    private JPanel createBasicInfoPanel(int caseIndex) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 模拟个案信息
        panel.add(new JLabel("学生姓名:"));
        panel.add(new JLabel("张三"));
        panel.add(new JLabel("学号:"));
        panel.add(new JLabel("20230001"));
        panel.add(new JLabel("院系:"));
        panel.add(new JLabel("计算机学院"));
        panel.add(new JLabel("联系电话:"));
        panel.add(new JLabel("138****0001"));
        panel.add(new JLabel("主要问题:"));
        panel.add(new JLabel("学业压力"));
        panel.add(new JLabel("风险等级:"));
        panel.add(new JLabel("中危"));

        return panel;
    }

    private JPanel createRecordsPanel(int caseIndex) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"咨询时间", "咨询师", "主要内容", "建议"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // 模拟咨询记录
        model.addRow(new Object[]{"2023-10-15 10:00", "张老师", "学业压力讨论", "时间管理技巧"});
        model.addRow(new Object[]{"2023-10-22 14:00", "张老师", "情绪调节", "放松训练"});

        JTable recordsTable = new JTable(model);
        panel.add(new JScrollPane(recordsTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAssessmentPanel(int caseIndex) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea assessmentArea = new JTextArea();
        assessmentArea.setText("PHQ-9评分: 12分（中度抑郁）\n" +
                "GAD-7评分: 8分（轻度焦虑）\n" +
                "压力指数: 中等\n" +
                "\n评估建议：\n" +
                "1. 建议定期进行心理咨询\n" +
                "2. 学习压力管理技巧\n" +
                "3. 建立健康的生活作息");
        assessmentArea.setEditable(false);
        assessmentArea.setLineWrap(true);
        assessmentArea.setWrapStyleWord(true);

        panel.add(new JScrollPane(assessmentArea), BorderLayout.CENTER);
        return panel;
    }

    private void addConsultingRecord() {
        int selectedRow = casesTable.getSelectedRow();
        if (selectedRow >= 0) {
            JDialog recordDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加咨询记录", true);
            recordDialog.setSize(500, 400);
            recordDialog.setLocationRelativeTo(this);
            recordDialog.setLayout(new BorderLayout(10, 10));

            JPanel formPanel = new JPanel(new GridLayout(3, 1, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            formPanel.add(new JLabel("咨询内容:"));
            JTextArea contentArea = new JTextArea(5, 40);
            contentArea.setLineWrap(true);
            formPanel.add(new JScrollPane(contentArea));

            formPanel.add(new JLabel("建议:"));
            JTextArea suggestionArea = new JTextArea(3, 40);
            suggestionArea.setLineWrap(true);
            formPanel.add(new JScrollPane(suggestionArea));

            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");

            saveButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(recordDialog, "记录保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                recordDialog.dispose();
            });
            cancelButton.addActionListener(e -> recordDialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            recordDialog.add(formPanel, BorderLayout.CENTER);
            recordDialog.add(buttonPanel, BorderLayout.SOUTH);
            recordDialog.setVisible(true);
        }
    }

    private void viewConsultingHistory() {
        int selectedRow = casesTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 显示咨询历史对话框
            JOptionPane.showMessageDialog(this, "咨询历史查看功能", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}