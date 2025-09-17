package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.StudentDAO;
import com.university.mentalhealth.entity.Student;
import com.university.mentalhealth.util.SessionManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentService {
    private static final Logger logger = Logger.getLogger(StudentService.class.getName());
    private final StudentDAO studentDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }

    /**
     * 获取当前登录学生的信息
     */
    public Optional<Student> getCurrentStudent() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("当前用户不是学生或未登录");
            return Optional.empty();
        }

        int userId = SessionManager.currentUser.getId();
        return studentDAO.findById(userId);
    }

    /**
     * 根据ID获取学生信息
     */
    public Optional<Student> getStudentById(int userId) {
        return studentDAO.findById(userId);
    }

    /**
     * 根据学号获取学生信息
     */
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentDAO.findByStudentId(studentId);
    }

    /**
     * 获取所有学生信息
     */
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    /**
     * 根据院系获取学生信息
     */
    public List<Student> getStudentsByDepartment(String department) {
        return studentDAO.findByDepartment(department);
    }

    /**
     * 更新学生联系信息
     */
    public boolean updateContactInfo(String contactPhone, String emergencyContact, String emergencyPhone) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("当前用户不是学生或未登录");
            return false;
        }

        int userId = SessionManager.currentUser.getId();

        // 验证联系信息
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            logger.warning("联系电话不能为空");
            return false;
        }

        if (emergencyContact == null || emergencyContact.trim().isEmpty()) {
            logger.warning("紧急联系人不能为空");
            return false;
        }

        if (emergencyPhone == null || emergencyPhone.trim().isEmpty()) {
            logger.warning("紧急联系电话不能为空");
            return false;
        }

        boolean result = studentDAO.updateContactInfo(userId, contactPhone, emergencyContact, emergencyPhone);
        if (result) {
            logger.info("学生联系信息更新成功: user_id=" + userId);
        } else {
            logger.severe("学生联系信息更新失败: user_id=" + userId);
        }

        return result;
    }

    /**
     * 更新学生完整信息
     */
    public boolean updateStudentInfo(Student student) {
        // 验证学号格式（示例：20230001）
        if (!isValidStudentId(student.getStudentId())) {
            logger.warning("学号格式不正确: " + student.getStudentId());
            return false;
        }

        // 验证姓名
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            logger.warning("学生姓名不能为空");
            return false;
        }

        // 验证院系
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            logger.warning("院系不能为空");
            return false;
        }

        boolean result = studentDAO.update(student);
        if (result) {
            logger.info("学生信息更新成功: " + student.getStudentId());
        } else {
            logger.severe("学生信息更新失败: " + student.getStudentId());
        }

        return result;
    }

    /**
     * 验证学号格式
     */
    private boolean isValidStudentId(String studentId) {
        if (studentId == null || studentId.length() != 8) {
            return false;
        }

        try {
            // 学号应该是8位数字
            Integer.parseInt(studentId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证电话号码格式
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        // 简单的电话号码验证：11位数字，以1开头
        return phoneNumber.matches("^1[0-9]{10}$");
    }

    /**
     * 获取院系列表
     */
    public List<String> getDepartments() {
        return Arrays.asList(
                "计算机学院",
                "心理学院",
                "经济学院",
                "文学院",
                "法学院",
                "工程学院",
                "医学院",
                "艺术学院"
        );
    }
}