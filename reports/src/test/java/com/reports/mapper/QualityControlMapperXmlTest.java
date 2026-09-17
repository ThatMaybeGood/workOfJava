package com.reports.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 只解析 mapper XML，确认语句能加载、id 和接口对得上。不需要连数据库。
 */
class QualityControlMapperXmlTest {

    private static final String XML = "mapper/outpatient/QualityControlMapper.xml";

    @Test
    void statementsAreDeclared() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(),
                new UnpooledDataSource("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:orcl", "u", "p")));

        try (InputStream in = Resources.getResourceAsStream(XML)) {
            new XMLMapperBuilder(in, configuration, XML, configuration.getSqlFragments()).parse();
        }

        for (String id : new String[]{"queryMonthly", "queryByMonth", "mergeMaintain"}) {
            assertTrue(configuration.hasStatement(QualityControlMapper.class.getName() + "." + id, false),
                    "缺少语句 " + id);
        }

        // 比率是在SQL里按分子分母现算的，确认片段展开后真的带上了列名
        String sql = configuration.getMappedStatement(QualityControlMapper.class.getName() + ".queryMonthly")
                .getBoundSql(null).getSql();
        assertTrue(sql.contains("emr_usage_rate_num / emr_usage_rate_den"), "比率列没展开");
        assertTrue(sql.contains("adverse_event_rate_num"), "最后一个指标没展开");
        assertFalse(sql.contains("${"), "还有没替换的占位符");
    }
}
