package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.intellij.psi.PsiTypeElement;

import java.util.*;


public class DefinitionSiteVariance {
    class Dvar
    {
        PsiTypeParameter typeParameter;
        PsiElement element;
        Variance variance;
        public Dvar(PsiTypeParameter typeParameter,PsiElement element,Variance variance)
        {
            this.typeParameter=typeParameter;
            this.element=element;
            this.variance=variance;
        }
    }

    private List<Dvar> variances;

    public DefinitionSiteVariance() {
        this.variances = new ArrayList<>();
    }

    public enum Variance {
        COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT,NONE
    }

    public void analyze(PsiElement root) {
        //fill tha List
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitTypeParameter(PsiTypeParameter typeParameter) {
                super.visitTypeParameter(typeParameter);
                PsiElement owner = typeParameter.getOwner();
                variances.add(new Dvar(typeParameter,owner,Variance.INVARIANT));
            }
        });
        maxDefinitionVariance();
    }

    private Variance Transformation(Variance v1, Variance v2) {
        if (v1 == Variance.BIVARIANT || v2 == Variance.BIVARIANT) {
            return Variance.BIVARIANT;
        }
        if (v1 == Variance.INVARIANT || v2 == Variance.INVARIANT) {
            return Variance.INVARIANT;
        }
        if (v1 == v2) {
            return v1;
        }
        return Variance.COVARIANT;
    }

    private Variance join(Variance v1, Variance v2) {
        if (v1 == Variance.BIVARIANT || v2 == Variance.BIVARIANT) {
            return Variance.BIVARIANT;
        }
        if (v1 == Variance.INVARIANT && v2 == Variance.INVARIANT) {
            return Variance.INVARIANT;
        }
        if (v1 == Variance.INVARIANT) {
            return v2;
        }
        if (v2 == Variance.INVARIANT) {
            return v1;
        }
        return Variance.COVARIANT;
    }

    private void maxDefinitionVariance() {
        boolean changed=false;
        do{
            for(Dvar dvar:variances) {
                changed = collectConstraints(dvar);
            }
        }while (changed==true);

    }

    private boolean collectConstraints(Dvar dvar) {
        PsiElement owner = dvar.element;
        PsiTypeParameter typeParameter=dvar.typeParameter;
        Variance currentVariance=dvar.variance;

        LocalSearchScope scope = new LocalSearchScope(typeParameter.getParent().getParent());
        Query<PsiReference> query = ReferencesSearch.search(typeParameter, scope);

        for (PsiReference reference : query) {
            PsiElement element = reference.getElement();
            PsiElement parent = element.getParent();
            while (parent instanceof PsiTypeElement) {
                element = parent;
                parent = element.getParent();
            }
            PsiTypeElement typeElement=(PsiTypeElement) element;
            Variance var=checkWildcards(typeElement);
            if(var ==Variance.NONE)
            {
                if (parent instanceof PsiReferenceParameterList) {
                }
                else if(parent instanceof  PsiMethod){
                }
            }
            else {
                PsiType psiType = typeElement.getType();
                processTypeElement(typeElement);
                if (parent instanceof PsiReferenceParameterList) {
                } else if (parent instanceof PsiMethod) {
                }
            }
        }
        return false;
    }

    public void processTypeElement(PsiTypeElement typeElement) {
        // Step 1: Navigate to the PsiReferenceParameterList
        PsiElement parent = typeElement.getParent();
        if (parent instanceof PsiReferenceParameterList) {
            PsiReferenceParameterList parameterList = (PsiReferenceParameterList) parent;

            // Step 2: Get the parent PsiJavaCodeReferenceElement
            PsiElement grandParent = parameterList.getParent();
            if (grandParent instanceof PsiJavaCodeReferenceElement) {
                PsiJavaCodeReferenceElement referenceElement = (PsiJavaCodeReferenceElement) grandParent;

                // Step 3: Resolve the PsiJavaCodeReferenceElement to get the PsiClass
                PsiElement resolvedElement = referenceElement.resolve();
                if (resolvedElement instanceof PsiClass) {
                    PsiClass psiClass = (PsiClass) resolvedElement;
                    System.out.println("Class using the type element: " + psiClass.getQualifiedName());

                    // Step 4: Get the type parameter declarations from the PsiClass
                    PsiTypeParameterList typeParameterList = psiClass.getTypeParameterList();
                    if (typeParameterList != null) {
                        for (PsiTypeParameter typeParameter : typeParameterList.getTypeParameters()) {
                            System.out.println("Type parameter declaration: " + typeParameter.getName());
                        }
                    }
                }
            }
        }
    }

    public Variance checkWildcards(PsiTypeElement typeElement) {
        PsiType psiType = typeElement.getType();

        if (psiType instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) psiType;

            if (wildcardType.isExtends()) {
                return Variance.COVARIANT;
            } else if (wildcardType.isSuper()) {
                return Variance.CONTRAVARIANT;
            } else {
                return Variance.BIVARIANT;
            }
        } else if (psiType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) psiType;
            PsiType[] parameters = classType.getParameters();

            for (PsiType param : parameters) {
                if (param instanceof PsiTypeElement) {
                    checkWildcards((PsiTypeElement) param);
                }
            }
        } else {
            return Variance.NONE;
        }
        return Variance.NONE;
    }

    public void printMap() {
        System.out.println("****** Definition-site variances ******");
        System.out.println("******");
    }
}
