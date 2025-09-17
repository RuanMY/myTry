package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.entity.TimeSlot;
import com.university.mentalhealth.service.AppointmentService;
import com.university.mentalhealth.service.CounselorService;
import com.university.mentalhealth.service.TimeSlotService;
import com.university.mentalhealth.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CounselorSchedulePanel extends JPanel {
    private final TimeSlotService timeSlotService;
    private final AppointmentService appointmentService;
    private final CounselorService counselorService;

    private JTable scheduleTable;
    private JTable appointmentTable;
    private JButton addTimeSlotButton;
    private JButton deleteTimeSlotButton;
    private JButton refreshButton;

    public CounselorSchedulePanel() {
        this.timeSlotService = new TimeSlotService();
        this.appointmentService = new AppointmentService();
        this.counselorService = new CounselorService();

        initUI();
        loadSchedule();
        loadAppointments();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部面板 - 按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        addTimeSlotButton = new JButton("添加工作时间段");
        addTimeSlotButton.addActionListener(e -> showAddTimeSlotDialog());

        deleteTimeSlotButton = new JButton("删除选中时间段");
        deleteTimeSlotButton.addActionListener(e -> deleteTimeSlot());

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshData());

        topPanel.add(addTimeSlotButton);
        topPanel.add(deleteTimeSlotButton);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // 中部面板 - 日程表和预约表
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);

        // 上部 - 日程表
        JPanel schedulePanel = new JPanel(new BorderLayout(5, 5));
        schedulePanel.setBorder(BorderFactory.createTitledBorder("我的工作时间表"));

        String[] scheduleColumns = {"开始时间", "结束时间", "状态", "操作"};
        DefaultTableModel scheduleModel = new DefaultTableModel(scheduleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // 只有操作列可编辑
            }
        };

        scheduleTable = new JTable(scheduleModel);
        JScrollPane scheduleScrollPane = new JScrollPane(scheduleTable);
        schedulePanel.add(scheduleScrollPane, BorderLayout.CENTER);

        // 下部 - 预约列表
        JPanel appointmentPanel = new JPanel(new BorderLayout(5, 5));
        appointmentPanel.setBorder(BorderFactory.createTitledBorder("预约管理"));

        String[] appointmentColumns = {"预约时间", "学生", "状态", "操作"};
        DefaultTableModel appointmentModel = new DefaultTableModel(appointmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // 只有操作列可编辑
            }
        };

        appointmentTable = new JTable(appointmentModel);
        JScrollPane appointmentScrollPane = new JScrollPane(appointmentTable);
        appointmentPanel.add(appointmentScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(schedulePanel);
        splitPane.setBottomComponent(appointmentPanel);
        add(splitPane, BorderLayout.CENTER);

        // 底部面板 - 统计信息
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("工作统计"));

        JLabel totalLabel = new JLabel("总时间段: 0", SwingConstants.CENTER);
        JLabel availableLabel = new JLabel("可预约: 0", SwingConstants.CENTER);
        JLabel bookedLabel = new JLabel("已预约: 0", SwingConstants.CENTER);

        statsPanel.add(totalLabel);
        statsPanel.add(availableLabel);
        statsPanel.add(bookedLabel);

        add(statsPanel, BorderLayout.SOUTH);
    }

    private void loadSchedule() {
        DefaultTableModel model = (DefaultTableModel) scheduleTable.getModel();
        model.setRowCount(0);

        List<TimeSlot> timeSlots = timeSlotService.getRecentAvailableTimeSlots(50); // 最近50个时间段
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (TimeSlot timeSlot : timeSlots) {
            String statusText = getStatusText(timeSlot.getStatus());
            String actionText = "available".equals(timeSlot.getStatus()) ? "删除" : "查看";

            Object[] row = {
                    timeSlot.getStartTime().format(formatter),
                    timeSlot.getEndTime().format(formatter),
                    statusText,
                    actionText
            };
            model.addRow(row);
        }
    }

    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getCounselorAppointments();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            String statusText = getStatusText(appointment.getStatus());
            String actionText = getActionText(appointment.getStatus());

            Object[] row = {
                    appointment.getStartTime().format(formatter) + " - " +
                            appointment.getEndTime().format(formatter).substring(11),
                    appointment.getStudentName(),
                    statusText,
                    actionText
            };
            model.addRow(row);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "available": return "可预约";
            case "booked": return "已预约";
            case "cancelled": return "已取消";
            case "pending": return "待确认";
            case "confirmed": return "已确认";
            case "completed": return "已完成";
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

    private void showAddTimeSlotDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加工作时间段", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 日期时间选择器（简化版）
        JTextField dateField = new JTextField(LocalDateTime.now().toLocalDate().toString());
        JTextField startTimeField = new JTextField("09:00");
        JTextField endTimeField = new JTextField("17:00");
        JTextField durationField = new JTextField("60");
        JTextField breakField = new JTextField("0");

        formPanel.add(new JLabel("日期:"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("开始时间:"));
        formPanel.add(startTimeField);
        formPanel.add(new JLabel("结束时间:"));
        formPanel.add(endTimeField);
        formPanel.add(new JLabel("时长(分钟):"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("间隔(分钟):"));
        formPanel.add(breakField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("添加");
        JButton cancelButton = new JButton("取消");

        addButton.addActionListener(e -> {
            // 这里实现添加时间段的逻辑
            JOptionPane.showMessageDialog(dialog, "添加时间段功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteTimeSlot() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的时间段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取选中的时间段并删除
        // 实际实现需要从表格数据中获取时间段ID
        JOptionPane.showMessageDialog(this, "删除时间段功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshData() {
        loadSchedule();
        loadAppointments();
    }
}