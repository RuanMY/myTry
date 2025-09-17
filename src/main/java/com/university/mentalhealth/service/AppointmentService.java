package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.AppointmentDAO;
import com.university.mentalhealth.dao.CounselorDAO;
import com.university.mentalhealth.dao.TimeSlotDAO;
import com.university.mentalhealth.entity.Appointment;
import com.university.mentalhealth.entity.Counselor;
import com.university.mentalhealth.entity.TimeSlot;
import com.university.mentalhealth.util.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentService {
    private static final Logger logger = Logger.getLogger(AppointmentService.class.getName());
    private final AppointmentDAO appointmentDAO;
    private final CounselorDAO counselorDAO;
    private final TimeSlotDAO timeSlotDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.counselorDAO = new CounselorDAO();
        this.timeSlotDAO = new TimeSlotDAO();
    }

    /**
     * 创建新的预约
     */
    public Optional<Appointment> createAppointment(int timeSlotId, String notes) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("只有学生可以创建预约");
            return Optional.empty();
        }

        try {
            // 检查时间段是否存在且可用
            Optional<TimeSlot> timeSlotOpt = timeSlotDAO.findById(timeSlotId);
            if (!timeSlotOpt.isPresent()) {
                logger.warning("时间段不存在: time_slot_id=" + timeSlotId);
                return Optional.empty();
            }

            TimeSlot timeSlot = timeSlotOpt.get();
            if (!"available".equals(timeSlot.getStatus())) {
                logger.warning("时间段不可用: time_slot_id=" + timeSlotId + ", status=" + timeSlot.getStatus());
                return Optional.empty();
            }

            // 检查时间段是否已被预约
            if (appointmentDAO.isTimeSlotBooked(timeSlotId)) {
                logger.warning("时间段已被预约: time_slot_id=" + timeSlotId);
                return Optional.empty();
            }

            // 创建预约
            int studentId = SessionManager.currentUser.getId();
            Appointment appointment = new Appointment(
                    studentId,
                    timeSlot.getCounselorId(),
                    timeSlotId,
                    notes
            );

            boolean saved = appointmentDAO.save(appointment);
            if (saved) {
                // 更新时间段状态为已预约
                timeSlotDAO.updateStatus(timeSlotId, "booked");
                logger.info("预约创建成功: appointment_id=" + appointment.getId());

                // 发送通知
                sendAppointmentNotification(appointment, "创建");
                return Optional.of(appointment);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "创建预约过程中发生错误", e);
        }
        return Optional.empty();
    }

    /**
     * 取消预约
     */
    public boolean cancelAppointment(int appointmentId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentDAO.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                logger.warning("预约不存在: appointment_id=" + appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();

            // 权限检查：学生只能取消自己的预约，咨询师和管理员可以取消任何预约
            if (SessionManager.isStudent() &&
                    appointment.getStudentId() != SessionManager.currentUser.getId()) {
                logger.warning("学生只能取消自己的预约");
                return false;
            }

            // 更新预约状态
            boolean updated = appointmentDAO.updateStatus(appointmentId, "cancelled");
            if (updated) {
                // 恢复时间段状态为可用
                timeSlotDAO.updateStatus(appointment.getTimeSlotId(), "available");
                logger.info("预约取消成功: appointment_id=" + appointmentId);

                // 发送通知
                sendAppointmentNotification(appointment, "取消");
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "取消预约过程中发生错误", e);
        }
        return false;
    }

    /**
     * 确认预约
     */
    public boolean confirmAppointment(int appointmentId) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以确认预约");
            return false;
        }

        try {
            Optional<Appointment> appointmentOpt = appointmentDAO.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                logger.warning("预约不存在: appointment_id=" + appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();

            // 检查咨询师权限
            if (appointment.getCounselorId() != SessionManager.currentUser.getId()) {
                logger.warning("咨询师只能确认自己的预约");
                return false;
            }

            boolean updated = appointmentDAO.updateStatus(appointmentId, "confirmed");
            if (updated) {
                logger.info("预约确认成功: appointment_id=" + appointmentId);

                // 发送通知
                sendAppointmentNotification(appointment, "确认");
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "确认预约过程中发生错误", e);
        }
        return false;
    }

    /**
     * 完成预约
     */
    public boolean completeAppointment(int appointmentId) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以完成预约");
            return false;
        }

        try {
            Optional<Appointment> appointmentOpt = appointmentDAO.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                logger.warning("预约不存在: appointment_id=" + appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();

            // 检查咨询师权限
            if (appointment.getCounselorId() != SessionManager.currentUser.getId()) {
                logger.warning("咨询师只能完成自己的预约");
                return false;
            }

            boolean updated = appointmentDAO.updateStatus(appointmentId, "completed");
            if (updated) {
                logger.info("预约完成成功: appointment_id=" + appointmentId);

                // 发送通知
                sendAppointmentNotification(appointment, "完成");
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "完成预约过程中发生错误", e);
        }
        return false;
    }

    /**
     * 获取当前学生的预约列表
     */
    public List<Appointment> getStudentAppointments() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isStudent()) {
            logger.warning("只有学生可以查看自己的预约");
            return Collections.emptyList(); // 使用 Collections.emptyList() 替代 List.of()
        }

        int studentId = SessionManager.currentUser.getId();
        return appointmentDAO.findByStudentId(studentId);
    }

    /**
     * 获取咨询师的预约列表
     */
    public List<Appointment> getCounselorAppointments() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以查看自己的预约");
            return Collections.emptyList(); // 使用 Collections.emptyList() 替代 List.of()
        }

        int counselorId = SessionManager.currentUser.getId();
        return appointmentDAO.findByCounselorId(counselorId);
    }

    /**
     * 获取所有预约（管理员用）
     */
    public List<Appointment> getAllAppointments() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isAdmin()) {
            logger.warning("只有管理员可以查看所有预约");
            return Collections.emptyList(); // 使用 Collections.emptyList() 替代 List.of()
        }

        return appointmentDAO.findAll();
    }

    /**
     * 根据ID获取预约详情
     */
    public Optional<Appointment> getAppointmentById(int appointmentId) {
        return appointmentDAO.findById(appointmentId);
    }

    /**
     * 检查时间段是否可用
     */
    public boolean isTimeSlotAvailable(int timeSlotId) {
        Optional<TimeSlot> timeSlotOpt = timeSlotDAO.findById(timeSlotId);
        if (!timeSlotOpt.isPresent()) {
            return false;
        }

        TimeSlot timeSlot = timeSlotOpt.get();
        return "available".equals(timeSlot.getStatus()) &&
                !appointmentDAO.isTimeSlotBooked(timeSlotId);
    }

    /**
     * 获取咨询师的可用时间段
     */
    public List<TimeSlot> getAvailableTimeSlots(int counselorId, LocalDateTime startDate, LocalDateTime endDate) {
        return timeSlotDAO.findAvailableTimeSlots(counselorId, startDate, endDate);
    }

    /**
     * 获取所有可用咨询师
     */
    public List<Counselor> getAvailableCounselors() {
        return counselorDAO.findAvailableCounselors();
    }

    /**
     * 检查预约冲突
     */
    public boolean hasAppointmentConflict(int studentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> appointments = appointmentDAO.findByStudentId(studentId);

        for (Appointment appointment : appointments) {
            if (appointment.getStartTime() != null && appointment.getEndTime() != null &&
                    !"cancelled".equals(appointment.getStatus()) &&
                    isTimeOverlap(appointment.getStartTime(), appointment.getEndTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查时间重叠
     */
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * 发送预约通知（模拟）
     */
    public void sendAppointmentNotification(Appointment appointment, String action) {
        // 这里可以集成邮件或短信服务
        logger.info("预约通知: " + action + " - " + appointment.toString());

        // 模拟发送通知
        String message = String.format(
                "预约%s通知: 学生%s 预约了 %s 在 %s 进行咨询",
                action,
                appointment.getStudentName() != null ? appointment.getStudentName() : "未知学生",
                appointment.getCounselorName() != null ? appointment.getCounselorName() : "未知咨询师",
                appointment.getStartTime() != null ? appointment.getStartTime().toString() : "未知时间"
        );

        logger.info(message);
    }

    /**
     * 获取待处理的预约数量（咨询师用）
     */
    public int getPendingAppointmentCount() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            return 0;
        }

        int counselorId = SessionManager.currentUser.getId();
        List<Appointment> appointments = appointmentDAO.findByCounselorId(counselorId);

        int count = 0;
        for (Appointment appointment : appointments) {
            if ("pending".equals(appointment.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取今日预约（咨询师用）
     */
    public List<Appointment> getTodayAppointments() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            return Collections.emptyList();
        }

        int counselorId = SessionManager.currentUser.getId();
        List<Appointment> appointments = appointmentDAO.findByCounselorId(counselorId);

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        List<Appointment> todayAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getStartTime() != null &&
                    !appointment.getStartTime().isBefore(todayStart) &&
                    appointment.getStartTime().isBefore(todayEnd) &&
                    !"cancelled".equals(appointment.getStatus())) {
                todayAppointments.add(appointment);
            }
        }

        return todayAppointments;
    }

    /**
     * 检查学生是否可以预约（时间冲突检查）
     */
    public boolean canStudentMakeAppointment(int studentId, LocalDateTime startTime, LocalDateTime endTime) {
        return !hasAppointmentConflict(studentId, startTime, endTime);
    }
}