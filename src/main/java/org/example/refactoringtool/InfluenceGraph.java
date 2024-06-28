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

    public void addEdge(PsiElement from, PsiElement to) {
       if (isDeclarationWithTypeParameter(from) && isDeclarationWithTypeParameter(to)) {
            // Ensure both nodes are in the graph
            graph.putIfAbsent(from, new ArrayList<>());
            graph.putIfAbsent(to, new ArrayList<>());
            // Add the edge
            graph.get(from).add(to);
        }
    }

    private boolean isDeclarationWithTypeParameter(PsiElement element) {
        if (isDeclaration(element)) {
            PsiType type = getTypeOfDeclaration(element);

            // Check if the declaration type uses type parameters
            return usesTypeParameter(type);
        }
        return false;
    }

    private boolean isDeclaration(PsiElement element) {
        return element instanceof PsiMethod ||
                element instanceof PsiField ||
                element instanceof PsiLocalVariable ||
                element instanceof PsiParameter;
    }

    private PsiType getTypeOfDeclaration(PsiElement element) {
        if (element instanceof PsiMethod) {
            return ((PsiMethod) element).getReturnType();
        } else if (element instanceof PsiField) {
            return ((PsiField) element).getType();
        } else if (element instanceof PsiLocalVariable) {
            return ((PsiLocalVariable) element).getType();
        } else if (element instanceof PsiParameter) {
            return ((PsiParameter) element).getType();
        }
        return null;
    }

    private boolean usesTypeParameter(PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiType[] typeArguments = classType.getParameters();

            // Check if the class type has any type arguments
            return typeArguments.length > 0;
        }
        // Array types and wildcard types are not considered to have type parameters in this context
        return false;
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

    public List<PsiElement> getElement(PsiElement element) {
        // Return the list of elements directly influenced by the given element
        return graph.getOrDefault(element, Collections.emptyList());
    }

    public List<PsiElement> getAllInfluencedElements(PsiElement element) {
        Set<PsiElement> visited = new HashSet<>();
        List<PsiElement> influencedElements = new ArrayList<>();
        dfs(element, visited, influencedElements);
        return influencedElements;
    }

    private void dfs(PsiElement element, Set<PsiElement> visited, List<PsiElement> influencedElements) {
        if (visited.contains(element)) {
            return;
        }
        visited.add(element);
        List<PsiElement> directlyInfluenced = graph.get(element);
        if (directlyInfluenced != null) {
            for (PsiElement influenced : directlyInfluenced) {
                if (!visited.contains(influenced)) {
                    influencedElements.add(influenced);
                    dfs(influenced, visited, influencedElements);
                }
            }
        }
    }

    public List<PsiElement> getInfluencedElements(PsiElement element) {
        List<PsiElement> influencedElements = new ArrayList<>();
        for (Map.Entry<PsiElement, List<PsiElement>> entry : graph.entrySet()) {
            PsiElement key = entry.getKey();
            List<PsiElement> values = entry.getValue();
            if (values.contains(element)) {
                influencedElements.add(key);
            }
        }
        return influencedElements;
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


