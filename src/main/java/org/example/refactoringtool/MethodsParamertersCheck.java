package org.example.refactoringtool;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;

public class MethodsParamertersCheck {
    DefinitionSiteVariance definitionSiteVariance;
    private ProblemsHolder holder;

    public MethodsParamertersCheck(DefinitionSiteVariance definitionSiteVariance,ProblemsHolder holder) {
        this.definitionSiteVariance = definitionSiteVariance;
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
                        DefinitionSiteVariance.Variance var=DefinitionSiteVariance.Variance.NONE;


                        if (typeArgument instanceof PsiClassType) {
                            // Case 1: P<X>
                            matchesPattern = true;
                            var= DefinitionSiteVariance.Variance.INVARIANT;
                        } else if (typeArgument instanceof PsiWildcardType) {
                            PsiWildcardType wildcardType = (PsiWildcardType) typeArgument;
                            PsiType bound = wildcardType.getBound();
                            if (bound instanceof PsiClassType) {
                                if (wildcardType.isExtends()) {
                                    // Case 2: P<? extends X>
                                    matchesPattern = true;
                                    var= DefinitionSiteVariance.Variance.COVARIANT;
                                } else if (wildcardType.isSuper()) {
                                    // Case 3: P<? super X>
                                    matchesPattern = true;
                                    var= DefinitionSiteVariance.Variance.CONTRAVARIANT;
                                }
                            } else {
                                // Case 4: P<?>
                                matchesPattern = true;
                                var= DefinitionSiteVariance.Variance.BIVARIANT;
                            }
                        }
                        if (matchesPattern) {
                            for (DefinitionSiteVariance.Dvar dvar : definitionSiteVariance.getDvarsList()) {
                                PsiClass resolvedOuterClass=null;
                                if (parameterType instanceof PsiClassType) {
                                    PsiClassType classType1 = (PsiClassType) parameterType;
                                    resolvedOuterClass = classType1.resolve();
                                }

                                if (resolvedOuterClass != null && resolvedOuterClass.equals(dvar.ownerClass)) {
                                    DefinitionSiteVariance.Variance new_var = join(var, dvar.var);
                                    if(new_var!=var) {
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
    public DefinitionSiteVariance.Variance join (DefinitionSiteVariance.Variance v1, DefinitionSiteVariance.Variance v2) {
        if (v1 == DefinitionSiteVariance.Variance.BIVARIANT || v2 == DefinitionSiteVariance.Variance.BIVARIANT) {
            return DefinitionSiteVariance.Variance.BIVARIANT;
        }
        if (v1 == DefinitionSiteVariance.Variance.INVARIANT && v2 == DefinitionSiteVariance.Variance.INVARIANT) {
            return DefinitionSiteVariance.Variance.INVARIANT;
        }
        if (v1 == DefinitionSiteVariance.Variance.INVARIANT) {
            return v2;
        }
        if (v2 == DefinitionSiteVariance.Variance.INVARIANT) {
            return v1;
        }
        if (v1 == v2)
        {
            return v1;
        }

        return DefinitionSiteVariance.Variance.BIVARIANT;
    }
}
