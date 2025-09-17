package com.university.mentalhealth.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportUtil {

    /**
     * 导出测评报告到文本文件
     */
    public static boolean exportToText(String content, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存测评报告");
        fileChooser.setSelectedFile(new File(fileName + ".txt"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                writer.write(content);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "导出失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    /**
     * 生成导出文件名
     */
    public static String generateFileName(String assessmentName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return assessmentName + "_" + sdf.format(new Date());
    }
}