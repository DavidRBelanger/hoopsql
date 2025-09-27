package com.hoopsql.compiler;

import java.util.List;

public class DebugOrderBy {
    public static void main(String[] args) {
        String query = "Player p = \"Stephen Curry\" get games where p.assists >= 10 order by most points";
        
        System.out.println("=== Debugging ORDER BY Issue ===");
        System.out.println("Query: " + query);
        
        try {
            // Parse the query
            Lexer lexer = new Lexer(query);
            List<Token> tokens = lexer.tokenize();
            System.out.println("Tokens: " + tokens.stream().map(t -> t.getType() + ":" + t.getText()).toList());
            
            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parse();
            System.out.println("Parsed successfully!");
            
            // Create execution plan
            Planner planner = new Planner();
            ExecutionPlan plan = planner.createExecutionPlan(program);
            
            String sql = plan.getSql();
            System.out.println("\nGenerated SQL:");
            System.out.println(sql);
            
            // Check for ORDER BY clause
            if (sql.contains("ORDER BY")) {
                String[] parts = sql.split("ORDER BY");
                System.out.println("\nORDER BY clause: ORDER BY" + parts[1]);
            } else {
                System.out.println("\n❌ ERROR: No ORDER BY clause found in SQL!");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
