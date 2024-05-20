//the implementation of the hole algorithm in section 4.5

package org.example.refactoringtool;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


public class MyInspection extends AbstractBaseJavaLocalInspectionTool {

    private ProblemsHolder holder;
    private JavaElementVisitor visitor;
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;
    private FlowDependenciesfromQualifiers  analysis4_2;
    private ExpressionTargets analysis4_3;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        this.holder = holder;
        this.auxiliaryFunctions = new AuxiliaryFunctions();
        this.influenceGraph = new InfluenceGraph();
        this.analysis4_2= new FlowDependenciesfromQualifiers(influenceGraph,auxiliaryFunctions);
        this.analysis4_3=new ExpressionTargets(influenceGraph,auxiliaryFunctions);
        this.visitor = createVisitor();
        return this.visitor;
    }

    private JavaElementVisitor createVisitor() {
        return new JavaElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                influenceGraph.processDeclarations(file);
                analysis4_2.analyze(file);
                analysis4_3.analyze(file);
                influenceGraph.printGraph();
                generateSuggestions();
            }
        };
    }

    private void generateSuggestions() {
        for (Map.Entry<PsiElement, List<PsiElement>> entry : influenceGraph.graph.entrySet()) {
            PsiElement node = entry.getKey();
            if (node instanceof PsiMethod) {
                // Check if this method can be refactored to use wildcards and register a suggestion
                holder.registerProblem(node, "Consider using wildcards for better generality", new MyQuickFix());
            }
        }
    }
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
