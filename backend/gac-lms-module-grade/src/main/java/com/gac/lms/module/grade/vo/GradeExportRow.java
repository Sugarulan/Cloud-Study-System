package com.gac.lms.module.grade.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩 Excel 导出行（easyexcel）。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(20)
public class GradeExportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("成绩ID")
    private Long id;

    @ExcelProperty("考试ID")
    private Long examId;

    @ExcelProperty("考试名称")
    private String examName;

    @ExcelProperty("学员ID")
    private Long userId;

    @ExcelProperty("学员姓名")
    private String userName;

    @ExcelProperty("总分")
    private BigDecimal totalScore;

    @ExcelProperty("客观题得分")
    private BigDecimal objectiveScore;

    @ExcelProperty("主观题得分")
    private BigDecimal subjectiveScore;

    @ExcelProperty("通过分")
    private BigDecimal passScore;

    @ExcelProperty("是否通过")
    private String isPassedLabel;

    @ExcelProperty("状态")
    private String statusLabel;

    @ExcelProperty("交卷时间")
    private LocalDateTime submittedAt;

    @ExcelProperty("发布时间")
    private LocalDateTime publishedAt;
}
