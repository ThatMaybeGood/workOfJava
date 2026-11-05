package com.reports.service.impl;

import com.reports.dto.request.MiReconRequest;
import com.reports.dto.response.cash.mi.MiReconCompareItem;
import com.reports.dto.response.cash.mi.MiReconFileItem;
import com.reports.dto.response.cash.mi.MiReconOrgCatItem;
import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;
import com.reports.entity.MiReconDetailEntity;
import com.reports.entity.MiReconFileEntity;
import com.reports.enums.MiReconInsurType;
import com.reports.enums.MiReconOrgCategory;
import com.reports.mapper.MiReconMapper;
import com.reports.service.MiReconMedInstService;
import com.reports.service.MiReconService;
import com.reports.util.MiReconExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 医保对账每日对比服务实现(报表库)
 */
@Slf4j
@Service
public class MiReconServiceImpl implements MiReconService {

    private final MiReconMapper miReconMapper;
    private final MiReconMedInstService miReconMedInstService;

    @Autowired
    public MiReconServiceImpl(MiReconMapper miReconMapper,
                              MiReconMedInstService miReconMedInstService) {
        this.miReconMapper = miReconMapper;
        this.miReconMedInstService = miReconMedInstService;
    }

    @Override
    public List<MiReconCompareItem> queryCompare(MiReconRequest request) {
        MiReconInsurType type = MiReconInsurType.ofCode(request.getInsurType());
        if (type == null) {
            throw new IllegalArgumentException("医保类型无效, 应为310/390/3101/3901之一");
        }
        // 对账月份折算为当月1号~月末(医疗机构侧SQL按此日期范围直查)
        String month = resolveMonth(request.getMonth());
        String start = month + "-01";
        String end = lastDayOfMonth(month);

        // 医保侧(上传数据)与医疗机构侧各按结算日期取数, 按日期并集对齐
        Map<String, MiReconCompareItem> merged = new TreeMap<>();
        try {
            for (MiReconDetailEntity d : miReconMapper.queryMiAgg(type.getCode(), start, end)) {
                MiReconCompareItem item = merged.computeIfAbsent(fmt(d.getSettleDate()), k -> baseItem(k, type));
                item.setMiQty(d.getQty());
                item.setMiFee(d.getTotalFee());
            }
        } catch (Exception e) {
            log.warn("查询医保对账上传数据失败", e);
        }
        // 医疗机构侧: HIS原始行在代码里聚合——日期合计入对比, 按来源类型(账平/医保负金额/医保正金额)汇总入明细
        Map<String, Map<String, MiReconOrgCatItem>> orgCatMap = new HashMap<>();
        try {
            for (MiReconOrgDtlItem raw : miReconMedInstService.queryOrgData(type.getCode(), start, end)) {
                accOrgRaw(raw, merged, orgCatMap, type,
                        (i, q) -> i.setOrgQty((i.getOrgQty() == null ? 0L : i.getOrgQty()) + (q == null ? 0L : q)),
                        (i, f) -> i.setOrgFee((i.getOrgFee() == null ? BigDecimal.ZERO : i.getOrgFee())
                                .add(f == null ? BigDecimal.ZERO : f)));
            }
        } catch (Exception e) {
            log.warn("查询医疗机构对账数据失败", e);
        }
        // 医保机构侧: pay_bills自身三分类(账平/医保负金额/医保正金额), 同口径聚合
        Map<String, Map<String, MiReconOrgCatItem>> miOrgCatMap = new HashMap<>();
        try {
            for (MiReconOrgDtlItem raw : miReconMedInstService.queryMiOrgData(type.getCode(), start, end)) {
                accOrgRaw(raw, merged, miOrgCatMap, type,
                        (i, q) -> i.setMiOrgQty((i.getMiOrgQty() == null ? 0L : i.getMiOrgQty()) + (q == null ? 0L : q)),
                        (i, f) -> i.setMiOrgFee((i.getMiOrgFee() == null ? BigDecimal.ZERO : i.getMiOrgFee())
                                .add(f == null ? BigDecimal.ZERO : f)));
            }
        } catch (Exception e) {
            log.warn("查询医保机构对账数据失败", e);
        }

        List<MiReconCompareItem> list = new ArrayList<>(merged.values());
        for (MiReconCompareItem item : list) {
            item.setOrgDtl(toOrgCats(orgCatMap, fmt(item.getSettleDate())));
            boolean both = item.getMiFee() != null && item.getOrgFee() != null;
            item.setDiffFee(both ? item.getMiFee().subtract(item.getOrgFee()) : null);
            // 金额对平才算对平; 单侧缺失或不等均不平
            item.setMatched(both && item.getMiFee().compareTo(item.getOrgFee()) == 0);
            item.setMiOrgDtl(toOrgCats(miOrgCatMap, fmt(item.getSettleDate())));
            boolean miBoth = item.getMiFee() != null && item.getMiOrgFee() != null;
            item.setMiOrgDiffFee(miBoth ? item.getMiFee().subtract(item.getMiOrgFee()) : null);
            item.setMiOrgMatched(miBoth && item.getMiFee().compareTo(item.getMiOrgFee()) == 0);
        }
        return list;
    }

