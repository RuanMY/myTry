package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.entity.UserType;
import com.university.mentalhealth.service.UserService;
import com.university.mentalhealth.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class UserDialog extends JDialog {
    private final UserService userService;
    private final User existingUser;
    private boolean success = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<UserType> userTypeComboBox;
    private JCheckBox activeCheckBox;

    public UserDialog(Frame parent, String title, User user) {
        super(parent, title, true);
        this.userService = new UserService();
        this.existingUser = user;

        initUI();
        if (user != null) {
            populateFields(user);
        }
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(400, 300));

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 用户名
        formPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        // 密码
        formPanel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        // 确认密码
        formPanel.add(new JLabel("确认密码:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        // 用户类型
        formPanel.add(new JLabel("用户类型:"));
        userTypeComboBox = new JComboBox<>(UserType.values());
        formPanel.add(userTypeComboBox);

        // 激活状态
        formPanel.add(new JLabel("账户状态:"));
        activeCheckBox = new JCheckBox("激活账户");
        activeCheckBox.setSelected(true);
        formPanel.add(activeCheckBox);

        add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        userTypeComboBox.setSelectedItem(user.getType());
        activeCheckBox.setSelected(true); // 默认激活

        // 编辑用户时密码字段留空
        passwordField.setText("");
        confirmPasswordField.setText("");

        // 如果是编辑模式，用户名不可编辑
        usernameField.setEditable(false);
    }

    private void saveUser() {
        if (!validateInput()) {
            return;
        }

        try {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            UserType userType = (UserType) userTypeComboBox.getSelectedItem();
            boolean isActive = activeCheckBox.isSelected();

            if (existingUser == null) {
                // 新建用户
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPasswordHash(password);
                newUser.setType(userType);
                newUser.setCreatedAt(LocalDateTime.now());

                boolean saved = userService.register(newUser);
                if (saved) {
                    success = true;
                    JOptionPane.showMessageDialog(this, "用户创建成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "用户创建失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // 编辑用户
                existingUser.setType(userType);

                // 如果密码不为空，则更新密码
                if (!password.isEmpty()) {
                    existingUser.setPasswordHash(password);
                }

                boolean updated = userService.updateUser(existingUser);
                if (updated) {
                    // 更新激活状态
                    if (isActive) {
                        userService.enableUser(existingUser.getId());
                    } else {
                        userService.disableUser(existingUser.getId());
                    }

                    success = true;
                    JOptionPane.showMessageDialog(this, "用户更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "用户更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "操作失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 验证用户名
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "用户名至少需要3个字符", "错误", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return false;
        }

        // 如果是新建用户，验证密码
        if (existingUser == null) {
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocus();
                return false;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "密码至少需要6个字符", "错误", JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocus();
                return false;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                confirmPasswordField.requestFocus();
                return false;
            }

            if (!PasswordUtil.validatePasswordStrength(password)) {
                JOptionPane.showMessageDialog(this,
                        "密码强度不足。密码必须包含字母和数字，且至少6位",
                        "错误", JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocus();
                return false;
            }
        } else {
            // 编辑用户时，如果输入了密码，需要验证
            if (!password.isEmpty()) {
                if (password.length() < 6) {
                    JOptionPane.showMessageDialog(this, "密码至少需要6个字符", "错误", JOptionPane.ERROR_MESSAGE);
                    passwordField.requestFocus();
                    return false;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                    confirmPasswordField.requestFocus();
                    return false;
                }

                if (!PasswordUtil.validatePasswordStrength(password)) {
                    JOptionPane.showMessageDialog(this,
                            "密码强度不足。密码必须包含字母和数字，且至少6位",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    passwordField.requestFocus();
                    return false;
                }
            }
        }

        // 检查用户名是否已存在（新建用户时）
        if (existingUser == null && userService.getUserByUsername(username).isPresent()) {
            JOptionPane.showMessageDialog(this, "用户名已存在", "错误", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return false;
        }

        return true;
    }

    public boolean isSuccess() {
        return success;
    }
}