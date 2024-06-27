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

// Traverse the method body and analyze the usage of type parameters
        body.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) element;
                    PsiExpression qualifierExpression = methodCallExpression.getMethodExpression().getQualifierExpression();
                    if (qualifierExpression != null) {
                        PsiType qualifierType = qualifierExpression.getType();
                        if (qualifierType != null && containsTypeParameter(qualifierType, uvar.typeParameter)) {
                            PsiMethod calledMethod = methodCallExpression.resolveMethod();
                            if (calledMethod != null) {
                                processMethodCall(methodCallExpression, uvar);
                            }
                        }
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
                            if (fieldType != null && containsTypeParameter(fieldType, uvar.typeParameter)) {
                                if (isAccessingTypeParameter(rightExpression, uvar)) {
                                    total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
                                }
                            }
                        }

                        // Check the previous sibling
                        if (prevSibling instanceof PsiExpression) {
                            PsiExpression leftExpression = (PsiExpression) prevSibling;
                            PsiType fieldType = leftExpression.getType();
                            if (fieldType != null && containsTypeParameter(fieldType, uvar.typeParameter)) {
                                if (isAccessingTypeParameter(leftExpression, uvar)) {
                                    total_variance = findVariances.meet(total_variance, Variance.CONTRAVARIANT);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isAccessingTypeParameter(PsiExpression expression, Uvar uvar) {
        if (expression instanceof PsiReferenceExpression) {
            PsiReferenceExpression refExpr = (PsiReferenceExpression) expression;
            PsiElement resolved = refExpr.resolve();
            if (resolved instanceof PsiField) {
                PsiField field = (PsiField) resolved;
                return field.getContainingClass() == uvar.typeParameter.getOwner();
            }
        }
        // Add more checks here for other ways the type parameter might be accessed
        return false;
    }

    private void processFieldAccess(PsiReferenceExpression referenceExpression, PsiField field, Uvar uvar) {
        PsiType fieldType = field.getType();
        if (containsTypeParameter(fieldType, uvar.typeParameter)) {
            PsiElement parent = referenceExpression.getParent();

            // Traverse up the tree to find the assignment expression
            while (parent != null && !(parent instanceof PsiAssignmentExpression)) {
                parent = parent.getParent();
            }

            if (parent instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) parent;
                PsiExpression lExpression = assignmentExpression.getLExpression();
                PsiExpression rExpression = assignmentExpression.getRExpression();

                if (referenceExpression.equals(lExpression)) {
                    // Field is being written to
                    total_variance = findVariances.meet(total_variance, Variance.CONTRAVARIANT);
                } else if (referenceExpression.equals(rExpression)) {
                    // Field is being read from
                    total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
                }
            }
        }
    }
    private void processMethodCall(PsiMethodCallExpression methodCallExpression, Uvar uvar) {
        PsiMethod calledMethod = methodCallExpression.resolveMethod();
        if (calledMethod != null) {
            PsiType returnType = methodCallExpression.getType();
            if (returnType != null && containsTypeParameter(returnType, uvar.typeParameter)) {
                total_variance = findVariances.meet(total_variance, Variance.COVARIANT);
            }

            PsiExpression[] arguments = methodCallExpression.getArgumentList().getExpressions();
            for (PsiExpression argument : arguments) {
                PsiType argumentType = argument.getType();
                if (argumentType != null && containsTypeParameter(argumentType, uvar.typeParameter)) {
                    total_variance = findVariances.meet(total_variance, Variance.CONTRAVARIANT);
                }
            }
        }
    }
    private boolean containsTypeParameter(PsiType type, PsiTypeParameter typeParameter) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiClass resolvedClass = classType.resolve();

            if (resolvedClass instanceof PsiTypeParameter) {
                PsiTypeParameter resolvedTypeParameter = (PsiTypeParameter) resolvedClass;
                if (resolvedTypeParameter.getName().equals(typeParameter.getName())) {
                    return true;
                }
            }

            PsiType[] parameters = classType.getParameters();
            for (PsiType parameter : parameters) {
                if (containsTypeParameter(parameter, typeParameter)) {
                    return true;
                }
            }
        } else if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            return containsTypeParameter(componentType, typeParameter);
        } else if (type instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) type;
            PsiType bound = wildcardType.getBound();
            if (bound != null) {
                return containsTypeParameter(bound, typeParameter);
            }
        } else if (type instanceof PsiCapturedWildcardType) {
            PsiCapturedWildcardType capturedWildcardType = (PsiCapturedWildcardType) type;
            PsiType bound = capturedWildcardType.getWildcard().getBound();
            if (bound != null) {
                return containsTypeParameter(bound, typeParameter);
            }
        } else if (type instanceof PsiTypeParameter) {
            return type.equals(typeParameter);
        }
        return false;
    }

}
