// the implementation of the Auxiliary Functions in Figure 5 in the paper used in the other sections.

package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.HashSet;
import java.util.Set;


public class AuxiliaryFunctions {
    private JavaElementVisitor visitor;
    public InfluenceGraph graph;

    public AuxiliaryFunctions(JavaElementVisitor visitor, InfluenceGraph graph) {
        this.visitor = visitor;
        this.graph = graph;
    }

    //takes a method call and return the method declaration
    public PsiMethod Lookup(PsiMethodCallExpression expression) {
        return expression.resolveMethod();
    }

    //check if the return type and one of the parameters of the method depend on a one of the types parameters of the method
    public boolean returnTypeDependsOnParams(PsiMethod method) {
        boolean returnTypeDependsOnTypeParameter = false;
        boolean oneOfParamsDependsOnTypeParameter = false;
        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            return false;
        }
        //getting the function type parameters
        PsiTypeParameter[] typeParameters = method.getTypeParameters();
        Set<String> typeParameterNames = new HashSet<>();
        for (PsiTypeParameter typeParameter : typeParameters) {
            typeParameterNames.add(typeParameter.getName());
        }
        //check if the return type uses one of the function type parameter
        if (usesTypeParameter(returnType, typeParameterNames)) {
            returnTypeDependsOnTypeParameter=true;
        }
        else {
            return false;
        }
        //check if one of the function parameter uses one of the type parameter
        PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            PsiType parameterType = parameter.getType();
            if (usesTypeParameter(parameterType, typeParameterNames)) {
                oneOfParamsDependsOnTypeParameter=true;
            }
        }
        if(oneOfParamsDependsOnTypeParameter && returnTypeDependsOnTypeParameter)
        {
            return true;
        }
        return false;
    }

    //helper function for returnTypeDependsOnParams checks if the type uses type parameter
    private boolean usesTypeParameter(PsiType type, Set<String> typeParameterNames) {
        //check if type is like List<T>
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiType[] typeArguments = classType.getParameters();
            for (PsiType typeArgument : typeArguments) {
                if (usesTypeParameter(typeArgument, typeParameterNames)) {
                    return true;
                }
            }
            PsiClass resolvedClass = classType.resolve();
            if (resolvedClass instanceof PsiTypeParameter) {
                return typeParameterNames.contains(resolvedClass.getName());
            }
        }

        //check if type is like T
        else if (type instanceof PsiTypeParameter) {
            return typeParameterNames.contains(type.getCanonicalText());

            //check if type is like T[]
        }else if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            return usesTypeParameter(componentType, typeParameterNames);

            //check if type is like List<? extends T>
        } else if (type instanceof PsiWildcardType) {
            PsiType bound = ((PsiWildcardType) type).getBound();
            return usesTypeParameter(bound, typeParameterNames);
        }
        return false;
    }


    public PsiVariable varDecl(PsiExpression expression) {
        if (expression instanceof PsiReferenceExpression) {
            PsiElement resolvedElement = ((PsiReferenceExpression) expression).resolve();
            if (resolvedElement instanceof PsiVariable) {
                return (PsiVariable) resolvedElement;
            }
        }
        return null;
    }

    // return all the variables or methods declarations in the expression
    public Set<PsiElement> accessedNodes(PsiExpression expression) {
        Set<PsiElement> accessedElements = new HashSet<>();
        expression.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression ref) {
                super.visitReferenceExpression(ref);
                PsiElement resolvedElement = ref.resolve();
                if (resolvedElement instanceof PsiVariable || resolvedElement instanceof PsiMethod) {
                    accessedElements.add(resolvedElement);
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression methodCall) {
                super.visitMethodCallExpression(methodCall);
                methodCall.getMethodExpression().accept(this);
                for (PsiExpression argument : methodCall.getArgumentList().getExpressions()) {
                    argument.accept(this);
                }
            }

            @Override
            public void visitNewExpression(PsiNewExpression newExpression) {
                super.visitNewExpression(newExpression);
                PsiJavaCodeReferenceElement classReference = newExpression.getClassReference();
                if (classReference != null) {
                    classReference.accept(this);
                }
                PsiExpressionList argumentList = newExpression.getArgumentList();
                if (argumentList != null) {
                    for (PsiExpression argument : argumentList.getExpressions()) {
                        argument.accept(this);
                    }
                }
            }
        });
        return accessedElements;
    }


}



