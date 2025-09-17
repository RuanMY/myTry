package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.CounselorDAO;
import com.university.mentalhealth.dao.TimeSlotDAO;
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

public class CounselorService {
    private static final Logger logger = Logger.getLogger(CounselorService.class.getName());
    private final CounselorDAO counselorDAO;
    private final TimeSlotDAO timeSlotDAO;

    public CounselorService() {
        this.counselorDAO = new CounselorDAO();
        this.timeSlotDAO = new TimeSlotDAO();
    }

    /**
     * 获取所有可用咨询师
     */
    public List<Counselor> getAvailableCounselors() {
        return counselorDAO.findAvailableCounselors();
    }

    /**
     * 根据ID获取咨询师
     */
    public Optional<Counselor> getCounselorById(int counselorId) {
        return counselorDAO.findById(counselorId);
    }

    /**
     * 获取当前登录的咨询师信息
     */
    public Optional<Counselor> getCurrentCounselor() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("当前用户不是咨询师或未登录");
            return Optional.empty();
        }

        int userId = SessionManager.currentUser.getId();
        return counselorDAO.findByUserId(userId);
    }

    /**
     * 更新咨询师可用状态
     */
    public boolean updateCounselorAvailability(boolean isAvailable) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以更新自己的可用状态");
            return false;
        }

        int counselorId = SessionManager.currentUser.getId();
        boolean updated = counselorDAO.updateAvailability(counselorId, isAvailable);

        if (updated) {
            logger.info("咨询师可用状态更新成功: counselor_id=" + counselorId + ", is_available=" + isAvailable);
        }

        return updated;
    }

    /**
     * 添加咨询师工作时间段
     */
    public Optional<TimeSlot> addTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以添加工作时间段");
            return Optional.empty();
        }

        try {
            int counselorId = SessionManager.currentUser.getId();

            // 检查时间冲突
            if (timeSlotDAO.hasTimeConflict(counselorId, startTime, endTime)) {
                logger.warning("时间段冲突: counselor_id=" + counselorId);
                return Optional.empty();
            }

            // 检查时间有效性（至少30分钟）
            if (startTime.plusMinutes(30).isAfter(endTime)) {
                logger.warning("时间段太短，至少需要30分钟");
                return Optional.empty();
            }

            TimeSlot timeSlot = new TimeSlot(counselorId, startTime, endTime);
            boolean saved = timeSlotDAO.save(timeSlot);

            if (saved) {
                logger.info("时间段添加成功: time_slot_id=" + timeSlot.getId());
                return Optional.of(timeSlot);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "添加时间段过程中发生错误", e);
        }
        return Optional.empty();
    }

    /**
     * 删除工作时间段
     */
    public boolean deleteTimeSlot(int timeSlotId) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以删除自己的时间段");
            return false;
        }

        try {
            Optional<TimeSlot> timeSlotOpt = timeSlotDAO.findById(timeSlotId);
            if (!timeSlotOpt.isPresent()) {
                logger.warning("时间段不存在: time_slot_id=" + timeSlotId);
                return false;
            }

            TimeSlot timeSlot = timeSlotOpt.get();

            // 检查权限
            if (timeSlot.getCounselorId() != SessionManager.currentUser.getId()) {
                logger.warning("只能删除自己的时间段");
                return false;
            }

            // 检查时间段是否已被预约
            if ("booked".equals(timeSlot.getStatus())) {
                logger.warning("已被预约的时间段不能删除");
                return false;
            }

            boolean deleted = timeSlotDAO.delete(timeSlotId);
            if (deleted) {
                logger.info("时间段删除成功: time_slot_id=" + timeSlotId);
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "删除时间段过程中发生错误", e);
        }
        return false;
    }

    /**
     * 获取咨询师的所有时间段
     */
    public List<TimeSlot> getCounselorTimeSlots() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以查看自己的时间段");
            return Collections.emptyList(); // 替换 List.of()
        }

        int counselorId = SessionManager.currentUser.getId();
        return timeSlotDAO.findByCounselorId(counselorId);
    }

    /**
     * 获取咨询师的工作统计
     */
    public CounselorWorkload getWorkloadStatistics() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以查看工作统计");
            return new CounselorWorkload();
        }

        int counselorId = SessionManager.currentUser.getId();
        List<TimeSlot> timeSlots = timeSlotDAO.findByCounselorId(counselorId);

        CounselorWorkload workload = new CounselorWorkload();
        workload.totalSlots = timeSlots.size();

        int availableCount = 0;
        int bookedCount = 0;
        for (TimeSlot timeSlot : timeSlots) {
            if ("available".equals(timeSlot.getStatus())) {
                availableCount++;
            } else if ("booked".equals(timeSlot.getStatus())) {
                bookedCount++;
            }
        }

        workload.availableSlots = availableCount;
        workload.bookedSlots = bookedCount;

        return workload;
    }

    /**
     * 咨询师工作统计类
     */
    public static class CounselorWorkload {
        public int totalSlots;
        public int availableSlots;
        public int bookedSlots;

        public int getUtilizationRate() {
            if (totalSlots == 0) return 0;
            return (bookedSlots * 100) / totalSlots;
        }
    }

    /**
     * 更新咨询师个人信息
     */
    public boolean updateCounselorInfo(String title, String specialization) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以更新个人信息");
            return false;
        }

        try {
            Optional<Counselor> counselorOpt = getCurrentCounselor();
            if (!counselorOpt.isPresent()) {
                return false;
            }

            Counselor counselor = counselorOpt.get();
            counselor.setTitle(title);
            counselor.setSpecialization(specialization);

            boolean updated = counselorDAO.update(counselor);
            if (updated) {
                logger.info("咨询师信息更新成功: counselor_id=" + counselor.getId());
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "更新咨询师信息过程中发生错误", e);
        }
        return false;
    }

    /**
     * 搜索咨询师（按专业领域）
     */
    public List<Counselor> searchCounselorsBySpecialization(String keyword) {
        List<Counselor> allCounselors = counselorDAO.findAvailableCounselors();
        List<Counselor> result = new ArrayList<>();

        for (Counselor counselor : allCounselors) {
            if (counselor.getSpecialization() != null &&
                    counselor.getSpecialization().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(counselor);
            }
        }

        return result;
    }

    /**
     * 获取咨询师的本周时间段
     */
    public List<TimeSlot> getThisWeekTimeSlots() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            return Collections.emptyList(); // 替换 List.of()
        }

        int counselorId = SessionManager.currentUser.getId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        return timeSlotDAO.findAvailableTimeSlots(counselorId, startOfWeek, endOfWeek);
    }

    /**
     * 批量添加时间段（按天）
     */
    public int addDailyTimeSlots(LocalDateTime date, int startHour, int endHour, int durationMinutes) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以添加时间段");
            return 0;
        }

        int counselorId = SessionManager.currentUser.getId();
        int addedCount = 0;

        LocalDateTime dayStart = date.withHour(startHour).withMinute(0).withSecond(0);
        LocalDateTime dayEnd = date.withHour(endHour).withMinute(0).withSecond(0);

        LocalDateTime currentStart = dayStart;

        while (currentStart.plusMinutes(durationMinutes).isBefore(dayEnd) ||
                currentStart.plusMinutes(durationMinutes).isEqual(dayEnd)) {

            LocalDateTime currentEnd = currentStart.plusMinutes(durationMinutes);

            // 检查时间冲突
            if (!timeSlotDAO.hasTimeConflict(counselorId, currentStart, currentEnd)) {
                TimeSlot timeSlot = new TimeSlot(counselorId, currentStart, currentEnd);
                if (timeSlotDAO.save(timeSlot)) {
                    addedCount++;
                }
            }

            currentStart = currentEnd;
        }

        logger.info("每日时间段添加完成: 成功添加 " + addedCount + " 个时间段");
        return addedCount;
    }

    /**
     * 获取咨询师的预约统计
     */
    public AppointmentStats getAppointmentStats() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            return new AppointmentStats();
        }

        int counselorId = SessionManager.currentUser.getId();
        List<TimeSlot> timeSlots = timeSlotDAO.findByCounselorId(counselorId);

        AppointmentStats stats = new AppointmentStats();
        for (TimeSlot timeSlot : timeSlots) {
            if ("booked".equals(timeSlot.getStatus())) {
                stats.totalAppointments++;
            } else if ("available".equals(timeSlot.getStatus())) {
                stats.availableSlots++;
            }
        }

        return stats;
    }

    /**
     * 预约统计类
     */
    public static class AppointmentStats {
        public int totalAppointments;
        public int availableSlots;

        public int getTotalSlots() {
            return totalAppointments + availableSlots;
        }

        public double getBookingRate() {
            int total = getTotalSlots();
            if (total == 0) return 0.0;
            return (totalAppointments * 100.0) / total;
        }
    }
}