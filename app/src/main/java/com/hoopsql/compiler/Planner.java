package com.hoopsql.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Planner implements ASTVisitor<String> {
    private final SchemaInfo schema;
    private PlanningContext context;
    private StringBuilder sqlBuilder;
    private Map<String, Object> parameters;
    private int parameterCount;
    
    public Planner() {
        this.schema = new SchemaInfo();
    }
    
    public ExecutionPlan createExecutionPlan(ProgramNode program) {
        // Initialize planning context
        this.context = new PlanningContext(new HashMap<>(), schema);
        this.sqlBuilder = new StringBuilder();
        this.parameters = new HashMap<>();
        this.parameterCount = 0;
        
        // Process variable declarations
        for (DeclarationNode decl : program.getDeclarations()) {
            decl.accept(this);
        }
        
        // Process main query
        String resultType = "unknown";
        if (program.getQuery() != null) {
            String scope = program.getQuery().getScope();
            if ("avg(games)".equals(scope)) {
                resultType = "averages";
            } else {
                resultType = scope;
            }
            
            // Actually visit the query to generate SQL!
            program.getQuery().accept(this);
        }
        
        return new ExecutionPlan(sqlBuilder.toString(), parameters, resultType);
    }
    
    @Override
    public String visitProgram(ProgramNode node) {
        // This won't be called directly since we handle it in createExecutionPlan
        return "";
    }
    
    @Override
    public String visitVariableDeclaration(VariableDeclarationNode node) {
        VariableBinding binding;
        
        if (node.getBinding() != null) {
            if (node.getBinding() instanceof LiteralNode) {
                // Standard literal binding: Player p = "LeBron James"
                Object boundValue = ((LiteralNode) node.getBinding()).getValue();
                binding = new VariableBinding(
                    node.getEntityType(), 
                    node.getVariableName(), 
                    boundValue
                );
            } else if (node.getBinding() instanceof FieldAccessNode) {
                // Relational binding: Player q = p.opponent
                FieldAccessNode fieldAccess = (FieldAccessNode) node.getBinding();
                String relationToVariable = fieldAccess.getVariableName();
                String relationshipType = fieldAccess.getFieldName();
                
                // Validate that the referenced variable exists
                VariableBinding referencedVar = context.getVariable(relationToVariable);
                if (referencedVar == null) {
                    throw new RuntimeException("Undefined variable in relational binding: " + relationToVariable);
                }
                
                // Validate relationship type based on entity types
                validateRelationalBinding(node.getEntityType(), referencedVar.getEntityType(), relationshipType);
                
                binding = new VariableBinding(
                    node.getEntityType(), 
                    node.getVariableName(), 
                    relationToVariable, 
                    relationshipType
                );
            } else {
                // Unbound variable declaration: Player p
                binding = new VariableBinding(
                    node.getEntityType(), 
                    node.getVariableName(), 
                    (Object) null
                );
            }
        } else {
            // Unbound variable declaration: Player p
            binding = new VariableBinding(
                node.getEntityType(), 
                node.getVariableName(), 
                (Object) null
            );
        }
        
        context.addVariable(node.getVariableName(), binding);
        return "";
    }
    
    private void validateRelationalBinding(String targetEntityType, String sourceEntityType, String relationshipType) {
        // Validate that the relationship makes sense based on HoopsSQL spec
        switch (relationshipType) {
            case "opponent":
                if (!"Player".equals(sourceEntityType)) {
                    throw new RuntimeException("'opponent' relationship is only valid from Player variables, not " + sourceEntityType);
                }
                if (!"Player".equals(targetEntityType) && !"Team".equals(targetEntityType) && !"Opponent".equals(targetEntityType)) {
                    throw new RuntimeException("'opponent' relationship can only bind to Player, Team, or Opponent variables, not " + targetEntityType);
                }
                break;
            case "team":
                if (!"Player".equals(sourceEntityType)) {
                    throw new RuntimeException("'team' relationship is only valid from Player variables, not " + sourceEntityType);
                }
                if (!"Team".equals(targetEntityType)) {
                    throw new RuntimeException("'team' relationship can only bind to Team variables, not " + targetEntityType);
                }
                break;
            case "played":
                if (!"Player".equals(sourceEntityType)) {
                    throw new RuntimeException("'played' relationship is only valid from Player variables, not " + sourceEntityType);
                }
                if (!"Player".equals(targetEntityType)) {
                    throw new RuntimeException("'played' relationship can only bind to Player variables, not " + targetEntityType);
                }
                break;
            default:
                throw new RuntimeException("Unknown relationship type: " + relationshipType + 
                    ". Valid relationships are: opponent, team, played");
        }
    }
    
    @Override
    public String visitQuery(QueryNode node) {
        // Support games scope and avg(games) scope
        String scope = node.getScope();
        if ("games".equals(scope)) {
            buildGamesQuery(node);
        } else if ("avg(games)".equals(scope)) {
            buildAvgGamesQuery(node);
        } else {
            throw new RuntimeException("Only 'games' and 'avg(games)' scopes are supported. Got: '" + scope + "'");
        }
        return sqlBuilder.toString();
    }
    
    private void buildGamesQuery(QueryNode node) {
        buildSelectClause(node);
        buildFromClause(node);
        buildWhereClause(node);
        
        // Build ORDER BY clause
        if (node.getOrderByClause() != null) {
            node.getOrderByClause().accept(this);
        }
        
        // Build LIMIT clause
        if (node.getLimitClause() != null) {
            node.getLimitClause().accept(this);
        }
    }
    
    private void buildAvgGamesQuery(QueryNode node) {
        // Build aggregated query that returns averages instead of individual games
        buildAvgSelectClause(node);
        buildFromClause(node);
        buildWhereClause(node);
        
        // No LIMIT needed for aggregated results
    }
    
    private void buildSelectClause(QueryNode node) {
        sqlBuilder.append("SELECT ");
        
        if (node.getSelectClause() != null) {
            // User specified fields
            SelectNode select = node.getSelectClause();
            List<String> sqlFields = new ArrayList<>();
            
            for (String field : select.getFields()) {
                // Map field names to actual database columns
                if ("date".equals(field)) {
                    sqlFields.add("games.gameDate AS date");
                } else if ("opponent".equals(field)) {
                    sqlFields.add("games.awayteamName AS opponent");
                } else if ("points".equals(field)) {
                    sqlFields.add("player_statistics.points AS points");
                } else if ("rebounds".equals(field)) {
                    sqlFields.add("player_statistics.reboundsTotal AS rebounds");
                } else if ("assists".equals(field)) {
                    sqlFields.add("player_statistics.assists AS assists");
                } else if ("name".equals(field)) {
                    // Could be player or team name - context dependent
                    sqlFields.add("TRIM(players.firstName || ' ' || players.lastName) AS name");
                } else {
                    // Default mapping - try player_statistics first
                    sqlFields.add("player_statistics." + field);
                }
            }
            
            sqlBuilder.append(String.join(", ", sqlFields));
        } else {
            // Default selection based on scope per spec
            if ("games".equals(node.getScope())) {
                // Per spec: game_date, season, home_or_away, team, opponent, win, margin
                // Plus player headline fields (points, rebounds, assists) if Player is declared
                List<String> defaultFields = new ArrayList<>();
                
                defaultFields.add("games.gameDate AS game_date");
                
                // Add player name if Player variables exist
                boolean hasPlayerVar = context.getVariables().values().stream()
                    .anyMatch(b -> "Player".equals(b.getEntityType()));
                    
                if (hasPlayerVar) {
                    // Use appropriate alias based on query complexity
                    boolean hasRelationalBindings = context.getVariables().values().stream()
                        .anyMatch(VariableBinding::isRelational);
                    long playerCount = context.getVariables().values().stream()
                        .filter(b -> "Player".equals(b.getEntityType()))
                        .count();
                    
                    // For multi-player queries, use the first player variable as the primary display
                    String primaryPlayerVar = null;
                    if (hasRelationalBindings || playerCount > 1) {
                        // Find the first player variable to use as the primary display
                        primaryPlayerVar = context.getVariables().values().stream()
                            .filter(b -> "Player".equals(b.getEntityType()))
                            .map(VariableBinding::getVariableName)
                            .findFirst()
                            .orElse("p");
                    }
                    
                    String statsAlias = (hasRelationalBindings || playerCount > 1) ? primaryPlayerVar + "_stats" : "player_statistics";
                    
                    defaultFields.add("TRIM(" + statsAlias + ".firstName || ' ' || " + statsAlias + ".lastName) AS player_name");
                    defaultFields.add(statsAlias + ".playerteamName AS team");
                    defaultFields.add(statsAlias + ".opponentteamName AS opponent");
                    defaultFields.add(statsAlias + ".win AS win");
                    defaultFields.add("ABS(CAST(games.homeScore AS INTEGER) - CAST(games.awayScore AS INTEGER)) AS margin");                    // Add ALL AVAILABLE STATS - be comprehensive!
                    Set<String> referencedFields = extractReferencedFieldsFromQuery(node);
                    
                    // Core stats - always include
                    defaultFields.add(statsAlias + ".points AS points");
                    defaultFields.add(statsAlias + ".reboundsTotal AS rebounds");
                    defaultFields.add(statsAlias + ".assists AS assists");
                    
                    // Add ALL other stats that are referenced or if no specific fields mentioned
                    Set<String> allPossibleStats = Set.of(
                        "steals", "blocks", "turnovers", "minutes", 
                        "reboundsDefensive", "reboundsOffensive",
                        "fieldGoalsAttempted", "fieldGoalsMade", "fieldGoalsPercentage",
                        "threePointersAttempted", "threePointersMade", "threePointersPercentage", 
                        "freeThrowsAttempted", "freeThrowsMade", "freeThrowsPercentage",
                        "foulsPersonal", "plusMinusPoints"
                    );
                    
                    for (String stat : allPossibleStats) {
                        if (referencedFields.contains(stat) || referencedFields.isEmpty()) {
                            String columnName = schema.getColumnName("Player", stat);
                            String alias = stat;
                            
                            // Handle special column mappings
                            if ("minutes".equals(stat)) {
                                defaultFields.add(statsAlias + ".numMinutes AS " + alias);
                            } else if (columnName.contains("||")) {
                                // Skip complex expressions for now
                                continue;
                            } else {
                                defaultFields.add(statsAlias + "." + columnName + " AS " + alias);
                            }
                        }
                    }
                } else {
                    // No player variables - just basic game info
                    defaultFields.add("games.hometeamName AS home_team");
                    defaultFields.add("games.awayteamName AS away_team");
                    defaultFields.add("games.homeScore AS home_score");
                    defaultFields.add("games.awayScore AS away_score");
                }
                
                sqlBuilder.append(String.join(", ", defaultFields));
            } else {
                sqlBuilder.append("*");
            }
        }
    }
    
    private void buildAvgSelectClause(QueryNode node) {
        sqlBuilder.append("SELECT ");
        
        // For avg(games), we want to show averages of all numeric stats
        List<String> avgFields = new ArrayList<>();
        
        // Add average of common stats
        avgFields.add("AVG(CAST(player_statistics.points AS REAL)) AS avg_points");
        avgFields.add("AVG(CAST(player_statistics.reboundsTotal AS REAL)) AS avg_rebounds"); 
        avgFields.add("AVG(CAST(player_statistics.assists AS REAL)) AS avg_assists");
        avgFields.add("AVG(CAST(player_statistics.steals AS REAL)) AS avg_steals");
        avgFields.add("AVG(CAST(player_statistics.blocks AS REAL)) AS avg_blocks");
        avgFields.add("AVG(CAST(player_statistics.turnovers AS REAL)) AS avg_turnovers");
        avgFields.add("AVG(CAST(player_statistics.threePointersMade AS REAL)) AS avg_threePointersMade");
        avgFields.add("AVG(CAST(player_statistics.fieldGoalsMade AS REAL)) AS avg_fieldGoalsMade");
        avgFields.add("AVG(CAST(player_statistics.freeThrowsMade AS REAL)) AS avg_freeThrowsMade");
        
        // Add count of games
        avgFields.add("COUNT(*) AS games_count");
        
        sqlBuilder.append(String.join(", ", avgFields));
    }
    
    private void buildFromClause(QueryNode node) {
        sqlBuilder.append(" FROM ");
        
        // Handle aggregation scopes
        String effectiveScope = node.getScope();
        if ("avg(games)".equals(effectiveScope)) {
            effectiveScope = "games";
        }
        
        String mainTable = schema.getMainTable(effectiveScope);
        sqlBuilder.append(mainTable);
        
        // For relational bindings, we need separate joins for each player variable
        if ("games".equals(effectiveScope) || "avg(games)".equals(node.getScope())) {
            Set<String> playerVariables = new HashSet<>();
            Set<String> teamVariables = new HashSet<>();
            
            for (VariableBinding binding : context.getVariables().values()) {
                if ("Player".equals(binding.getEntityType()) || "Opponent".equals(binding.getEntityType())) {
                    // Both Player and Opponent variables need player_statistics JOINs
                    playerVariables.add(binding.getVariableName());
                } else if ("Team".equals(binding.getEntityType())) {
                    teamVariables.add(binding.getVariableName());
                }
            }
            
            // Check if we have relational bindings - if so, use variable-specific aliases
            boolean hasRelationalBindings = context.getVariables().values().stream()
                .anyMatch(VariableBinding::isRelational);
            
            if (hasRelationalBindings || playerVariables.size() > 1) {
                // Use variable-specific aliases for complex queries
                for (String playerVar : playerVariables) {
                    sqlBuilder.append(" LEFT JOIN player_statistics AS ").append(playerVar).append("_stats")
                              .append(" ON games.gameId = ").append(playerVar).append("_stats.gameId");
                }
                
                // Add joins for team variables (but skip if relationally bound to players)
                for (String teamVar : teamVariables) {
                    VariableBinding teamBinding = context.getVariable(teamVar);
                    if (teamBinding != null && teamBinding.isRelational()) {
                        if ("team".equals(teamBinding.getRelationshipType()) || "opponent".equals(teamBinding.getRelationshipType())) {
                            // Skip join for Team t = p.team and Opponent o = p.opponent - we'll use player_statistics team names
                            continue;
                        }
                    }
                    sqlBuilder.append(" LEFT JOIN team_histories AS ").append(teamVar).append("_team")
                              .append(" ON games.hometeamId = ").append(teamVar).append("_team.teamId OR games.awayteamId = ").append(teamVar).append("_team.teamId");
                }
            } else {
                // Use simple aliases for backward compatibility with existing tests
                Set<String> entityTypes = new HashSet<>();
                for (VariableBinding binding : context.getVariables().values()) {
                    entityTypes.add(binding.getEntityType());
                }
                
                List<String> joins = schema.getRequiredJoins(effectiveScope, new ArrayList<>(entityTypes));
                for (String join : joins) {
                    sqlBuilder.append(" ").append(join);
                }
            }
        } else {
            // For non-games scopes, use basic joins
            Set<String> entityTypes = new HashSet<>();
            for (VariableBinding binding : context.getVariables().values()) {
                entityTypes.add(binding.getEntityType());
            }
            
            // Use effective scope for aggregation scopes like avg(games)
            String scopeForJoins = "avg(games)".equals(node.getScope()) ? "games" : node.getScope();
            List<String> joins = schema.getRequiredJoins(scopeForJoins, new ArrayList<>(entityTypes));
            for (String join : joins) {
                sqlBuilder.append(" ").append(join);
            }
        }
    }
    
    private void buildWhereClause(QueryNode node) {
        List<String> conditions = new ArrayList<>();
        
        // Add variable binding conditions
        boolean hasRelationalBindings = context.getVariables().values().stream()
            .anyMatch(VariableBinding::isRelational);
        long playerCount = context.getVariables().values().stream()
            .filter(b -> "Player".equals(b.getEntityType()))
            .count();
            
        for (VariableBinding binding : context.getVariables().values()) {
            if (binding.isBound() && "Player".equals(binding.getEntityType())) {
                parameterCount++;
                String paramName = "param" + parameterCount;
                
                // Use specific player alias for multi-player or relational queries
                if (hasRelationalBindings || playerCount > 1) {
                    String statsAlias = binding.getVariableName() + "_stats";
                    conditions.add("TRIM(" + statsAlias + ".firstName || ' ' || " + statsAlias + ".lastName) = ?" + parameterCount);
                } else {
                    // Use simple alias for backward compatibility
                    conditions.add("TRIM(player_statistics.firstName || ' ' || player_statistics.lastName) = ?" + parameterCount);
                }
                parameters.put(paramName, binding.getBoundValue());
            } else if (binding.isRelational()) {
                // Handle relational bindings like Player q = p.opponent
                String relationalCondition = buildRelationalCondition(binding);
                if (relationalCondition != null && !relationalCondition.isEmpty()) {
                    conditions.add(relationalCondition);
                }
            }
        }
        
        // AUTOMATIC SAME-GAME CONSTRAINT: If multiple players are declared, automatically ensure they're in the same games
        if (playerCount > 1) {
            List<String> playerVars = context.getVariables().values().stream()
                .filter(b -> "Player".equals(b.getEntityType()))
                .map(VariableBinding::getVariableName)
                .sorted() // Ensure consistent ordering
                .toList();
                
            // Add same-game constraints between all pairs of players
            for (int i = 0; i < playerVars.size() - 1; i++) {
                String var1 = playerVars.get(i);
                String var2 = playerVars.get(i + 1);
                conditions.add(var1 + "_stats.gameId = " + var2 + "_stats.gameId");
            }
        }
        
        // Add WHERE clause conditions
        for (ExpressionNode condition : node.getWhereConditions()) {
            conditions.add(condition.accept(this));
        }
        
        if (!conditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(String.join(" AND ", conditions));
        }
    }
    
    private String buildRelationalCondition(VariableBinding binding) {
        // Generate SQL condition for relational bindings like Player q = p.opponent
        String relationshipType = binding.getRelationshipType();
        String relationToVariable = binding.getRelationToVariable();
        
        // Get the referenced variable binding
        VariableBinding referencedVar = context.getVariable(relationToVariable);
        if (referencedVar == null) {
            throw new RuntimeException("Referenced variable not found: " + relationToVariable);
        }
        
        switch (relationshipType) {
            case "opponent":
                // For both Player q = p.opponent and Opponent o = p.opponent
                // We need opponents to be in the same game but on different teams
                if (("Player".equals(binding.getEntityType()) || "Opponent".equals(binding.getEntityType())) 
                    && "Player".equals(referencedVar.getEntityType())) {
                    // Both are player entities - they must be in the same game but different teams
                    return buildOpponentPlayerCondition(binding.getVariableName(), relationToVariable);
                } else if ("Team".equals(binding.getEntityType())) {
                    // Team represents the opposing team to player p
                    return buildOpponentTeamCondition(binding.getVariableName(), relationToVariable);
                }
                break;
            case "team":
                // For Team t = p.team, link the player's team in their game
                if ("Team".equals(binding.getEntityType()) && "Player".equals(referencedVar.getEntityType())) {
                    return buildPlayerTeamCondition(binding.getVariableName(), relationToVariable);
                }
                break;
            case "played":
                // For Player q = p.played (same game), this is now handled automatically by multi-player detection
                // No explicit condition needed since same-game constraints are added automatically
                return null;
        }
        
        return null; // No condition needed for unhandled cases
    }
    
    private String buildOpponentPlayerCondition(String playerVar1, String playerVar2) {
        // Both players are in the same game but on different teams
        // gameId condition is redundant since tables are already JOINed on gameId
        return String.format("(%s_stats.playerteamName != %s_stats.playerteamName)", 
                           playerVar1, playerVar2);
    }
    
    private String buildOpponentTeamCondition(String teamVar, String playerVar) {
        // The team variable represents the opponent of the player
        // For now, implement a simplified version 
        // TODO: Implement proper logic to determine opponent team based on which team player was on
        return String.format("(%s_team.teamId = games.hometeamId OR %s_team.teamId = games.awayteamId)",
                           teamVar, teamVar);
    }
    
    private String buildPlayerTeamCondition(String teamVar, String playerVar) {
        // Team t = p.team means the team variable is bound to the player's team in each game
        // We don't need complex joins - just ensure any team conditions match the player's team
        // The actual team name checking will be done in the WHERE conditions 
        return "1=1"; // This condition is always true - the real constraint comes from WHERE conditions
    }
    

    
    @Override
    public String visitBinaryExpression(BinaryExpressionNode node) {
        // Handle season comparisons (season = "1996-97")
        if ("=".equals(node.getOperator()) && 
            node.getLeft() instanceof IdentifierNode && 
            "season".equals(((IdentifierNode) node.getLeft()).getName()) &&
            node.getRight() instanceof LiteralNode) {
            
            LiteralNode seasonLiteral = (LiteralNode) node.getRight();
            String seasonName = seasonLiteral.getValue().toString();
            
            // Import SeasonMapper here (we'll add this import at the top of the file)
            com.hoopsql.util.SeasonMapper.SeasonDates dates = com.hoopsql.util.SeasonMapper.getSeasonDates(seasonName);
            if (dates != null) {
                // Create parameter placeholders for start and end dates
                parameterCount++;
                String startParamName = "param" + parameterCount;
                parameters.put(startParamName, dates.getStartDate());
                String startParamPlaceholder = "?" + parameterCount;
                
                parameterCount++;
                String endParamName = "param" + parameterCount;
                parameters.put(endParamName, dates.getEndDate());
                String endParamPlaceholder = "?" + parameterCount;
                
                return "games.gameDate >= " + startParamPlaceholder + " AND games.gameDate <= " + endParamPlaceholder;
            } else {
                throw new RuntimeException("Unknown season: " + seasonName);
            }
        }
        
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);
        
        // Handle player variable comparisons like p != q
        if (("=".equals(node.getOperator()) || "!=".equals(node.getOperator())) && 
            node.getLeft() instanceof IdentifierNode && node.getRight() instanceof IdentifierNode) {
            
            IdentifierNode leftId = (IdentifierNode) node.getLeft();
            IdentifierNode rightId = (IdentifierNode) node.getRight();
            
            VariableBinding leftBinding = context.getVariable(leftId.getName());
            VariableBinding rightBinding = context.getVariable(rightId.getName());
            
            if (leftBinding != null && rightBinding != null && 
                "Player".equals(leftBinding.getEntityType()) && "Player".equals(rightBinding.getEntityType())) {
                
                // Use personId for player comparisons with proper aliases
                String leftAlias = leftId.getName() + "_stats";
                String rightAlias = rightId.getName() + "_stats";
                String sqlOperator = "=".equals(node.getOperator()) ? "=" : "!=";
                return leftAlias + ".personId " + sqlOperator + " " + rightAlias + ".personId";
            }
        }
        
        // Handle field access comparisons like p1.played = p2.played (which should be automatic now)
        if (("=".equals(node.getOperator()) || "!=".equals(node.getOperator())) && 
            node.getLeft() instanceof FieldAccessNode && node.getRight() instanceof FieldAccessNode) {
            
            FieldAccessNode leftField = (FieldAccessNode) node.getLeft();
            FieldAccessNode rightField = (FieldAccessNode) node.getRight();
            
            if ("played".equals(leftField.getFieldName()) && "played".equals(rightField.getFieldName())) {
                // p1.played = p2.played is automatically handled by multi-player same-game constraints
                // Just return a condition that's always true since the automatic constraints handle this
                return "1=1";
            }
        }
        
        // Map HoopsSQL operators to SQL operators
        String sqlOperator = switch (node.getOperator()) {
            case ">=" -> ">=";
            case ">" -> ">";
            case "<=" -> "<=";
            case "<" -> "<";
            case "=" -> "=";
            case "!=" -> "!=";
            default -> node.getOperator();
        };
        
        // Handle numeric comparisons - cast left side to REAL if it's a stat column
        if (left.contains("points") || left.contains("assists") || left.contains("rebounds") || 
            left.contains("steals") || left.contains("blocks") || left.contains("turnovers") || 
            left.contains("threePointersMade") || left.contains("threePointersAttempted") ||
            left.contains("fieldGoalsMade") || left.contains("fieldGoalsAttempted") ||
            left.contains("freeThrowsMade") || left.contains("freeThrowsAttempted") ||
            left.contains("foulsPersonal") || left.contains("plusMinusPoints") ||
            left.contains("numMinutes") || left.contains("reboundsDefensive") || left.contains("reboundsOffensive")) {
            left = "CAST(" + left + " AS REAL)";
        }
        
        return left + " " + sqlOperator + " " + right;
    }
    
    @Override
    public String visitFieldAccess(FieldAccessNode node) {
        VariableBinding binding = context.getVariable(node.getVariableName());
        if (binding == null) {
            throw new RuntimeException("Undefined variable: " + node.getVariableName());
        }
        
        // Handle special case: p.played means "player participated in game" 
        if ("played".equals(node.getFieldName()) && "Player".equals(binding.getEntityType())) {
            // For p.played, we just return a condition that's always true since the JOIN already ensures the player played
            // The real constraint is that the player_statistics record exists (via JOIN)
            return "1=1"; // Always true - the JOIN constraint does the work
        }
        
        String columnName = schema.getColumnName(binding.getEntityType(), node.getFieldName());
        
        // Return the appropriate SQL column reference based on actual database structure
        if ("Player".equals(binding.getEntityType())) {
            // Check if we need variable-specific aliases  
            boolean hasRelationalBindings = context.getVariables().values().stream()
                .anyMatch(VariableBinding::isRelational);
            long playerCount = context.getVariables().values().stream()
                .filter(b -> "Player".equals(b.getEntityType()))
                .count();
                
            if (hasRelationalBindings || playerCount > 1) {
                // Use variable-specific aliases for complex queries
                String varName = node.getVariableName();
                if ("name".equals(node.getFieldName())) {
                    // Player names require joining players table
                    return "TRIM(" + varName + "_player.firstName || ' ' || " + varName + "_player.lastName)";
                } else {
                    // Stats fields are in player_statistics
                    return varName + "_stats." + columnName;
                }
            } else {
                // Use simple aliases for backward compatibility
                if ("name".equals(node.getFieldName())) {
                    return "TRIM(players.firstName || ' ' || players.lastName)";
                } else {
                    return "player_statistics." + columnName;
                }
            }
        } else if ("Team".equals(binding.getEntityType())) {
            // If Team is relationally bound (Team t = p.team), use player's team name
            if (binding.isRelational() && "team".equals(binding.getRelationshipType())) {
                String playerVar = binding.getRelationToVariable();
                String statsAlias = playerVar + "_stats";
                if ("name".equals(node.getFieldName())) {
                    return statsAlias + ".playerteamName";
                } else {
                    return statsAlias + ".playerteam" + node.getFieldName();
                }
            } else {
                // Team fields are directly in games table as home team
                return "games." + columnName;
            }
        } else if ("Opponent".equals(binding.getEntityType())) {
            // Opponent is a Player entity - an opposing player, not a team
            if (binding.isRelational() && "opponent".equals(binding.getRelationshipType())) {
                String playerVar = binding.getRelationToVariable();
                String playerAlias = playerVar + "_stats";
                String opponentAlias = binding.getVariableName() + "_stats";
                
                if ("name".equals(node.getFieldName())) {
                    // Opponent player name - need to join opponent player stats
                    return "TRIM(" + opponentAlias + ".firstName || ' ' || " + opponentAlias + ".lastName)";
                } else if ("team".equals(node.getFieldName())) {
                    // Opponent's team name
                    return opponentAlias + ".playerteamName";
                } else {
                    // Other opponent player stats
                    return opponentAlias + "." + columnName;
                }
            }
            // For other opponent fields, map to games table
            return "games." + columnName;
        }
        
        return columnName;
    }
    
    @Override
    public String visitLiteral(LiteralNode node) {
        parameterCount++;
        String paramName = "param" + parameterCount;
        parameters.put(paramName, node.getValue());
        return "?" + parameterCount;
    }
    
    @Override
    public String visitIdentifier(IdentifierNode node) {
        // Handle standalone identifiers
        String name = node.getName();
        
        // Map special field references to SQL columns
        if ("game_date".equals(name)) {
            return "games.gameDate";
        }
        
        return name;
    }
    
    @Override
    public String visitSelect(SelectNode node) {
        // This is handled in buildSelectClause
        return "";
    }
    
    @Override
    public String visitOrderBy(OrderByNode node) {
        sqlBuilder.append(" ORDER BY ");
        
        // Map field name to actual database column with proper casting for numeric sorts
        String columnName = node.getField();
        if ("points".equals(columnName)) {
            columnName = "CAST(player_statistics.points AS INTEGER)";
        } else if ("rebounds".equals(columnName)) {
            columnName = "CAST(player_statistics.reboundsTotal AS INTEGER)";
        } else if ("assists".equals(columnName)) {
            columnName = "CAST(player_statistics.assists AS INTEGER)";
        } else if ("minutes".equals(columnName)) {
            columnName = "CAST(player_statistics.numMinutes AS REAL)";
        } else if ("name".equals(columnName)) {
            columnName = "TRIM(player_statistics.firstName || ' ' || player_statistics.lastName)";
        } else {
            // Default mapping - try to cast as integer for numeric fields
            columnName = "CAST(player_statistics." + columnName + " AS INTEGER)";
        }
        
        sqlBuilder.append(columnName);
        sqlBuilder.append(node.isAscending() ? " ASC" : " DESC");
        
        return "";
    }
    
    @Override
    public String visitLimit(LimitNode node) {
        sqlBuilder.append(" LIMIT ").append(node.getCount());
        return "";
    }
    
    @Override
    public String visitAggregation(AggregationNode node) {
        // Handle aggregation functions
        String function = node.getFunction().toUpperCase();
        String expression = node.getExpression().accept(this);
        return function + "(" + expression + ")";
    }

    // Extract fields referenced in WHERE conditions for smart column selection
    private Set<String> extractReferencedFieldsFromQuery(QueryNode node) {
        Set<String> fields = new HashSet<>();
        
        // Extract fields from WHERE conditions
        if (node.getWhereConditions() != null) {
            for (ExpressionNode condition : node.getWhereConditions()) {
                extractFieldsFromExpression(condition, fields);
            }
        }
        
        // Extract fields from ORDER BY clause
        if (node.getOrderByClause() != null) {
            fields.add(node.getOrderByClause().getField());
        }
        
        return fields;
    }
    
    private void extractFieldsFromExpression(ExpressionNode expr, Set<String> fields) {
        if (expr instanceof BinaryExpressionNode) {
            BinaryExpressionNode binaryExpr = (BinaryExpressionNode) expr;
            extractFieldsFromExpression(binaryExpr.getLeft(), fields);
            extractFieldsFromExpression(binaryExpr.getRight(), fields);
        } else if (expr instanceof FieldAccessNode) {
            FieldAccessNode fieldAccess = (FieldAccessNode) expr;
            fields.add(fieldAccess.getFieldName());
        }
    }
    

}
