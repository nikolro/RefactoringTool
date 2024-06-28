package org.example.refactoringtool;

import com.intellij.psi.*;

import java.util.Set;

public class ExpressionTargets {
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    public ExpressionTargets(InfluenceGraph influenceGraph, AuxiliaryFunctions auxiliaryFunctions) {
        this.influenceGraph = influenceGraph;
        this.auxiliaryFunctions = auxiliaryFunctions;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                processExpression(expression);
            }

            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                processExpression(expression);
            }

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                super.visitNewExpression(expression);
                processExpression(expression);
            }
        });
    }

    private void processExpression(PsiElement expression) {
        if (expression == null) {
            return;
        }

        Set<PsiElement> affectingNodes = auxiliaryFunctions.nodesAffectingType(expression);
        PsiElement destination = auxiliaryFunctions.destinationNode(expression);

        if (destination != null) {
            for (PsiElement node : affectingNodes) {
                influenceGraph.addEdge(node, destination);
            }
        }
    }
}
