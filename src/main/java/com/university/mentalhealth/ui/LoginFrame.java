package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.UserType;
import com.university.mentalhealth.service.UserService;
import com.university.mentalhealth.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    protected static final Logger logger = Logger.getLogger(LoginFrame.class.getName());

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<UserType> userTypeComboBox;
    private JButton loginButton;
    private JButton exitButton;
    private JButton registerButton;

    private final UserService userService;

    public LoginFrame() {
        this.userService = new UserService();
        initUI();
        setupKeyboardShortcuts();
    }

    private void initUI() {
        setTitle("大学生心理护航系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建标题
        JLabel titleLabel = new JLabel("大学生心理护航系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "用户登录"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(18);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);

        // 用户类型
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel typeLabel = new JLabel("用户类型:");
        typeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        userTypeComboBox = new JComboBox<>(UserType.values());
        userTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(userTypeComboBox, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.BLACK);
        loginButton.setPreferredSize(new Dimension(100, 35));

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(80, 35));

        exitButton = new JButton("退出");
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        exitButton.setPreferredSize(new Dimension(80, 35));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        addEventListeners();

        add(mainPanel);
    }

    private void setupKeyboardShortcuts() {
        // 回车键登录
        getRootPane().setDefaultButton(loginButton);

        // ESC键退出
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });
    }

    private void addEventListeners() {
        // 登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationDialog();
            }
        });

        // 退出按钮事件
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });

        // 回车键登录
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        // 获取输入数据
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserType userType = (UserType) userTypeComboBox.getSelectedItem();

        // 前端验证
        if (username.isEmpty()) {
            showError("用户名不能为空");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("密码不能为空");
            passwordField.requestFocus();
            return;
        }

        // 创建加载对话框（必须在EDT中创建）
        JDialog loadingDialog = createLoadingDialog("正在登录...");

        // 使用SwingWorker处理后台任务
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // 模拟网络延迟（可选）
                    Thread.sleep(500);

                    // 执行登录验证
                    return userService.login(username, password, userType);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "登录过程异常", e);
                    return false;
                }
            }

            @Override
            protected void done() {
                // 在EDT中执行UI更新
                loadingDialog.dispose();

                try {
                    Boolean loginSuccess = get();

                    if (loginSuccess != null && loginSuccess) {
                        showSuccess("登录成功!");
                        openMainFrame();
                        dispose(); // 关闭登录窗口
                    } else {
                        showError("用户名或密码错误，或用户类型不匹配");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    showError("登录过程中发生错误: " + e.getMessage());
                    logger.log(Level.SEVERE, "登录结果处理异常", e);
                }
            }
        }.execute();

        // 显示加载对话框
        loadingDialog.setVisible(true);
    }

    private void showRegistrationDialog() {
        JDialog registerDialog = new JDialog(this, "用户注册", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("用户名:"));
        JTextField regUsernameField = new JTextField();
        panel.add(regUsernameField);

        panel.add(new JLabel("密码:"));
        JPasswordField regPasswordField = new JPasswordField();
        panel.add(regPasswordField);

        panel.add(new JLabel("确认密码:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        panel.add(new JLabel("用户类型:"));
        JComboBox<UserType> regTypeCombo = new JComboBox<>(UserType.values());
        panel.add(regTypeCombo);

        JButton registerBtn = new JButton("注册");
        registerBtn.addActionListener(e -> {
            // 这里可以添加注册逻辑
            JOptionPane.showMessageDialog(registerDialog, "注册功能尚未实现");
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> registerDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        registerDialog.add(panel, BorderLayout.CENTER);
        registerDialog.add(buttonPanel, BorderLayout.SOUTH);
        registerDialog.setVisible(true);
    }

    private JDialog createLoadingDialog(String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setSize(200, 100);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(panel);
        return dialog;
    }

    private void openMainFrame() {
        if (SessionManager.isStudent()) {
            new StudentMainFrame().setVisible(true);
        } else if (SessionManager.isCounselor()) {
            new CounselorMainFrame().setVisible(true);
        } else if (SessionManager.isAdmin()) {
            new AdminMainFrame().setVisible(true);
        }
    }

    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要退出系统吗?",
                "确认退出",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "错误",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "成功",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}