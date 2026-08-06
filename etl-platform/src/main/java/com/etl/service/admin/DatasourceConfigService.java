package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.DatasourceConfig;
import com.etl.mapper.DatasourceConfigMapper;
import com.etl.service.core.DataSourceManager;
import com.etl.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DatasourceConfigService extends ServiceImpl<DatasourceConfigMapper, DatasourceConfig> {

    @Autowired
    private DataSourceManager dataSourceManager;

    public DatasourceConfig getByName(String dsName) {
        return getOne(new QueryWrapper<DatasourceConfig>()
                .eq("ds_name", dsName));
    }

    @Override
    public boolean save(DatasourceConfig entity) {
        if (entity.getPassword() != null && !entity.getPassword().startsWith("ENC(")) {
            entity.setPassword(CryptoUtil.encrypt(entity.getPassword()));
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(DatasourceConfig entity) {
        if (entity.getPassword() != null && !entity.getPassword().startsWith("ENC(")) {
            entity.setPassword(CryptoUtil.encrypt(entity.getPassword()));
        }
        boolean result = super.updateById(entity);
        if (result) {
            dataSourceManager.refreshDataSource(entity.getDsName());
        }
        return result;
    }

    public boolean testConnection(Long id) {
        DatasourceConfig config = getById(id);
        if (config == null) {
            return false;
        }
        config.setPassword(CryptoUtil.decrypt(config.getPassword()));
        return dataSourceManager.testConnection(config);
    }

    public List<DatasourceConfig> listEnabled() {
        return list(new QueryWrapper<DatasourceConfig>()
                .eq("enabled", "Y"));
    }
}
