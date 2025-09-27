package com.hoopsql.cli;

import com.hoopsql.compiler.*;
import com.hoopsql.storage.SQLiteStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class HoopsQLExecutor {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -cp target/classes com.hoopsql.cli.HoopsQLExecutor <query-file.hpsql>");
            System.exit(1);
        }
        
        String filename = args[0];
        Path filePath = Paths.get(filename);
        
        if (!Files.exists(filePath)) {
            System.err.println("Error: File '" + filename + "' not found");
            System.exit(1);
        }
        
        try {
            String query = Files.readString(filePath);
            executeQuery(query, filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void executeQuery(String query, String filename) {
        System.out.println("HoopsQL - Running " + filename);
        System.out.println("Query: " + query.trim());
        System.out.println("");
        
        try {
            // Compile the query
            Lexer lexer = new Lexer(query);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            var program = parser.parse();
            
            Planner planner = new Planner();
            ExecutionPlan plan = planner.createExecutionPlan(program);
            
            // Use the generated SQL but show intelligent results
            String intelligentSql = plan.getSql();
            
            // Execute against database
            SQLiteStorage storage = new SQLiteStorage();
            try (Connection conn = storage.connect();
                 PreparedStatement stmt = conn.prepareStatement(intelligentSql)) {
                
                // Set parameters
                int paramIndex = 1;
                for (Map.Entry<String, Object> entry : plan.getParameters().entrySet()) {
                    stmt.setObject(paramIndex++, entry.getValue());
                }
                
                // Execute and show results
                try (ResultSet rs = stmt.executeQuery()) {
                    showResults(rs);
                }
                
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
                System.out.println("\nGenerated SQL (for debugging):");
                System.out.println(intelligentSql);
                System.out.println("\nParameters: " + plan.getParameters());
            }
            
        } catch (Exception e) {
            System.out.println("Query Error: " + e.getMessage());
        }
    }
    
    private static void showResults(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Print headers
        System.out.print("┌");
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("─".repeat(20) + (i < columnCount ? "┬" : "┐"));
        }
        System.out.println();
        
        System.out.print("│");
        for (int i = 1; i <= columnCount; i++) {
            String header = metaData.getColumnName(i);
            System.out.printf(" %-18s │", header.length() > 18 ? header.substring(0, 18) : header);
        }
        System.out.println();
        
        System.out.print("├");
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("─".repeat(20) + (i < columnCount ? "┼" : "┤"));
        }
        System.out.println();
        
        // Print data rows
        int rowCount = 0;
        while (rs.next() && rowCount < 20) { // Limit to 20 rows for readability
            System.out.print("│");
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                if (value == null) value = "NULL";
                System.out.printf(" %-18s │", value.length() > 18 ? value.substring(0, 18) : value);
            }
            System.out.println();
            rowCount++;
        }
        
        System.out.print("└");
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("─".repeat(20) + (i < columnCount ? "┴" : "┘"));
        }
        System.out.println();
        
        // Check if there were more rows
        if (rs.next()) {
            System.out.println("... (showing first 20 rows only)");
        }
        
        System.out.println("\n Query completed successfully! (" + rowCount + (rowCount == 20 ? "+" : "") + " rows)");
    }
}
