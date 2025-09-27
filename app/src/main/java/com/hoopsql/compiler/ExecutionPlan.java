package com.hoopsql.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents an execution plan for a HoopsQL query
public class ExecutionPlan {
    private final String sql;
    private final Map<String, Object> parameters;
    private final String resultType; // "games", "seasons", "careers"
    private final Set<String> referencedFields; // Fields mentioned in WHERE conditions
    private final Set<String> playerVariables; // Player variable names
    
    public ExecutionPlan(String sql, Map<String, Object> parameters, String resultType) {
        this.sql = sql;
        this.parameters = parameters;
        this.resultType = resultType;
        this.referencedFields = new HashSet<>();
        this.playerVariables = new HashSet<>();
    }
    
    public ExecutionPlan(String sql, Map<String, Object> parameters, String resultType, 
                        Set<String> referencedFields, Set<String> playerVariables) {
        this.sql = sql;
        this.parameters = parameters;
        this.resultType = resultType;
        this.referencedFields = referencedFields;
        this.playerVariables = playerVariables;
    }
    
    public String getSql() { return sql; }
    public Map<String, Object> getParameters() { return parameters; }
    public String getResultType() { return resultType; }
    public Set<String> getReferencedFields() { return referencedFields; }
    public Set<String> getPlayerVariables() { return playerVariables; }
    
    @Override
    public String toString() {
        return String.format("ExecutionPlan{sql='%s', params=%s, type='%s'}", 
                           sql, parameters, resultType);
    }
}

// Context for planning - tracks variable bindings and metadata
class PlanningContext {
    private final Map<String, VariableBinding> variables;
    private final SchemaInfo schema;
    
    public PlanningContext(Map<String, VariableBinding> variables, SchemaInfo schema) {
        this.variables = variables;
        this.schema = schema;
    }
    
    public Map<String, VariableBinding> getVariables() { return variables; }
    public SchemaInfo getSchema() { return schema; }
    
    public VariableBinding getVariable(String name) {
        return variables.get(name);
    }
    
    public void addVariable(String name, VariableBinding binding) {
        variables.put(name, binding);
    }
}

// Represents a bound variable (Player p = LeBron_James)
class VariableBinding {
    private final String entityType; // Player, Team, Opponent
    private final String variableName;
    private final Object boundValue; // null if unbound
    private final String relationToVariable; // For relational bindings like "Player q = p.opponent"
    private final String relationshipType; // "opponent", "team", "game", etc.
    
    // Constructor for literal bindings (Player p = "LeBron James")
    public VariableBinding(String entityType, String variableName, Object boundValue) {
        this.entityType = entityType;
        this.variableName = variableName;
        this.boundValue = boundValue;
        this.relationToVariable = null;
        this.relationshipType = null;
    }
    
    // Constructor for relational bindings (Player q = p.opponent)
    public VariableBinding(String entityType, String variableName, String relationToVariable, String relationshipType) {
        this.entityType = entityType;
        this.variableName = variableName;
        this.boundValue = null;
        this.relationToVariable = relationToVariable;
        this.relationshipType = relationshipType;
    }
    
    public String getEntityType() { return entityType; }
    public String getVariableName() { return variableName; }
    public Object getBoundValue() { return boundValue; }
    public boolean isBound() { return boundValue != null; }
    public boolean isRelational() { return relationToVariable != null; }
    public String getRelationToVariable() { return relationToVariable; }
    public String getRelationshipType() { return relationshipType; }
}

