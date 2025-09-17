package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.entity.UserType;
import com.university.mentalhealth.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class UserManagementPanel extends JPanel {
    private final UserService userService;
    private JTable userTable;
    private JTextField searchField;
    private JComboBox<UserType> userTypeFilter;

    public UserManagementPanel() {
        this.userService = new UserService();
        initUI();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部搜索和操作面板
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("搜索:"));

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchUsers());
        searchPanel.add(searchButton);

        searchPanel.add(new JLabel("用户类型:"));
        userTypeFilter = new JComboBox<>(UserType.values());
        userTypeFilter.insertItemAt(null, 0); // 添加"全部"选项
        userTypeFilter.setSelectedIndex(0);
        userTypeFilter.addActionListener(e -> filterUsers());
        searchPanel.add(userTypeFilter);

        // 操作按钮面板
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addButton = new JButton("添加用户");
        JButton editButton = new JButton("编辑用户");
        JButton disableButton = new JButton("禁用/启用");
        JButton resetPasswordButton = new JButton("重置密码");
        JButton refreshButton = new JButton("刷新");

        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        disableButton.addActionListener(e -> toggleUserStatus());
        resetPasswordButton.addActionListener(e -> resetPassword());
        refreshButton.addActionListener(e -> loadUsers());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(disableButton);
        actionPanel.add(resetPasswordButton);
        actionPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 用户表格
        String[] columns = {"ID", "用户名", "用户类型", "状态", "注册时间", "最后登录", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑
            }
        };

        userTable = new JTable(model);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部统计信息
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("用户统计"));

        JLabel totalLabel = new JLabel("总用户: 0");
        JLabel studentLabel = new JLabel("学生: 0");
        JLabel counselorLabel = new JLabel("咨询师: 0");
        JLabel adminLabel = new JLabel("管理员: 0");
        JLabel activeLabel = new JLabel("活跃: 0");

        statsPanel.add(totalLabel);
        statsPanel.add(studentLabel);
        statsPanel.add(counselorLabel);
        statsPanel.add(adminLabel);
        statsPanel.add(activeLabel);

        add(statsPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);

        List<User> users = userService.getAllUsers();
        updateStatistics(users);

        for (User user : users) {
            String status = user.toString().contains("is_active=true") ? "活跃" : "禁用";
            String lastLogin = "从未登录"; // 实际应该从数据库获取

            Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getType().getDisplayName(),
                    status,
                    user.getCreatedAt().toString().substring(0, 10),
                    lastLogin,
                    "操作"
            };
            model.addRow(row);
        }
    }

    private void updateStatistics(List<User> users) {
        int total = users.size();
        int students = 0;
        int counselors = 0;
        int admins = 0;
        int active = 0;

        for (User user : users) {
            switch (user.getType()) {
                case student: students++; break;
                case counselor: counselors++; break;
                case admin: admins++; break;
            }
            if (user.toString().contains("is_active=true")) {
                active++;
            }
        }

        // 更新统计标签
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        ((JLabel) components[0]).setText("总用户: " + total);
        ((JLabel) components[1]).setText("学生: " + students);
        ((JLabel) components[2]).setText("咨询师: " + counselors);
        ((JLabel) components[3]).setText("管理员: " + admins);
        ((JLabel) components[4]).setText("活跃: " + active);
    }

    private void searchUsers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }

        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);

        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.getUsername().contains(keyword) ||
                    String.valueOf(user.getId()).contains(keyword)) {
                addUserToTable(user, model);
            }
        }
    }

    private void filterUsers() {
        UserType selectedType = (UserType) userTypeFilter.getSelectedItem();
        if (selectedType == null) {
            loadUsers();
            return;
        }

        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);

        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.getType() == selectedType) {
                addUserToTable(user, model);
            }
        }
    }

    private void addUserToTable(User user, DefaultTableModel model) {
        String status = user.toString().contains("is_active=true") ? "活跃" : "禁用";
        String lastLogin = "从未登录";

        Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getType().getDisplayName(),
                status,
                user.getCreatedAt().toString().substring(0, 10),
                lastLogin,
                "操作"
        };
        model.addRow(row);
    }

    private void addUser() {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加用户", null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadUsers();
        }
    }

    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (Integer) userTable.getValueAt(selectedRow, 0);
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑用户", userOpt.get());
                dialog.setVisible(true);
                if (dialog.isSuccess()) {
                    loadUsers();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要编辑的用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleUserStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (Integer) userTable.getValueAt(selectedRow, 0);
            String currentStatus = (String) userTable.getValueAt(selectedRow, 3);
            boolean newStatus = !"活跃".equals(currentStatus);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要" + (newStatus ? "启用" : "禁用") + "这个用户吗？",
                    "确认操作", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = newStatus ? userService.enableUser(userId) : userService.disableUser(userId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "操作成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "操作失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要操作的用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (Integer) userTable.getValueAt(selectedRow, 0);
            String username = (String) userTable.getValueAt(selectedRow, 1);

            String newPassword = JOptionPane.showInputDialog(this,
                    "为用户 " + username + " 设置新密码:", "重置密码", JOptionPane.QUESTION_MESSAGE);

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (userService.changePassword(userId, "temp_password", newPassword)) {
                    JOptionPane.showMessageDialog(this, "密码重置成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "密码重置失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要重置密码的用户", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
}