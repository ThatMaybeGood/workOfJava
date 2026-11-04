package com.reports.util;

import com.reports.entity.MiReconDetailEntity;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.MulRKRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 医保对账上传文件解析
 * 文件4列: 定点医药机构编号/对账数据量/医疗费总额/结算日期, 首行表头跳过, 按结算日期聚合
 * WPS导出的部分xls记录次序不合规, 用户模型解析失败时降级事件API顺序读取
 */
public final class MiReconExcelUtil {

    private static final String[] DATE_PATTERNS = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyyMMdd"};

    /**
     * 解析上传文件, 返回按结算日期聚合的明细(同日多行数量/金额累加)
     */
    public static List<MiReconDetailEntity> parse(InputStream in, String insurType) throws Exception {
        byte[] bytes = IOUtils.toByteArray(in);
        Map<Integer, Map<Integer, Object>> grid;
        try {
            try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
                grid = readUserModel(wb);
            }
        } catch (Exception e) {
            grid = readByEvent(bytes);
        }
        return aggregate(grid, insurType);
    }

    /** 用户模型: 单元格值统一成String或Double */
    private static Map<Integer, Map<Integer, Object>> readUserModel(Workbook wb) {
        Map<Integer, Map<Integer, Object>> grid = new TreeMap<>();
        Sheet sheet = wb.getSheetAt(0);
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            Map<Integer, Object> cells = new TreeMap<>();
            for (int c = 0; c < 4; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                if (cell.getCellType() == CellType.NUMERIC) {
                    cells.put(c, cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    cells.put(c, cell.getStringCellValue());
                } else if (cell.getCellType() == CellType.FORMULA) {
                    try {
                        cells.put(c, cell.getNumericCellValue());
                    } catch (Exception ignore) {
                        // 非数值公式结果忽略
                    }
                }
            }
            grid.put(r, cells);
        }
        return grid;
    }

    /** 事件模型: 顺序读记录绕过用户模型的次序校验, 只取第一个工作表 */
    private static Map<Integer, Map<Integer, Object>> readByEvent(byte[] bytes) throws Exception {
        final List<String> sst = new ArrayList<>();
        final Map<Integer, Map<Integer, Object>> grid = new TreeMap<>();
        final int[] sheetSeq = {0};
        final boolean[] inFirstSheet = {false};
        HSSFListener listener = record -> {
            switch (record.getSid()) {
                case SSTRecord.sid:
                    SSTRecord s = (SSTRecord) record;
                    for (int i = 0; i < s.getNumUniqueStrings(); i++) {
                        sst.add(s.getString(i).getString());
                    }
                    break;
                case BOFRecord.sid:
                    if (((BOFRecord) record).getType() == BOFRecord.TYPE_WORKSHEET) {
                        sheetSeq[0]++;
                        inFirstSheet[0] = sheetSeq[0] == 1;
                    }
                    break;
                case EOFRecord.sid:
                    inFirstSheet[0] = false;
                    break;
                case LabelSSTRecord.sid:
                    if (inFirstSheet[0]) {
                        LabelSSTRecord l = (LabelSSTRecord) record;
                        put(grid, l.getRow(), l.getColumn(), sst.get(l.getSSTIndex()));
                    }
                    break;
                case LabelRecord.sid:
                    if (inFirstSheet[0]) {
                        LabelRecord l = (LabelRecord) record;
                        put(grid, l.getRow(), l.getColumn(), l.getValue());
                    }
                    break;
                case NumberRecord.sid:
                    if (inFirstSheet[0]) {
                        NumberRecord n = (NumberRecord) record;
                        put(grid, n.getRow(), n.getColumn(), n.getValue());
                    }
                    break;
                case RKRecord.sid:
                    if (inFirstSheet[0]) {
                        RKRecord rk = (RKRecord) record;
                        put(grid, rk.getRow(), rk.getColumn(), rk.getRKNumber());
                    }
                    break;
                case MulRKRecord.sid:
                    if (inFirstSheet[0]) {
                        MulRKRecord mul = (MulRKRecord) record;
                        for (int i = 0; i < mul.getNumColumns(); i++) {
                            put(grid, mul.getRow(), mul.getFirstColumn() + i, mul.getRKNumberAt(i));
                        }
                    }
                    break;
                case FormulaRecord.sid:
                    if (inFirstSheet[0]) {
                        FormulaRecord f = (FormulaRecord) record;
                        if (f.getCachedResultTypeEnum() == CellType.NUMERIC) {
                            put(grid, f.getRow(), f.getColumn(), f.getValue());
                        }
                    }
                    break;
                default:
                    break;
            }
        };
        HSSFRequest request = new HSSFRequest();
        request.addListenerForAllRecords(listener);
        try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
            new HSSFEventFactory().processWorkbookEvents(request, fs);
        }
        return grid;
    }

    private static void put(Map<Integer, Map<Integer, Object>> grid, int row, int col, Object value) {
        grid.computeIfAbsent(row, k -> new TreeMap<>()).put(col, value);
    }

    /** 首行表头跳过, 按结算日期聚合数量/金额 */
    private static List<MiReconDetailEntity> aggregate(Map<Integer, Map<Integer, Object>> grid, String insurType) {
        Map<String, MiReconDetailEntity> agg = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<Integer, Object>> rowEntry : grid.entrySet()) {
            if (rowEntry.getKey() == 0) {
                continue;
            }
            Map<Integer, Object> cells = rowEntry.getValue();
            Long qty = longVal(cells.get(1));
            BigDecimal fee = decimalVal(cells.get(2));
            String settleDay = dateVal(cells.get(3));
            if (qty == null && fee == null && settleDay == null) {
                continue;
            }
            if (settleDay == null) {
                throw new IllegalArgumentException("第" + (rowEntry.getKey() + 1) + "行结算日期为空或格式无法识别");
            }
            MiReconDetailEntity item = agg.computeIfAbsent(settleDay, k -> {
                MiReconDetailEntity e = new MiReconDetailEntity();
                e.setInsurType(insurType);
                e.setQty(0L);
                e.setTotalFee(BigDecimal.ZERO);
                e.setSettleDate(dayOf(k));
                return e;
            });
            if (qty != null) {
                item.setQty(item.getQty() + qty);
            }
            if (fee != null) {
                item.setTotalFee(item.getTotalFee().add(fee));
            }
        }
        return new ArrayList<>(agg.values());
    }

    private static Long longVal(Object v) {
        BigDecimal d = decimalVal(v);
        return d == null ? null : d.longValue();
    }

    private static BigDecimal decimalVal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return BigDecimal.valueOf(((Number) v).doubleValue());
        }
        String text = v.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 结算日期: 文本按常见格式解析, 数值按Excel日期序列解析, 统一返回yyyy-MM-dd */
    private static String dateVal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.getJavaDate(((Number) v).doubleValue()));
        }
        String text = v.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern);
                fmt.setLenient(false);
                return new SimpleDateFormat("yyyy-MM-dd").format(fmt.parse(text));
            } catch (Exception ignore) {
                // 换下一个格式
            }
        }
        return null;
    }

    private static java.util.Date dayOf(String yyyyMMdd) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Integer.parseInt(yyyyMMdd.substring(0, 4)),
                Integer.parseInt(yyyyMMdd.substring(5, 7)) - 1,
                Integer.parseInt(yyyyMMdd.substring(8, 10)));
        return c.getTime();
    }

    private MiReconExcelUtil() {
    }
}
