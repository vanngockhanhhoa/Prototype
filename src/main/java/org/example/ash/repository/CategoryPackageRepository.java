//package org.example.ash.repository;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import oracle.jdbc.OracleTypes;
//import org.example.ash.entity.oracle.Category;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.SqlOutParameter;
//import org.springframework.jdbc.core.SqlParameter;
//import org.springframework.jdbc.core.simple.SimpleJdbcCall;
//import org.springframework.stereotype.Repository;
//
//import java.math.BigDecimal;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Repository
//@RequiredArgsConstructor
//public class CategoryPackageRepository {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    private SimpleJdbcCall getCategoryPageCall;
//
//    @PostConstruct
//    void init() {
//        getCategoryPageCall = new SimpleJdbcCall(jdbcTemplate)
//                .withCatalogName("PKG_CATEGORY")
//                .withProcedureName("GET_CATEGORY_PAGE")
//                .declareParameters(
//                        new SqlParameter("p_page_number", Types.NUMERIC),
//                        new SqlOutParameter("p_result", OracleTypes.CURSOR)
//                )
//                .withoutProcedureColumnMetaDataAccess(); // 👈 tránh query metadata
//    }
//
////    public List<Category> getCategoryPage(int pageNumber) {
////        Map<String, Object> result = getCategoryPageCall.execute(Map.of("p_page_number", pageNumber));
////        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("p_result");
////
////        return rows.stream().map(this::mapToCategory).toList();
////    }
//
//    public List<Category> getCategoryPage(int pageNumber) {
//        Map<String, Object> result = getCategoryPageCall.execute(Map.of("p_page_number", pageNumber));
//        Object cursor = result.get("p_result");
//        if (cursor == null) return Collections.emptyList();
//
//        @SuppressWarnings("unchecked")
//        List<Map<String, Object>> rows = (List<Map<String, Object>>) cursor;
//
//        return rows.stream()
//                .map(this::mapToCategory)
//                .collect(Collectors.toList());
//    }
//
//
//    private Category mapToCategory(Map<String, Object> row) {
//        Category c = new Category();
//        c.setId(((Number) row.get("ID")).longValue());
//        c.setName((String) row.get("NAME"));
//        c.setStatusQueue((String) row.get("STATUS_QUEUE"));
//        c.setRetryQueue(row.get("RETRY_QUEUE") != null
//                ? ((BigDecimal) row.get("RETRY_QUEUE")).intValue()
//                : null);
//        return c;
//    }
//}
//
