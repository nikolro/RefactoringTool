//i dont know yet

package org.example.refactoringtool;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


public class MyInspection extends AbstractBaseJavaLocalInspectionTool {

    private ProblemsHolder holder;
    private JavaElementVisitor visitor;
    private InfluenceGraph influenceGraph;
    private AuxiliaryFunctions auxiliaryFunctions;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        this.holder = holder;
        this.visitor = createVisitor();
        this.auxiliaryFunctions = new AuxiliaryFunctions();
        this.influenceGraph = new InfluenceGraph();
        return this.visitor;
    }

    private JavaElementVisitor createVisitor() {
        return new JavaElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                influenceGraph.processDeclarations(file);
                influenceGraph.printGraph();
            }
        };
    }
    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getGroupDisplayName() {
        return "Java";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDisplayName() {
        return "My Custom Inspection";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getStaticDescription() {
        return "This inspection detects specific issues and provides suggestions.";
    }
}
