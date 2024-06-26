//InfluenceGraph creating and initializing before 4.2 section

package org.example.refactoringtool;

import com.intellij.psi.*;

import java.util.*;

public class InfluenceGraph {

    private final Map<PsiElement,List<PsiElement>> graph;
    private final Set<PsiElement> nonRewritableDeclarations;

    public InfluenceGraph() {
        graph = new HashMap<>();
        nonRewritableDeclarations=new HashSet<>();
    }

    public void addNode(PsiElement element) {
        graph.putIfAbsent(element,new ArrayList<>());
    }

    public void addEdge(PsiElement from, PsiElement to) {
        if (isMethodParamWithTypeParameter(from) && isMethodParamWithTypeParameter(to)) {
            // Ensure both nodes are in the graph
            graph.putIfAbsent(from, new ArrayList<>());
            graph.putIfAbsent(to, new ArrayList<>());
            // Add the edge
            graph.get(from).add(to);
        }
    }

    private boolean isMethodParamWithTypeParameter(PsiElement element) {
        if (element instanceof PsiParameter) {
            PsiParameter param = (PsiParameter) element;
            PsiType paramType = param.getType();

            // Check if the parameter type uses type parameters
            return usesTypeParameter(paramType);
        }
        return false;
    }

    private boolean usesTypeParameter(PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiType[] typeArguments = classType.getParameters();

            // Check if any of the type arguments are type parameters
            for (PsiType typeArg : typeArguments) {
                if (typeArg instanceof PsiTypeParameter) {
                    return true;
                }
                // Recursively check nested types
                if (usesTypeParameter(typeArg)) {
                    return true;
                }
            }

            // Check if the type itself is a type parameter
            PsiClass resolvedClass = classType.resolve();
            return resolvedClass instanceof PsiTypeParameter;
        } else if (type instanceof PsiArrayType) {
            // Check array component type
            return usesTypeParameter(((PsiArrayType) type).getComponentType());
        } else if (type instanceof PsiWildcardType) {
            // Check wildcard bound
            PsiType bound = ((PsiWildcardType) type).getBound();
            return bound != null && usesTypeParameter(bound);
        }

        return false;
    }

    private boolean isDeclaration(PsiElement element) {
        return element instanceof PsiMethod ||
                element instanceof PsiField ||
                element instanceof PsiLocalVariable ||
                element instanceof PsiParameter;
    }
    public void markNonRewritable(PsiElement element) {
        nonRewritableDeclarations.add(element);
    }

    public boolean isNonRewritable(PsiElement element) {
        return nonRewritableDeclarations.contains(element);
    }

    public Set<PsiElement> getNonRewritableDeclarations() {
        return nonRewritableDeclarations;
    }

    public List<PsiElement> getInfluencedElements(PsiElement element) {
        return graph.get(element);
    }
    public List<PsiElement> getInfluencingElements(PsiElement element) {
        List<PsiElement> influencingElements = new ArrayList<>();
        for (Map.Entry<PsiElement, List<PsiElement>> entry : graph.entrySet()) {
            PsiElement key = entry.getKey();
            List<PsiElement> values = entry.getValue();
            if (values.contains(element)) {
                influencingElements.add(key);
            }
        }
        return influencingElements;
    }

    public void printGraph() {
        System.out.println("****** Influence Graph ******");
        for (Map.Entry<PsiElement, List<PsiElement>> entry : graph.entrySet()) {
            PsiElement key = entry.getKey();
            List<PsiElement> values = entry.getValue();
            printNode(key);
            for (PsiElement value : values) {
                System.out.println("  Edge to: " + value);
            }
        }
        System.out.println("******");
    }

    public void printNode(PsiElement element)
    {
        if(element instanceof PsiMethod){
            System.out.println("PsiMethod: "+ ((PsiMethod) element).getName());
        }
        else if(element instanceof PsiField) {
            System.out.println("PsiField: "+ ((PsiField) element).getName());
        }
        else if(element instanceof PsiParameter) {
            System.out.println("PsiParameter: "+ ((PsiParameter) element).getText());
        }
        else if(element instanceof PsiLocalVariable) {
            System.out.println("PsiLocalVariable: "+ ((PsiLocalVariable) element).getName());
        }
        else
        {
            System.out.println("Node is not declaration");
            System.out.println(element.toString());
        }

    }

    public void printNonRewritableDeclarations() {
        System.out.println("****** Non-rewritable Declarations ******");
        for (PsiElement element : getNonRewritableDeclarations()) {
            System.out.println("Non-rewritable: ");
            printNode(element);
        }
        System.out.println("*****************************************");
    }
}


