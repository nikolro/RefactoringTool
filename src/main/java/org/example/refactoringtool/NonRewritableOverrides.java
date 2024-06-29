package org.example.refactoringtool;

import com.intellij.psi.*;

public class NonRewritableOverrides {
    private InfluenceGraph influenceGraph;

    public NonRewritableOverrides(InfluenceGraph influenceGraph) {
        this.influenceGraph = influenceGraph;
    }

    public void analyze() {
        for (PsiElement element : influenceGraph.getGraph().keySet()) {
            if (isExternalDeclaration(element)) {
                influenceGraph.markNonRewritable(element);
            }
        }
    }

    private boolean isExternalDeclaration(PsiElement element) {
        if (element instanceof PsiMethod) {
            return isExternalMethod((PsiMethod) element);
        } else if (element instanceof PsiClass) {
            return isExternalClass((PsiClass) element);
        } else if (element instanceof PsiField) {
            return isExternalField((PsiField) element);
        } else if (element instanceof PsiParameter) {
            return isExternalParameter((PsiParameter) element);
        }
        return false;
    }

    private boolean isExternalMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        return containingClass != null && isExternalClass(containingClass);
    }

    private boolean isExternalClass(PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        return qualifiedName != null && (qualifiedName.startsWith("java.") ||
                qualifiedName.startsWith("javax.") ||
                qualifiedName.startsWith("org.") ||
                qualifiedName.startsWith("com."));
    }

    private boolean isExternalField(PsiField field) {
        PsiClass containingClass = field.getContainingClass();
        return containingClass != null && isExternalClass(containingClass);
    }

    private boolean isExternalParameter(PsiParameter parameter) {
        PsiElement parent = parameter.getParent();
        if (parent instanceof PsiParameterList) {
            PsiElement grandParent = parent.getParent();
            if (grandParent instanceof PsiMethod) {
                return isExternalMethod((PsiMethod) grandParent);
            }
        }
        return false;
    }
}