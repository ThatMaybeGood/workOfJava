package com.example.auto_demo.model;


public class ExportExcelDTO {
    /*
     * 结算ID
     */

    private String setlId;
    /*
     * 就诊ID
     */
    private String mdtrtId;
    /*
     * 人员编号
     */
    private String psnNo;
    /*
     * 医疗费用总额
     */
    private String medfeeSumamt;
    /*
     * 结算时间
     */
    private String billDate;
    /*
     * 交易类型 1：正常交易 2：退款交易
     */
    private String tranType;  // "medfeeSumamt" 小于0为"2"，否则为"1"
    /*
     * 数据来源
     */
    private String dataSource = "2"; //"2"
    /*
     * 患者姓名
     */
    private String psnName;
    /*
     * 患者医保类型
     */
    private String insutype;
    /*
     * 报文ID 与  msgid相等
     */
    private String medinsSetlId;
    /*
     * 报文ID
     */
    private String msgid;
    /*
     * 医疗类别
     */
    private String medType;
     /*
     * 身份证号
     */
    private String certno;

    /*
     * 入院时间
     */
    private String admDate;

    /*
     * 出院时间
     */
     private String disDate;

    public String getSetlId() {
        return setlId;
    }

    public void setSetlId(String setlId) {
        this.setlId = setlId;
    }

    public String getMdtrtId() {
        return mdtrtId;
    }

    public void setMdtrtId(String mdtrtId) {
        this.mdtrtId = mdtrtId;
    }

    public String getPsnNo() {
        return psnNo;
    }

    public void setPsnNo(String psnNo) {
        this.psnNo = psnNo;
    }

    public String getMedfeeSumamt() {
        return medfeeSumamt;
    }

    public void setMedfeeSumamt(String medfeeSumamt) {
        this.medfeeSumamt = medfeeSumamt;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getPsnName() {
        return psnName;
    }

    public void setPsnName(String psnName) {
        this.psnName = psnName;
    }

    public String getInsutype() {
        return insutype;
    }

    public void setInsutype(String insutype) {
        this.insutype = insutype;
    }

    public String getMedinsSetlId() {
        return medinsSetlId;
    }

    public void setMedinsSetlId(String medinsSetlId) {
        this.medinsSetlId = medinsSetlId;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getMedType() {
        return medType;
    }

    public void setMedType(String medType) {
        this.medType = medType;
    }

    public String getCertno() {
        return certno;
    }

    public void setCertno(String certno) {
        this.certno = certno;
    }

    public String getAdmDate() {
        return admDate;
    }

    public void setAdmDate(String admDate) {
        this.admDate = admDate;
    }

    public String getDisDate() {
        return disDate;
    }

    public void setDisDate(String disDate) {
        this.disDate = disDate;
    }


    @Override
    public String toString() {
        return "ExportExcelDTO{" +
                "setlId='" + setlId + '\'' +
                ", mdtrtId='" + mdtrtId + '\'' +
                ", psnNo='" + psnNo + '\'' +
                ", medfeeSumamt='" + medfeeSumamt + '\'' +
                ", billDate='" + billDate + '\'' +
                ", tranType='" + tranType + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", psnName='" + psnName + '\'' +
                ", insutype='" + insutype + '\'' +
                ", medinsSetlId='" + medinsSetlId + '\'' +
                ", msgid='" + msgid + '\'' +
                ", medType='" + medType + '\'' +
                ", certno='" + certno + '\'' +
                ", admDate='" + admDate + '\'' +
                ", disDate='" + disDate + '\'' +
                '}';
    }
}
