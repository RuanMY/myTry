package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentManagementDialog extends JDialog {
    private final AppointmentService appointmentService;
    private JTable appointmentTable;
    private JButton refreshButton;
    private JButton closeButton;

    public AppointmentManagementDialog(JFrame parent) {
        super(parent, "预约管理", true);
        this.appointmentService = new AppointmentService();

        initUI();
        loadAppointments();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(800, 600));

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        refreshButton = new JButton("刷新");
        closeButton = new JButton("关闭");

        refreshButton.addActionListener(e -> loadAppointments());
        closeButton.addActionListener(e -> dispose());

        topPanel.add(refreshButton);
        topPanel.add(closeButton);
        add(topPanel, BorderLayout.NORTH);

        // 中部表格
        String[] columns = {"ID", "学生", "咨询师", "预约时间", "状态", "创建时间", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑
            }
        };

        appointmentTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("双击行可以查看详情"));
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getAllAppointments();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            Object[] row = {
                    appointment.getId(),
                    appointment.getStudentName(),
                    appointment.getCounselorName(),
                    appointment.getStartTime() != null ?
                            appointment.getStartTime().format(formatter) + " - " +
                                    appointment.getEndTime().format(formatter).substring(11) : "N/A",
                    getStatusText(appointment.getStatus()),
                    appointment.getCreatedAt().format(formatter),
                    "查看详情"
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
}