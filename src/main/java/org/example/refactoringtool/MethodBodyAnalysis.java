package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

public class MethodBodyAnalysis {
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    public enum Variance {
        COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT,NONE
    }

    public MethodBodyAnalysis(InfluenceGraph influenceGraph, AuxiliaryFunctions auxiliaryFunctions) {
        this.influenceGraph = influenceGraph;
        this.auxiliaryFunctions = auxiliaryFunctions;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                analyzeMethodBody(method);
            }
        });
    }

    private void analyzeMethodBody(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        if (body != null) {
            body.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitReferenceExpression(PsiReferenceExpression expression) {
                    super.visitReferenceExpression(expression);
                    PsiElement resolved = expression.resolve();
                    if (resolved instanceof PsiField) {
                        PsiField field = (PsiField) resolved;
                        if (PsiUtil.isAccessedForWriting(expression)) {
                            // Field read (MB-FIELDREAD)
                            // uvar(Y; C; x) v var(Y; T)
                            applyUvarConstraint(expression, field.getType(), Variance.COVARIANT);
                        } else {
                            // Field write (MB-FIELDWRITE)
                            // uvar(Y; C; x) v − ⊗ var(Y; T)
                            applyUvarConstraint(expression, field.getType(), Variance.CONTRAVARIANT);
                        }
                    }
                }

                @Override
                public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                    super.visitMethodCallExpression(expression);
                    PsiMethod calledMethod = expression.resolveMethod();
                    if (calledMethod != null) {
                        // Method call (MB-METHODCALL)
                        // uvar(Y; C; x) v u |U| i=1 ( − ⊗ var(Y; U) )
                        // uvar(Y; T) u |T| i=1 ( − ⊗ localvar(Y; Ti; xi) )
                        PsiParameter[] parameters = calledMethod.getParameterList().getParameters();
                        for (PsiParameter parameter : parameters) {
                            applyUvarConstraint(expression, parameter.getType(), Variance.CONTRAVARIANT);
                        }
                        applyLocalVarConstraint(expression, calledMethod.getReturnType(), Variance.COVARIANT);
                    }
                }

                @Override
                public void visitVariable(PsiVariable variable) {
                    super.visitVariable(variable);
                    PsiExpression initializer = variable.getInitializer();
                    if (initializer != null) {
                        // Assignment to a variable (MB-ASSIGNTOGENERIC-SAME and MB-ASSIGNTOGENERIC-BASE)
                        // uvar(Y; C; x) v inferredUseSite(y; v; C; i)
                        applyUvarConstraint(initializer, variable.getType(), Variance.COVARIANT);
                    }
                }

                @Override
                public void visitReturnStatement(PsiReturnStatement statement) {
                    super.visitReturnStatement(statement);
                    PsiExpression returnValue = statement.getReturnValue();
                    if (returnValue != null) {
                        // Return statement (MB-RETURN)
                        // uvar(Y; C; x) v var(Y; T)
                        applyUvarConstraint(returnValue, method.getReturnType(), Variance.COVARIANT);
                    }
                }
            });
        }
    }

    private void applyUvarConstraint(PsiExpression expression, PsiType type, Variance variance) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiClass psiClass = classType.resolve();
            if (psiClass != null) {
                PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    PsiTypeParameter typeParameter = typeParameters[i];
                    PsiType typeArgument = classType.getParameters()[i];
                    // uvar(Y; C; x) v var(Y; T)
                    // Apply the constraint based on the variance and type argument
                    applyConstraint(expression, typeParameter, typeArgument, variance);
                }
            }
        }
    }

    private void applyLocalVarConstraint(PsiExpression expression, PsiType type, Variance variance) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiClass psiClass = classType.resolve();
            if (psiClass != null) {
                PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    PsiTypeParameter typeParameter = typeParameters[i];
                    PsiType typeArgument = classType.getParameters()[i];
                    // localvar(Y; T; x)
                    // Apply the constraint based on the variance and type argument
                    applyConstraint(expression, typeParameter, typeArgument, variance);
                }
            }
        }
    }

    private void applyConstraint(PsiExpression expression, PsiTypeParameter typeParameter, PsiType typeArgument, Variance variance) {
        // Apply the constraint based on the variance and type argument
        // You can use the influenceGraph to add edges or constraints as needed
        // For example:
        // influenceGraph.addConstraint(typeParameter, typeArgument, variance);
    }
}
