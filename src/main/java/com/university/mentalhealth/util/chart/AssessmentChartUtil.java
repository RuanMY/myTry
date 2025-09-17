package com.university.mentalhealth.util.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;

public class AssessmentChartUtil {

    /**
     * 创建分数对比柱状图
     */
    public static ChartPanel createScoreComparisonChart(String[] categories, int[] scores, int[] averages) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < categories.length; i++) {
            dataset.addValue(scores[i], "您的分数", categories[i]);
            dataset.addValue(averages[i], "平均分数", categories[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "测评分数对比",
                "测评类型",
                "分数",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        return chartPanel;
    }

    /**
     * 创建风险等级饼图
     */
    public static ChartPanel createRiskLevelChart(int score, int threshold) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (score < threshold) {
            dataset.setValue("安全范围", threshold - score);
            dataset.setValue("风险阈值", score);
        } else {
            dataset.setValue("风险范围", score - threshold);
            dataset.setValue("安全阈值", threshold);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "风险等级分析",
                dataset,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    /**
     * 创建历史趋势折线图
     */
    public static ChartPanel createHistoryTrendChart(String[] dates, int[] scores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < dates.length; i++) {
            dataset.addValue(scores[i], "测评分数", dates[i]);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "历史测评趋势",
                "测评时间",
                "分数",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));
        return chartPanel;
    }
}