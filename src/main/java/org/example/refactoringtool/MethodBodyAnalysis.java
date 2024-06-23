//package org.example.refactoringtool;
//
//import com.intellij.psi.*;
//import java.util.List;
//
//class MethodBodyAnalysis {
//
//    private List<DefinitionSiteVariance.Constraint> constraints_list;
//    private DefinitionSiteVariance definitionSiteVariance;
//
//    public MethodBodyAnalysis (List<DefinitionSiteVariance.Constraint> constraints_list, DefinitionSiteVariance definitionSiteVariance) {
//        this.constraints_list = constraints_list;
//        this.definitionSiteVariance = definitionSiteVariance;
//    }
//
//    public void analyzeMethodBody(PsiMethod method, DefinitionSiteVariance.Dvar dvar) {
//        method.accept(new JavaRecursiveElementVisitor() {
//            @Override
//            public void visitReferenceExpression(PsiReferenceExpression expression) {
//                super.visitReferenceExpression(expression);
//
//                // Check if this is a field read
//                PsiElement resolvedElement = expression.resolve();
//                if (resolvedElement instanceof PsiParameter) {
//                    PsiElement parent = expression.getParent();
//                    if (parent instanceof PsiReferenceExpression) {
//                        PsiElement fieldElement = ((PsiReferenceExpression) parent).resolve();
//                        if (fieldElement instanceof PsiField && !isWriteTarget(parent)) {
//                            PsiField field = (PsiField) fieldElement;
//
//                            PsiClass containingClass = field.getContainingClass();
//
//                            if (containingClass != null && isFieldAccessibleFromTypeParameter(containingClass, dvar)) {
//                                PsiMethod enclosingMethod = enclosingMethod(expression);
//                                if (enclosingMethod != null && enclosingMethod.equals(method)) {
//                                    updateConstraintWithUvar(dvar, field, DefinitionSiteVariance.Variance.COVARIANT, method, (PsiParameter) resolvedElement);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
////            @Override
////            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
////                super.visitMethodCallExpression(expression);
////                PsiMethod calledMethod = expression.resolveMethod();
////                if (calledMethod != null) {
////                    for (PsiType typeArgument : expression.getTypeArguments()) {
////                        PsiTypeParameter typeParameter = definitionSiteVariance.getTypeParameter(typeArgument);
////                        if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
////                            generateConstraint(dvar, typeArgument, method);
////                        }
////                    }
////                }
////            }
////
////            @Override
////            public void visitLocalVariable(PsiLocalVariable variable) {
////                super.visitLocalVariable(variable);
////                PsiType type = variable.getType();
////                PsiTypeParameter typeParameter = definitionSiteVariance.getTypeParameter(type);
////                if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
////                    generateConstraint(dvar, type, method);
////                }
////            }
////
////            @Override
////            public void visitReturnStatement(PsiReturnStatement statement) {
////                super.visitReturnStatement(statement);
////                PsiExpression returnValue = statement.getReturnValue();
////                if (returnValue != null) {
////                    PsiType returnType = returnValue.getType();
////                    if (returnType != null) {
////                        PsiTypeParameter typeParameter = definitionSiteVariance.getTypeParameter(returnType);
////                        if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
////                            generateConstraint(dvar, returnType, method);
////                        }
////                    }
////                }
////            }
////
////            @Override
////            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
////                super.visitAssignmentExpression(expression);
////                PsiExpression lExpression = expression.getLExpression();
////                PsiExpression rExpression = expression.getRExpression();
////                if (lExpression != null && rExpression != null) {
////                    PsiType lType = lExpression.getType();
////                    PsiType rType = rExpression.getType();
////                    if (lType != null && rType != null) {
////                        PsiTypeParameter lTypeParameter = definitionSiteVariance.getTypeParameter(lType);
////                        PsiTypeParameter rTypeParameter = definitionSiteVariance.getTypeParameter(rType);
////                        if (lTypeParameter != null && lTypeParameter.equals(dvar.typeParameter)) {
////                            generateConstraint(dvar, rType, method);
////                        }
////                        if (rTypeParameter != null && rTypeParameter.equals(dvar.typeParameter)) {
////                            generateConstraint(dvar, lType, method);
////                        }
////                    }
////                }
////            }
//        });
//    }
//    private boolean isFieldAccessibleFromTypeParameter(PsiClass fieldClass, DefinitionSiteVariance.Dvar dvar) {
//        // Check if the field class is a supertype of the type parameter's bounds
//        PsiClassType typeParameterType = (PsiClassType) dvar.typeParameter.getExtendsListTypes()[0];
//        PsiClass typeParameterClass = typeParameterType.resolve();
//        return typeParameterClass != null && (fieldClass.equals(typeParameterClass) || typeParameterClass.isInheritor(fieldClass, true));
//    }
//    private void updateConstraintWithUvar(DefinitionSiteVariance.Dvar dvar, PsiField field, DefinitionSiteVariance.Variance uvar, PsiMethod method, PsiParameter resolvedParameter) {
//        for (DefinitionSiteVariance.Constraint constraint : constraints_list) {
//            if (constraint.dvar.equals(dvar) && constraint.parameter != null && constraint.parameter.equals(resolvedParameter)) {
//                constraint.uvar = uvar;
//            }
//        }
//    }
//
//    public boolean isWriteTarget(PsiElement element) {
//        if (element.getParent() instanceof PsiAssignmentExpression) {
//            PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) element.getParent();
//            return assignmentExpression.getLExpression() == element;
//        }
//        return false;
//    }
//
//    public PsiType LookupType(PsiElement element) {
//        if (element instanceof PsiField) {
//            return ((PsiField) element).getType();
//        }
//        return null;
//    }
//
//    public PsiMethod enclosingMethod(PsiElement element) {
//        while (element != null && !(element instanceof PsiFile)) {
//            if (element instanceof PsiMethod) {
//                return (PsiMethod) element;
//            }
//            element = element.getParent();
//        }
//        return null;
//    }
////    private void generateConstraint(DefinitionSiteVariance.Dvar dvar, PsiType type, PsiMethod method) {
////        DefinitionSiteVariance.Variance variance = method.getReturnType() != null && method.getReturnType().equals(type)
////                ? DefinitionSiteVariance.Variance.COVARIANT
////                : DefinitionSiteVariance.Variance.CONTRAVARIANT;
////        DefinitionSiteVariance.Variance wildCardVariance = definitionSiteVariance.checkWildcards(type);
////        DefinitionSiteVariance.Constraint constraint = new DefinitionSiteVariance.Constraint(
////                dvar, variance, dvar, wildCardVariance, null);
////        constraintsList.add(constraint);
////    }
//}
//

