//the implementation of the Analysis from section 4.6
package org.example.refactoringtool;

import com.intellij.psi.*;

import java.util.*;

public class NonRewritableOverrides {
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    public NonRewritableOverrides(InfluenceGraph influenceGraph, AuxiliaryFunctions auxiliaryFunctions) {
        this.influenceGraph = influenceGraph;
        this.auxiliaryFunctions = auxiliaryFunctions;
    }
    public void analyze(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                if (isNonRewritable(method)) {
                    influenceGraph.markNonRewritable(method);
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression methodDec) {
                super.visitMethodCallExpression(methodDec);
                if (isNonRewritable(methodDec)) {
                    influenceGraph.markNonRewritable(methodDec);
                }
            }

            @Override
            public void visitParameter(PsiParameter parameter) {
                super.visitParameter(parameter);
                if (isNonRewritable(parameter)) {
                    influenceGraph.markNonRewritable(parameter);
                }
            }

            @Override
            public void visitClass(PsiClass psiClass) {
                super.visitClass(psiClass);
                if (isNonRewritable(psiClass)) {
                    influenceGraph.markNonRewritable(psiClass);
                }
            }

            @Override
            public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
                super.visitReferenceElement(reference);
                if (isParentTypeDeclaration(reference)) {
                    PsiElement resolved = reference.resolve();
                    if (resolved != null) {
                        if (resolved instanceof PsiClass) {
                            PsiClass resolvedClass = (PsiClass) resolved;
                            markClassMethodsAndFieldsNonRewritable(resolvedClass);
                        }
                    }
                }
            }
        });
        propagateNonRewritability();
    }

    private boolean isNonRewritable(PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            if (isNonStaticNonFinal(method) || isExternalMethod(method)) {
                PsiType returnType = method.getReturnType();
                if (returnType != null && isSimpleTypeVariable(returnType)) {
                    return true;
                }
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    PsiType paramType = parameter.getType();
                    if (isSimpleTypeVariable(paramType) || isExternalParameter(parameter)) {
                        return true;
                    }
                }
            }
        }
        if(element instanceof PsiMethodCallExpression)
        {
            PsiMethod methodDec=auxiliaryFunctions.Lookup((PsiMethodCallExpression)element);
            if(isExternalMethod(methodDec)) {
                return true;
            }
        }
        if(element instanceof PsiParameter)
        {
            if(isExternalParameter((PsiParameter) element))
            {
                return true;
            }
        }
        if (isParentTypeDeclaration(element)) {
            return true;
        }
        return false;
    }

    private boolean isSimpleTypeVariable(PsiType type) {
        return type instanceof PsiTypeParameter;
    }

    private boolean isNonStaticNonFinal(PsiMethod method) {
        return !method.hasModifierProperty(PsiModifier.STATIC) && !method.hasModifierProperty(PsiModifier.FINAL);
    }

    private void markClassMethodsAndFieldsNonRewritable(PsiClass psiClass) {
        for (PsiMethod method : psiClass.getMethods()) {
            influenceGraph.markNonRewritable(method);
        }
        for (PsiField field : psiClass.getFields()) {
            influenceGraph.markNonRewritable(field);
        }
    }

    private boolean isParentTypeDeclaration(PsiElement element) {
        if (element instanceof PsiJavaCodeReferenceElement) {
            PsiJavaCodeReferenceElement referenceElement = (PsiJavaCodeReferenceElement) element;
            PsiElement parent = referenceElement.getParent();
            if (parent instanceof PsiReferenceList) {
                PsiReferenceList referenceList = (PsiReferenceList) parent;
                return isExtendsOrImplementsList(referenceList);
            }
        }
        return false;
    }

    private boolean isExtendsOrImplementsList(PsiReferenceList referenceList) {
        PsiElement parent = referenceList.getParent();
        return parent instanceof PsiClass &&
                (referenceList.equals(((PsiClass) parent).getExtendsList()) ||
                        referenceList.equals(((PsiClass) parent).getImplementsList()));
    }
    private boolean isExternalMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return true; // Treat methods without a containing class as external
        }
        String qualifiedName = containingClass.getQualifiedName();
        // Check if the qualified name belongs to java.* or other known libraries
        return qualifiedName != null && (qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.") || qualifiedName.startsWith("org.") || qualifiedName.startsWith("com."));
    }

    private boolean isExternalParameter(PsiParameter parameter) {
        PsiType paramType = parameter.getType();
        if (paramType instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) paramType).resolve();
            if (psiClass != null) {
                String qualifiedName = psiClass.getQualifiedName();
                return qualifiedName != null && (qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.") || qualifiedName.startsWith("org.") || qualifiedName.startsWith("com."));
            }
        }
        return false;
    }

    private void propagateNonRewritability() {
        Set<PsiElement> visited = new HashSet<>();
        Queue<PsiElement> toProcess = new LinkedList<>(influenceGraph.getNonRewritableDeclarations());

        while (!toProcess.isEmpty()) {
            PsiElement current = toProcess.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            //propagateNonRewritability(current, visited, toProcess);
        }
    }

    /*
    private void propagateNonRewritability(PsiElement element, Set<PsiElement> visited, Queue<PsiElement> toProcess) {
        List<PsiElement> influencingElements = influenceGraph.getInfluencingElements(element);
        if (influencingElements != null) {
            for (PsiElement influencer : influencingElements) {
                if (!influenceGraph.isNonRewritable(influencer)) {
                    influenceGraph.markNonRewritable(influencer);
                    toProcess.add(influencer);
                }
            }
        }
    }
     */

}
