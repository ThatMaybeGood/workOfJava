package com.mergedata.server;

public class test {




//
//package com.mergedata.service;
//
//// ... (导入 ProductDTO, ProductRowMapper, ProcedureCaller)
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//    @Service
//    public class ProductService {
//
//        @Autowired
//        private ProcedureCaller procedureCaller;
//
//        @Autowired
//        private ProductRowMapper productRowMapper; // 假设映射 ProductDTO
//
//        public List<ProductDTO> getProductsFlexible(String date, Integer status) {
//
//            // 1. 构建输入参数 Map
//            Map<String, Object> params = new HashMap<>();
//
//            // 动态添加参数：Map的键必须与存储过程的参数名完全一致
//            if (date != null) {
//                params.put("P_DATE", date);
//            }
//            if (status != null) {
//                params.put("P_STATUS", status);
//            }
//            // 如果存储过程接收了参数但未在 Map 中提供，JDBC驱动会报错，
//            // 除非该参数在存储过程中定义了默认值。
//
//            // 2. 调用通用方法
//            return procedureCaller.executeQueryFlexibleParams(
//                    "GET_RECORDS_FLEXIBLE", // 存储过程名
//                    productRowMapper,       // 映射器
//                    params,                 // 输入参数 Map
//                    "P_CURSOR"              // 游标 OUT 参数名
//            );
//        }
//    }




//
//// 假设这是您的 Service 层代码
//
//// ... (导入和 @Autowired YQStoredProcedureDao)
//
//        // 在 Service 方法中：
//        public List<SomeOtherDTO> getProducts(String categoryId) {
//            // 假设您有一个名为 "GET_PRODUCTS_BY_CATEGORY" 的过程，接收一个 String
//            // 并且您已经定义了 SomeOtherDTORowMapper
//
//            RowMapper<SomeOtherDTO> productMapper = new SomeOtherDTORowMapper();
//
//            return yqStoredProcedureDao.executeQuerySingleStringParam(
//                    "GET_PRODUCTS_BY_CATEGORY", // 过程名
//                    categoryId,                 // 输入参数
//                    productMapper               // 映射器
//            );
//        }
//
//        public List<YQCashRegRecordDTO> getRecords(String date) {
//            // 假设您有一个名为 "GET_RECORDS_BY_DATE" 的过程，接收一个 String
//            RowMapper<YQCashRegRecordDTO> recordMapper = new YQCashRegRecordRowMapper();
//
//            return yqStoredProcedureDao.executeQuerySingleStringParam(
//                    "GET_RECORDS_BY_DATE",
//                    date,
//                    recordMapper
//            );
//        }
}
