package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.UserDAO;
import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.entity.UserType;
import com.university.mentalhealth.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public boolean login(String username, String password, UserType expectedType) {
        // 添加详细的调试信息
        logger.info("登录尝试 - 用户名: " + username + ", 预期类型: " + expectedType);
        logger.info("输入密码: " + password);

        // 前端验证
        if (username == null || username.trim().isEmpty()) {
            logger.warning("登录失败: 用户名为空");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warning("登录失败: 密码为空");
            return false;
        }

        try {
            // 后端验证
            logger.info("正在验证用户信息...");
            Optional<User> userOpt = userDAO.authenticate(username, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("找到用户: " + user.getUsername() + ", 数据库中的类型: " + user.getType());
                logger.info("用户状态: " + (userDAO.findById(user.getId()).get() != null ? "活跃" : "禁用"));

                // 检查用户类型是否符合预期
                if (user.getType() != expectedType) {
                    logger.warning("类型不匹配 - 预期: " + expectedType + ", 实际: " + user.getType());
                    return false;
                }

                // 登录成功，设置会话
                SessionManager.login(user, user.getType());
                logger.info("登录成功: " + username);
                return true;
            } else {
                logger.warning("认证失败: 未找到用户或密码错误");
                // 进一步诊断为什么认证失败
                diagnoseAuthenticationFailure(username, password);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "登录过程发生异常", e);
        }

        return false;
    }

    // 添加诊断方法
    private void diagnoseAuthenticationFailure(String username, String password) {
        logger.info("开始诊断认证失败原因...");

        try {
            // 检查用户是否存在
            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("用户存在: " + user.getUsername() + ", 类型: " + user.getType() + ", 状态: 活跃");
                logger.info("数据库存储的密码: " + user.getPasswordHash());
                logger.info("用户输入的密码: " + password);
                logger.info("密码匹配: " + user.getPasswordHash().equals(password));
            } else {
                logger.warning("用户不存在: " + username);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "诊断过程中发生错误", e);
        }
    }

    public void logout() {
        if (SessionManager.isLoggedIn()) {
            logger.info("用户登出: " + SessionManager.currentUser.getUsername());
            SessionManager.logout();
        }
    }

    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            logger.warning("注册失败: 用户名已存在 - " + user.getUsername());
            return false;
        }

        // 验证密码强度
        if (!validatePasswordStrength(user.getPasswordHash())) {
            logger.warning("注册失败: 密码强度不足 - " + user.getUsername());
            return false;
        }

        return userDAO.save(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // 前端验证
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            logger.warning("修改密码失败: 旧密码为空");
            return false;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            logger.warning("修改密码失败: 新密码为空");
            return false;
        }

        if (newPassword.length() < 6) {
            logger.warning("修改密码失败: 新密码长度不足6位");
            return false;
        }

        // 获取用户
        Optional<User> userOpt = userDAO.findById(userId);
        if (!userOpt.isPresent()) {
            logger.warning("修改密码失败: 用户不存在");
            return false;
        }

        User user = userOpt.get();

        // 验证旧密码
        if (!user.getPasswordHash().equals(oldPassword)) {
            logger.warning("修改密码失败: 旧密码不正确");
            return false;
        }

        // 更新密码
        user.setPasswordHash(newPassword);
        boolean result = userDAO.update(user);
        if (result) {
            logger.info("密码修改成功: 用户ID=" + userId);
        } else {
            logger.severe("密码修改失败: 用户ID=" + userId);
        }

        return result;
    }

    public Optional<User> getUserById(int userId) {
        return userDAO.findById(userId);
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getUsersByType(UserType userType) {
        return userDAO.findByType(userType);
    }

    public boolean updateUser(User user) {
        // 验证用户名是否已被其他用户使用
        Optional<User> existingUser = userDAO.findByUsername(user.getUsername());
        if (existingUser.isPresent() && !(existingUser.get().getId() == user.getId())) {
            logger.warning("更新用户失败: 用户名已被其他用户使用");
            return false;
        }

        boolean result = userDAO.update(user);
        if (result) {
            logger.info("用户信息更新成功: " + user.getUsername());
        } else {
            logger.severe("用户信息更新失败: " + user.getUsername());
        }

        return result;
    }

    /**
     * 禁用用户
     */
    public boolean disableUser(int userId) {
        return userDAO.updateUserStatus(userId, false);
    }

    /**
     * 启用用户
     */
    public boolean enableUser(int userId) {
        return userDAO.updateUserStatus(userId, true);
    }

    public boolean deleteUser(int userId) {
        boolean result = userDAO.delete(userId);
        if (result) {
            logger.info("用户删除成功: ID=" + userId);
        } else {
            logger.severe("用户删除失败: ID=" + userId);
        }

        return result;
    }

    public boolean isUsernameAvailable(String username) {
        return !userDAO.findByUsername(username).isPresent();
    }

    public boolean validatePasswordStrength(String password) {
        // 密码强度验证：至少6位，包含字母和数字
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }
}