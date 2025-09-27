package com.hoopsql.compiler;

public enum TokenType {
    // Keywords
    GET, WHERE, SELECT, ORDER, BY, MOST, LEAST, BETWEEN, IN, 
    AND, OR, NOT, TRUE, FALSE, LIMIT, BASIC,
    
    // Aggregation functions
    AVG, SUM, COUNT, MIN, MAX,
    
    // Scopes
    GAMES, SEASONS, CAREERS,
    
    // Entity types
    PLAYER, TEAM, OPPONENT,
    
    // Special fields
    GAME_DATE, SEASON,
    
    // Operators
    EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, 
    GREATER_EQUAL, LESS_EQUAL,
    
    // Punctuation
    COMMA, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
    
    // Literals
    NUMBER, STRING, CANONICAL_NAME, DATE,
    
    // Identifiers
    IDENTIFIER, FIELD_ACCESS, // e.g., p.points
    
    // Special
    NEWLINE, COMMENT, EOF
}
