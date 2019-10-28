package com.adobe.aem.compgenerator.javacodemodel;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

final class TernaryOperator extends JExpressionImpl {

    JExpression condition;
    JExpression ifTrue;
    JExpression ifFalse;

    public TernaryOperator(JExpression condition, JExpression ifTrue, JExpression ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public void generate(JFormatter f) {
        f.g(condition).p(" ? ").g(ifTrue).p(" : ").g(ifFalse);
    }

}