package org.example.refactoringtool;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class MyQuickFix implements LocalQuickFix {
    private PsiParameter parameter;
    private DefinitionSiteVariance.Variance joinedVariance;

    public MyQuickFix(PsiParameter parameter, DefinitionSiteVariance.Variance joinedVariance) {
        this.parameter = parameter;
        this.joinedVariance = joinedVariance;
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
        PsiElement element = descriptor.getPsiElement();
        if (element instanceof PsiTypeElement) {
            PsiTypeElement typeElement = (PsiTypeElement) element;
            PsiType parameterType = typeElement.getType();
            if (parameterType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) parameterType;
                PsiType refactoredType = createRefactoredType(project, classType, joinedVariance);
                if (refactoredType != null) {
                    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                    PsiTypeElement newTypeElement = elementFactory.createTypeElement(refactoredType);
                    typeElement.replace(newTypeElement);
                }
            }
        }
    }

    private PsiType createRefactoredType(Project project, PsiClassType classType, DefinitionSiteVariance.Variance variance) {
        PsiType[] typeArguments = classType.getParameters();
        if (typeArguments.length == 0) {
            return null;
        }

        PsiType oldTypeArgument = typeArguments[0];
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiType newTypeArgument;

        if (oldTypeArgument instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) oldTypeArgument;
            PsiType bound = wildcardType.getBound();

            switch (variance) {
                case COVARIANT:
                    newTypeArgument = elementFactory.createTypeFromText("? extends " + bound.getCanonicalText(), null);
                    break;
                case CONTRAVARIANT:
                    newTypeArgument = elementFactory.createTypeFromText("? super " + bound.getCanonicalText(), null);
                    break;
                case INVARIANT:
                    newTypeArgument = bound;
                    break;
                default:
                    newTypeArgument = elementFactory.createTypeFromText("?", null);
            }
        } else {
            switch (variance) {
                case COVARIANT:
                    newTypeArgument = elementFactory.createTypeFromText("? extends " + oldTypeArgument.getCanonicalText(), null);
                    break;
                case CONTRAVARIANT:
                    newTypeArgument = elementFactory.createTypeFromText("? super " + oldTypeArgument.getCanonicalText(), null);
                    break;
                case INVARIANT:
                    newTypeArgument = oldTypeArgument;
                    break;
                default:
                    newTypeArgument = elementFactory.createTypeFromText("?", null);
            }
        }

        return elementFactory.createType(classType.resolve(), new PsiType[]{newTypeArgument});
    }
}