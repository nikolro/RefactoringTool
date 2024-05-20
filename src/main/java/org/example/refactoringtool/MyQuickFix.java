package org.example.refactoringtool;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class MyQuickFix implements LocalQuickFix {

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
        PsiElement element = descriptor.getPsiElement();
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            refactorMethod(method);
        }
    }

    private void refactorMethod(PsiMethod method) {
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(method.getProject());

        for (PsiParameter parameter : parameters) {
            PsiType parameterType = parameter.getType();
            if (parameterType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) parameterType;
                if (!classType.hasParameters()) {
                    continue;
                }

                // Create a wildcard type: "? extends Type"
                PsiType[] typeArguments = classType.getParameters();
                PsiType wildcardType = PsiWildcardType.createExtends(method.getManager(), typeArguments[0]);

                // Replace the parameter type with the wildcard type
                PsiTypeElement newTypeElement = elementFactory.createTypeElementFromText(
                        classType.rawType().getCanonicalText() + "<" + wildcardType.getCanonicalText() + ">", null);
                parameter.getTypeElement().replace(newTypeElement);
            }
        }
    }
}


