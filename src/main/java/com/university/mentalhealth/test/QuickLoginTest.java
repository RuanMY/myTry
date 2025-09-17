package com.university.mentalhealth.test;

import com.university.mentalhealth.service.UserService;
import com.university.mentalhealth.entity.UserType;
import java.util.logging.Logger;

public class QuickLoginTest {
    private static final Logger logger = Logger.getLogger(QuickLoginTest.class.getName());

    public static void main(String[] args) {
        logger.info("快速登录测试开始...");

        UserService userService = new UserService();

        // 测试学生登录
        testLogin(userService, "stu001", "123456", UserType.student, "学生登录测试");

        // 测试咨询师登录
        testLogin(userService, "coun01", "123456", UserType.counselor, "咨询师登录测试");

        // 测试管理员登录
        testLogin(userService, "admin1", "123456", UserType.admin, "管理员登录测试");

        logger.info("测试完成");
    }

    private static void testLogin(UserService userService, String username, String password,
                                  UserType userType, String testName) {
        logger.info("\n=== " + testName + " ===");
        logger.info("用户名: " + username);
        logger.info("密码: " + password);
        logger.info("预期类型: " + userType);

        boolean result = userService.login(username, password, userType);

        if (result) {
            logger.info("测试通过: 登录成功");
        } else {
            logger.info("测试失败: 登录失败");
        }

        logger.info("---");
    }
}