// Schema information for mapping HoopsQL concepts to SQL
class SchemaInfo {
    // Map HoopsQL field names to SQL column names
    public String getColumnName(String entityType, String fieldName) {
        // Player fields - ALL STATS AVAILABLE
        if ("Player".equals(entityType)) {
            return switch (fieldName) {
                case "name" -> "firstName || ' ' || lastName";
                case "points" -> "points";
                case "rebounds" -> "reboundsTotal";
                case "assists" -> "assists";
                case "steals" -> "steals";
                case "blocks" -> "blocks";
                case "turnovers" -> "turnovers";
                case "minutes" -> "numMinutes";
                case "game" -> "gameId";
                case "team" -> "playerteamName";
                case "opponent" -> "opponentteamName";  // opponent team name!
                
                // ALL THE OTHER STATS FROM THE DATABASE
                case "reboundsDefensive" -> "reboundsDefensive";
                case "reboundsOffensive" -> "reboundsOffensive";
                case "fieldGoalsAttempted" -> "fieldGoalsAttempted";
                case "fieldGoalsMade" -> "fieldGoalsMade";
                case "fieldGoalsPercentage" -> "fieldGoalsPercentage";
                case "threePointersAttempted" -> "threePointersAttempted";
                case "threePointersMade" -> "threePointersMade";
                case "threePointersPercentage" -> "threePointersPercentage";
                case "freeThrowsAttempted" -> "freeThrowsAttempted";
                case "freeThrowsMade" -> "freeThrowsMade";
                case "freeThrowsPercentage" -> "freeThrowsPercentage";
                case "foulsPersonal" -> "foulsPersonal";
                case "plusMinusPoints" -> "plusMinusPoints";
                
                // Shorter aliases for common stats
                case "fg" -> "fieldGoalsMade";
                case "fga" -> "fieldGoalsAttempted";
                case "fgpct" -> "fieldGoalsPercentage";
                case "threept" -> "threePointersMade";
                case "threepta" -> "threePointersAttempted";
                case "threeptpct" -> "threePointersPercentage";
                case "ft" -> "freeThrowsMade";
                case "fta" -> "freeThrowsAttempted";
                case "ftpct" -> "freeThrowsPercentage";
                case "oreb" -> "reboundsOffensive";
                case "dreb" -> "reboundsDefensive";
                case "to" -> "turnovers";
                case "pf" -> "foulsPersonal";
                case "plusminus" -> "plusMinusPoints";
                
                default -> fieldName;
            };
        }
        
        // Team fields - based on actual games table structure
        if ("Team".equals(entityType)) {
            return switch (fieldName) {
                case "name" -> "hometeamName";
                case "city" -> "hometeamCity";
                case "score" -> "homeScore";
                default -> fieldName;
            };
        }
        
        // Opponent fields - away team in games table
        if ("Opponent".equals(entityType)) {
            return switch (fieldName) {
                case "name" -> "awayteamName";
                case "city" -> "awayteamCity";
                case "score" -> "awayScore";
                default -> fieldName;
            };
        }
        
        return fieldName;
    }
    
    // Map HoopsQL scopes to SQL tables/joins
    public String getMainTable(String scope) {
        return switch (scope) {
            case "games" -> "games";
            case "seasons" -> "player_statistics"; // Aggregated by season
            case "careers" -> "player_statistics"; // Aggregated across all seasons
            default -> scope;
        };
    }
    
    // Get required joins for entity types
    public List<String> getRequiredJoins(String scope, List<String> entityTypes) {
        List<String> joins = new java.util.ArrayList<>();
        
        if ("games".equals(scope)) {
            for (String entityType : entityTypes) {
                if ("Player".equals(entityType)) {
                    // Join player statistics - it has firstName/lastName columns already
                    joins.add("LEFT JOIN player_statistics ON games.gameId = player_statistics.gameId");
                } else if ("Team".equals(entityType)) {
                    // Teams are referenced directly in games table, but we can join team_histories for more info
                    joins.add("LEFT JOIN team_histories AS home_team ON games.hometeamId = home_team.teamId");
                } else if ("Opponent".equals(entityType)) {
                    // Opponent is the away team
                    joins.add("LEFT JOIN team_histories AS away_team ON games.awayteamId = away_team.teamId");
                }
            }
        }
        
        return joins;
    }
}
