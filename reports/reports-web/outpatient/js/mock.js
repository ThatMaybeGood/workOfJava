/**
 * Mock 数据服务
 * 模拟后端接口返回数据
 */
const MockService = {
    /**
     * 获取门诊运行数据统计（概览 + 表格）
     * @param {Object} params - 查询参数 { page, pageSize, deptName, startDate, endDate }
     * @returns {Promise} 返回模拟数据
     */
    getOperationStats(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateTableData();
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                // 科室筛选
                let filteredData = allData;
                if (params.deptName) {
                    filteredData = allData.filter(item => item.deptName.includes(params.deptName));
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            totalVisits: 12536,
                            appointmentRate: '83.10%',
                            visitCount: 112,
                            examRate: '56.50%',
                            efficiency: 27.5,
                            effectiveUnits: 112,
                            totalUnits: 251,
                            famousExpert: 112,
                            specialExpert: 112,
                            knownExpert: 112,
                            expertA: 112,
                            expertB: 112,
                            ordinary: 112,
                            unitFamousEffective: 52,
                            unitFamousTotal: 112,
                            unitSpecialEffective: 52,
                            unitSpecialTotal: 112,
                            unitKnownEffective: 52,
                            unitKnownTotal: 112,
                            unitAEffective: 52,
                            unitATotal: 112,
                            unitBEffective: 52,
                            unitBTotal: 112,
                            unitOrdinaryEffective: 52,
                            unitOrdinaryTotal: 112
                        },
                        table: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取统计概览数据
     * @returns {Promise} 返回模拟数据
     */
    getOverviewData() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        totalVisits: 12536,
                        appointmentRate: '83.10%',
                        visitCount: 112,
                        examRate: '56.50%',
                        efficiency: 27.5,
                        effectiveUnits: 112,
                        totalUnits: 251,
                        famousExpert: 112,
                        specialExpert: 112,
                        knownExpert: 112,
                        expertA: 112,
                        expertB: 112,
                        ordinary: 112,
                        unitFamousEffective: 52,
                        unitFamousTotal: 112,
                        unitSpecialEffective: 52,
                        unitSpecialTotal: 112,
                        unitKnownEffective: 52,
                        unitKnownTotal: 112,
                        unitAEffective: 52,
                        unitATotal: 112,
                        unitBEffective: 52,
                        unitBTotal: 112,
                        unitOrdinaryEffective: 52,
                        unitOrdinaryTotal: 112
                    }
                });
            }, 200);
        });
    },

    /**
     * 获取表格数据
     * @param {Object} params - 查询参数 { page, pageSize, deptName, startDate, endDate }
     * @returns {Promise} 返回模拟数据
     */
    getTableData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateTableData();
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                // 科室筛选
                let filteredData = allData;
                if (params.deptName) {
                    filteredData = allData.filter(item => item.deptName.includes(params.deptName));
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        list,
                        total,
                        page,
                        pageSize
                    }
                });
            }, 300);
        });
    },

    /**
     * 生成表格数据
     * @returns {Array} 科室门诊统计数据
     */
    generateTableData() {
        const deptNames = [
            '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
            '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊',
            '胸外科门诊', '普外科门诊', '妇科门诊', '产科门诊', '儿科门诊',
            '眼科门诊', '耳鼻喉科门诊', '口腔科门诊', '皮肤科门诊', '中医科门诊',
            '康复医学科门诊', '肿瘤科门诊', '内分泌科门诊', '血液科门诊', '风湿免疫科门诊',
            '感染科门诊', '精神科门诊', '老年医学科门诊', '疼痛科门诊', '营养科门诊',
            '全科医学科门诊', '急诊科门诊', '针灸科门诊', '推拿科门诊', '理疗科门诊',
            '体检中心', '放疗科门诊', '介入科门诊', '麻醉科门诊', '病理科门诊',
            '影像科门诊', '超声科门诊', '核医学科门诊', '检验科门诊', '输血科门诊',
            '药学门诊', '心理咨询门诊', '睡眠门诊', '肥胖门诊', '记忆门诊',
            '眩晕门诊', '戒烟门诊', '伤口造口门诊', 'PICC门诊', '糖尿病门诊'
        ];

        return deptNames.map((deptName, index) => ({
            deptName,
            totalVisits: 350 + Math.floor(Math.random() * 100),
            appointmentRate: (60 + Math.random() * 25).toFixed(2) + '%',
            visitCount: 25 + Math.floor(Math.random() * 15),
            examRate: (70 + Math.random() * 20).toFixed(2) + '%',
            efficiency: (20 + Math.random() * 15).toFixed(2),
            effectiveUnits: 45 + Math.floor(Math.random() * 15),
            totalUnits: 50 + Math.floor(Math.random() * 10),
            famousExpert: Math.floor(Math.random() * 5),
            specialExpert: Math.floor(Math.random() * 8),
            knownExpert: Math.floor(Math.random() * 6),
            expertA: Math.floor(Math.random() * 12),
            expertB: Math.floor(Math.random() * 10),
            ordinary: Math.floor(Math.random() * 6),
            unitFamousEffective: Math.floor(Math.random() * 4),
            unitFamousTotal: 4,
            unitSpecialEffective: Math.floor(Math.random() * 4),
            unitSpecialTotal: 4,
            unitKnownEffective: Math.floor(Math.random() * 4),
            unitKnownTotal: 4,
            unitAEffective: Math.floor(Math.random() * 4),
            unitATotal: 4,
            unitBEffective: Math.floor(Math.random() * 4),
            unitBTotal: 4,
            unitOrdinaryEffective: Math.floor(Math.random() * 4),
            unitOrdinaryTotal: 4
        }));
    },

    /**
     * 获取门诊收入概览数据
     */
    getRevenueOverviewData() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        overview: {
                            outpatientRevenue: 52612536.3,
                            serviceRevenue: 7353266.8
                        }
                    }
                });
            }, 200);
        });
    },

    /**
     * 获取科室收入统计数据
     */
    getDeptRevenueData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateDeptRevenueData();
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                let filteredData = allData;
                if (params.deptName) {
                    filteredData = filteredData.filter(item => item.deptName.includes(params.deptName));
                }
                if (params.deptCode) {
                    filteredData = filteredData.filter(item => item.deptCode === params.deptCode);
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成科室收入数据
     */
    generateDeptRevenueData() {
        const deptNames = [
            '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
            '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
        ];
        return deptNames.map(deptName => ({
            deptName,
            outpatientRevenue: (300 + Math.random() * 200).toFixed(1),
            serviceRevenue: (300 + Math.random() * 200).toFixed(1)
        }));
    },

    /**
     * 获取医生收入统计数据
     */
    getDoctorRevenueData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateDoctorRevenueData();
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                let filteredData = allData;
                if (params.deptName) {
                    filteredData = filteredData.filter(item => item.deptName.includes(params.deptName));
                }
                if (params.deptCode) {
                    filteredData = filteredData.filter(item => item.deptCode === params.deptCode);
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成医生收入数据
     */
    generateDoctorRevenueData() {
        const doctors = [
            { name: '张三', dept: '心血管内科门诊' },
            { name: '李四', dept: '呼吸科门诊' },
            { name: '王五', dept: '消化科门诊' },
            { name: '赵六', dept: '神经内科门诊' },
            { name: '孙七', dept: '肾内科门诊' },
            { name: '周八', dept: '骨科门诊' },
            { name: '吴九', dept: '泌尿外科门诊' },
            { name: '郑十', dept: '神经外科门诊' },
            { name: '钱十一', dept: '心脏血管外科门诊' },
            { name: '陈十二', dept: '肝胆胰外科门诊' }
        ];
        const result = [];
        for (let i = 0; i < 55; i++) {
            const doctor = doctors[i % doctors.length];
            result.push({
                doctorName: doctor.name,
                deptName: doctor.dept,
                doctorBenefit: (300 + Math.random() * 200).toFixed(1),
                serviceRevenue: (300 + Math.random() * 200).toFixed(1)
            });
        }
        return result;
    },

    /**
     * 获取患者画像数据
     */
    getPatientPortraitData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const patientType = params.patientType || 'outpatient';
                resolve({
                    code: 200,
                    data: {
                        ageAnalysis: this.generateAgeAnalysisData(patientType),
                        insuranceAnalysis: this.generateInsuranceAnalysisData(),
                        identityAnalysis: this.generateIdentityAnalysisData(),
                        registerOriginAnalysis: this.generateOriginAnalysisData(),
                        archiveOriginAnalysis: this.generateOriginAnalysisData()
                    }
                });
            }, 300);
        });
    },

    /**
     * 生成年龄区间分析数据
     */
    generateAgeAnalysisData(patientType) {
        const categories = ['0-5', '6-10', '11-15', '16-20', '21-25', '26-30', '31-35', '36-40', '41-45', '80+'];
        const archiveData = [8500, 6500, 5000, 3800, 3500, 2800, 2200, 1800, 1500, 1200];
        const outpatientData = [4500, 5800, 7200, 6800, 7000, 7500, 8000, 4200, 2800, 1500];
        return {
            categories,
            archiveData,
            outpatientData: patientType === 'outpatient' ? outpatientData : outpatientData.map(v => Math.floor(v * 0.6))
        };
    },

    /**
     * 生成医保身份构成数据
     */
    generateInsuranceAnalysisData() {
        return [
            { name: '本地职工医保', value: 31 },
            { name: '本地居民医保', value: 31 },
            { name: '本地自费', value: 31 },
            { name: '本地其他', value: 31 },
            { name: '异地职工医保', value: 31 },
            { name: '异地居民医保', value: 31 },
            { name: '异地自费', value: 31 },
            { name: '异地其他', value: 31 }
        ];
    },

    /**
     * 生成身份类别构成数据
     */
    generateIdentityAnalysisData() {
        return [
            { name: '一般人员', value: 31 },
            { name: '军属', value: 31 },
            { name: '离休干部', value: 31 },
            { name: '其他（地/退军人）', value: 31 }
        ];
    },

    /**
     * 生成归属地分析数据
     */
    generateOriginAnalysisData() {
        return [
            { name: '重庆', value: 31 },
            { name: '四川', value: 31 },
            { name: '贵州', value: 31 },
            { name: '云南', value: 31 },
            { name: '其他', value: 31 }
        ];
    },

    /**
     * 获取人工窗口统计数据
     */
    getWindowStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        overview: {
                            registerCount: 12536,
                            paymentCount: 12536,
                            refundCount: 12536
                        },
                        originAnalysis: [
                            { name: '重庆', value: 31 },
                            { name: '四川', value: 31 },
                            { name: '贵州', value: 31 },
                            { name: '云南', value: 31 },
                            { name: '其他', value: 31 }
                        ],
                        ageAnalysis: {
                            categories: ['0-14', '15-19', '20-29', '30-39', '40-49', '50-59', '60-69', '70-79', '80-89', '90+'],
                            data: [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
                        },
                        timeAnalysis: {
                            categories: ['08:00~09:00', '09:00~10:00', '10:00~11:00', '11:00~12:00', '12:00~13:00', '13:00~14:00', '14:00~15:00', '15:00~16:00', '16:00~17:00'],
                            data: [3500, 5200, 3800, 5500, 4800, 6200, 8500, 9000, 7800]
                        },
                        workloadTable: {
                            headers: ['08:00~09:00', '09:00~10:00', '10:00~11:00', '11:00~12:00', '12:00~13:00', '13:00~14:00', '14:00~15:00', '15:00~16:00', '16:00~17:00'],
                            rows: [
                                { business: '挂号', data: [357, 357, 357, 357, 357, 357, 357, 357, 357] },
                                { business: '缴费', data: [357, 357, 357, 357, 357, 357, 357, 357, 357] },
                                { business: '退费', data: [357, 357, 357, 357, 357, 357, 357, 357, 357] }
                            ]
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取检验统计数据
     */
    getLabStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        overview: {
                            bloodCollection: 12536,
                            bloodEfficiency: '37.5分',
                            labEfficiency: '92.6%'
                        },
                        timeAnalysis: {
                            categories: ['08:00~09:00', '09:00~10:00', '10:00~11:00', '11:00~12:00', '12:00~13:00', '13:00~14:00', '14:00~15:00', '15:00~16:00', '16:00~17:00'],
                            data: [2800, 5500, 4800, 6200, 5200, 7500, 8500, 7200, 4500]
                        },
                        reportRank: {
                            categories: ['项目1', '项目2', '项目3', '项目4', '项目5', '项目6', '项目7', '项目8', '项目9', '项目10'],
                            data: [130, 100, 75, 60, 55, 45, 35, 30, 25, 20]
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取医技统计数据
     */
    getMedTechStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const tableData = [
                    { deptName: 'CT', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '核磁', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: 'X光', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '妇科超声', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '其他超声', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '胃肠镜', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '支气管', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '神经内科', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '心电图', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 },
                    { deptName: '心内科', checkCount: 357, onTimeRate: '62.2%', waitTime: 15.8, avgWaitLate: 56.6, avgReportTime: 56.6 }
                ];

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const startIndex = (page - 1) * pageSize;
                const list = tableData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            checkCount: 12536,
                            onTimeRate: '73.2%',
                            waitTime: '26.6分',
                            avgWaitLate: '216.7分',
                            avgReportTime: '216.7分'
                        },
                        table: {
                            list,
                            total: tableData.length,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取爽约退号统计数据
     */
    getNoShowStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
                    '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
                ];
                const tableData = deptNames.map(deptName => ({
                    deptName,
                    refundCount: 357,
                    refundRate: '62.2%',
                    refundOrigin: { chongqing: '357 (30%)', sichuan: '357 (30%)', guizhou: '357 (30%)', yunnan: '357 (30%)', other: '357 (30%)' },
                    refundChannel: { window: 357, miniprogram: 357 },
                    noShowCount: 357,
                    noShowRate: '62.5%',
                    noShowOrigin: { chongqing: '357 (30%)', sichuan: '357 (30%)', guizhou: '357 (30%)', yunnan: '357 (30%)', other: '357 (30%)' }
                }));

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const startIndex = (page - 1) * pageSize;
                const list = tableData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            refundCount: 12536,
                            refundRate: '14.6%',
                            noShowCount: 12536,
                            noShowRate: '14.6%'
                        },
                        refundOrigin: [
                            { name: '重庆', value: 31 },
                            { name: '四川', value: 31 },
                            { name: '贵州', value: 31 },
                            { name: '云南', value: 31 },
                            { name: '其他', value: 31 }
                        ],
                        refundChannel: [
                            { name: '窗口', value: 31 },
                            { name: '小程序', value: 31 }
                        ],
                        ageAnalysis: {
                            categories: ['0-14', '15-19', '20-29', '30-39', '40-49', '50-59', '60-69', '70-79', '80-89', '90+'],
                            data: [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
                        },
                        table: {
                            list,
                            total: tableData.length,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取门诊预警统计数据
     */
    getAlertStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
                    '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
                ];
                const doctors = [
                    { name: '张三', dept: '心血管内科门诊' },
                    { name: '李四', dept: '呼吸科门诊' },
                    { name: '王五', dept: '消化科门诊' },
                    { name: '赵六', dept: '神经内科门诊' },
                    { name: '孙七', dept: '肾内科门诊' },
                    { name: '周八', dept: '骨科门诊' },
                    { name: '吴九', dept: '泌尿外科门诊' },
                    { name: '郑十', dept: '神经外科门诊' },
                    { name: '钱十一', dept: '心脏血管外科门诊' },
                    { name: '陈十二', dept: '肝胆胰外科门诊' }
                ];

                const deptTable = deptNames.map(deptName => ({
                    deptName,
                    remainAlert: 357,
                    appointmentAlert: 357,
                    earlyLeave: 357
                }));

                const doctorTable = [];
                for (let i = 0; i < 55; i++) {
                    const d = doctors[i % doctors.length];
                    doctorTable.push({
                        doctorName: d.name,
                        deptName: d.dept,
                        remainAlert: 357,
                        appointmentAlert: 357,
                        earlyLeave: 357
                    });
                }

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                let filteredDept = deptTable;
                let filteredDoctor = doctorTable;
                if (params.deptName) {
                    filteredDept = deptTable.filter(item => item.deptName.includes(params.deptName));
                    filteredDoctor = doctorTable.filter(item => item.deptName.includes(params.deptName));
                }

                const deptStart = (page - 1) * pageSize;
                const doctorStart = (page - 1) * pageSize;

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            remainAlert: 462,
                            appointmentAlert: 462,
                            earlyLeave: 462
                        },
                        deptTable: {
                            list: filteredDept.slice(deptStart, deptStart + pageSize),
                            total: filteredDept.length,
                            page,
                            pageSize
                        },
                        doctorTable: {
                            list: filteredDoctor.slice(doctorStart, doctorStart + pageSize),
                            total: filteredDoctor.length,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取诊室使用率统计数据
     */
    getRoomUsageStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
                    '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
                ];

                const tableData = deptNames.map(deptName => ({
                    deptName,
                    avgUsage: '62.2%',
                    amUsage: '62.2%',
                    pmUsage: '62.2%',
                    holidayUsage: '62.2%'
                }));

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                let filteredData = tableData;
                if (params.deptName) {
                    filteredData = tableData.filter(item => item.deptName.includes(params.deptName));
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            avgUsage: '66.9%',
                            amUsage: '66.9%',
                            pmUsage: '66.9%',
                            holidayUsage: '66.9%'
                        },
                        table: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取专科治疗量统计数据
     */
    getSpecialtyTreatmentStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
                    '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
                ];

                const tableData = deptNames.map(deptName => ({
                    deptName,
                    treatmentCount: 257,
                    treatmentAmount: 343734.6,
                    patientCount: 52
                }));

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                let filteredData = tableData;
                if (params.deptName) {
                    filteredData = tableData.filter(item => item.deptName.includes(params.deptName));
                }

                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            treatmentCount: 256,
                            treatmentAmount: 5566.9,
                            patientCount: 66
                        },
                        table: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取治疗统计报表数据
     */
    getTreatmentStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科', '呼吸科', '消化科', '神经内科', '肾内科',
                    '骨科', '泌尿外科', '神经外科', '心脏血管外科', '肝胆胰外科'
                ];

                // 生成30天趋势数据
                const trendDates = [];
                const trendData = [];
                for (let i = 29; i >= 0; i--) {
                    const d = new Date();
                    d.setDate(d.getDate() - i);
                    const m = String(d.getMonth() + 1).padStart(2, '0');
                    const day = String(d.getDate()).padStart(2, '0');
                    trendDates.push(`${m}-${day}`);
                    trendData.push(Math.floor(Math.random() * 80) + 20);
                }

                // TOP10治疗项目
                const topProjects = [
                    { name: '推拿按摩', value: 452 },
                    { name: '针灸治疗', value: 389 },
                    { name: '拔罐疗法', value: 356 },
                    { name: '艾灸治疗', value: 298 },
                    { name: '刮痧治疗', value: 267 },
                    { name: '中药熏蒸', value: 234 },
                    { name: '穴位贴敷', value: 198 },
                    { name: '经络检测', value: 176 },
                    { name: '电针治疗', value: 154 },
                    { name: '耳穴压豆', value: 132 }
                ];

                // 表格数据
                const tableData = deptNames.map((deptName, index) => {
                    const patientCount = Math.floor(Math.random() * 60) + 20;
                    const treatmentCount = patientCount * Math.floor(Math.random() * 5) + 5;
                    const treatmentAmount = treatmentCount * (Math.random() * 1000 + 500);
                    return {
                        rank: index + 1,
                        deptName,
                        patientCount,
                        treatmentCount,
                        treatmentAmount: parseFloat(treatmentAmount.toFixed(1)),
                        avgAmount: parseFloat((treatmentAmount / treatmentCount).toFixed(1))
                    };
                });

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = tableData.length;
                const startIndex = (page - 1) * pageSize;
                const list = tableData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            patientCount: 256,
                            treatmentCount: 1236,
                            treatmentAmount: 5566.9,
                            avgAmount: 43.4
                        },
                        trend: {
                            dates: trendDates,
                            data: trendData
                        },
                        topProjects,
                        table: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取预测门诊量统计数据
     */
    getForecastStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
                const monthDates = [];
                const monthData = [];
                for (let i = 0; i < 30; i++) {
                    const d = new Date();
                    d.setDate(d.getDate() + i);
                    const day = String(d.getDate()).padStart(2, '0');
                    const weekday = weekdays[d.getDay()];
                    monthDates.push(`${day}\n${weekday}`);
                    monthData.push(Math.floor(Math.random() * 100) + 30);
                }

                const yearMonths = [];
                const yearData = [];
                for (let i = 1; i <= 12; i++) {
                    yearMonths.push(String(i).padStart(2, '0'));
                    yearData.push(1000);
                }

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            tomorrow: 462,
                            nextWeek: 462,
                            nextMonth: 462,
                            nextYear: 462
                        },
                        monthForecast: {
                            dates: monthDates,
                            data: monthData
                        },
                        yearForecast: {
                            months: yearMonths,
                            data: yearData
                        }
                    }
                });
            }, 300);
        });
    },

    getServiceQualityStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptNames = [
                    '心血管内科门诊', '呼吸科门诊', '消化科门诊', '神经内科门诊', '肾内科门诊',
                    '骨科门诊', '泌尿外科门诊', '神经外科门诊', '心脏血管外科门诊', '肝胆胰外科门诊'
                ];
                const positions = ['医师', '护士', '技师', '药师', '劳务派遣', '第三方购买服务'];
                const complaintCategories = ['病历问题', '费用问题', '服务问题', '告知问题', '沟通问题', '医疗质量'];
                const complaintResults = ['有效投诉', '无效投诉'];
                const praiseMethods = ['锦旗', '感谢信', '口头传达'];
                const feedbacks = ['已反馈', '未反馈'];

                const generateComplaints = () => deptNames.map((dept, i) => ({
                    time: '2025-11-25 13:50',
                    dept,
                    person: '张三',
                    position: positions[i % positions.length],
                    category: complaintCategories[i % complaintCategories.length],
                    result: complaintResults[i % complaintResults.length],
                    remark: ''
                }));

                const generatePraise = () => deptNames.map((dept, i) => ({
                    time: '2025-11-25 13:50',
                    dept,
                    person: '张三',
                    position: positions[i % positions.length],
                    method: praiseMethods[i % praiseMethods.length],
                    feedback: feedbacks[i % feedbacks.length],
                    remark: ''
                }));

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const tab = params.tab || 'complaint';

                const allData = tab === 'complaint' ? generateComplaints() : generatePraise();
                let filteredData = allData;
                if (params.deptName) {
                    filteredData = allData.filter(item => item.dept.includes(params.deptName));
                }
                const total = filteredData.length;
                const startIndex = (page - 1) * pageSize;
                const list = filteredData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            complaintCount: 46,
                            praiseCount: 46
                        },
                        [tab]: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    getQualityControlStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const months = ['2025-12', '2025-11', '2025-10', '2025-09', '2025-08', '2025-07', '2025-06', '2025-05', '2025-04', '2025-03'];
                const indicators = [
                    'emrUsageRate', 'standardDiagnosisRate', 'onTimeRate', 'stopRate',
                    'chemoRecordRate', 'chemoAdverseRate', 'chemoInfusionRate',
                    'criticalValueRate', 'bloodDrawErrorRate', 'surgeryComplicationRate', 'adverseEventRate'
                ];

                const tableData = months.map(month => {
                    const row = { month };
                    indicators.forEach(ind => {
                        row[ind] = '62.2%';
                    });
                    return row;
                });

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = tableData.length;
                const startIndex = (page - 1) * pageSize;
                const list = tableData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            emrUsageRate: '46.2%',
                            standardDiagnosisRate: '46.2%',
                            onTimeRate: '46.2%',
                            stopRate: '46.2%',
                            chemoRecordRate: '46.2%',
                            chemoAdverseRate: '46.2%',
                            chemoInfusionRate: '46.2%',
                            criticalValueRate: '46.2%',
                            bloodDrawErrorRate: '46.2%',
                            surgeryComplicationRate: '46.2%',
                            adverseEventRate: '46.2%'
                        },
                        table: {
                            list,
                            total,
                            page,
                            pageSize
                        }
                    }
                });
            }, 300);
        });
    },

    getInternetHospitalStatsData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const operationItems = [
                    { name: '诊察号量（含退号）', current: 120, last: 100 },
                    { name: '便民咨询（不含号）', current: 120, last: 100 },
                    { name: '病历书写量', current: 120, last: 100 },
                    { name: '药品处方开具量', current: 120, last: 100 },
                    { name: '药品处方执行量', current: 120, last: 100 },
                    { name: '挂号费（元）', current: '12,000.00', last: '10,000.00' },
                    { name: '检查检验费（元）', current: '12,000.00', last: '10,000.00' },
                    { name: '药费处方费（元）', current: '12,000.00', last: '10,000.00' },
                    { name: '互联网服务量', current: 100, last: 120 }
                ];

                const businessCategories = ['在线诊疗', '便民咨询', '护理咨询', '诊间咨询', '线上免费问诊', '网络诊方', '特殊制剂', '检查项目', '自助开单', '心理咨询'];
                const businessData = businessCategories.map(() => ({
                    current: Math.floor(Math.random() * 5000) + 5000,
                    last: Math.floor(Math.random() * 5000) + 5000
                }));

                const deptNames = ['皮肤_风湿免疫科门诊', '全科医学科门诊', '心血管内科门诊', '神经内科门诊', '肾病科门诊', '营养科门诊', '血液科门诊', '妇产科门诊', '中医科门诊', '消化科门诊'];
                const deptRanking = deptNames.map((dept, i) => ({
                    rank: i + 1,
                    deptName: dept,
                    currentMonth: 120,
                    lastMonth: 100,
                    growth: `+${Math.floor(Math.random() * 30) + 10}%`
                }));

                const doctorNames = ['张一', '张二', '张三', '张四', '张五', '张六', '张七', '张八', '张九', '张十'];
                const doctorRanking = doctorNames.map((name, i) => ({
                    rank: i + 1,
                    doctorName: name,
                    deptName: deptNames[i % deptNames.length],
                    title: '主治医师',
                    currentMonth: 100
                }));

                const growthCategories = ['皮肤_风湿免疫科门诊', '心血管内科门诊', '神经内科门诊', '消化科门诊', '肾病科门诊', '中医科门诊', '血液科门诊', '营养科门诊', '妇产科门诊', '全科医学科门诊'];
                const growthData = growthCategories.map(() => Math.floor(Math.random() * 80) + 20);

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;

                resolve({
                    code: 200,
                    data: {
                        overview: {
                            outpatientVolume: 4652,
                            doctorRatio: '73.5%',
                            receptionRate: '92.6%',
                            prescriptionRate: '83.1%',
                            recordRate: '95.7%',
                            reviewRate: '68.7%',
                            executionRate: '71.9%'
                        },
                        operationTable: operationItems.map(item => ({
                            ...item,
                            growth: `+${Math.floor(Math.random() * 30) + 10}%`
                        })),
                        businessChart: {
                            categories: businessCategories,
                            current: businessData.map(d => d.current),
                            last: businessData.map(d => d.last)
                        },
                        deptRanking: {
                            list: deptRanking.slice(0, pageSize),
                            total: deptRanking.length,
                            page,
                            pageSize
                        },
                        doctorRanking: {
                            list: doctorRanking.slice(0, pageSize),
                            total: doctorRanking.length,
                            page,
                            pageSize
                        },
                        growthChart: {
                            categories: growthCategories,
                            data: growthData
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取科室字典
     * @param {Object} params - { deptType, deptCode, deptName, ...ext }
     * @returns {Promise} 返回科室列表
     */
    getDeptDict(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const deptType = params.deptType != null ? params.deptType : 0;
                const deptCode = params.deptCode || '';
                const deptName = params.deptName || '';

                // 门诊科室样例数据
                const outpatientDepts = [
                    { deptCode: '0000', deptName: '全部', deptType: 0, parentCode: '', level: 0 },
                    { deptCode: '0101', deptName: '心血管内科门诊', deptType: 0, parentCode: '0100', level: 2 },
                    { deptCode: '0102', deptName: '呼吸科门诊', deptType: 0, parentCode: '0100', level: 2 },
                    { deptCode: '0103', deptName: '消化科门诊', deptType: 0, parentCode: '0100', level: 2 },
                    { deptCode: '0104', deptName: '神经内科门诊', deptType: 0, parentCode: '0100', level: 2 },
                    { deptCode: '0105', deptName: '肾内科门诊', deptType: 0, parentCode: '0100', level: 2 },
                    { deptCode: '0201', deptName: '骨科门诊', deptType: 0, parentCode: '0200', level: 2 },
                    { deptCode: '0202', deptName: '泌尿外科门诊', deptType: 0, parentCode: '0200', level: 2 },
                    { deptCode: '0203', deptName: '神经外科门诊', deptType: 0, parentCode: '0200', level: 2 },
                    { deptCode: '0204', deptName: '心脏血管外科门诊', deptType: 0, parentCode: '0200', level: 2 },
                    { deptCode: '0205', deptName: '肝胆胰外科门诊', deptType: 0, parentCode: '0200', level: 2 }
                ];

                // 住院科室样例数据
                const inpatientDepts = [
                    { deptCode: '0000', deptName: '全部', deptType: 1, parentCode: '', level: 0 },
                    { deptCode: '1101', deptName: '心血管内科病房', deptType: 1, parentCode: '1100', level: 2 },
                    { deptCode: '1102', deptName: '呼吸科病房', deptType: 1, parentCode: '1100', level: 2 },
                    { deptCode: '1103', deptName: '消化科病房', deptType: 1, parentCode: '1100', level: 2 }
                ];

                // 其他科室样例数据
                const otherDepts = [
                    { deptCode: '0000', deptName: '全部', deptType: 2, parentCode: '', level: 0 },
                    { deptCode: '9001', deptName: '体检中心', deptType: 2, parentCode: '', level: 1 },
                    { deptCode: '9002', deptName: '检验科', deptType: 2, parentCode: '', level: 1 }
                ];

                let list = [];
                if (deptType === 0) list = outpatientDepts;
                else if (deptType === 1) list = inpatientDepts;
                else if (deptType === 2) list = otherDepts;
                else list = [...outpatientDepts, ...inpatientDepts, ...otherDepts];

                // 按 deptCode 精确筛选（0000 表示全部，保留所有）
                if (deptCode && deptCode !== '0000') {
                    list = list.filter(item => item.deptCode === deptCode);
                }

                // 按 deptName 模糊匹配
                if (deptName) {
                    list = list.filter(item => item.deptName.includes(deptName));
                }

                resolve({
                    code: 200,
                    data: {
                        list,
                        total: list.length
                    }
                });
            }, 200);
        });
    },

    /**
     * 导出 Excel（模拟）
     */
    exportExcel(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ code: 200, data: { message: '导出成功' } });
            }, 300);
        });
    }
};
