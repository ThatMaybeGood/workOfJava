package com.mergedata.model.vo.pie;

import java.math.BigDecimal;

// 统计辅助类

/**
 * 项目统计数据类
 * 用于统计收入金额、收入条数、退款金额、退款条数
 */
public class ItemStats {

    private BigDecimal incomeAmt = BigDecimal.ZERO;  // 收入金额合计
    private int incomeCnt = 0;                       // 收入条数
    private BigDecimal refundAmt = BigDecimal.ZERO;  // 退款金额合计
    private int refundCnt = 0;                       // 退款条数

    // 无参构造
    public ItemStats() {
    }

    // 全参构造
    public ItemStats(BigDecimal incomeAmt, int incomeCnt, BigDecimal refundAmt, int refundCnt) {
        this.incomeAmt = incomeAmt;
        this.incomeCnt = incomeCnt;
        this.refundAmt = refundAmt;
        this.refundCnt = refundCnt;
    }

    // Getter 和 Setter
    public BigDecimal getIncomeAmt() {
        return incomeAmt;
    }

    public void setIncomeAmt(BigDecimal incomeAmt) {
        this.incomeAmt = incomeAmt;
    }

    public int getIncomeCnt() {
        return incomeCnt;
    }

    public void setIncomeCnt(int incomeCnt) {
        this.incomeCnt = incomeCnt;
    }

    public BigDecimal getRefundAmt() {
        return refundAmt;
    }

    public void setRefundAmt(BigDecimal refundAmt) {
        this.refundAmt = refundAmt;
    }

    public int getRefundCnt() {
        return refundCnt;
    }

    public void setRefundCnt(int refundCnt) {
        this.refundCnt = refundCnt;
    }

    // 添加收入
    public void addIncome(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return;
        }
        this.incomeAmt = this.incomeAmt.add(amount);
        this.incomeCnt++;
    }

    // 添加退款
    public void addRefund(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        this.refundAmt = this.refundAmt.add(amount.abs());
        this.refundCnt++;
    }

    // 合并另一个统计对象
    public void merge(ItemStats other) {
        if (other == null) {
            return;
        }
        this.incomeAmt = this.incomeAmt.add(other.getIncomeAmt());
        this.incomeCnt += other.getIncomeCnt();
        this.refundAmt = this.refundAmt.add(other.getRefundAmt());
        this.refundCnt += other.getRefundCnt();
    }

    // 重置所有统计
    public void reset() {
        this.incomeAmt = BigDecimal.ZERO;
        this.incomeCnt = 0;
        this.refundAmt = BigDecimal.ZERO;
        this.refundCnt = 0;
    }

    @Override
    public String toString() {
        return "ItemStats{" +
                "incomeAmt=" + incomeAmt +
                ", incomeCnt=" + incomeCnt +
                ", refundAmt=" + refundAmt +
                ", refundCnt=" + refundCnt +
                '}';
    }
}