package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.List;

class MethodBodyAnalysis {

    private List<DefinitionSiteVariance.Constraint> constraints_list;
    private List<DefinitionSiteVariance.Dvar> dvars_list;
    private DefinitionSiteVariance definitionSiteVariance;

    public MethodBodyAnalysis(List<DefinitionSiteVariance.Constraint> constraints_list, List<DefinitionSiteVariance.Dvar> dvars_list, DefinitionSiteVariance definitionSiteVariance) {
        this.constraints_list = constraints_list;
        this.definitionSiteVariance = definitionSiteVariance;
    }

    public void analyzeMethodBody(PsiMethod method, DefinitionSiteVariance.Dvar dvar) {
        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                PsiElement qualifierElement = getQualifierElement (expression);

                // Check if this is a field read
                PsiElement resolvedElement = expression.resolve();
                PsiType type = LookupType (resolvedElement);
                PsiMethod enclosingMethod = enclosingMethod(qualifierElement);

                if (resolvedElement instanceof PsiField && enclosingMethod != null && enclosingMethod.equals(method)) {
                    DefinitionSiteVariance.Variance var = simpleVarCalc(type, dvar.typeParameter);
                    if (!isWriteTarget(expression) )
                    {
                        for (DefinitionSiteVariance.Constraint constraint : constraints_list) {
                            if (constraint.dvar.equals(dvar) && constraint.parameter != null && constraint.parameter.equals(qualifierElement)) {
                                if (constraint.uvar == DefinitionSiteVariance.Variance.NONE) {
                                    constraint.uvar = var;
                                    ;
                                } else {
                                    constraint.uvar = definitionSiteVariance.meet(constraint.uvar, var);
                                }

                            }
                        }
                    }
                    else // field write
                    {
                        for (DefinitionSiteVariance.Constraint constraint : constraints_list) {
                            if (constraint.dvar.equals(dvar) && constraint.parameter != null && constraint.parameter.equals(qualifierElement)) {
                                if (constraint.uvar == DefinitionSiteVariance.Variance.NONE) {
                                    constraint.uvar = definitionSiteVariance.transform (DefinitionSiteVariance.Variance.CONTRAVARIANT, var);
                                } else {
                                    constraint.uvar = definitionSiteVariance.meet(constraint.uvar,  definitionSiteVariance.transform (DefinitionSiteVariance.Variance.CONTRAVARIANT, var));
                                }

                            }
                        }
                    }
                }
            }


            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiElement qualifierElement = getQualifierElement(expression);
                PsiMethod enclosingMethod = enclosingMethod(qualifierElement);

