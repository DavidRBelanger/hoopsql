package com.hoopsql.compiler;

// Visitor pattern interface for traversing AST nodes
public interface ASTVisitor<T> {
    T visitProgram(ProgramNode node);
    T visitVariableDeclaration(VariableDeclarationNode node);
    T visitQuery(QueryNode node);
    T visitBinaryExpression(BinaryExpressionNode node);
    T visitFieldAccess(FieldAccessNode node);
    T visitLiteral(LiteralNode node);
    T visitIdentifier(IdentifierNode node);
    T visitSelect(SelectNode node);
    T visitOrderBy(OrderByNode node);
    T visitLimit(LimitNode node);
    T visitAggregation(AggregationNode node);
}
