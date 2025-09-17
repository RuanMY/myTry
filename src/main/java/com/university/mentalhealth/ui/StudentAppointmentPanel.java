package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.entity.Counselor;
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
import java.util.Optional;

public class StudentAppointmentPanel extends JPanel {
    private final AppointmentService appointmentService;
    private final CounselorService counselorService;
    private final TimeSlotService timeSlotService;

    private JComboBox<Counselor> counselorComboBox;
    private JTable timeSlotTable;
    private JTable appointmentTable;
    private JTextArea notesTextArea;
    private JButton bookButton;
    private JButton cancelButton;
    private JButton refreshButton;

    public StudentAppointmentPanel() {
        this.appointmentService = new AppointmentService();
        this.counselorService = new CounselorService();
        this.timeSlotService = new TimeSlotService();

        initUI();
        loadCounselors();
        loadAppointments();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部面板 - 咨询师选择
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("选择咨询师:"));

        counselorComboBox = new JComboBox<>();
        counselorComboBox.setPreferredSize(new Dimension(250, 30));
        counselorComboBox.addActionListener(e -> loadTimeSlots());
        topPanel.add(counselorComboBox);

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // 中部面板 - 时间段表格和预约表格
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);

        // 左侧 - 可用时间段
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("可用时间段"));

        String[] timeSlotColumns = {"时间", "咨询师", "状态", "操作"};
        DefaultTableModel timeSlotModel = new DefaultTableModel(timeSlotColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // 只有操作列可编辑
            }
        };

        timeSlotTable = new JTable(timeSlotModel);
        timeSlotTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());
        JScrollPane timeSlotScrollPane = new JScrollPane(timeSlotTable);
        leftPanel.add(timeSlotScrollPane, BorderLayout.CENTER);

        // 右侧 - 我的预约
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("我的预约"));

        String[] appointmentColumns = {"预约时间", "咨询师", "状态", "操作"};
        DefaultTableModel appointmentModel = new DefaultTableModel(appointmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // 只有操作列可编辑
            }
        };

        appointmentTable = new JTable(appointmentModel);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());
        JScrollPane appointmentScrollPane = new JScrollPane(appointmentTable);
        rightPanel.add(appointmentScrollPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // 底部面板 - 备注和按钮
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
        notesPanel.setBorder(BorderFactory.createTitledBorder("预约备注"));
        notesTextArea = new JTextArea(3, 40);
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);
        notesPanel.add(new JScrollPane(notesTextArea), BorderLayout.CENTER);
        bottomPanel.add(notesPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bookButton = new JButton("预约选中时间段");
        bookButton.setEnabled(false);
        bookButton.addActionListener(e -> bookAppointment());

        cancelButton = new JButton("取消选中预约");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelAppointment());

        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCounselors() {
        counselorComboBox.removeAllItems();
        List<Counselor> counselors = counselorService.getAvailableCounselors();
        for (Counselor counselor : counselors) {
            counselorComboBox.addItem(counselor);
        }
    }

    private void loadTimeSlots() {
        Counselor selectedCounselor = (Counselor) counselorComboBox.getSelectedItem();
        if (selectedCounselor == null) return;

        DefaultTableModel model = (DefaultTableModel) timeSlotTable.getModel();
        model.setRowCount(0);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusWeeks(2); // 未来两周

        List<TimeSlot> timeSlots = timeSlotService.getAvailableTimeSlots(
                selectedCounselor.getId(), now, endDate
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (TimeSlot timeSlot : timeSlots) {
            if (timeSlotService.isTimeSlotBookable(timeSlot.getId())) {
                Object[] row = {
                        timeSlot.getStartTime().format(formatter) + " - " +
                                timeSlot.getEndTime().format(formatter).substring(11),
                        selectedCounselor.getName(),
                        "可预约",
                        "预约"
                };
                model.addRow(row);
            }
        }
    }

    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        List<Appointment> appointments = appointmentService.getStudentAppointments();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment appointment : appointments) {
            String statusText = getStatusText(appointment.getStatus());
            String actionText = "pending".equals(appointment.getStatus()) ? "取消" : "查看";

            Object[] row = {
                    appointment.getStartTime().format(formatter) + " - " +
                            appointment.getEndTime().format(formatter).substring(11),
                    appointment.getCounselorName(),
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

    private void updateButtonState() {
        boolean timeSlotSelected = timeSlotTable.getSelectedRow() >= 0;
        boolean appointmentSelected = appointmentTable.getSelectedRow() >= 0;

        bookButton.setEnabled(timeSlotSelected);
        cancelButton.setEnabled(appointmentSelected);
    }

    private void bookAppointment() {
        int selectedRow = timeSlotTable.getSelectedRow();
        if (selectedRow < 0) return;

        Counselor selectedCounselor = (Counselor) counselorComboBox.getSelectedItem();
        if (selectedCounselor == null) return;

        DefaultTableModel model = (DefaultTableModel) timeSlotTable.getModel();
        String timeRange = (String) model.getValueAt(selectedRow, 0);

        // 从表格数据中解析出时间段ID（这里需要实际的时间段ID）
        // 在实际应用中，应该在表格中存储时间段ID
        List<TimeSlot> timeSlots = timeSlotService.getAvailableTimeSlots(
                selectedCounselor.getId(), LocalDateTime.now(), LocalDateTime.now().plusWeeks(2)
        );

        if (selectedRow < timeSlots.size()) {
            TimeSlot selectedTimeSlot = timeSlots.get(selectedRow);
            String notes = notesTextArea.getText().trim();

            Optional<Appointment> result = appointmentService.createAppointment(
                    selectedTimeSlot.getId(), notes
            );

            if (result.isPresent()) {
                JOptionPane.showMessageDialog(this, "预约成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                notesTextArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "预约失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) return;

        List<Appointment> appointments = appointmentService.getStudentAppointments();
        if (selectedRow < appointments.size()) {
            Appointment appointment = appointments.get(selectedRow);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "确定要取消这个预约吗？",
                    "确认取消",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = appointmentService.cancelAppointment(appointment.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "预约已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "取消预约失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void refreshData() {
        loadCounselors();
        loadTimeSlots();
        loadAppointments();
        updateButtonState();
    }
}