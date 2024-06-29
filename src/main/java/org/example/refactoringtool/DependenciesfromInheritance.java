//the implementation of the Analysis from section 4.4
package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.Set;

public class DependenciesfromInheritance {
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    public DependenciesfromInheritance(InfluenceGraph influenceGraph, AuxiliaryFunctions auxiliaryFunctions) {
        this.influenceGraph = influenceGraph;
        this.auxiliaryFunctions = auxiliaryFunctions;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                Set<PsiMethod> hierarchyMethods = auxiliaryFunctions.hierarchyMethods(method);
                for (PsiMethod hierarchyMethod : hierarchyMethods) {
                    influenceGraph.addEdge(method, hierarchyMethod);

                    PsiParameter[] methodParams = method.getParameterList().getParameters();
                    for (PsiParameter param : methodParams) {
                        Set<PsiParameter> hierarchyParams = auxiliaryFunctions.hierarchyParams(param);
                        for (PsiParameter hierarchyParam : hierarchyParams) {
                            influenceGraph.addEdge(param, hierarchyParam);
                        }
                    }
                }
            }
        });
    }
}
