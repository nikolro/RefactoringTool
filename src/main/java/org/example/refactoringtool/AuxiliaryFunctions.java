// the implementation of the Auxiliary Functions in Figure 5 in the paper used in the other sections.

package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.util.Query;
import java.util.HashSet;
import java.util.Set;


public class AuxiliaryFunctions {

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

    //like if have method a(){int x} and run the functon on x i its return a
    public PsiMethod enclosingMethod(PsiElement element) {
        while (element != null && !(element instanceof PsiFile)) {
            if (element instanceof PsiMethod) {
                return (PsiMethod) element;
            }
            element = element.getParent();
        }
        return null;
    }

    // Find all methods in the class hierarchy that either override the given method or are overridden by it
    public Set<PsiMethod> hierarchyMethods(PsiMethod method) {
        Set<PsiMethod> methods = new HashSet<>();
        PsiClass containingClass = method.getContainingClass();
        if (containingClass != null) {
            for (PsiMethod superMethod : method.findSuperMethods()) {
                methods.add(superMethod);
            }
            Query<PsiMethod> query = OverridingMethodsSearch.search(method);
            for (PsiMethod overridingMethod : query) {
                methods.add(overridingMethod);
            }
        }
        return methods;
    }

    // Find all parameters in the hierarchy of the given method that are in the same position as the given parameter
    public Set<PsiParameter> hierarchyParams(PsiParameter parameter) {
        Set<PsiParameter> parameters = new HashSet<>();
        PsiMethod method = (PsiMethod) parameter.getDeclarationScope();
        if (method != null) {
            int paramIndex=-1;
            PsiParameter[] methodParams = method.getParameterList().getParameters();
            for (int i = 0; i < methodParams.length; i++) {
                if (methodParams[i].equals(parameter)) {
                    paramIndex=i;
                }
            }
            if (paramIndex != -1) {
                Set<PsiMethod> methods = hierarchyMethods(method);
                methods.add(method);
                for (PsiMethod m : methods) {
                    PsiParameter[] params = m.getParameterList().getParameters();
                    if (paramIndex < params.length) {
                        parameters.add(params[paramIndex]);
                    }
                }
            }
        }
        return parameters;
    }

    // Find all nodes affecting the type of the given expression
    public Set<PsiElement> nodesAffectingType(PsiElement expression) {
        Set<PsiElement> nodes = new HashSet<>();
        if (expression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expression;
            PsiMethod method = Lookup(methodCall);
            if (method != null) {
                if (returnTypeDependsOnParams(method)) {
                    // N-GENERICMETHOD
                    nodes.add(method);
                    for (PsiExpression arg : methodCall.getArgumentList().getExpressions()) {
                        nodes.addAll(nodesAffectingType(arg));
                    }
                } else {
                    // N-MONOMETHOD
                    nodes.add(method);
                }
                return nodes;
            }
        }
        // N-NONMETHODCALL
        nodes.addAll(accessedNodes((PsiExpression) expression));
        return nodes;
    }

    // Find the destination node for a given expression
    public PsiElement destinationNode(PsiElement expression) {
        PsiElement parent = expression.getParent();
        if (parent instanceof PsiExpressionList && parent.getParent() instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) parent.getParent();
            PsiMethod method = Lookup(methodCall);
            if (method != null) {
                // D-METHODCALL
                PsiExpression[] arguments = methodCall.getArgumentList().getExpressions();
                for (int i = 0; i < arguments.length; i++) {
                    if (arguments[i].equals(expression)) {
                        return method.getParameterList().getParameters()[i];
                    }
                }
            }
        }// Check if the expression is part of an assignment
        else if (parent instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignment = (PsiAssignmentExpression) parent;
            PsiExpression lhs = assignment.getLExpression();
            PsiExpression rhs = assignment.getRExpression();

            // If the expression is the RHS of the assignment
            if (rhs.equals(expression)) {
                // D-ASSIGNMENT
                return varDecl(lhs);
            }
        }     // Check if the expression is the return value of a return statement
        else if (parent instanceof PsiReturnStatement) {
            PsiReturnStatement returnStatement = (PsiReturnStatement) parent;
            PsiExpression returnValue = returnStatement.getReturnValue();

            // If the expression is the return value
            if (returnValue != null && returnValue.equals(expression)) {
                // D-RETURN
                return enclosingMethod(returnStatement);
            }
        }
        return null;
    }

}



