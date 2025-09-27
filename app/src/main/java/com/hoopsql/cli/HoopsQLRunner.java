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
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class HoopsQLRunner {
    public static void main(String[] args) {
        if (args.length == 0) {
            // Interactive mode
            runInteractive();
        } else if (args.length == 1) {
            // Single query mode
            String input = args[0];
            Path filePath = Paths.get(input);
            
            if (Files.exists(filePath) && input.endsWith(".hpsql")) {
                // It's a file - read the query from file
                try {
                    String query = Files.readString(filePath);
                    runQuery(query);
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                // It's a direct query string
                runQuery(input);
            }
        } else {
            System.err.println("Usage:");
            System.err.println("  hoopsql                           # Interactive mode");
            System.err.println("  hoopsql \"<query>\"                 # Run single query");
            System.err.println("  hoopsql <file.hpsql>              # Run query from file");
            System.err.println();
            System.err.println("Examples:");
            System.err.println("  hoopsql");
            System.err.println("  hoopsql \"Player p = \\\"Kobe Bryant\\\" get games where p.points >= 40\"");
            System.err.println("  hoopsql my_query.hpsql");
            System.exit(1);
        }
    }
    
    private static void runInteractive() {
        System.out.println("=== HoopsQL Interactive Shell ===");
        System.out.println("Type your HoopsQL queries below. Type 'exit' to quit, 'help' for examples.");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("hoopsql> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            } else if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            } else if (input.isEmpty()) {
                continue;
            }
            
            runQuery(input);
            System.out.println();
        }
        
        scanner.close();
    }
    
    private static void showHelp() {
        System.out.println("HoopsQL Query Examples:");
        System.out.println();
        System.out.println("Basic queries:");
        System.out.println("  Player p = \"Kobe Bryant\" get games where p.points >= 40");
        System.out.println("  Player p = \"LeBron James\" get games where p.points >= 30 limit 5");
        System.out.println();
        System.out.println("Relational queries:");
        System.out.println("  Player p = \"Kobe Bryant\" Player q = p.opponent get games where p.points >= 30 and q.points >= 30");
        System.out.println("  Player p = \"Stephen Curry\" Team t = p.team get games where p.points >= 40 and t.name = \"Warriors\"");
        System.out.println("  Player p = \"Kobe Bryant\" get games where p.points >= 40 and p.opponent = \"Celtics\"");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  help  - Show this help");
        System.out.println("  exit  - Quit the shell");
        System.out.println();
    }
    
    private static void runQuery(String query) {
        try {
            Lexer lexer = new Lexer(query);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            var program = parser.parse();
            
            Planner planner = new Planner();
            ExecutionPlan plan = planner.createExecutionPlan(program);
            
            executeAndShowResults(plan);
            
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg.contains("Unexpected token") && query.contains("=") && !query.contains("\"")) {
                System.out.println("Error: Player names must be in quotes. Try: Player p = \"Kobe Bryant\"");
            } else {
                System.out.println("Error: " + msg);
            }
        }
    }
    
    private static void executeAndShowResults(ExecutionPlan plan) {
        try {
            SQLiteStorage storage = new SQLiteStorage();
            try (Connection conn = storage.connect();
                 PreparedStatement stmt = conn.prepareStatement(plan.getSql())) {
                
                // Set parameters in correct order (if any)
                if (!plan.getParameters().isEmpty()) {
                    java.util.List<String> sortedKeys = new java.util.ArrayList<>(plan.getParameters().keySet());
                    sortedKeys.sort((a, b) -> {
                        int numA = Integer.parseInt(a.substring(5));
                        int numB = Integer.parseInt(b.substring(5));
                        return Integer.compare(numA, numB);
                    });
                    
                    int paramIndex = 1;
                    for (String key : sortedKeys) {
                        Object value = plan.getParameters().get(key);
                        stmt.setObject(paramIndex++, value);
                    }
                }
                
                // First, execute a COUNT query to get total results
                int totalGames = 0;
                if (!"averages".equals(plan.getResultType())) {
                    String countSql = plan.getSql().replaceFirst("SELECT.*?FROM", "SELECT COUNT(*) FROM");
                    // Remove ORDER BY clause for counting
                    if (countSql.contains("ORDER BY")) {
                        countSql = countSql.substring(0, countSql.indexOf("ORDER BY"));
                    }
                    // Remove LIMIT clause for counting
                    if (countSql.contains("LIMIT")) {
                        countSql = countSql.substring(0, countSql.indexOf("LIMIT"));
                    }
                    
                    try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                        // Set parameters for count query (same as main query)
                        if (!plan.getParameters().isEmpty()) {
                            java.util.List<String> sortedKeys = new java.util.ArrayList<>(plan.getParameters().keySet());
                            sortedKeys.sort((a, b) -> {
                                int numA = Integer.parseInt(a.substring(5));
                                int numB = Integer.parseInt(b.substring(5));
                                return Integer.compare(numA, numB);
                            });
                            
                            int paramIndex = 1;
                            for (String key : sortedKeys) {
                                Object value = plan.getParameters().get(key);
                                countStmt.setObject(paramIndex++, value);
                            }
                        }
                        
                        try (ResultSet countRs = countStmt.executeQuery()) {
                            if (countRs.next()) {
                                totalGames = countRs.getInt(1);
                            }
                        }
                    } catch (SQLException e) {
                        // If count fails, we'll just show results without total
                        totalGames = -1;
                    }
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("\nResults:");
                    System.out.println("========");
                    
                    // Check if this is an averaging query
                    java.sql.ResultSetMetaData metaData = rs.getMetaData();
                    boolean isAveraging = "averages".equals(plan.getResultType());
                    
                    if (isAveraging) {
                        // For averaging queries, the games count is in the result
                        if (rs.next()) {
                            System.out.println("Season Averages:");
                            try {
                                System.out.printf("  Points: %.1f\n", rs.getDouble("avg_points"));
                                System.out.printf("  Rebounds: %.1f\n", rs.getDouble("avg_rebounds"));
                                System.out.printf("  Assists: %.1f\n", rs.getDouble("avg_assists"));
                                System.out.printf("  Steals: %.1f\n", rs.getDouble("avg_steals"));
                                System.out.printf("  Blocks: %.1f\n", rs.getDouble("avg_blocks"));
                                System.out.printf("  Turnovers: %.1f\n", rs.getDouble("avg_turnovers"));
                                System.out.printf("  3PM: %.1f\n", rs.getDouble("avg_threePointersMade"));
                                System.out.printf("  Games: %d\n", rs.getInt("games_count"));
                            } catch (SQLException e) {
                                System.out.println("Error displaying averages: " + e.getMessage());
                            }
                        } else {
                            System.out.println("No data found for the specified criteria.");
                        }
                    } else {
                        // Display individual game results
                        if (totalGames >= 0) {
                            System.out.printf("(%d games)\n", totalGames);
                            System.out.println("");
                        }
                        
                        int count = 0;
                        while (rs.next() && count < 10) {
                            count++;
                            
                            // Game header info
                            String gameDate = rs.getString("game_date");
                            String playerName = rs.getString("player_name");
                            String team = rs.getString("team");
                            String opponent = rs.getString("opponent");
                            String result = rs.getString("win").equals("1") ? "W" : "L";
                            
                            System.out.printf("\n%d. %s - %s (%s vs %s) - %s\n", 
                                count, gameDate, playerName, team, opponent, result);
                            
                            // Build stats line with proper spacing
                            StringBuilder stats = new StringBuilder("   Stats: ");
                            
                            // Core stats (always show)
                            stats.append(String.format("%.0f pts", rs.getDouble("points")));
                            stats.append(String.format(", %.0f reb", rs.getDouble("rebounds")));
                            stats.append(String.format(", %.0f ast", rs.getDouble("assists")));
                            
                            // Show queried stats (in addition to core stats)
                            try {
                                int columnCount = metaData.getColumnCount();
                                
                                for (int i = 1; i <= columnCount; i++) {
                                    String columnName = metaData.getColumnName(i).toLowerCase();
                                    
                                    // Skip core stats (already shown) and non-stat columns
                                    if (columnName.equals("points") || columnName.equals("rebounds") || 
                                        columnName.equals("assists") || columnName.equals("game_date") || 
                                        columnName.equals("player_name") || columnName.equals("team") || 
                                        columnName.equals("opponent") || columnName.equals("win") || 
                                        columnName.equals("margin")) {
                                        continue;
                                    }
                                    
                                    // Get the value and show it
                                    try {
                                        String value = rs.getString(columnName);
                                        if (value != null && !value.isEmpty()) {
                                            String label = getStatLabel(columnName);
                                            if (label != null) {
                                                double numValue = Double.parseDouble(value);
                                                // Always show important stats, even if zero (especially for ORDER BY fields)
                                                stats.append(String.format(", %.0f %s", numValue, label));
                                            }
                                        }
                                    } catch (SQLException | NumberFormatException ignored2) {
                                        // Skip if can't read this column or parse as number
                                    }
                                }
                            } catch (SQLException ignored) {
                                // Some stats might not be in the result set - that's okay
                            }
                            
                            System.out.println(stats.toString());
                        }
                        
                        if (count == 0) {
                            System.out.println("\nNo results found");
                        } else {
                            // Check if there are more results
                            if (rs.next()) {
                                System.out.println("\n... (showing first " + count + " results)");
                            }
                        }
                    }
                }
                
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("Execution Error: " + e.getMessage());
        }
    }
    
    private static String getStatLabel(String columnName) {
        return switch (columnName.toLowerCase()) {
            case "steals" -> "stl";
            case "blocks" -> "blk";
            case "turnovers" -> "to";
            case "threepointersmode", "threepointersmade" -> "3PM";
            case "threepointersattempted" -> "3PA";
            case "fieldgoalsmade" -> "FGM";
            case "fieldgoalsattempted" -> "FGA";
            case "freethrowsmade" -> "FTM";
            case "freethrowsattempted" -> "FTA";
            case "foulspersonal" -> "PF";
            case "plusminuspoints" -> "+/-";
            case "reboundsdefensive" -> "DREB";
            case "reboundsoffensive" -> "OREB";
            case "numminutes" -> "MIN";
            default -> null; // Don't show unknown stats
        };
    }
}
