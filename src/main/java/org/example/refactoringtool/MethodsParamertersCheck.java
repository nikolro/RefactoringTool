package org.example.refactoringtool;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public class MethodsParamertersCheck {
    FindVariances findVariances;
    private ProblemsHolder holder;
    private InfluenceGraph influenceGraph;

    public MethodsParamertersCheck(FindVariances findVariances, ProblemsHolder holder, InfluenceGraph influenceGraph) {
        this.findVariances = findVariances;
        this.holder = holder;
        this.influenceGraph = influenceGraph;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                analyzeMethod(method);
            }
        });
    }

    private void analyzeMethod(PsiMethod method) {
        PsiParameterList parameterList = method.getParameterList();
        for (PsiParameter parameter : parameterList.getParameters()) {
            analyzeParameter(method, parameter);
        }
    }

    private void analyzeParameter(PsiMethod method, PsiParameter parameter) {
        PsiType parameterType = parameter.getType();
        if (parameterType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) parameterType;
            PsiType[] typeArguments = classType.getParameters();

            if (typeArguments.length == 0) {
                return;
            }

            PsiType typeArgument = typeArguments[0];
            FindVariances.Variance var = determineVariance(typeArgument);
            if (var != FindVariances.Variance.NONE) {
                handleVariance(method, parameter, var, parameterType);
            }
        }
    }

    private FindVariances.Variance determineVariance(PsiType typeArgument) {
        if (typeArgument instanceof PsiClassType) {
            // Case 1: P<X>
            return FindVariances.Variance.INVARIANT;
        } else if (typeArgument instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) typeArgument;
            PsiType bound = wildcardType.getBound();
            if (bound instanceof PsiClassType) {
                if (wildcardType.isExtends()) {
                    // Case 2: P<? extends X>
                    return FindVariances.Variance.COVARIANT;
                } else if (wildcardType.isSuper()) {
                    // Case 3: P<? super X>
                    return FindVariances.Variance.CONTRAVARIANT;
                }
            } else {
                // Case 4: P<?>
                return FindVariances.Variance.BIVARIANT;
            }
        }
        return FindVariances.Variance.NONE;
    }

    private void handleVariance(PsiMethod method, PsiParameter parameter, FindVariances.Variance var, PsiType parameterType) {
        FindVariances.Variance uvar_var = getUvarVariance(parameter);

        PsiClass resolvedOuterClass = null;
        if (parameterType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) parameterType;
            resolvedOuterClass = classType.resolve();
        }

        boolean first_time=true;
        for (FindVariances.Dvar dvar : findVariances.getDvarsList()) {
            if (resolvedOuterClass != null && resolvedOuterClass.equals(dvar.ownerClass)) {
                FindVariances.Variance new_var1 = join(var, dvar.var);
                FindVariances.Variance new_var = join(new_var1, uvar_var);
                if (new_var != var) {
                    if(first_time==true)
                    {
                        suggestChange(method, parameter, new_var,var);
                        first_time=false;
                    }
                }
            }
        }
    }

    private FindVariances.Variance getUvarVariance(PsiParameter parameter) {
        for (FindVariances.Uvar uvar : findVariances.getUvarsList()) {
            if (uvar.element == parameter) {
                return uvar.var;
            }
        }
        return FindVariances.Variance.NONE;
    }

    private void suggestChange(PsiMethod method, PsiParameter parameter, FindVariances.Variance new_var,FindVariances.Variance var) {
        List<PsiElement> influenced_decls = influenceGraph.getAllInfluencedElements(parameter);
        boolean not_rewritable=false;
        FindVariances.Variance final_var= new_var;
        for(PsiElement element: influenced_decls)
        {
            if(influenceGraph.isNonRewritable(element)==true)
            {
                not_rewritable=true;
            }
            if(influenceGraph.getInfluencedElements(element).contains(parameter)==true)
            {
                if(element instanceof PsiParameter){
                FindVariances.Variance element_variance = findVar((PsiParameter) element);
                final_var = findVariances.meet(element_variance, new_var);
            }
            }
        }
        if(not_rewritable==false && final_var!=var) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Consider changing the parameter ")
                    .append(parameter.getName())
                    .append(" in method ")
                    .append(method.getName())
                    .append(" to use a more specific type with variance: ")
                    .append(final_var)
                    .append(".");
            messageBuilder.append("\n");
            if (!influenced_decls.isEmpty()) {
                messageBuilder.append(" This change influences the following elements: \n");
                for (PsiElement influencedElement : influenced_decls) {
                    if (influencedElement instanceof PsiParameter) {
                        PsiParameter influencedParameter = (PsiParameter) influencedElement;
                        PsiMethod influencedMethod = (PsiMethod) influencedParameter.getParent().getParent();
                        messageBuilder.append(influencedParameter.getName())
                                .append(" in method ")
                                .append(influencedMethod.getName())
                                .append(".");
                        messageBuilder.append("\n");
                    } else if (influencedElement instanceof PsiLocalVariable) {
                        PsiLocalVariable influencedVariable = (PsiLocalVariable) influencedElement;
                        PsiElement scope = influencedVariable.getParent();
                        while (scope != null && !(scope instanceof PsiMethod || scope instanceof PsiClass || scope instanceof PsiFile)) {
                            scope = scope.getParent();
                        }
                        if (scope instanceof PsiMethod) {
                            PsiMethod influencedMethod = (PsiMethod) scope;
                            messageBuilder.append(influencedVariable.getName())
                                    .append(" in method ")
                                    .append(influencedMethod.getName())
                                    .append(".");
                            messageBuilder.append("\n");
                        } else if (scope instanceof PsiClass) {
                            PsiClass influencedClass = (PsiClass) scope;
                            messageBuilder.append(influencedVariable.getName())
                                    .append(" in class ")
                                    .append(influencedClass.getName())
                                    .append(".");
                            messageBuilder.append("\n");
                        } else if (scope instanceof PsiFile) {
                            messageBuilder.append(influencedVariable.getName())
                                    .append(" in file scope.");
                            messageBuilder.append("\n");
                        }
                    } else if (influencedElement instanceof PsiField) {
                        PsiField influencedField = (PsiField) influencedElement;
                        PsiClass influencedClass = (PsiClass) influencedField.getParent();
                        messageBuilder.append(influencedField.getName())
                                .append(" in class ")
                                .append(influencedClass.getName())
                                .append(".");
                        messageBuilder.append("\n");
                    }
                }
                // Remove the last comma and space
                messageBuilder.setLength(messageBuilder.length() - 2);
                messageBuilder.append(".");

            }

            String message = messageBuilder.toString();
            PsiElement typeElement = parameter.getTypeElement();
            if (typeElement != null) {
                holder.registerProblem(typeElement, message, new MyQuickFix(parameter, final_var, influenced_decls));
            }
        }
    }

    // Helper method to check if a class is external
    private boolean isExternalClass(PsiClass psiClass) {
        if (psiClass != null) {
            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName != null) {
                // Check if the class is part of the Java standard library or other external libraries
                return qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.");
            }
        }
        return false;
    }

    public FindVariances.Variance join(FindVariances.Variance v1, FindVariances.Variance v2) {
        if (v1 == FindVariances.Variance.BIVARIANT || v2 == FindVariances.Variance.BIVARIANT) {
            return FindVariances.Variance.BIVARIANT;
        }
        if (v1 == FindVariances.Variance.INVARIANT && v2 == FindVariances.Variance.INVARIANT) {
            return FindVariances.Variance.INVARIANT;
        }
        if (v1 == FindVariances.Variance.INVARIANT) {
            return v2;
        }
        if (v2 == FindVariances.Variance.INVARIANT) {
            return v1;
        }
        if (v1 == v2) {
            return v1;
        }

        return FindVariances.Variance.BIVARIANT;
    }
    public FindVariances.Variance findVar(PsiParameter parameter)
    {
        PsiType parameterType = parameter.getType();
        if (parameterType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) parameterType;
            PsiType[] typeArguments = classType.getParameters();

            if (typeArguments.length == 0) {
                return FindVariances.Variance.NONE;
            }
            PsiType typeArgument = typeArguments[0];
            FindVariances.Variance var = determineVariance(typeArgument);
            if (var != FindVariances.Variance.NONE) {
                FindVariances.Variance uvar_var = getUvarVariance(parameter);

                PsiClass resolvedOuterClass = null;
                if (parameterType instanceof PsiClassType) {
                    PsiClassType classType1 = (PsiClassType) parameterType;
                    resolvedOuterClass = classType.resolve();
                }
                for (FindVariances.Dvar dvar : findVariances.getDvarsList()) {
                    if (resolvedOuterClass != null && resolvedOuterClass.equals(dvar.ownerClass)) {
                        FindVariances.Variance new_var1 = join(var, dvar.var);
                        FindVariances.Variance new_var = join(new_var1, uvar_var);
                        return new_var;
                    }
                }
            }
        }
        return FindVariances.Variance.NONE;
    }

}
