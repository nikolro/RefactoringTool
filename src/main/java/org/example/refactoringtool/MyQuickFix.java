package org.example.refactoringtool;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyQuickFix implements LocalQuickFix {
    private PsiParameter parameter;
    private FindVariances.Variance joinedVariance;
    private List<PsiElement> influencedDeclarations;

    public MyQuickFix(PsiParameter parameter, FindVariances.Variance joinedVariance, List<PsiElement> influencedDeclarations) {
        this.parameter = parameter;
        this.joinedVariance = joinedVariance;
        this.influencedDeclarations = influencedDeclarations;
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
            applyRefactoring(project, (PsiTypeElement) element, joinedVariance);
        }

        for (PsiElement influencedElement : influencedDeclarations) {
            if (influencedElement instanceof PsiParameter) {
                PsiParameter influencedParameter = (PsiParameter) influencedElement;
                PsiTypeElement typeElement = influencedParameter.getTypeElement();
                if (typeElement != null) {
                    applyRefactoring(project, typeElement, joinedVariance);
                }
            } else if (influencedElement instanceof PsiLocalVariable) {
                PsiLocalVariable influencedVariable = (PsiLocalVariable) influencedElement;
                PsiTypeElement typeElement = influencedVariable.getTypeElement();
                if (typeElement != null) {
                    applyRefactoring(project, typeElement, joinedVariance);
                }
            } else if (influencedElement instanceof PsiField) {
                PsiField influencedField = (PsiField) influencedElement;
                PsiTypeElement typeElement = influencedField.getTypeElement();
                if (typeElement != null) {
                    applyRefactoring(project, typeElement, joinedVariance);
                }
            }
        }
    }

    private void applyRefactoring(@NotNull Project project, @NotNull PsiTypeElement typeElement, FindVariances.Variance variance) {
        PsiType parameterType = typeElement.getType();
        if (parameterType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) parameterType;
            PsiType refactoredType = createRefactoredType(project, classType, variance);
            if (refactoredType != null) {
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                PsiTypeElement newTypeElement = elementFactory.createTypeElement(refactoredType);
                typeElement.replace(newTypeElement);
            }
        }
    }

    private PsiType createRefactoredType(Project project, PsiClassType classType, FindVariances.Variance variance) {
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
                    newTypeArgument = elementFactory.createTypeFromText("? extends " + (bound != null ? bound.getCanonicalText() : ""), null);
                    break;
                case CONTRAVARIANT:
                    newTypeArgument = elementFactory.createTypeFromText("? super " + (bound != null ? bound.getCanonicalText() : ""), null);
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
