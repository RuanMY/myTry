package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentProcessingDialog extends JDialog {
    private final AppointmentService appointmentService;
    private JTable appointmentsTable;
    private JButton confirmButton;
    private JButton completeButton;
    private JButton cancelButton;
    private JButton viewDetailsButton;

    public AppointmentProcessingDialog(JFrame parent) {
        super(parent, "预约处理", true);
        this.appointmentService = new AppointmentService();

        initUI();
        loadAppointments();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(900, 500));

        // 筛选面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("筛选条件"));

        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"全部", "待确认", "已确认", "已完成", "已取消"});
        JComboBox<String> dateComboBox = new JComboBox<>(new String[]{"今天", "本周", "本月", "全部"});
        JButton filterButton = new JButton("筛选");

        filterButton.addActionListener(e -> filterAppointments(
                (String) statusComboBox.getSelectedItem(),
                (String) dateComboBox.getSelectedItem()
        ));

        filterPanel.add(new JLabel("状态:"));
        filterPanel.add(statusComboBox);
        filterPanel.add(new JLabel("时间:"));
        filterPanel.add(dateComboBox);
        filterPanel.add(filterButton);

        add(filterPanel, BorderLayout.NORTH);

        // 预约表格
        String[] columns = {"预约时间", "学生", "联系方式", "预约原因", "状态", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有操作列可编辑
            }
        };

        appointmentsTable = new JTable(model);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        confirmButton = new JButton("确认预约");
        completeButton = new JButton("完成咨询");
        cancelButton = new JButton("取消预约");
        viewDetailsButton = new JButton("查看详情");
        JButton closeButton = new JButton("关闭");

        confirmButton.addActionListener(e -> processAppointment("confirm"));
        completeButton.addActionListener(e -> processAppointment("complete"));
        cancelButton.addActionListener(e -> processAppointment("cancel"));
        viewDetailsButton.addActionListener(e -> viewAppointmentDetails());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        updateButtonState();
    }

    private void loadAppointments() {
        loadAppointmentsWithFilter("全部", "全部");
    }

    private void filterAppointments(String status, String dateRange) {
        loadAppointmentsWithFilter(status, dateRange);
    }

    private void loadAppointmentsWithFilter(String statusFilter, String dateRange) {
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getCounselorAppointments();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            if (matchesFilter(appointment, statusFilter, dateRange)) {
                String statusText = getStatusText(appointment.getStatus());

                Object[] row = {
                        appointment.getStartTime().format(formatter) + " - " +
                                appointment.getEndTime().format(formatter).substring(11),
                        appointment.getStudentName(),
                        "138****" + (appointment.getStudentId() % 10000),
                        appointment.getNotes() != null ?
                                (appointment.getNotes().length() > 20 ?
                                        appointment.getNotes().substring(0, 20) + "..." :
                                        appointment.getNotes()) : "无",
                        statusText,
                        "操作"
                };
                model.addRow(row);
            }
        }
    }

    private boolean matchesFilter(Appointment appointment, String statusFilter, String dateRange) {
        // 状态过滤
        if (!"全部".equals(statusFilter)) {
            String actualStatus = getStatusText(appointment.getStatus());
            if (!actualStatus.equals(statusFilter)) {
                return false;
            }
        }

        // 时间范围过滤（简化实现）
        // 实际项目中应该根据dateRange进行时间过滤

        return true;
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

    private void updateButtonState() {
        int selectedRow = appointmentsTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;

        confirmButton.setEnabled(hasSelection);
        completeButton.setEnabled(hasSelection);
        cancelButton.setEnabled(hasSelection);
        viewDetailsButton.setEnabled(hasSelection);

        if (hasSelection) {
            String status = (String) appointmentsTable.getValueAt(selectedRow, 4);
            confirmButton.setEnabled("待确认".equals(status));
            completeButton.setEnabled("已确认".equals(status));
            cancelButton.setEnabled(!"已取消".equals(status) && !"已完成".equals(status));
        }
    }

    private void processAppointment(String action) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Appointment> appointments = appointmentService.getCounselorAppointments();
            if (selectedRow < appointments.size()) {
                Appointment appointment = appointments.get(selectedRow);

                boolean success = false;
                String message = "";

                switch (action) {
                    case "confirm":
                        success = appointmentService.confirmAppointment(appointment.getId());
                        message = "确认";
                        break;
                    case "complete":
                        success = appointmentService.completeAppointment(appointment.getId());
                        message = "完成";
                        break;
                    case "cancel":
                        success = appointmentService.cancelAppointment(appointment.getId());
                        message = "取消";
                        break;
                }

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "预约" + message + "成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void viewAppointmentDetails() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<Appointment> appointments = appointmentService.getCounselorAppointments();
            if (selectedRow < appointments.size()) {
                Appointment appointment = appointments.get(selectedRow);
                showAppointmentDetailDialog(appointment);
            }
        }
    }

    private void showAppointmentDetailDialog(Appointment appointment) {
        JDialog detailDialog = new JDialog(this, "预约详情", true);
        detailDialog.setSize(500, 400);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        infoPanel.add(new JLabel("学生姓名:"));
        infoPanel.add(new JLabel(appointment.getStudentName()));
        infoPanel.add(new JLabel("预约时间:"));
        infoPanel.add(new JLabel(appointment.getStartTime().format(formatter) + " - " +
                appointment.getEndTime().format(formatter).substring(11)));
        infoPanel.add(new JLabel("预约状态:"));
        infoPanel.add(new JLabel(getStatusText(appointment.getStatus())));
        infoPanel.add(new JLabel("创建时间:"));
        infoPanel.add(new JLabel(appointment.getCreatedAt().format(formatter)));
        infoPanel.add(new JLabel("预约备注:"));
        JTextArea notesArea = new JTextArea(appointment.getNotes() != null ? appointment.getNotes() : "无");
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEditable(false);
        infoPanel.add(new JScrollPane(notesArea));

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> detailDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        detailDialog.add(infoPanel, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }
}