//i dont know yet

package org.example.refactoringtool;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


public class MyInspection extends AbstractBaseJavaLocalInspectionTool {

    private ProblemsHolder holder;
    private JavaElementVisitor visitor;
    private InfluenceGraph influenceGraph;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        this.holder = holder;
        this.visitor = createVisitor();
        this.influenceGraph = new InfluenceGraph(visitor);
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
}
