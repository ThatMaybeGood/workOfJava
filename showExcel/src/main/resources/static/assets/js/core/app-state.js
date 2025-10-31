// 应用状态管理
const AppState = {
    currentView: 'table',
    currentParams: {
        targetIncome: 100000,
        warningThreshold: 80,
        dataRetentionDays: 90,
        autoBackup: true
    },
    holidays: [],
    currentCalendarDate: new Date(),
    tableData: []
};

