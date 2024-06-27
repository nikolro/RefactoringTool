package org.example.refactoringtool;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;

public class MethodsParamertersCheck {
    FindVariances findVariances;
    private ProblemsHolder holder;

    public MethodsParamertersCheck(FindVariances findVariances, ProblemsHolder holder) {
        this.findVariances = findVariances;
        this.holder = holder;
    }

    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {

            @Override
            public void visitMethod(PsiMethod method) {
                PsiParameterList parameterList = method.getParameterList();
                for (PsiParameter parameter : parameterList.getParameters()) {
                    PsiType parameterType = parameter.getType();
                    if (parameterType instanceof PsiClassType) {
                        PsiClassType classType = (PsiClassType) parameterType;
                        PsiType[] typeArguments = classType.getParameters();

                        if (typeArguments.length == 0) {
                            continue; // Skip if there are no type arguments
                        }

                        PsiType typeArgument = typeArguments[0];
                        boolean matchesPattern = false;
                        FindVariances.Variance var=FindVariances.Variance.NONE;


                        if (typeArgument instanceof PsiClassType) {
                            // Case 1: P<X>
                            matchesPattern = true;
                            var= FindVariances.Variance.INVARIANT;
                        } else if (typeArgument instanceof PsiWildcardType) {
                            PsiWildcardType wildcardType = (PsiWildcardType) typeArgument;
                            PsiType bound = wildcardType.getBound();
                            if (bound instanceof PsiClassType) {
                                if (wildcardType.isExtends()) {
                                    // Case 2: P<? extends X>
                                    matchesPattern = true;
                                    var= FindVariances.Variance.COVARIANT;
                                } else if (wildcardType.isSuper()) {
                                    // Case 3: P<? super X>
                                    matchesPattern = true;
                                    var= FindVariances.Variance.CONTRAVARIANT;
                                }
                            } else {
                                // Case 4: P<?>
                                matchesPattern = true;
                                var= FindVariances.Variance.BIVARIANT;
                            }
                        }
                        if (matchesPattern) {
                            FindVariances.Variance uvar_var=FindVariances.Variance.NONE;
                            for(FindVariances.Uvar uvar : findVariances.getUvarsList()){
                                if(uvar.element==parameter)
                                {
                                    uvar_var=uvar.var;
                                }
                            }
                            PsiClass resolvedOuterClass = null;
                            if (parameterType instanceof PsiClassType) {
                                PsiClassType classType1 = (PsiClassType) parameterType;
                                resolvedOuterClass = classType1.resolve();
                            }
                                // Existing logic for non-external classes
                                for (FindVariances.Dvar dvar : findVariances.getDvarsList()) {
                                    if (resolvedOuterClass != null && resolvedOuterClass.equals(dvar.ownerClass)) {
                                        FindVariances.Variance new_var1 = join(var, dvar.var);
                                        FindVariances.Variance new_var = join(new_var1, uvar_var);
                                        if (new_var != var) {
                                            String message = "Consider changing the parameter " + parameter.getName() +
                                                    " in method " + method.getName() + " to use a more specific type.";
                                            PsiElement typeElement = parameter.getTypeElement();
                                            if (typeElement != null) {
                                                holder.registerProblem(typeElement, message, new MyQuickFix(parameter, new_var));
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

    public FindVariances.Variance join (FindVariances.Variance v1, FindVariances.Variance v2) {
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
        if (v1 == v2)
        {
            return v1;
        }

        return FindVariances.Variance.BIVARIANT;
    }
}
