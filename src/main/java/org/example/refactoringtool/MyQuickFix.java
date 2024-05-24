package org.example.refactoringtool;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

// MyQuickFix.java
public class MyQuickFix implements LocalQuickFix {
    private PsiElement declaration;
    private PsiType refactoredType;

    public MyQuickFix(PsiElement declaration, PsiType refactoredType) {
        this.declaration = declaration;
        this.refactoredType = refactoredType;
    }

    @Override
    public @NotNull String getName() {
        return "Apply wildcard refactoring";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Refactoring";
    }


    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (declaration instanceof PsiVariable) {
            //((PsiVariable) declaration).setType(refactoredType);
        } else if (declaration instanceof PsiMethod) {
            ((PsiMethod) declaration).getReturnTypeElement().replace(createTypeElement(project, refactoredType));
        }
    }

    private PsiTypeElement createTypeElement(Project project, PsiType type) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiElementFactory elementFactory = psiFacade.getElementFactory();
        return elementFactory.createTypeElement(type);
    }
}