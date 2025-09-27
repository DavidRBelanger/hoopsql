package com.hoopsql.compiler;

public abstract class ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}

// Root node for a HoopsQL program
class ProgramNode extends ASTNode {
    private final java.util.List<DeclarationNode> declarations;
    private final QueryNode query;
    
    public ProgramNode(java.util.List<DeclarationNode> declarations, QueryNode query) {
        this.declarations = declarations;
        this.query = query;
    }
    
    public java.util.List<DeclarationNode> getDeclarations() { return declarations; }
    public QueryNode getQuery() { return query; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}

// Variable declarations (Player p, Team t = Lakers)
abstract class DeclarationNode extends ASTNode {}

class VariableDeclarationNode extends DeclarationNode {
    private final String entityType; // Player, Team, Opponent
    private final String variableName;
    private final ExpressionNode binding; // null if unbound
    
    public VariableDeclarationNode(String entityType, String variableName, ExpressionNode binding) {
        this.entityType = entityType;
        this.variableName = variableName;
        this.binding = binding;
    }
    
    public String getEntityType() { return entityType; }
    public String getVariableName() { return variableName; }
    public ExpressionNode getBinding() { return binding; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}

// Main query (get games where ...)
class QueryNode extends ASTNode {
    private final String scope; // games, seasons, careers
    private final java.util.List<ExpressionNode> whereConditions;
    private final SelectNode selectClause; // null if no select
    private final OrderByNode orderByClause; // null if no order by
    private final LimitNode limitClause; // null if no limit
    
    public QueryNode(String scope, java.util.List<ExpressionNode> whereConditions, 
                     SelectNode selectClause, OrderByNode orderByClause, LimitNode limitClause) {
        this.scope = scope;
        this.whereConditions = whereConditions;
        this.selectClause = selectClause;
        this.orderByClause = orderByClause;
        this.limitClause = limitClause;
    }
    
    public String getScope() { return scope; }
    public java.util.List<ExpressionNode> getWhereConditions() { return whereConditions; }
    public SelectNode getSelectClause() { return selectClause; }
    public OrderByNode getOrderByClause() { return orderByClause; }
    public LimitNode getLimitClause() { return limitClause; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitQuery(this);
    }
}

// Expressions (comparisons, field access, etc.)
abstract class ExpressionNode extends ASTNode {}

class BinaryExpressionNode extends ExpressionNode {
    private final ExpressionNode left;
    private final String operator; // >=, =, !=, etc.
    private final ExpressionNode right;
    
    public BinaryExpressionNode(ExpressionNode left, String operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public ExpressionNode getLeft() { return left; }
    public String getOperator() { return operator; }
    public ExpressionNode getRight() { return right; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}

class FieldAccessNode extends ExpressionNode {
    private final String variableName;
    private final String fieldName;
    
    public FieldAccessNode(String variableName, String fieldName) {
        this.variableName = variableName;
        this.fieldName = fieldName;
    }
    
    public String getVariableName() { return variableName; }
    public String getFieldName() { return fieldName; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFieldAccess(this);
    }
}

class LiteralNode extends ExpressionNode {
    private final Object value;
    private final String type; // number, string, canonical_name, date, boolean
    
    public LiteralNode(Object value, String type) {
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() { return value; }
    public String getType() { return type; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}

class IdentifierNode extends ExpressionNode {
    private final String name;
    
    public IdentifierNode(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
}

// SELECT clause
class SelectNode extends ASTNode {
    private final java.util.List<String> fields;
    private final boolean basicMode; // true unless basic=false
    
    public SelectNode(java.util.List<String> fields, boolean basicMode) {
        this.fields = fields;
        this.basicMode = basicMode;
    }
    
    public java.util.List<String> getFields() { return fields; }
    public boolean isBasicMode() { return basicMode; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSelect(this);
    }
}

// ORDER BY clause
class OrderByNode extends ASTNode {
    private final String field;
    private final boolean ascending; // false for "most", true for "least"
    
    public OrderByNode(String field, boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }
    
    public String getField() { return field; }
    public boolean isAscending() { return ascending; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitOrderBy(this);
    }
}

// LIMIT clause
class LimitNode extends ASTNode {
    private final int count;
    
    public LimitNode(int count) {
        this.count = count;
    }
    
    public int getCount() { return count; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLimit(this);
    }
}
