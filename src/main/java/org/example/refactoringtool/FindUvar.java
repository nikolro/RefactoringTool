//find the uvar for a parameter
package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.example.refactoringtool.FindVariances.Variance;
import org.example.refactoringtool.FindVariances.Uvar;

public class FindUvar {
    FindVariances findVariances;
    Variance total_variance;

    public FindUvar(FindVariances findVariances) {
        total_variance = Variance.BIVARIANT;
        this.findVariances = findVariances;
    }

    public void analyze(Uvar uvar) {
        PsiElement element = uvar.element;
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (containingMethod != null) {
            analyzeMethodBody(containingMethod, uvar);
        }
    }

    private void analyzeMethodBody(PsiMethod method, Uvar uvar) {
        PsiCodeBlock body = method.getBody();
        if (body == null) return;

        body.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) element;
                    PsiExpression qualifierExpression = methodCallExpression.getMethodExpression().getQualifierExpression();
                    if (qualifierExpression != null) {
                        PsiElement qualifierDeclaration = null;
                        if (qualifierExpression instanceof PsiReferenceExpression) {
                            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) qualifierExpression;
                            qualifierDeclaration = referenceExpression.resolve();
                        }
                        if (qualifierDeclaration != null && qualifierDeclaration.equals(uvar.element)) {
                            PsiMethod calledMethod = methodCallExpression.resolveMethod();
                            if (calledMethod != null) {
                                processMethodCall(methodCallExpression, uvar);
                            }
                        }
                    }
                    if (methodCallExpression != null && isUvarElementArgument(methodCallExpression, uvar.element)) {
                        total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
                    }
                }
                if (element instanceof PsiJavaToken) {
                    PsiJavaToken token = (PsiJavaToken) element;
                    if (token.getTokenType() == JavaTokenType.EQ) {
                        PsiElement nextSibling = token.getNextSibling();
                        while (nextSibling instanceof PsiWhiteSpace || nextSibling instanceof PsiComment) {
                            nextSibling = nextSibling.getNextSibling();
                        }

                        PsiElement prevSibling = token.getPrevSibling();
                        while (prevSibling instanceof PsiWhiteSpace || prevSibling instanceof PsiComment) {
                            prevSibling = prevSibling.getPrevSibling();
                        }

                        if (nextSibling instanceof PsiExpression) {
                            PsiExpression rightExpression = (PsiExpression) nextSibling;
                            PsiType fieldType = rightExpression.getType();
                            if (fieldType != null && containsTypeExpression(fieldType, uvar.typeExpression)) {
                                if (isAccessingTypeExpression(rightExpression, uvar)) {
                                    total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
                                    if (rightExpression instanceof PsiReferenceExpression) {
                                        PsiReferenceExpression refExpr = (PsiReferenceExpression) rightExpression;
                                        PsiElement resolvedElement = refExpr.resolve();
                                        if (resolvedElement instanceof PsiField) {
                                            PsiField field = (PsiField) resolvedElement;
                                            FindVariances.Constraint constraint=findVariances.getFields().get(field);
                                            constraint.var_type=Variance.COVARIANT;
                                        }
                                    }
                                }
                            }
                        }
                        if (prevSibling instanceof PsiExpression) {
                            PsiExpression leftExpression = (PsiExpression) prevSibling;
                            PsiType fieldType = leftExpression.getType();
                            if (fieldType != null && containsTypeExpression(fieldType, uvar.typeExpression)) {
                                if (isAccessingTypeExpression(leftExpression, uvar)) {
                                    total_variance = findVariances.meet(total_variance, Variance.CONTRAVARIANT);
                                    if (leftExpression instanceof PsiReferenceExpression) {
                                        PsiReferenceExpression refExpr = (PsiReferenceExpression) leftExpression;
                                        PsiElement resolvedElement = refExpr.resolve();
                                        if (resolvedElement instanceof PsiField) {
                                            PsiField field = (PsiField) resolvedElement;
                                            FindVariances.Constraint constraint=findVariances.getFields().get(field);
                                            constraint.var_type=Variance.CONTRAVARIANT;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isUvarElementArgument(PsiMethodCallExpression methodCallExpression, PsiElement uvarElement) {
        PsiExpression[] arguments = methodCallExpression.getArgumentList().getExpressions();
        for (PsiExpression argument : arguments) {
            if (argument instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) argument;
                PsiElement resolvedElement = referenceExpression.resolve();
                if (resolvedElement != null && resolvedElement.equals(uvarElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAccessingTypeExpression(PsiExpression expression, Uvar uvar) {
        if (expression instanceof PsiReferenceExpression) {
            PsiReferenceExpression refExpr = (PsiReferenceExpression) expression;
            PsiElement resolved = refExpr.resolve();
            if (resolved instanceof PsiField) {
                PsiField field = (PsiField) resolved;
                return field.getContainingClass() == uvar.ownerClass;
            }
        }
        return false;
    }

    private void processMethodCall(PsiMethodCallExpression methodCallExpression, Uvar uvar) {
        PsiMethod calledMethod = methodCallExpression.resolveMethod();
        if (calledMethod != null) {
            PsiType returnType = methodCallExpression.getType();
            if (returnType != null && containsTypeExpression(returnType, uvar.typeExpression)) {
                total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
            }

            PsiExpression[] arguments = methodCallExpression.getArgumentList().getExpressions();
            for (PsiExpression argument : arguments) {
                PsiType argumentType = argument.getType();
                if (argumentType != null && containsTypeExpression(argumentType, uvar.typeExpression)) {
                    total_variance = findVariances.meet(total_variance, Variance.CONTRAVARIANT);
                }
            }
        }
    }

    private boolean containsTypeExpression(PsiType type, PsiType targetType) {
        if (type.equals(targetType)) {
            return true;
        }
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiClass resolvedClass = classType.resolve();

            if (resolvedClass != null && resolvedClass.equals(targetType)) {
                return true;
            }

            PsiType[] parameters = classType.getParameters();
            for (PsiType parameter : parameters) {
                if (containsTypeExpression(parameter, targetType)) {
                    return true;
                }
            }
        } else if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            return containsTypeExpression(componentType, targetType);
        } else if (type instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) type;
            PsiType bound = wildcardType.getBound();
            if (bound != null) {
                return containsTypeExpression(bound, targetType);
            }
        } else if (type instanceof PsiCapturedWildcardType) {
            PsiCapturedWildcardType capturedWildcardType = (PsiCapturedWildcardType) type;
            PsiType bound = capturedWildcardType.getWildcard().getBound();
            if (bound != null) {
                return containsTypeExpression(bound, targetType);
            }
        }
        return false;
    }
}