    /** 医疗机构/医保机构侧原始行聚合: 日期合计入item, 按来源类型汇总入catMap */
    private void accOrgRaw(MiReconOrgDtlItem raw, Map<String, MiReconCompareItem> merged,
                           Map<String, Map<String, MiReconOrgCatItem>> catMap, MiReconInsurType type,
                           java.util.function.BiConsumer<MiReconCompareItem, Long> addQty,
                           java.util.function.BiConsumer<MiReconCompareItem, BigDecimal> addFee) {
        if (raw == null || raw.getSettleDate() == null) {
            return;
        }
        String day = fmt(raw.getSettleDate());
        MiReconCompareItem item = merged.computeIfAbsent(day, k -> baseItem(k, type));
        addQty.accept(item, raw.getQty());
        addFee.accept(item, raw.getFee());
        String sourceType = raw.getSourceType() == null ? "" : raw.getSourceType().trim();
        MiReconOrgCatItem cat = catMap.computeIfAbsent(day, k -> new LinkedHashMap<>())
                .computeIfAbsent(sourceType, k -> {
                    MiReconOrgCatItem c = new MiReconOrgCatItem();
                    c.setSourceType(k);
                    c.setSourceTypeName(MiReconOrgCategory.nameOf(k));
                    c.setQty(0L);
                    c.setFee(BigDecimal.ZERO);
                    return c;
                });
        if (raw.getQty() != null) {
            cat.setQty(cat.getQty() + raw.getQty());
        }
        if (raw.getFee() != null) {
            cat.setFee(cat.getFee().add(raw.getFee()));
        }
    }

    /** 某日按来源类型的汇总明细(无则null) */
    private List<MiReconOrgCatItem> toOrgCats(Map<String, Map<String, MiReconOrgCatItem>> catMap, String day) {
        Map<String, MiReconOrgCatItem> cats = catMap.get(day);
        if (cats == null || cats.isEmpty()) {
            return null;
        }
        return sortOrgCats(new ArrayList<>(cats.values()));
    }

    /** 明细弹窗类别排序: 账平/医保负金额/医保正金额在前, 未知类别排后 */
    private List<MiReconOrgCatItem> sortOrgCats(List<MiReconOrgCatItem> cats) {
        cats.sort(Comparator.comparingInt(c -> {
            Integer ord = MiReconOrgCategory.ordinalOf(c.getSourceType());
            return ord == null ? MiReconOrgCategory.values().length : ord;
        }));
        return cats;
    }

    @Override
    public List<MiReconZpDiffItem> queryZpDiff(String insurType, String settleDate) {
        if (MiReconInsurType.ofCode(insurType) == null) {
            throw new IllegalArgumentException("医保类型无效, 应为310/390/3101/3901之一");
        }
        // 先解析校验日期格式
        parseDay(settleDate);
        try {
            return miReconMedInstService.queryZpDiff(insurType.trim(), settleDate.trim());
        } catch (Exception e) {
            log.warn("查询账平差异失败: insurType={} settleDate={}", insurType, settleDate, e);
            return new ArrayList<>();
        }
    }

