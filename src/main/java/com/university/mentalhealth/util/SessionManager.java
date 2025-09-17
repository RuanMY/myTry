package com.university.mentalhealth.util;

import com.university.mentalhealth.entity.User;
import com.university.mentalhealth.entity.UserType;

public class SessionManager {
    // 全局静态变量存储当前用户信息
    public static User currentUser = null;
    public static UserType currentUserType = null;

    public static void login(User user, UserType userType) {
        currentUser = user;
        currentUserType = userType;
    }

    public static void logout() {
        currentUser = null;
        currentUserType = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isStudent() {
        return currentUserType == UserType.student;
    }

    public static boolean isCounselor() {
        return currentUserType == UserType.counselor;
    }

    public static boolean isAdmin() {
        return currentUserType == UserType.admin;
    }
}