package com.reports.service.impl;

import com.reports.dto.request.PreHospOperRequest;
import com.reports.dto.response.cash.prehosp.PreHospUserItem;
import com.reports.entity.PreHospOperEntity;
import com.reports.mapper.PreHospOperMapper;
import com.reports.service.PreHospOperService;
import com.reports.service.PreHospTransHisService;
import com.reports.util.GbkHexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 院前操作员维护服务实现(报表库; 姓名回显/候选搜索经HIS users)
 */
@Slf4j
@Service
public class PreHospOperServiceImpl implements PreHospOperService {

    private final PreHospOperMapper preHospOperMapper;
    private final PreHospTransHisService preHospTransHisService;

    @Autowired
    public PreHospOperServiceImpl(PreHospOperMapper preHospOperMapper,
                                  PreHospTransHisService preHospTransHisService) {
        this.preHospOperMapper = preHospOperMapper;
        this.preHospTransHisService = preHospTransHisService;
    }

    @Override
    public List<PreHospOperEntity> queryList() {
        try {
            List<PreHospOperEntity> list = preHospOperMapper.queryList();
            fillOperName(list);
            return list;
        } catch (Exception e) {
            log.warn("查询院前操作员维护列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<PreHospOperEntity> searchStaff(String keyword, int page, int size) {
        try {
            String kw = keyword == null ? "" : keyword.trim();
            if (kw.isEmpty()) {
                return new ArrayList<>();
            }
            int p = Math.max(1, page);
            int s = Math.max(1, size);
            // 工号/登录ID模糊 + 姓名GBK-hex模糊(US7ASCII), 排除已维护的, 分页
            List<PreHospUserItem> users = preHospTransHisService.searchUsers(
                    kw, GbkHexUtil.encodeHex(kw), (p - 1) * s + 1, p * s);
            List<String> exists = preHospOperMapper.queryAllOperNos();
            List<PreHospOperEntity> result = new ArrayList<>();
            for (PreHospUserItem u : users) {
                if (exists.contains(u.getOperNo())) {
                    continue;
                }
                PreHospOperEntity entity = new PreHospOperEntity();
                entity.setOperNo(u.getOperNo());
                entity.setDbuser(u.getOperDbuser());
                entity.setOperName(nameOf(u));
                result.add(entity);
            }
            return result;
        } catch (Exception e) {
            log.warn("搜索候选人员失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public int countStaff(String keyword) {
        try {
            String kw = keyword == null ? "" : keyword.trim();
            if (kw.isEmpty()) {
                return 0;
            }
            return preHospTransHisService.countUsers(kw, GbkHexUtil.encodeHex(kw));
        } catch (Exception e) {
            log.warn("统计候选人员失败", e);
            return 0;
        }
    }

    /**
     * 手动录入姓名+登录ID保存: 登录ID反查HIS users取USER_ID落库;
     * 登录ID为空/users中不存在/已维护(按USER_ID判重)均返回0
     */
    @Override
    public int addOper(PreHospOperRequest request) {
        String dbuser = request.getDbuser() == null ? "" : request.getDbuser().trim();
        if (dbuser.isEmpty()) {
            return 0;
        }
        try {
            PreHospUserItem user = preHospTransHisService.queryUserByDbuser(dbuser);
            if (user == null || user.getOperNo() == null || user.getOperNo().trim().isEmpty()) {
                return 0;
            }
            String operNo = user.getOperNo().trim();
            if (preHospOperMapper.countByOperNo(operNo) > 0) {
                return 0;
            }
            return preHospOperMapper.insertOper(operNo, dbuser);
        } catch (Exception e) {
            log.warn("添加院前操作员失败", e);
            return 0;
        }
    }

    @Override
    public int removeOper(Long id) {
        return id == null ? 0 : preHospOperMapper.deleteById(id);
    }

    @Override
    public int setValid(Long id, Integer validFlag) {
        if (id == null || validFlag == null || (validFlag != 0 && validFlag != 1)) {
            return 0;
        }
        return preHospOperMapper.updateValid(id, validFlag);
    }

    /** 姓名回显: HIS users批量取, 查不到回退工号 */
    private void fillOperName(List<PreHospOperEntity> list) {
        if (list.isEmpty()) {
            return;
        }
        try {
            List<String> ids = new ArrayList<>();
            for (PreHospOperEntity r : list) {
                if (r.getOperNo() != null && !ids.contains(r.getOperNo())) {
                    ids.add(r.getOperNo());
                }
            }
            Map<String, String> names = new HashMap<>();
            if (!ids.isEmpty()) {
                for (PreHospUserItem u : preHospTransHisService.queryUsersByIds(ids)) {
                    names.putIfAbsent(u.getOperNo(), nameOf(u));
                }
            }
            for (PreHospOperEntity r : list) {
                r.setOperName(names.getOrDefault(r.getOperNo(), r.getOperNo()));
            }
        } catch (Exception e) {
            log.warn("回显操作员姓名失败", e);
            for (PreHospOperEntity r : list) {
                r.setOperName(r.getOperNo());
            }
        }
    }

    private static String nameOf(PreHospUserItem u) {
        String name = GbkHexUtil.decode(u.getOperNameHex());
        return name == null || name.isEmpty() ? u.getOperNo() : name;
    }
}
