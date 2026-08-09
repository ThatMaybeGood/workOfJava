package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.DatasourceConfig;
import com.etl.mapper.DatasourceConfigMapper;
import com.etl.service.core.DataSourceManager;
import com.etl.service.reader.HttpReader;
import com.etl.service.reader.WebServiceReader;
import com.etl.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class DatasourceConfigService extends ServiceImpl<DatasourceConfigMapper, DatasourceConfig> {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private HttpReader httpReader;

    @Autowired
    private WebServiceReader webServiceReader;

    public DatasourceConfig getByName(String dsName) {
        return getOne(new QueryWrapper<DatasourceConfig>()
                .eq("ds_name", dsName));
    }

    @Override
    public boolean save(DatasourceConfig entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()
                && !entity.getPassword().startsWith("ENC(")) {
            entity.setPassword(CryptoUtil.encrypt(entity.getPassword()));
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(DatasourceConfig entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()
                && !entity.getPassword().startsWith("ENC(")) {
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

        String protocol = config.getProtocol();
        if (protocol == null || "JDBC".equalsIgnoreCase(protocol)) {
            // JDBC 数据源
            if (config.getPassword() != null && config.getPassword().startsWith("ENC(")) {
                config.setPassword(CryptoUtil.decrypt(config.getPassword()));
            }
            return dataSourceManager.testConnection(config);
        } else if ("HTTP".equalsIgnoreCase(protocol)) {
            // HTTP 接口
            return httpReader.testConnection(config, dataSourceManager);
        } else if ("SOAP".equalsIgnoreCase(protocol)) {
            // SOAP/WebService
            return webServiceReader.testConnection(config, dataSourceManager);
        } else if ("FILE".equalsIgnoreCase(protocol)) {
            // 文件
            return testFileConnection(config);
        }
        log.warn("未知的数据源协议: {}", protocol);
        return false;
    }

    /**
     * 测试文件连接 —— 检查文件路径是否存在
     */
    private boolean testFileConnection(DatasourceConfig config) {
        try {
            String path = config.getJdbcUrl();
            if (path == null || path.trim().isEmpty()) {
                return false;
            }
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            log.warn("文件连接测试失败: {}", config.getJdbcUrl(), e);
            return false;
        }
    }

    public List<DatasourceConfig> listEnabled() {
        return list(new QueryWrapper<DatasourceConfig>()
                .eq("enabled", "Y"));
    }
}
