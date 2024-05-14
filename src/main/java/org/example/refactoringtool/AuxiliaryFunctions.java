// the implementation of the Auxiliary Functions in Figure 5 in the paper used in the other sections.

package org.example.refactoringtool;

import com.intellij.psi.*;

import java.util.List;
import java.util.Map;

public class AuxiliaryFunctions {
    private JavaElementVisitor visitor;
    public InfluenceGraph graph;

    public AuxiliaryFunctions(JavaElementVisitor visitor, InfluenceGraph graph) {
        this.visitor = visitor;
        this.graph = graph;
    }

    public PsiMethod Lookup(PsiMethodCallExpression expression) {
        PsiMethod method = expression.resolveMethod();
        if (method != null) {
            return method;
        }
        return null;
    }

    public boolean returnTypeDependsOnParams(PsiMethod method) {
        if (method.getReturnType() instanceof PsiClassType) {
            PsiClass returnClass = ((PsiClassType) method.getReturnType()).resolve();
        }
        return true;
    }
}



