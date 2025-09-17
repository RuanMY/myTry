package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.AssessmentSession;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssessmentHistoryDialog extends JDialog {
    public AssessmentHistoryDialog(JFrame parent, List<AssessmentSession> history) {
        super(parent, "测评历史记录", true);
        setSize(800, 400);
        setLocationRelativeTo(parent);

        initUI(history);
    }

    private void initUI(List<AssessmentSession> history) {
        setLayout(new BorderLayout());

        // 表格数据
        String[] columns = {"测评名称", "测评时间", "总得分", "风险等级"};
        Object[][] data = new Object[history.size()][4];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < history.size(); i++) {
            AssessmentSession session = history.get(i);
            data[i][0] = session.getAssessmentName();
            data[i][1] = session.getEndTime().format(formatter);
            data[i][2] = session.getTotalScore();

            // 简单风险评估
            String riskLevel;
            if (session.getTotalScore() >= 15) {
                riskLevel = "高风险";
            } else if (session.getTotalScore() >= 10) {
                riskLevel = "中风险";
            } else {
                riskLevel = "低风险";
            }
            data[i][3] = riskLevel;
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}
