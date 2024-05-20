//the implementation of the Analysis from section 4.2

package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.Set;

public class FlowDependenciesfromQualifiers {
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    public FlowDependenciesfromQualifiers(InfluenceGraph influenceGraph, AuxiliaryFunctions auxiliaryFunctions) {
        this.influenceGraph = influenceGraph;
        this.auxiliaryFunctions = auxiliaryFunctions;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiVariable qualifierDecl= auxiliaryFunctions.varDecl(expression);
                if(qualifierDecl != null)
                {
                    PsiMethod method = auxiliaryFunctions.Lookup(expression);
                    if (method != null) {
                        for (PsiParameter parameter : method.getParameterList().getParameters()) {
                            influenceGraph.addEdge(qualifierDecl, parameter);
                        }
                    }

                }
            }
        });
    }
}
