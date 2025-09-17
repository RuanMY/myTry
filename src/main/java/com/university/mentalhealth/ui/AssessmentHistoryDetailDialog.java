package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.*;
import com.university.mentalhealth.service.AssessmentService;
import com.university.mentalhealth.util.chart.AssessmentChartUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.university.mentalhealth.util.chart.AssessmentChartUtil.*;

public class AssessmentHistoryDetailDialog extends JDialog {
    private final AssessmentService assessmentService;
    private final AssessmentSession session;

    public AssessmentHistoryDetailDialog(JFrame parent, AssessmentSession session) {
        super(parent, "测评详情 - " + session.getAssessmentName(), true);
        this.assessmentService = new AssessmentService();
        this.session = session;

        initialize();
        initUI();
    }

    private void initialize() {
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
    }

    private void initUI() {
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 基本信息选项卡
        tabbedPane.addTab("基本信息", createBasicInfoPanel());

        // 题目详情选项卡
        tabbedPane.addTab("题目详情", createQuestionDetailPanel());

        // 统计分析选项卡
        tabbedPane.addTab("统计分析", createAnalysisPanel());

        // 添加关闭按钮
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("关闭");
        JButton exportButton = new JButton("导出报告");

        closeButton.addActionListener(e -> dispose());
        exportButton.addActionListener(e -> exportReport());

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("测评名称:"));
        panel.add(new JLabel(session.getAssessmentName()));

        panel.add(new JLabel("测评时间:"));
        panel.add(new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(
                java.sql.Timestamp.valueOf(session.getEndTime()))));

        panel.add(new JLabel("总得分:"));
        panel.add(new JLabel(String.valueOf(session.getTotalScore())));

        panel.add(new JLabel("测评时长:"));
        long duration = java.time.Duration.between(
                session.getStartTime(), session.getEndTime()).toMinutes();
        panel.add(new JLabel(duration + " 分钟"));

        panel.add(new JLabel("风险等级:"));
        String riskLevel = assessmentService.getRiskLevel(
                assessmentService.getAssessmentById(session.getAssessmentId()).orElse(null),
                session.getTotalScore()
        );
        panel.add(new JLabel(riskLevel));

        return panel;
    }

    private JPanel createQuestionDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 获取详细题目数据
        List<AssessmentDetail> details = assessmentService.getAssessmentDetails(session.getId());

        // 创建表格模型
        String[] columns = {"题号", "题目内容", "答案", "得分"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (int i = 0; i < details.size(); i++) {
            AssessmentDetail detail = details.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    detail.getQuestionText(),
                    getAnswerText(detail.getAnswerValue()),
                    detail.getAnswerValue()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加图表
        try {
            // 分数对比图
            String[] categories = {"PHQ-9", "GAD-7", "压力测试"};
            int[] scores = {session.getTotalScore(), session.getTotalScore() - 2, session.getTotalScore() + 3};
            int[] averages = {10, 8, 12};

            panel.add(createScoreComparisonChart(categories, scores, averages));
            panel.add(createRiskLevelChart(session.getTotalScore(), 10));

        } catch (Exception e) {
            panel.add(new JLabel("图表生成失败: " + e.getMessage()));
        }

        return panel;
    }

    private void exportReport() {
        // 导出功能实现
        JOptionPane.showMessageDialog(this, "导出功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getAnswerText(int value) {
        switch (value) {
            case 0: return "完全没有";
            case 1: return "有几天";
            case 2: return "一半以上时间";
            case 3: return "几乎每天";
            default: return "未知";
        }
    }
}