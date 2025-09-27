package com.hoopsql.compiler;

public class AggregationNode extends ExpressionNode {
    private final String function; // avg, sum, count, min, max
    private final ExpressionNode expression;
    
    public AggregationNode(String function, ExpressionNode expression) {
        this.function = function;
        this.expression = expression;
    }
    
    public String getFunction() {
        return function;
    }
    
    public ExpressionNode getExpression() {
        return expression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAggregation(this);
    }
}
