package com.hoopsql.runtime;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of executing a HoopsQL query
 */
public class QueryResult {
    private final List<Map<String, Object>> rows;
    private final List<String> columnNames;
    private final String resultType;
    
    public QueryResult(List<Map<String, Object>> rows, List<String> columnNames, String resultType) {
        this.rows = rows;
        this.columnNames = columnNames;
        this.resultType = resultType;
    }
    
    public List<Map<String, Object>> getRows() { return rows; }
    public List<String> getColumnNames() { return columnNames; }
    public String getResultType() { return resultType; }
    public int getRowCount() { return rows.size(); }
    
    public boolean isEmpty() { return rows.isEmpty(); }
    
    public void printResults() {
        System.out.println("=== Query Results (" + resultType + ") ===");
        System.out.println("Found " + rows.size() + " row(s)");
        
        if (isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        
        // Print header
        System.out.println();
        for (String column : columnNames) {
            System.out.printf("%-15s ", column);
        }
        System.out.println();
        System.out.println("-".repeat(15 * columnNames.size()));
        
        // Print rows
        for (Map<String, Object> row : rows) {
            for (String column : columnNames) {
                Object value = row.get(column);
                String displayValue = (value != null) ? value.toString() : "null";
                if (displayValue.length() > 14) {
                    displayValue = displayValue.substring(0, 11) + "...";
                }
                System.out.printf("%-15s ", displayValue);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    @Override
    public String toString() {
        return String.format("QueryResult{type='%s', rows=%d, columns=%s}", 
                           resultType, rows.size(), columnNames);
    }
}
