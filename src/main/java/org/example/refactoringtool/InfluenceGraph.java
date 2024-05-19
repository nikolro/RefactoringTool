//InfluenceGraph creating and initializing before 4.2 section

package org.example.refactoringtool;

import com.intellij.psi.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class InfluenceGraph {

    public final Map<PsiElement,List<PsiElement>> graph;

    public InfluenceGraph() {
        graph = new HashMap<>();
    }

    public void addNode(PsiElement element) {
        graph.putIfAbsent(element,new ArrayList<>());
    }

    public void  addEdge(PsiElement from, PsiElement to) {
        graph.get(from).add(to);
    }
    private boolean isDeclaration(PsiElement element) {
        return element instanceof PsiMethod ||
                element instanceof PsiField ||
                element instanceof PsiVariable ||
                element instanceof PsiParameter;
    }

    public void processDeclarations(PsiElement root) {
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (isDeclaration(element)) {
                    addNode(element);
                }
            }
        });
    }
    public void printGraph() {
        System.out.println("******");
        for (Map.Entry<PsiElement, List<PsiElement>> entry : graph.entrySet()) {
            PsiElement key = entry.getKey();
            List<PsiElement> values = entry.getValue();
            System.out.println("Node: " + key);
            for (PsiElement value : values) {
                System.out.println("  Edge to: " + value);
            }
        }
        System.out.println("******");
    }

}


