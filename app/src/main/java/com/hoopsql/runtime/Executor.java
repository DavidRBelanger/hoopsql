package com.hoopsql.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hoopsql.compiler.ExecutionPlan;
import com.hoopsql.storage.SQLiteStorage;

public class Executor {
    private final SQLiteStorage storage;
    
    public Executor() {
        this.storage = new SQLiteStorage();
    }
    
    public Executor(SQLiteStorage storage) {
        this.storage = storage;
    }
    
    public QueryResult execute(ExecutionPlan plan) throws SQLException {
        try (Connection connection = storage.connect()) {
            return executeQuery(connection, plan);
        }
    }
    
    private QueryResult executeQuery(Connection connection, ExecutionPlan plan) throws SQLException {
        String sql = plan.getSql();
        Map<String, Object> parameters = plan.getParameters();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters
            setParameters(stmt, parameters);
            
            // Execute query
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return processResultSet(rs, plan.getResultType());
            }
        }
    }
    
    private void setParameters(PreparedStatement stmt, Map<String, Object> parameters) throws SQLException {
        // Parameters are numbered ?1, ?2, ?3, etc.
        for (int i = 1; i <= parameters.size(); i++) {
            String paramKey = "param" + i;
            Object value = parameters.get(paramKey);
            
            if (value == null) {
                stmt.setNull(i, Types.NULL);
            } else if (value instanceof String) {
                stmt.setString(i, (String) value);
            } else if (value instanceof Integer) {
                stmt.setInt(i, (Integer) value);
            } else if (value instanceof Double) {
                stmt.setDouble(i, (Double) value);
            } else if (value instanceof Boolean) {
                stmt.setBoolean(i, (Boolean) value);
            } else {
                stmt.setString(i, value.toString());
            }
        }
    }
    
    private QueryResult processResultSet(java.sql.ResultSet rs, String resultType) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        
        // Get column metadata
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }
        
        // Process rows
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }
        
        return new QueryResult(rows, columnNames, resultType);
    }
}
