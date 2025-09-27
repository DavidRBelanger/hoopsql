package com.hoopsql.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    
    static {
        // Keywords
        KEYWORDS.put("get", TokenType.GET);
        KEYWORDS.put("where", TokenType.WHERE);
        KEYWORDS.put("select", TokenType.SELECT);
        KEYWORDS.put("order", TokenType.ORDER);
        KEYWORDS.put("by", TokenType.BY);
        KEYWORDS.put("most", TokenType.MOST);
        KEYWORDS.put("least", TokenType.LEAST);
        KEYWORDS.put("between", TokenType.BETWEEN);
        KEYWORDS.put("in", TokenType.IN);
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("or", TokenType.OR);
        KEYWORDS.put("not", TokenType.NOT);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("limit", TokenType.LIMIT);
        KEYWORDS.put("basic", TokenType.BASIC);
        
        // Aggregation functions
        KEYWORDS.put("avg", TokenType.AVG);
        KEYWORDS.put("sum", TokenType.SUM);
        KEYWORDS.put("count", TokenType.COUNT);
        KEYWORDS.put("min", TokenType.MIN);
        KEYWORDS.put("max", TokenType.MAX);
        
        // Scopes
        KEYWORDS.put("games", TokenType.GAMES);
        KEYWORDS.put("seasons", TokenType.SEASONS);
        KEYWORDS.put("careers", TokenType.CAREERS);
        
        // Entity types
        KEYWORDS.put("Player", TokenType.PLAYER);
        KEYWORDS.put("Team", TokenType.TEAM);
        KEYWORDS.put("Opponent", TokenType.OPPONENT);
        
        // Special fields
        KEYWORDS.put("game_date", TokenType.GAME_DATE);
        KEYWORDS.put("season", TokenType.SEASON);
    }
    
    public Lexer(String input) {
        this.input = input;
    }
    
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        while (!isAtEnd()) {
            Token token = scanToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }
    
    private Token scanToken() {
        char c = advance();
        
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                return null;
            case '\n':
                Token newline = new Token(TokenType.NEWLINE, "\n", line, column - 1);
                line++;
                column = 1;
                return newline;
            case ',': return new Token(TokenType.COMMA, ",", line, column - 1);
            case '(': return new Token(TokenType.LEFT_PAREN, "(", line, column - 1);
            case ')': return new Token(TokenType.RIGHT_PAREN, ")", line, column - 1);
            case '[': return new Token(TokenType.LEFT_BRACKET, "[", line, column - 1);
            case ']': return new Token(TokenType.RIGHT_BRACKET, "]", line, column - 1);
            case '=': return new Token(TokenType.EQUALS, "=", line, column - 1);
            case '>':
                if (match('=')) {
                    return new Token(TokenType.GREATER_EQUAL, ">=", line, column - 2);
                }
                return new Token(TokenType.GREATER_THAN, ">", line, column - 1);
            case '<':
                if (match('=')) {
                    return new Token(TokenType.LESS_EQUAL, "<=", line, column - 2);
                }
                return new Token(TokenType.LESS_THAN, "<", line, column - 1);
            case '!':
                if (match('=')) {
                    return new Token(TokenType.NOT_EQUALS, "!=", line, column - 2);
                }
                throw new RuntimeException("Unexpected character '!' at line " + line + ", column " + (column - 1));
            case '#':
                // Comment - consume until end of line
                return scanComment();
            case '"':
                return scanString();
            default:
                if (isDigit(c)) {
                    return scanNumber();
                }
                if (isAlpha(c)) {
                    return scanIdentifier();
                }
                throw new RuntimeException("Unexpected character '" + c + "' at line " + line + ", column " + (column - 1));
        }
    }
    
    private Token scanComment() {
        int startColumn = column - 1;
        StringBuilder text = new StringBuilder("#");
        
        while (peek() != '\n' && !isAtEnd()) {
            text.append(advance());
        }
        
        return new Token(TokenType.COMMENT, text.toString(), line, startColumn);
    }
    
    private Token scanString() {
        int startColumn = column - 1;
        StringBuilder text = new StringBuilder();
        
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            text.append(advance());
        }
        
        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }
        
        // Consume closing "
        advance();
        
        return new Token(TokenType.STRING, text.toString(), line, startColumn);
    }
    
    private Token scanNumber() {
        int startColumn = column - 1;
        StringBuilder text = new StringBuilder();
        text.append(input.charAt(current - 1));
        
        while (isDigit(peek())) {
            text.append(advance());
        }
        
        // Look for decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            text.append(advance()); // consume '.'
            while (isDigit(peek())) {
                text.append(advance());
            }
        }
        
        return new Token(TokenType.NUMBER, text.toString(), line, startColumn);
    }
    
    private Token scanIdentifier() {
        int startColumn = column - 1;
        StringBuilder text = new StringBuilder();
        text.append(input.charAt(current - 1));
        
        while (isAlphaNumeric(peek()) || peek() == '_') {
            text.append(advance());
        }
        
        String identifier = text.toString();
        
        // Check for field access (e.g., p.points)
        if (peek() == '.') {
            text.append(advance()); // consume '.'
            while (isAlphaNumeric(peek()) || peek() == '_') {
                text.append(advance());
            }
            return new Token(TokenType.FIELD_ACCESS, text.toString(), line, startColumn);
        }
        
        // Check if it's a keyword
        TokenType type = KEYWORDS.get(identifier);
        if (type != null) {
            return new Token(type, identifier, line, startColumn);
        }
        
        // Check if it looks like a date (YYYY-MM-DD or MM-DD-YYYY)
        if (isDatePattern(identifier)) {
            return new Token(TokenType.DATE, identifier, line, startColumn);
        }
        
        // Check if it's a canonical name (contains underscore or starts with capital)
        if (identifier.contains("_") || Character.isUpperCase(identifier.charAt(0))) {
            return new Token(TokenType.CANONICAL_NAME, identifier, line, startColumn);
        }
        
        return new Token(TokenType.IDENTIFIER, identifier, line, startColumn);
    }
    
    private boolean isDatePattern(String text) {
        // Simple check for date patterns like 2016-12-25 or 12-25-2016
        return text.matches("\\d{4}-\\d{2}-\\d{2}") || text.matches("\\d{2}-\\d{2}-\\d{4}");
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(current) != expected) return false;
        
        current++;
        column++;
        return true;
    }
    
    private char advance() {
        column++;
        return input.charAt(current++);
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= input.length()) return '\0';
        return input.charAt(current + 1);
    }
    
    private boolean isAtEnd() {
        return current >= input.length();
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z');
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