                PsiMethod calledMethod = AuxiliaryFunctions.Lookup (expression);

                if (calledMethod != null) {

                    PsiClass containingClass = calledMethod.getContainingClass();

                    // we are so sorry. We really did try. It shouldn't be like this. Nothing should be like this...
                    for (DefinitionSiteVariance.Constraint constraint : constraints_list) {
                        String method_name = calledMethod.getName();
                        if (constraint.dvar.equals(dvar) && enclosingMethod != null && enclosingMethod.equals(method) &&
                                constraint.parameter != null && constraint.parameter.equals(qualifierElement)) {
                            if (method_name.equals("get")) // COVARIANT
                            {
                                if (constraint.uvar == DefinitionSiteVariance.Variance.NONE)
                                {
                                    constraint.uvar =  DefinitionSiteVariance.Variance.COVARIANT;
                                }
                                else
                                {
                                    constraint.uvar = definitionSiteVariance.meet(constraint.uvar, DefinitionSiteVariance.Variance.COVARIANT);
                                }
                            }
                            else if (method_name.equals("add")) // CONTRAVARIANT
                            {
                                if (constraint.uvar == DefinitionSiteVariance.Variance.NONE)
                                {
                                    constraint.uvar =  DefinitionSiteVariance.Variance.CONTRAVARIANT;
                                }
                                else
                                {
                                    constraint.uvar = definitionSiteVariance.meet(constraint.uvar, DefinitionSiteVariance.Variance.CONTRAVARIANT);
                                }
                            }
                        }
                        else if (constraint.dvar.equals(dvar) && method_name.equals("println"))
                        {
                            if (constraint.uvar == DefinitionSiteVariance.Variance.NONE)
                            {
                                constraint.uvar =  DefinitionSiteVariance.Variance.BIVARIANT;
                            }
                            else
                            {
                                constraint.uvar = definitionSiteVariance.meet(constraint.uvar, DefinitionSiteVariance.Variance.CONTRAVARIANT);
                            }
                        }
                    }
                }
            }


//            @Override
//            public void visitLocalVariable(PsiLocalVariable variable) {
//                super.visitLocalVariable(variable);
//                PsiType type = variable.getType();
//                if (type instanceof PsiClassType) {
//                    PsiClassType classType = (PsiClassType) type;
//                    PsiTypeParameter typeParameter = definitionSiteVariance.getTypeParameter(classType);
//                   // if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
//                        PsiClass resolvedClass = classType.resolve();
//                     //   if (resolvedClass != null) {
//                   //         updateConstraintWithUvar(dvar, null, DefinitionSiteVariance.Variance.COVARIANT, null);
//                      //  }
//                  //  }
//                }
//            }


//            @Override
//            public void visitReturnStatement(PsiReturnStatement statement) {
//                super.visitReturnStatement(statement);
//                PsiExpression returnValue = statement.getReturnValue();
//                if (returnValue != null) {
//                    PsiType returnType = returnValue.getType();
//                    PsiTypeParameter typeParameter = definitionSiteVariance.getTypeParameter(returnType);
//                 //   if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
//                     //   updateConstraintWithUvar(dvar, null, DefinitionSiteVariance.Variance.COVARIANT, null);
//                  //  }
//                }
//            }

