////the implementation of the Algorithm from section 4.5
package org.example.refactoringtool;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyInspection extends AbstractBaseJavaLocalInspectionTool {

    private ProblemsHolder holder;
    private JavaElementVisitor visitor;
    private DefinitionSiteVariance definitionSiteVariance;
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;
    private FlowDependenciesfromQualifiers analysis4_2;
    private ExpressionTargets analysis4_3;
    private DependenciesfromInheritance analysis4_4;
    private NonRewritableOverrides analysis4_6;
    private MethodBodyAnalysis methodBodyAnalysis;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        this.holder = holder;
        this.definitionSiteVariance=new DefinitionSiteVariance();
        this.auxiliaryFunctions = new AuxiliaryFunctions();
        this.influenceGraph = new InfluenceGraph();
        this.analysis4_2 = new FlowDependenciesfromQualifiers(influenceGraph, auxiliaryFunctions);
        this.analysis4_3 = new ExpressionTargets(influenceGraph, auxiliaryFunctions);
        this.analysis4_4 = new DependenciesfromInheritance(influenceGraph, auxiliaryFunctions);
        this.analysis4_6 = new NonRewritableOverrides(influenceGraph, auxiliaryFunctions);
        //this.methodBodyAnalysis=new MethodBodyAnalysis(influenceGraph,auxiliaryFunctions);
        this.visitor = createVisitor();
        return this.visitor;
    }

    private JavaElementVisitor createVisitor() {
        return new JavaElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                definitionSiteVariance.analyze(file);
                analysis4_2.analyze(file);
                analysis4_3.analyze(file);
                analysis4_4.analyze(file);
                analysis4_6.analyze(file);
                //methodBodyAnalysis.analyze(file);
                definitionSiteVariance.printMap();
                influenceGraph.printGraph();
                influenceGraph.printNonRewritableDeclarations();
                //generateSuggestions();
            }
        };
    }

    /*
    private void generateSuggestions() {
        for (PsiElement declaration : influenceGraph.getDeclarations()) {
            if (influenceGraph.isRewritable(declaration)) {
                PsiType originalType = getType(declaration);
                PsiType refactoredType = getRefactoredType(declaration);
                if (!originalType.equals(refactoredType)) {
                    holder.registerProblem(declaration, "Type can be generalized with wildcards",
                            new MyQuickFix(declaration, refactoredType));
                }
            }
        }
    }

    private PsiType getType(PsiElement declaration) {
        if (declaration instanceof PsiVariable) {
            return ((PsiVariable) declaration).getType();
        } else if (declaration instanceof PsiMethod) {
            return ((PsiMethod) declaration).getReturnType();
        }
        return null;
    }

    private PsiType getRefactoredType(PsiElement declaration) {
        // Implement the logic to compute the refactored type based on the analysis results
        // You can use the definition-site variance and use-site variance information
        // to determine the appropriate wildcard annotations
        // For example:
        // if (declaration instanceof PsiVariable) {
        //     PsiVariable variable = (PsiVariable) declaration;
        //     PsiType originalType = variable.getType();
        //     if (originalType instanceof PsiClassType) {
        //         PsiClassType classType = (PsiClassType) originalType;
        //         PsiClass psiClass = classType.resolve();
        //         if (psiClass != null) {
        //             PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
        //             // Compute the refactored type based on the type parameters and variances
        //             // ...
        //         }
        //     }
        // }
        // Return the refactored type
        return null;
    }

     */



    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getGroupDisplayName() {
        return "Java";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDisplayName() {
        return "MyInspection";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getStaticDescription() {
        return "This inspection detects specific issues and provides suggestions.";
    }
}
