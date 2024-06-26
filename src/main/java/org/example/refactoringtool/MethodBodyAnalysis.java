package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.List;

class MethodBodyAnalysis {

    private List<DefinitionSiteVariance.Constraint> constraints_list;
    private List<DefinitionSiteVariance.Dvar> dvars_list;
    private DefinitionSiteVariance definitionSiteVariance;

    List<DefinitionSiteVariance.Constraint> getConstraints_list() {return constraints_list;}

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
                          public PsiMethod Lookup(PsiMethodCallExpression expression) {
                              return expression.resolveMethod();
                          }

                          @Override
                          public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                              super.visitMethodCallExpression(expression);
                              PsiElement qualifierElement = getQualifierElement(expression);
                              PsiMethod enclosingMethod = enclosingMethod(qualifierElement);

                              PsiMethod calledMethod = Lookup (expression);

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