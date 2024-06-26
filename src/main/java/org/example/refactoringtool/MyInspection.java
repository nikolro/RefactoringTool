package org.example.refactoringtool;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
    private MethodsParamertersCheck methodsParamertersCheck;
    private MethodBodyAnalysis methodBodyAnalysis;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        this.holder = holder;
        this.definitionSiteVariance = new DefinitionSiteVariance();
        this.methodsParamertersCheck=new MethodsParamertersCheck(definitionSiteVariance,this.holder);
        this.auxiliaryFunctions = new AuxiliaryFunctions();
        this.influenceGraph = new InfluenceGraph();
        this.analysis4_2 = new FlowDependenciesfromQualifiers(influenceGraph, auxiliaryFunctions);
        this.analysis4_3 = new ExpressionTargets(influenceGraph, auxiliaryFunctions);
        this.analysis4_4 = new DependenciesfromInheritance(influenceGraph, auxiliaryFunctions);
        this.analysis4_6 = new NonRewritableOverrides(influenceGraph, auxiliaryFunctions);
        this.visitor = createVisitor();
        return this.visitor;
    }

    private JavaElementVisitor createVisitor() {
        return new JavaElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                definitionSiteVariance.analyze(file);
                methodsParamertersCheck.analyze(file);
                definitionSiteVariance.printMap();
//                analysis4_2.analyze(file);
//                analysis4_3.analyze(file);
//                analysis4_4.analyze(file);
//                analysis4_6.analyze(file);
//                influenceGraph.printGraph();
            }
        };
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