//            @Override
//            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
//                super.visitAssignmentExpression(expression);
//                PsiExpression lExpression = expression.getLExpression();
//                PsiExpression rExpression = expression.getRExpression();
//                if (lExpression != null && rExpression != null) {
//                    PsiType lType = lExpression.getType();
//                    PsiType rType = rExpression.getType();
//                    if (lType instanceof PsiClassType && rType instanceof PsiClassType) {
//                        PsiClassType lClassType = (PsiClassType) lType;
//                        PsiClassType rClassType = (PsiClassType) rType;
//                        PsiTypeParameter lTypeParameter = definitionSiteVariance.getTypeParameter(lClassType);
//                        PsiTypeParameter rTypeParameter = definitionSiteVariance.getTypeParameter(rClassType);
//                       // if (lTypeParameter != null && lTypeParameter.equals(dvar.typeParameter)) {
//                            //updateConstraintWithUvar(dvar, null, DefinitionSiteVariance.Variance.CONTRAVARIANT, null);
//                      //  }
//                       // if (rTypeParameter != null && rTypeParameter.equals(dvar.typeParameter)) {
//                       //     updateConstraintWithUvar(dvar, null, DefinitionSiteVariance.Variance.CONTRAVARIANT, null);
//                     //   }
//                    }
//                }
//            }
        }
        );
    }

    // Method to get the qualifier element from PsiReferenceExpression
    private PsiElement getQualifierElement(PsiReferenceExpression expression) {
        PsiExpression qualifierExpression = expression.getQualifierExpression();
        if (qualifierExpression instanceof PsiReferenceExpression) {
            return ((PsiReferenceExpression) qualifierExpression).resolve();
        }
        return null;
    }


    private PsiElement getQualifierElement(PsiMethodCallExpression expression) {
        PsiExpression qualifierExpression = expression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression != null && qualifierExpression instanceof PsiReferenceExpression) {
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) qualifierExpression;
            return referenceExpression.resolve();
        }
        return null;
    }

    private PsiElement getResolvedElement(PsiReferenceExpression expression) {
        if (expression != null) {
            return expression.resolve();
        }
        return null;
    }

    // as I go with the code and the project, I realize more and more the mistakes we have made...
    // This var func of course doesn't cover all the cases...
    // but here we are...
    private DefinitionSiteVariance.Variance simpleVarCalc (PsiType type1, PsiTypeParameter type2) {

        if (type1 != null && type2 != null) {
            if (type1.getCanonicalText().equals(type2.getName()))
            {
                return DefinitionSiteVariance.Variance.COVARIANT;
            }
            else
            {
                return DefinitionSiteVariance.Variance.BIVARIANT;
            }
        }
        return null;
    }
    private boolean isFieldAccessibleFromTypeParameter(PsiClass fieldClass, DefinitionSiteVariance.Dvar dvar) {
        PsiClassType[] extendsListTypes = dvar.typeParameter.getExtendsListTypes();
        if (extendsListTypes.length == 0) {
            return false;
        }
        PsiClassType typeParameterType = extendsListTypes[0];
        PsiClass typeParameterClass = typeParameterType.resolve();
        return typeParameterClass != null && (fieldClass.equals(typeParameterClass) || typeParameterClass.isInheritor(fieldClass, true));
    }

        public PsiType LookupType(PsiElement element) {
        if (element instanceof PsiField) {
            return ((PsiField) element).getType();
        }
        return null;
    }

    public boolean isWriteTarget(PsiElement element) {
        if (element.getParent() instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) element.getParent();
            return assignmentExpression.getLExpression() == element;
        }
        return false;
    }

    public PsiMethod enclosingMethod(PsiElement element) {
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof PsiMethod) {
                return (PsiMethod) element;
            }
            element = element.getParent();
        }
        return null;
    }


    private PsiTypeParameter[] getTypeParameters(PsiMethod method) {
        return method.getTypeParameters();
    }
    private PsiType getReturnType(PsiMethod method) {
        return method.getReturnType();
    }

}

