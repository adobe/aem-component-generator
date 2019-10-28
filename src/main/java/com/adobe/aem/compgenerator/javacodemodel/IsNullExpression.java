package com.adobe.aem.compgenerator.javacodemodel;

import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

final class IsNullExpression extends JExpressionImpl {
    private final JFieldVar variable;
    private final boolean isNullType;

    public IsNullExpression(JFieldVar variable, boolean isNullType) {
        this.variable = variable;
        this.isNullType = isNullType;
    }

    @Override
    public void generate(JFormatter f) {
        if (this.isNullType) {
            f.g(this.variable).p(" == null");
        } else {
            f.g(this.variable).p(" != null");
        }
    }
}