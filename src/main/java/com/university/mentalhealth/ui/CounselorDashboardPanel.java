package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.service.AppointmentService;
import com.university.mentalhealth.service.CounselorService;
import com.university.mentalhealth.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CounselorDashboardPanel extends JPanel {
    private final AppointmentService appointmentService;
    private final CounselorService counselorService;

    private JLabel todayAppointmentsLabel;
    private JLabel pendingAppointmentsLabel;
    private JLabel upcomingAppointmentsLabel;
    private JTable todayAppointmentsTable;

    public CounselorDashboardPanel() {
        this.appointmentService = new AppointmentService();
        this.counselorService = new CounselorService();

        initUI();
        loadDashboardData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部欢迎面板
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("咨询师工作台", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(0, 102, 204));
        welcomePanel.add(welcomeLabel, BorderLayout.NORTH);

        JLabel dateLabel = new JLabel("今日: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE")),
                SwingConstants.CENTER);
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        welcomePanel.add(dateLabel, BorderLayout.CENTER);

        add(welcomePanel, BorderLayout.NORTH);

        // 统计信息面板
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);

        // 今日预约面板
        JPanel appointmentsPanel = createTodayAppointmentsPanel();
        add(appointmentsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 今日预约统计
        JPanel todayPanel = createStatCard("今日预约", "0", new Color(70, 130, 180));
        todayAppointmentsLabel = (JLabel) todayPanel.getComponent(1);

        // 待处理预约
        JPanel pendingPanel = createStatCard("待处理预约", "0", new Color(220, 20, 60));
        pendingAppointmentsLabel = (JLabel) pendingPanel.getComponent(1);

        // 即将到来预约
        JPanel upcomingPanel = createStatCard("即将到来", "0", new Color(34, 139, 34));
        upcomingAppointmentsLabel = (JLabel) upcomingPanel.getComponent(1);

        // 咨询完成率
        JPanel completionPanel = createStatCard("完成率", "0%", new Color(255, 140, 0));

        panel.add(todayPanel);
        panel.add(pendingPanel);
        panel.add(upcomingPanel);
        panel.add(completionPanel);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTodayAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("今日预约安排"));

        // 表格列
        String[] columns = {"时间", "学生", "联系方式", "状态", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // 只有操作列可编辑
            }
        };

        todayAppointmentsTable = new JTable(model);
        todayAppointmentsTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(todayAppointmentsTable);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("确认预约");
        JButton completeButton = new JButton("完成咨询");
        JButton cancelButton = new JButton("取消预约");
        JButton refreshButton = new JButton("刷新");

        confirmButton.addActionListener(e -> confirmSelectedAppointment());
        completeButton.addActionListener(e -> completeSelectedAppointment());
        cancelButton.addActionListener(e -> cancelSelectedAppointment());
        refreshButton.addActionListener(e -> loadDashboardData());

        buttonPanel.add(confirmButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDashboardData() {
        loadStatistics();
        loadTodayAppointments();
    }

    private void loadStatistics() {
        int pendingCount = appointmentService.getPendingAppointmentCount();
        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        List<Appointment> upcomingAppointments = getUpcomingAppointments();

        todayAppointmentsLabel.setText(String.valueOf(todayAppointments.size()));
        pendingAppointmentsLabel.setText(String.valueOf(pendingCount));
        upcomingAppointmentsLabel.setText(String.valueOf(upcomingAppointments.size()));
    }

    private List<Appointment> getUpcomingAppointments() {
        List<Appointment> allAppointments = appointmentService.getCounselorAppointments();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // 筛选未来1小时内的预约
        java.util.List<Appointment> upcoming = new java.util.ArrayList<>();
        for (Appointment appointment : allAppointments) {
            if (appointment.getStartTime() != null &&
                    appointment.getStartTime().isAfter(now) &&
                    appointment.getStartTime().isBefore(oneHourLater) &&
                    !"cancelled".equals(appointment.getStatus())) {
                upcoming.add(appointment);
            }
        }
        return upcoming;
    }

    private void loadTodayAppointments() {
        DefaultTableModel model = (DefaultTableModel) todayAppointmentsTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getTodayAppointments();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Appointment appointment : appointments) {
            String statusText = getStatusText(appointment.getStatus());
            String actionText = getActionText(appointment.getStatus());

            Object[] row = {
                    appointment.getStartTime().format(timeFormatter) + "-" +
                            appointment.getEndTime().format(timeFormatter),
                    appointment.getStudentName(),
                    "138****" + (appointment.getStudentId() % 10000),
                    statusText,
                    actionText
            };
            model.addRow(row);
        }
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

    private String getActionText(String status) {
        switch (status) {
            case "pending": return "确认";
            case "confirmed": return "完成";
            default: return "查看";
        }
    }

    private void confirmSelectedAppointment() {
        int selectedRow = todayAppointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Appointment> appointments = appointmentService.getTodayAppointments();
            if (selectedRow < appointments.size()) {
                Appointment appointment = appointments.get(selectedRow);
                boolean success = appointmentService.confirmAppointment(appointment.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "预约确认成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadDashboardData();
                }
            }
        }
    }

    private void completeSelectedAppointment() {
        int selectedRow = todayAppointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Appointment> appointments = appointmentService.getTodayAppointments();
            if (selectedRow < appointments.size()) {
                Appointment appointment = appointments.get(selectedRow);
                boolean success = appointmentService.completeAppointment(appointment.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "咨询完成", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadDashboardData();
                }
            }
        }
    }

    private void cancelSelectedAppointment() {
        int selectedRow = todayAppointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要取消这个预约吗？", "确认取消", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                List<Appointment> appointments = appointmentService.getTodayAppointments();
                if (selectedRow < appointments.size()) {
                    Appointment appointment = appointments.get(selectedRow);
                    boolean success = appointmentService.cancelAppointment(appointment.getId());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "预约已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadDashboardData();
                    }
                }
            }
        }
    }
}