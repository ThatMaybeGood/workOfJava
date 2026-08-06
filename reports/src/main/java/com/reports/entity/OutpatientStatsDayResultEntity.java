package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊退号与爽约按日统计结果宽表
 */
@Data
@TableName("TR_OUTPATIENT_STATS_DAY_RESULT")
public class OutpatientStatsDayResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期(主键) */
    @TableId("stats_date")
    private Date statsDate;

    /** 就诊科室名称(主键) */
    @TableField("dept_name")
    private String deptName;

    /** 总挂号数 */
    @TableField("total_guahao")
    private Integer totalGuahao;

    // ==================== 退号数据 ====================

    /** 退号总人数 */
    @TableField("tui_hao_shu")
    private Integer tuiHaoShu;

    /** 退号-重庆人数 */
    @TableField("tui_hao_chong_qing")
    private Integer tuiHaoChongQing;

    /** 退号-四川人数 */
    @TableField("tui_hao_si_chuan")
    private Integer tuiHaoSiChuan;

    /** 退号-贵州人数 */
    @TableField("tui_hao_gui_zhou")
    private Integer tuiHaoGuiZhou;

    /** 退号-云南人数 */
    @TableField("tui_hao_yun_nan")
    private Integer tuiHaoYunNan;

    /** 退号-其他地区人数 */
    @TableField("tui_hao_qi_ta")
    private Integer tuiHaoQiTa;

    /** 退号-窗口渠道人数 */
    @TableField("tui_hao_chuang_kou")
    private Integer tuiHaoChuangKou;

    /** 退号-自助机渠道人数 */
    @TableField("tui_hao_zi_zhu_ji")
    private Integer tuiHaoZiZhuJi;

    /** 退号-0至14岁人数 */
    @TableField("tui_hao_age_0_14")
    private Integer tuiHaoAge014;

    /** 退号-15至19岁人数 */
    @TableField("tui_hao_age_15_19")
    private Integer tuiHaoAge1519;

    /** 退号-20至29岁人数 */
    @TableField("tui_hao_age_20_29")
    private Integer tuiHaoAge2029;

    /** 退号-30至39岁人数 */
    @TableField("tui_hao_age_30_39")
    private Integer tuiHaoAge3039;

    /** 退号-40至49岁人数 */
    @TableField("tui_hao_age_40_49")
    private Integer tuiHaoAge4049;

    /** 退号-50至59岁人数 */
    @TableField("tui_hao_age_50_59")
    private Integer tuiHaoAge5059;

    /** 退号-60至69岁人数 */
    @TableField("tui_hao_age_60_69")
    private Integer tuiHaoAge6069;

    /** 退号-70至79岁人数 */
    @TableField("tui_hao_age_70_79")
    private Integer tuiHaoAge7079;

    /** 退号-80至89岁人数 */
    @TableField("tui_hao_age_80_89")
    private Integer tuiHaoAge8089;

    /** 退号-90岁及以上人数 */
    @TableField("tui_hao_age_90_up")
    private Integer tuiHaoAge90Up;

    // ==================== 爽约数据 ====================

    /** 爽约总人数 */
    @TableField("shuang_yue_shu")
    private Integer shuangYueShu;

    /** 爽约-重庆人数 */
    @TableField("shuang_yue_chong_qing")
    private Integer shuangYueChongQing;

    /** 爽约-四川人数 */
    @TableField("shuang_yue_si_chuan")
    private Integer shuangYueSiChuan;

    /** 爽约-贵州人数 */
    @TableField("shuang_yue_gui_zhou")
    private Integer shuangYueGuiZhou;

    /** 爽约-云南人数 */
    @TableField("shuang_yue_yun_nan")
    private Integer shuangYueYunNan;

    /** 爽约-其他地区人数 */
    @TableField("shuang_yue_qi_ta")
    private Integer shuangYueQiTa;

    /** 爽约-0至14岁人数 */
    @TableField("shuang_yue_age_0_14")
    private Integer shuangYueAge014;

    /** 爽约-15至19岁人数 */
    @TableField("shuang_yue_age_15_19")
    private Integer shuangYueAge1519;

    /** 爽约-20至29岁人数 */
    @TableField("shuang_yue_age_20_29")
    private Integer shuangYueAge2029;

    /** 爽约-30至39岁人数 */
    @TableField("shuang_yue_age_30_39")
    private Integer shuangYueAge3039;

    /** 爽约-40至49岁人数 */
    @TableField("shuang_yue_age_40_49")
    private Integer shuangYueAge4049;

    /** 爽约-50至59岁人数 */
    @TableField("shuang_yue_age_50_59")
    private Integer shuangYueAge5059;

    /** 爽约-60至69岁人数 */
    @TableField("shuang_yue_age_60_69")
    private Integer shuangYueAge6069;

    /** 爽约-70至79岁人数 */
    @TableField("shuang_yue_age_70_79")
    private Integer shuangYueAge7079;

    /** 爽约-80至89岁人数 */
    @TableField("shuang_yue_age_80_89")
    private Integer shuangYueAge8089;

    /** 爽约-90岁及以上人数 */
    @TableField("shuang_yue_age_90_up")
    private Integer shuangYueAge90Up;

    // ==================== 时间戳 ====================

    /** 数据创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 数据更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
