package com.university.mentalhealth.test;

import com.university.mentalhealth.dao.AssessmentDAO;
import com.university.mentalhealth.entity.Assessment;

import java.util.List;
import java.util.logging.Logger;

public class DataTypeValidationTest {
    private static final Logger logger = Logger.getLogger(DataTypeValidationTest.class.getName());

    public static void main(String[] args) {
        logger.info("=== 数据类型验证测试 ===");

        AssessmentDAO dao = new AssessmentDAO();
        List<Assessment> assessments = dao.getAllAssessments();

        logger.info("找到 " + assessments.size() + " 个量表");

        for (Assessment assessment : assessments) {
            logger.info("量表: " + assessment.getName() +
                    ", ID: " + assessment.getId() +
                    ", ID类型: " + assessment.getId().getClass().getSimpleName());

            // 验证ID值范围
            if (assessment.getId() > 0 && assessment.getId() < 1000) {
                logger.info("ID值 " + assessment.getId() + " 在合理范围内(INT类型)");
            } else {
                logger.warning("ID值 " + assessment.getId() + " 可能超出INT范围");
            }
        }
    }
}