    /** 某日两表全量合计(不限zp_flag), 校验口径同queryZpDiff, 失败返回空表 */
    @Override
    public List<MiReconTableTotalItem> queryDayTotals(String insurType, String settleDate) {
        if (MiReconInsurType.ofCode(insurType) == null) {
            throw new IllegalArgumentException("医保类型无效, 应为310/390/3101/3901之一");
        }
        parseDay(settleDate);
        try {
            return miReconMedInstService.queryDayTotals(insurType.trim(), settleDate.trim());
        } catch (Exception e) {
            log.warn("查询两表全量合计失败: insurType={} settleDate={}", insurType, settleDate, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<MiReconFileItem> queryRecords() {
        List<MiReconFileItem> result = new ArrayList<>();
        try {
            for (MiReconFileEntity entity : miReconMapper.queryFileList()) {
                MiReconFileItem item = new MiReconFileItem();
                item.setId(entity.getId());
                item.setFileName(entity.getFileName());
                item.setInsurType(entity.getInsurType());
                MiReconInsurType type = MiReconInsurType.ofCode(entity.getInsurType());
                item.setInsurTypeName(entity.getInsurTypeName() != null
                        ? entity.getInsurTypeName()
                        : (type == null ? entity.getInsurType() : type.getChineseName()));
                item.setDataDate(entity.getDataDate());
                item.setRowCount(entity.getRowCount());
                item.setCreateTime(entity.getCreateTime());
                result.add(item);
            }
        } catch (Exception e) {
            log.warn("查询医保对账上传记录失败", e);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateType(Long id, String insurType) {
        if (id == null || MiReconInsurType.ofCode(insurType) == null) {
            throw new IllegalArgumentException("记录id或医保类型无效");
        }
        int affected = miReconMapper.updateFileType(id, insurType.trim(), typeName(insurType));
        try {
            miReconMapper.cascadeDetailType(id, insurType.trim(), typeName(insurType));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "修改失败：目标类型在相同结算日期已存在数据，请先删除冲突类型的上传记录");
        }
        return affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upload(MultipartFile file, String insurType, String dataDate) {
        MiReconInsurType type = MiReconInsurType.ofCode(insurType);
        if (type == null) {
            throw new IllegalArgumentException("医保类型无效, 应为310/390/3101/3901之一");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        Date dataDay = parseDay(dataDate);

        List<MiReconDetailEntity> details;
        try {
            details = MiReconExcelUtil.parse(file.getInputStream(), type.getCode());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("医保对账文件解析失败: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("文件解析失败, 请确认是医保对账xls/xlsx文件");
        }
        if (details.isEmpty()) {
            throw new IllegalArgumentException("文件中没有有效数据行");
        }

        MiReconFileEntity fileEntity = new MiReconFileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setInsurType(type.getCode());
        fileEntity.setInsurTypeName(type.getChineseName());
        fileEntity.setDataDate(dataDay);
        fileEntity.setRowCount(details.size());
        miReconMapper.insertFile(fileEntity);

        List<String> settleDates = new ArrayList<>();
        for (MiReconDetailEntity d : details) {
            settleDates.add(fmt(d.getSettleDate()));
        }
        miReconMapper.deleteDetailByTypeAndDates(type.getCode(), settleDates);
        for (MiReconDetailEntity d : details) {
            d.setFileId(fileEntity.getId());
            d.setInsurTypeName(type.getChineseName());
            miReconMapper.insertDetail(d);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileEntity.getId());
        result.put("fileName", fileEntity.getFileName());
        result.put("insurType", type.getCode());
        result.put("insurTypeName", type.getChineseName());
        result.put("rowCount", details.size());
        return result;
    }

    private MiReconCompareItem baseItem(String settleDate, MiReconInsurType type) {
        MiReconCompareItem item = new MiReconCompareItem();
        item.setSettleDate(parseDay(settleDate));
        item.setInsurType(type.getCode());
        return item;
    }

    /** 默认取当月 */
    private String resolveMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return new SimpleDateFormat("yyyy-MM").format(new Date());
        }
        String m = month.trim();
        if (!m.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("对账月份格式无效, 应为yyyy-MM");
        }
        return m;
    }

    /** 当月最后一天, yyyy-MM-dd */
    private String lastDayOfMonth(String month) {
        int year = Integer.parseInt(month.substring(0, 4));
        int mon = Integer.parseInt(month.substring(5, 7));
        if (mon < 1 || mon > 12) {
            throw new IllegalArgumentException("对账月份格式无效, 应为yyyy-MM");
        }
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, mon - 1, 1);
        int last = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        return month + "-" + String.format("%02d", last);
    }

    private Date parseDay(String yyyyMMdd) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            fmt.setLenient(false);
            return fmt.parse(yyyyMMdd);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式无效, 应为yyyy-MM-dd");
        }
    }

    private String fmt(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String typeName(String insurType) {
        MiReconInsurType t = MiReconInsurType.ofCode(insurType);
        return t == null ? insurType : t.getChineseName();
    }
}
