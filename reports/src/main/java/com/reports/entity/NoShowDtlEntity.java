package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 爽约退号分析-科室明细
 */
@Data
@TableName("TR_NOSHOW_DTL")
public class NoShowDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 退号人数 */
    @TableField("refund_count")
    private Integer refundCount;

    /** 退号率 */
    @TableField("refund_rate")
    private String refundRate;

    /** 爽约人数 */
    @TableField("no_show_count")
    private Integer noShowCount;

    /** 爽约率 */
    @TableField("no_show_rate")
    private String noShowRate;

    // ==================== 科室表的细分列 ====================
    // 这几个不是表字段，是 queryDeptDetail 里 SUM 出来的结果列，
    // service 用它们拼 TableItem 的 refundOrigin / refundChannel / noShowOrigin 嵌套结构。
    // （前端 renderTable 直接读 row.refundOrigin.chongqing，不给就抛 TypeError，整张表都渲染不出来）

    /** 退号-重庆 */
    private Integer refundChongqing;

    /** 退号-四川 */
    private Integer refundSichuan;

    /** 退号-贵州 */
    private Integer refundGuizhou;

    /** 退号-云南 */
    private Integer refundYunnan;

    /** 退号-其他 */
    private Integer refundOther;

    /** 退号渠道-窗口 */
    private Integer refundWindow;

    /** 退号渠道-小程序（宽表里存的是自助机列 tui_hao_zi_zhu_ji） */
    private Integer refundMiniprogram;

    /** 爽约-重庆 */
    private Integer noShowChongqing;

    /** 爽约-四川 */
    private Integer noShowSichuan;

    /** 爽约-贵州 */
    private Integer noShowGuizhou;

    /** 爽约-云南 */
    private Integer noShowYunnan;

    /** 爽约-其他 */
    private Integer noShowOther;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 扩展字段1 */
    @TableField("ext1")
    private String ext1;

    /** 扩展字段2 */
    @TableField("ext2")
    private String ext2;

    /** 扩展字段3 */
    @TableField("ext3")
    private String ext3;
}
