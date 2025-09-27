package com.hoopsql.compiler;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public ProgramNode parse() {
        List<DeclarationNode> declarations = new ArrayList<>();
        
        skipWhitespace();
        
        // Parse variable declarations
        while (!isAtEnd() && isDeclaration()) {
            declarations.add(parseDeclaration());
            skipWhitespace();
        }
        
        // Parse query (optional)
        QueryNode query = null;
        if (!isAtEnd() && check(TokenType.GET)) {
            query = parseQuery();
        }
        
        return new ProgramNode(declarations, query);
    }
    
    private boolean isDeclaration() {
        skipWhitespace();
        return check(TokenType.PLAYER) || check(TokenType.TEAM) || check(TokenType.OPPONENT);
    }
    
    private DeclarationNode parseDeclaration() {
        // Match the entity type first
        match(TokenType.PLAYER, TokenType.TEAM, TokenType.OPPONENT);
        String entityType = previous().getText();
        
        skipWhitespace();
        
        if (!check(TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected variable name after " + entityType);
        }
        String variableName = advance().getText();
        
        skipWhitespace();
        
        ExpressionNode binding = null;
        if (match(TokenType.EQUALS)) {
            skipWhitespace();
            binding = parseExpression();
        }
        
        return new VariableDeclarationNode(entityType, variableName, binding);
    }
    
    private QueryNode parseQuery() {
        if (!match(TokenType.GET)) {
            throw new RuntimeException("Expected 'get' to start query");
        }
        
        skipWhitespace();
        
        // Parse scope (games, seasons, careers, or avg(games))
        String scope;
        if (check(TokenType.AVG)) {
            advance(); // consume 'avg'
            if (!match(TokenType.LEFT_PAREN)) {
                throw new RuntimeException("Expected '(' after 'avg'");
            }
            if (!check(TokenType.GAMES)) {
                throw new RuntimeException("Expected 'games' after 'avg('");
            }
            advance(); // consume 'games'
            if (!match(TokenType.RIGHT_PAREN)) {
                throw new RuntimeException("Expected ')' after 'games'");
            }
            scope = "avg(games)";
        } else if (check(TokenType.GAMES) || check(TokenType.SEASONS) || check(TokenType.CAREERS) || check(TokenType.IDENTIFIER)) {
            scope = advance().getText();
        } else {
            throw new RuntimeException("Expected scope after 'get'");
        }
        
        skipWhitespace();
        
        // Parse WHERE conditions
        List<ExpressionNode> whereConditions = new ArrayList<>();
        if (match(TokenType.WHERE)) {
            skipWhitespace();
            whereConditions.add(parseExpression());
            
            // Handle multiple conditions with 'and'
            while (true) {
                skipWhitespace();
                if (!match(TokenType.AND)) break;
                skipWhitespace();
                whereConditions.add(parseExpression());
            }
        }
        
        // Parse optional clauses
        SelectNode selectClause = null;
        OrderByNode orderByClause = null;
        LimitNode limitClause = null;
        
        while (!isAtEnd()) {
            skipWhitespace();
            if (match(TokenType.SELECT)) {
                selectClause = parseSelect();
            } else if (match(TokenType.ORDER)) {
                // Handle "ORDER BY most/least field" syntax
                skipWhitespace();
                if (!match(TokenType.BY)) {
                    throw new RuntimeException("Expected 'by' after 'order'");
                }
                skipWhitespace();
                orderByClause = parseOrderBy();
            } else if (check(TokenType.MOST) || check(TokenType.LEAST)) {
                // Handle shorthand "most/least field" syntax
                orderByClause = parseOrderBy();
            } else if (match(TokenType.LIMIT)) {
                limitClause = parseLimit();
            } else {
                break;
            }
            skipWhitespace();
        }
        
        return new QueryNode(scope, whereConditions, selectClause, orderByClause, limitClause);
    }
    
    private SelectNode parseSelect() {
        List<String> fields = new ArrayList<>();
        boolean basicMode = true;
        
        skipWhitespace();
        
        // Parse field list
        if (!check(TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected field name after 'select'");
        }
        fields.add(advance().getText()); // First field
        
        while (true) {
            skipWhitespace();
            if (!match(TokenType.COMMA)) break;
            skipWhitespace();
            if (!check(TokenType.IDENTIFIER)) {
                throw new RuntimeException("Expected field name after comma");
            }
            fields.add(advance().getText());
        }
        
        skipWhitespace();
        
        // Check for basic=false
        if (match(TokenType.BASIC)) {
            skipWhitespace();
            if (!match(TokenType.EQUALS)) {
                throw new RuntimeException("Expected '=' after 'basic'");
            }
            skipWhitespace();
            if (match(TokenType.FALSE)) {
                basicMode = false;
            } else if (!match(TokenType.TRUE)) {
                throw new RuntimeException("Expected 'true' or 'false' after 'basic='");
            }
        }
        
        return new SelectNode(fields, basicMode);
    }
    
    private OrderByNode parseOrderBy() {
        boolean ascending;
        if (match(TokenType.MOST)) {
            ascending = false;
        } else if (match(TokenType.LEAST)) {
            ascending = true;
        } else {
            throw new RuntimeException("Expected 'most' or 'least'");
        }
        
        skipWhitespace();
        
        if (!check(TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected field name after order keyword");
        }
        String field = advance().getText();
        
        return new OrderByNode(field, ascending);
    }
    
    private LimitNode parseLimit() {
        skipWhitespace();
        
        if (!check(TokenType.NUMBER)) {
            throw new RuntimeException("Expected number after 'limit'");
        }
        int count = Integer.parseInt(advance().getText());
        return new LimitNode(count);
    }
    
    private ExpressionNode parseExpression() {
        return parseComparison();
    }
    
    private ExpressionNode parseComparison() {
        ExpressionNode left = parsePrimary();
        
        if (match(TokenType.GREATER_THAN, TokenType.GREATER_EQUAL, 
                  TokenType.LESS_THAN, TokenType.LESS_EQUAL,
                  TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            String operator = previous().getText();
            ExpressionNode right = parsePrimary();
            return new BinaryExpressionNode(left, operator, right);
        }
        
        return left;
    }
    
    private ExpressionNode parsePrimary() {
        // Field access (p.points, t.wins)
        if (check(TokenType.FIELD_ACCESS)) {
            String fieldAccess = advance().getText();
            String[] parts = fieldAccess.split("\\.");
            return new FieldAccessNode(parts[0], parts[1]);
        }
        
        // Numbers
        if (check(TokenType.NUMBER)) {
            String value = advance().getText();
            return new LiteralNode(parseNumber(value), "number");
        }
        
        // Strings
        if (check(TokenType.STRING)) {
            String value = advance().getText();
            // Lexer already removed quotes, so use value directly
            return new LiteralNode(value, "string");
        }
        
        // Canonical names (LeBron_James)
        if (check(TokenType.CANONICAL_NAME)) {
            String value = advance().getText();
            return new LiteralNode(value, "canonical_name");
        }
        
        // Dates
        if (check(TokenType.DATE)) {
            String value = advance().getText();
            return new LiteralNode(value, "date");
        }
        
        // Booleans
        if (check(TokenType.TRUE)) {
            advance();
            return new LiteralNode(true, "boolean");
        }
        if (check(TokenType.FALSE)) {
            advance();
            return new LiteralNode(false, "boolean");
        }
        
        // Game date field reference
        if (check(TokenType.GAME_DATE)) {
            advance();
            return new IdentifierNode("game_date");
        }
        
        // Season field reference (season = "1996-97")
        if (check(TokenType.SEASON)) {
            advance();
            return new IdentifierNode("season");
        }
        
        // Identifiers
        if (check(TokenType.IDENTIFIER)) {
            String name = advance().getText();
            return new IdentifierNode(name);
        }
        
        throw new RuntimeException("Unexpected token: " + peek().getText());
    }
    
    private Object parseNumber(String value) {
        if (value.contains(".")) {
            return Double.parseDouble(value);
        } else {
            return Integer.parseInt(value);
        }
    }
    
    // Utility methods
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }
    
    private Token peek() {
        if (current >= tokens.size()) {
            return new Token(TokenType.EOF, "", tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).getLine(), 0);
        }
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private void skipWhitespace() {
        while (!isAtEnd() && (check(TokenType.NEWLINE) || check(TokenType.COMMENT))) {
            advance();
        }
    }
}
