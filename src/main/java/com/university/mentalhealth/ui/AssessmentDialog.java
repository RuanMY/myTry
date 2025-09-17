package com.university.mentalhealth.ui;

import com.university.mentalhealth.entity.*;
import com.university.mentalhealth.service.AssessmentService;
import com.university.mentalhealth.util.JSONUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.university.mentalhealth.ui.LoginFrame.logger;

public class AssessmentDialog extends JDialog {
    private Assessment assessment;
    private AssessmentService assessmentService;
    private AssessmentSession currentSession;
    private List<AssessmentAnswer> answers;
    private List<AssessmentQuestion> questions;

    private JLabel questionLabel;
    private ButtonGroup answerGroup;
    private JPanel answersPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JButton submitButton;
    private JProgressBar progressBar;
    private JLabel timerLabel;

    private int currentQuestionIndex = 0;
    private Timer timer;
    private int elapsedSeconds = 0;

    public AssessmentDialog(JFrame parent, Assessment assessment) {
        super(parent, "心理测评 - " + assessment.getName(), true);
        this.assessment = assessment;
        this.assessmentService = new AssessmentService();
        this.answers = new ArrayList<>();

        initialize();
        loadQuestions();
        initUI();
        startTimer();
    }

    private void initialize() {
        setSize(700, 550);
        setLocationRelativeTo(getParent());
        setResizable(true);

        // 初始化测评会话
        currentSession = assessmentService.startAssessmentSession(assessment.getId());
        if (currentSession == null) {
            JOptionPane.showMessageDialog(this, "无法开始测评", "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void loadQuestions() {
        questions = assessmentService.getAssessmentQuestions(assessment.getId());
        if (questions == null || questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该测评暂无题目", "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // 顶部面板 - 进度条和计时器
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topPanel.setBackground(Color.WHITE);

        progressBar = new JProgressBar(0, questions.size());
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        timerLabel = new JLabel("时间: 00:00", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        timerLabel.setForeground(Color.GRAY);

        topPanel.add(progressBar, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);

        // 问题面板
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        questionPanel.setBackground(Color.WHITE);

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 答案选项面板
        answersPanel = new JPanel();
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
        answersPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(answersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        prevButton = createButton("上一题", new Color(100, 150, 200));
        nextButton = createButton("下一题", new Color(70, 130, 180));
        submitButton = createButton("提交测评", new Color(60, 120, 60));
        JButton cancelButton = createButton("取消", new Color(200, 100, 100));

        prevButton.addActionListener(e -> showPreviousQuestion());
        nextButton.addActionListener(e -> showNextQuestion());
        submitButton.addActionListener(e -> submitAssessment());
        cancelButton.addActionListener(e -> confirmCancel());

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(topPanel, BorderLayout.NORTH);
        add(questionPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 显示第一题
        showQuestion(0);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return button;
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            timerLabel.setText(String.format("时间: %02d:%02d", minutes, seconds));
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }

        currentQuestionIndex = index;
        AssessmentQuestion question = questions.get(index);

        // 更新问题文本
        questionLabel.setText(String.format("第 %d 题（共 %d 题）: %s",
                index + 1, questions.size(), question.getQuestionText()));

        // 清空答案选项
        answersPanel.removeAll();
        answerGroup = new ButtonGroup();

        try {
            // 替换JSON解析部分：
            String[] optionsText = {"完全没有", "有几天", "一半以上时间", "几乎每天"};
            int[] optionsValue = {0, 1, 2, 3};

            for (int i = 0; i < optionsText.length; i++) {
                JRadioButton radioButton = new JRadioButton(optionsText[i] + " (" + optionsValue[i] + "分)");
                radioButton.setActionCommand(String.valueOf(optionsValue[i]));
                radioButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                radioButton.setBackground(Color.WHITE);

                // 检查是否已经选择过该题的答案
                if (getAnswerForQuestion(question.getId()) == optionsValue[i]) {
                    radioButton.setSelected(true);
                }

                answerGroup.add(radioButton);
                answersPanel.add(radioButton);
                answersPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "解析题目选项失败", e);
            JOptionPane.showMessageDialog(this, "题目数据格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 更新按钮状态
        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < questions.size() - 1);
        submitButton.setEnabled(index == questions.size() - 1);

        // 更新进度条
        progressBar.setValue(index + 1);
        progressBar.setString("进度: " + (index + 1) + "/" + questions.size());

        // 刷新界面
        answersPanel.revalidate();
        answersPanel.repaint();
    }



    /**
     * 确认取消测评
     */
    private void confirmCancel() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要取消测评吗？所有已回答的内容将会丢失。",
                "确认取消",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            stopTimer();
            dispose();
        }
    }

    /**
     * 获取指定题目的答案
     */
    private int getAnswerForQuestion(int questionId) {
        for (AssessmentAnswer answer : answers) {
            if (answer.getQuestionId() == questionId) {
                return answer.getAnswerValue();
            }
        }
        return -1;
    }

    /**
     * 保存当前题目的答案
     */
    private void saveCurrentAnswer() {
        if (answerGroup.getSelection() != null) {
            int answerValue = Integer.parseInt(answerGroup.getSelection().getActionCommand());
            AssessmentQuestion currentQuestion = questions.get(currentQuestionIndex);

            // 检查是否已经存在该题的答案
            boolean found = false;
            for (AssessmentAnswer answer : answers) {
                if (answer.getQuestionId() == currentQuestion.getId()) {
                    answer.setAnswerValue(answerValue);
                    found = true;
                    break;
                }
            }

            // 如果不存在，创建新的答案
            if (!found) {
                AssessmentAnswer answer = new AssessmentAnswer();
                answer.setQuestionId(currentQuestion.getId());
                answer.setAnswerValue(answerValue);
                answers.add(answer);
            }
        }
    }

    /**
     * 显示上一题
     */
    private void showPreviousQuestion() {
        saveCurrentAnswer();
        showQuestion(currentQuestionIndex - 1);
    }

    /**
     * 显示下一题
     */
    private void showNextQuestion() {
        saveCurrentAnswer();
        showQuestion(currentQuestionIndex + 1);
    }

    /**
     * 提交测评
     */
    private void submitAssessment() {
        saveCurrentAnswer();

        // 检查是否所有题目都已回答
        if (answers.size() < questions.size()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "还有未回答的题目，确定要提交吗？",
                    "确认提交",
                    JOptionPane.YES_NO_OPTION
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // 提交测评
        AssessmentSession completedSession = assessmentService.submitAssessment(currentSession, answers);
        if (completedSession != null) {
            // 显示测评结果
            String result = assessmentService.getDetailedAssessmentResult(assessment, completedSession.getTotalScore());

            // 创建结果对话框
            showAssessmentResult(completedSession, result);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "提交测评失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示测评结果
     */
    private void showAssessmentResult(AssessmentSession session, String resultText) {
        JDialog resultDialog = new JDialog(this, "测评结果", true);
        resultDialog.setSize(500, 400);
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setLayout(new BorderLayout());

        JTextArea resultArea = new JTextArea(resultText);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> resultDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        resultDialog.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        resultDialog.setVisible(true);
    }

}