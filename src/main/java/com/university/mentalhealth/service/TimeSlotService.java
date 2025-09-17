package com.university.mentalhealth.service;

import com.university.mentalhealth.dao.TimeSlotDAO;
import com.university.mentalhealth.entity.TimeSlot;
import com.university.mentalhealth.util.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeSlotService {
    private static final Logger logger = Logger.getLogger(TimeSlotService.class.getName());
    private final TimeSlotDAO timeSlotDAO;

    public TimeSlotService() {
        this.timeSlotDAO = new TimeSlotDAO();
    }

    // 根据咨询师ID和时间范围获取可用时间段
    public List<TimeSlot> getAvailableTimeSlots(int counselorId, LocalDateTime startDate, LocalDateTime endDate) {
        return timeSlotDAO.findAvailableTimeSlots(counselorId, startDate, endDate);
    }

    // 获取所有可用时间段（不限定咨询师）
    public List<TimeSlot> getAvailableTimeSlots(LocalDateTime startDate, LocalDateTime endDate) {
        return timeSlotDAO.findAvailableTimeSlots(startDate, endDate);
    }

    /**
     * 根据ID获取时间段
     */
    public Optional<TimeSlot> getTimeSlotById(int timeSlotId) {
        return timeSlotDAO.findById(timeSlotId);
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
        return "available".equals(timeSlot.getStatus());
    }

    /**
     * 更新时间段状态
     */
    public boolean updateTimeSlotStatus(int timeSlotId, String status) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以更新时间段状态");
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
                logger.warning("只能更新自己的时间段");
                return false;
            }

            boolean updated = timeSlotDAO.updateStatus(timeSlotId, status);
            if (updated) {
                logger.info("时间段状态更新成功: time_slot_id=" + timeSlotId + ", status=" + status);
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "更新时间段状态过程中发生错误", e);
        }
        return false;
    }

    /**
     * 批量添加时间段
     */
    public int addBatchTimeSlots(LocalDateTime startTime, LocalDateTime endTime, int durationMinutes, int breakMinutes) {
        if (!SessionManager.isLoggedIn() || !SessionManager.isCounselor()) {
            logger.warning("只有咨询师可以添加时间段");
            return 0;
        }

        int counselorId = SessionManager.currentUser.getId();
        int addedCount = 0;

        LocalDateTime currentStart = startTime;

        while (currentStart.plusMinutes(durationMinutes).isBefore(endTime) ||
                currentStart.plusMinutes(durationMinutes).isEqual(endTime)) {

            LocalDateTime currentEnd = currentStart.plusMinutes(durationMinutes);

            // 检查时间冲突
            if (!timeSlotDAO.hasTimeConflict(counselorId, currentStart, currentEnd)) {
                TimeSlot timeSlot = new TimeSlot(counselorId, currentStart, currentEnd);
                if (timeSlotDAO.save(timeSlot)) {
                    addedCount++;
                }
            }

            currentStart = currentEnd.plusMinutes(breakMinutes);
        }

        logger.info("批量添加时间段完成: 成功添加 " + addedCount + " 个时间段");
        return addedCount;
    }

    /**
     * 获取最近的可预约时间段
     */
    public List<TimeSlot> getRecentAvailableTimeSlots(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusWeeks(2); // 未来两周

        List<TimeSlot> allSlots = timeSlotDAO.findAvailableTimeSlots(now, endDate);

        // 手动排序和限制数量（替代Stream API）
        Collections.sort(allSlots, (ts1, ts2) -> ts1.getStartTime().compareTo(ts2.getStartTime()));

        if (allSlots.size() > limit) {
            return allSlots.subList(0, limit);
        }
        return allSlots;
    }

    /**
     * 清理过期的不可用时间段
     */
    public int cleanupExpiredTimeSlots() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isAdmin()) {
            logger.warning("只有管理员可以清理过期时间段");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<TimeSlot> allSlots = timeSlotDAO.findAll();

        int cleanedCount = 0;

        for (TimeSlot timeSlot : allSlots) {
            if (timeSlot.getEndTime().isBefore(now) &&
                    !"booked".equals(timeSlot.getStatus())) {
                if (timeSlotDAO.delete(timeSlot.getId())) {
                    cleanedCount++;
                }
            }
        }

        logger.info("过期时间段清理完成: 清理了 " + cleanedCount + " 个时间段");
        return cleanedCount;
    }

    /**
     * 获取某个日期的时间段
     */
    public List<TimeSlot> getTimeSlotsByDate(int counselorId, LocalDateTime date) {
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return timeSlotDAO.findAvailableTimeSlots(counselorId, startOfDay, endOfDay);
    }

    /**
     * 验证时间段的合理性
     */
    public boolean validateTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            logger.warning("开始时间不能晚于结束时间");
            return false;
        }

        if (startTime.plusMinutes(30).isAfter(endTime)) {
            logger.warning("时间段至少需要30分钟");
            return false;
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            logger.warning("不能创建过去的时间段");
            return false;
        }

        return true;
    }

    /**
     * 获取咨询师的本月时间段统计
     */
    public MonthlyStats getMonthlyStats(int counselorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<TimeSlot> monthSlots = timeSlotDAO.findAvailableTimeSlots(counselorId, startOfMonth, endOfMonth);

        MonthlyStats stats = new MonthlyStats();
        for (TimeSlot slot : monthSlots) {
            if ("available".equals(slot.getStatus())) {
                stats.availableCount++;
            } else if ("booked".equals(slot.getStatus())) {
                stats.bookedCount++;
            }
        }
        stats.totalCount = monthSlots.size();

        return stats;
    }

    /**
     * 月度统计类
     */
    public static class MonthlyStats {
        public int totalCount;
        public int availableCount;
        public int bookedCount;

        public double getUtilizationRate() {
            if (totalCount == 0) return 0.0;
            return (bookedCount * 100.0) / totalCount;
        }
    }

    /**
     * 检查时间段是否可被预约
     */
    public boolean isTimeSlotBookable(int timeSlotId) {
        Optional<TimeSlot> timeSlotOpt = timeSlotDAO.findById(timeSlotId);
        if (!timeSlotOpt.isPresent()) {
            return false;
        }

        TimeSlot timeSlot = timeSlotOpt.get();
        return "available".equals(timeSlot.getStatus()) &&
                timeSlot.getStartTime().isAfter(LocalDateTime.now());
    }

    /**
     * 获取咨询师的工作时间偏好
     */
    public WorkTimePreference getWorkTimePreference(int counselorId) {
        List<TimeSlot> allSlots = timeSlotDAO.findByCounselorId(counselorId);

        WorkTimePreference preference = new WorkTimePreference();
        for (TimeSlot slot : allSlots) {
            int hour = slot.getStartTime().getHour();
            if (hour >= 9 && hour < 12) {
                preference.morningCount++;
            } else if (hour >= 14 && hour < 18) {
                preference.afternoonCount++;
            } else if (hour >= 19 && hour < 21) {
                preference.eveningCount++;
            }
        }

        return preference;
    }

    /**
     * 工作时间偏好类
     */
    public static class WorkTimePreference {
        public int morningCount;
        public int afternoonCount;
        public int eveningCount;

        public String getPreferredTime() {
            if (morningCount >= afternoonCount && morningCount >= eveningCount) {
                return "上午";
            } else if (afternoonCount >= morningCount && afternoonCount >= eveningCount) {
                return "下午";
            } else {
                return "晚上";
            }
        }
    }
}