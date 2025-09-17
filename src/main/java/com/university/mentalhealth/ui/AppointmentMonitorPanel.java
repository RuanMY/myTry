package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentMonitorPanel extends JPanel {
    private final AppointmentService appointmentService;
    private JTable appointmentTable;
    private JComboBox<String> statusFilter;
    private JComboBox<String> dateRangeFilter;
    private JTextField dateFromField;
    private JTextField dateToField;

    public AppointmentMonitorPanel() {
        this.appointmentService = new AppointmentService();
        initUI();
        loadAppointments();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部筛选面板
        JPanel filterPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // 第一行筛选条件
        JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1Panel.add(new JLabel("状态筛选:"));

        statusFilter = new JComboBox<>(new String[]{"全部", "待确认", "已确认", "已完成", "已取消"});
        statusFilter.addActionListener(e -> filterAppointments());
        row1Panel.add(statusFilter);

        row1Panel.add(new JLabel("时间范围:"));
        dateRangeFilter = new JComboBox<>(new String[]{"今天", "本周", "本月", "自定义"});
        dateRangeFilter.addActionListener(e -> updateDateRange());
        row1Panel.add(dateRangeFilter);

        // 第二行日期选择
        JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2Panel.add(new JLabel("从:"));

        dateFromField = new JTextField(10);
        dateFromField.setText(LocalDate.now().minusDays(7).toString());
        row2Panel.add(dateFromField);

        row2Panel.add(new JLabel("到:"));
        dateToField = new JTextField(10);
        dateToField.setText(LocalDate.now().toString());
        row2Panel.add(dateToField);

        JButton filterButton = new JButton("应用筛选");
        filterButton.addActionListener(e -> filterAppointments());
        row2Panel.add(filterButton);

        JButton exportButton = new JButton("导出数据");
        exportButton.addActionListener(e -> exportData());
        row2Panel.add(exportButton);

        filterPanel.add(row1Panel);
        filterPanel.add(row2Panel);
        add(filterPanel, BorderLayout.NORTH);

        // 预约表格
        String[] columns = {"预约ID", "学生", "咨询师", "预约时间", "状态", "创建时间", "备注"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        appointmentTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部统计面板
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("预约统计"));

        JLabel totalLabel = new JLabel("总预约: 0", SwingConstants.CENTER);
        JLabel pendingLabel = new JLabel("待确认: 0", SwingConstants.CENTER);
        JLabel confirmedLabel = new JLabel("已确认: 0", SwingConstants.CENTER);
        JLabel completedLabel = new JLabel("已完成: 0", SwingConstants.CENTER);
        JLabel cancelledLabel = new JLabel("已取消: 0", SwingConstants.CENTER);

        totalLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        pendingLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        confirmedLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        completedLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        cancelledLabel.setBorder(BorderFactory.createLineBorder(Color.RED));

        panel.add(totalLabel);
        panel.add(pendingLabel);
        panel.add(confirmedLabel);
        panel.add(completedLabel);
        panel.add(cancelledLabel);

        return panel;
    }

    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getAllAppointments();
        updateStatistics(appointments);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            addAppointmentToTable(appointment, model, formatter);
        }
    }

    private void addAppointmentToTable(Appointment appointment, DefaultTableModel model, DateTimeFormatter formatter) {
        Object[] row = {
                appointment.getId(),
                appointment.getStudentName(),
                appointment.getCounselorName(),
                appointment.getStartTime().format(formatter) + " - " +
                        appointment.getEndTime().format(formatter).substring(11),
                getStatusText(appointment.getStatus()),
                appointment.getCreatedAt().format(formatter),
                appointment.getNotes() != null ?
                        (appointment.getNotes().length() > 30 ?
                                appointment.getNotes().substring(0, 30) + "..." :
                                appointment.getNotes()) : "无"
        };
        model.addRow(row);
    }

    private void updateStatistics(List<Appointment> appointments) {
        int total = appointments.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int cancelled = 0;

        for (Appointment appointment : appointments) {
            switch (appointment.getStatus()) {
                case "pending": pending++; break;
                case "confirmed": confirmed++; break;
                case "completed": completed++; break;
                case "cancelled": cancelled++; break;
            }
        }

        JPanel statsPanel = (JPanel) getComponent(2);
        Component[] components = statsPanel.getComponents();

        ((JLabel) components[0]).setText("总预约: " + total);
        ((JLabel) components[1]).setText("待确认: " + pending);
        ((JLabel) components[2]).setText("已确认: " + confirmed);
        ((JLabel) components[3]).setText("已完成: " + completed);
        ((JLabel) components[4]).setText("已取消: " + cancelled);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "待确认";
            case "confirmed": return "已确认";
            case "completed": return "已完成";
            case "cancelled": return "已取消";
            default: return status;
        }
    }

    private void updateDateRange() {
        String range = (String) dateRangeFilter.getSelectedItem();
        LocalDate today = LocalDate.now();

        switch (range) {
            case "今天":
                dateFromField.setText(today.toString());
                dateToField.setText(today.toString());
                break;
            case "本周":
                dateFromField.setText(today.with(java.time.DayOfWeek.MONDAY).toString());
                dateToField.setText(today.toString());
                break;
            case "本月":
                dateFromField.setText(today.withDayOfMonth(1).toString());
                dateToField.setText(today.toString());
                break;
            case "自定义":
                // 保持当前日期不变
                break;
        }
    }

    private void filterAppointments() {
        // 实现筛选逻辑
        JOptionPane.showMessageDialog(this, "筛选功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出预约数据");
        fileChooser.setSelectedFile(new File("预约数据导出_" + LocalDate.now() + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "数据导出功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}