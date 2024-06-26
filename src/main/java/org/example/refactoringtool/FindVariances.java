//find the variances and combine with the uvars
package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindVariances {

    public enum Variance {
        COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT, NONE
    }

    static class Dvar {
        PsiTypeParameter typeParameter;
        PsiElement element;
        PsiClass ownerClass;
        Variance var;

        public Dvar(PsiTypeParameter typeParameter, PsiElement element, PsiClass ownerClass, Variance var) {
            this.typeParameter = typeParameter;
            this.element = element;
            this.ownerClass = ownerClass;
            this.var = var;
        }
        @Override
        public String toString() {
            return "(" + ownerClass + "," + typeParameter + ")";
        }
    }

    public static class Constraint {
        Dvar dvar; // our dvar that is on the left side of the equation
        Variance var_type; // arg type or return type
        Dvar dependant_var; // the var we are dependent on
        Variance var_wilcard;  // variance to join with because of wild card
        Variance uvar;
        //dvar <= var_type *transform* (dependant_var *join* var_wildcard)

        public Constraint(Dvar dvar,Variance var_type,Dvar dependant_var,Variance var_wilcard,Variance uvar) {
            this.dvar = dvar;
            this.var_type = var_type;
            this.dependant_var = dependant_var;
            this.var_wilcard = var_wilcard;
            this.uvar = uvar;
        }
    }

    static class Uvar {
        PsiType typeExpression;
        PsiElement element;
        PsiClass ownerClass;
        Variance var;
        public Uvar(PsiType typeExpression, PsiElement element, PsiClass ownerClass, Variance var) {
            this.typeExpression = typeExpression;
            this.element = element;
            this.ownerClass = ownerClass;
            this.var = var;
        }

        public Variance getVar() {
            return var;
        }
    }

    private List<Dvar> dvars_list;
    private List<Uvar> uvars_list;
    private List<Constraint> constraints_list;
    private List<PsiMethod> methods_list;
    private Map<PsiField, Constraint> fields;

    public FindVariances()
    {
        dvars_list = new ArrayList<Dvar>();
        uvars_list=new ArrayList<Uvar>();
        constraints_list=new ArrayList<Constraint>();
        methods_list = new ArrayList<PsiMethod>();
        fields=new HashMap<PsiField, Constraint>();
    }

    public List<Dvar> getDvarsList(){
        return dvars_list ;
    }
    public List<Uvar> getUvarsList(){return uvars_list;}
    public Map<PsiField, Constraint> getFields(){return fields;}

    public void analyze(PsiElement root) {

        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitTypeParameter(PsiTypeParameter typeParameter) {
                super.visitTypeParameter(typeParameter);
                PsiElement owner = typeParameter.getOwner();
                if (owner instanceof PsiClass) {
                    PsiClass ownerClass = (PsiClass) owner;
                    dvars_list.add(new Dvar(typeParameter, owner, ownerClass, Variance.BIVARIANT));
                }
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                processField(field);
            }


            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                methods_list.add(method);
            }
        });

        for (PsiMethod method : methods_list) {
            PsiParameter[] parameters = method.getParameterList().getParameters();
            for (PsiParameter parameter : parameters) {
                processParameter(parameter);
            }
            processReturnType(method);
        }
        calculateDvarVariances();
        printMap();
    }

    private void processParameter(PsiParameter parameter) {
        PsiClass declaringClass = PsiTreeUtil.getParentOfType(parameter, PsiClass.class);
        PsiType paramType = parameter.getType();
        if (paramType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) paramType;
            PsiClass paramClass = classType.resolve();
            if (paramClass != null) {
                if (isExternalLibraryClass(paramClass)) {
                    processExternalClassParameter(parameter);
                }
                PsiType[] typeParameters = classType.getParameters();
                if (typeParameters.length == 1) {
                    PsiType typeParameter = typeParameters[0];
                    Variance variance = determineVariance(typeParameter);
                    if (typeParameter instanceof PsiWildcardType) {
                        processWildcardType(parameter, declaringClass, paramClass, typeParameter, variance);
                    } else if (typeParameter instanceof PsiClassType) {
                        processClassType(parameter, declaringClass, paramClass, typeParameter, variance);
                    }
                }
            }
        }
    }

    private void processWildcardType(PsiParameter parameter, PsiClass declaringClass, PsiClass paramClass, PsiType typeParameter, Variance variance) {
        PsiWildcardType wildcardType = (PsiWildcardType) typeParameter;
        PsiType bound = wildcardType.getBound();
        if (bound instanceof PsiClassType) {
            PsiClassType boundClassType = (PsiClassType) bound;
            PsiClass parameterClass = boundClassType.resolve();
            if (parameterClass != null) {
                Dvar left_dvar = findSuitableDvar(declaringClass);
                Dvar right_dvar = findSuitableDvar(paramClass);
                Uvar new_uvar = new Uvar(bound, parameter, paramClass, Variance.BIVARIANT);
                FindUvar findUvar = new FindUvar(this);
                findUvar.analyze(new_uvar);
                new_uvar.var = findUvar.total_variance;
                uvars_list.add(new_uvar);
                if(left_dvar != null) {
                    Constraint new_constraint = new Constraint(left_dvar, Variance.CONTRAVARIANT, right_dvar, variance, new_uvar.getVar());
                    constraints_list.add(new_constraint);
                }
            }
        }
    }

    private void processClassType(PsiParameter parameter, PsiClass declaringClass, PsiClass paramClass, PsiType typeParameter, Variance variance) {
        PsiClassType parameterClassType = (PsiClassType) typeParameter;
        PsiClass parameterClass = parameterClassType.resolve();
        if (parameterClass != null) {
            Dvar left_dvar = findSuitableDvar(declaringClass);
            Dvar right_dvar = findSuitableDvar(paramClass);
            Uvar new_uvar = new Uvar(typeParameter, parameter, paramClass, Variance.BIVARIANT);
            FindUvar findUvar = new FindUvar(this);
            findUvar.analyze(new_uvar);
            new_uvar.var = findUvar.total_variance;
            uvars_list.add(new_uvar);
            if(left_dvar != null) {
                Constraint new_constraint = new Constraint(left_dvar, Variance.CONTRAVARIANT, right_dvar, variance, new_uvar.getVar());
                constraints_list.add(new_constraint);
            }
        }
    }

    private Variance determineVariance(PsiType typeParameter) {
        if (typeParameter instanceof PsiClassType) {
            // Invariant case: p<X>
            return Variance.INVARIANT;
        } else if (typeParameter instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) typeParameter;
            if (wildcardType.isExtends()) {
                // Covariant case: p<? extends X>
                return Variance.COVARIANT;
            } else if (wildcardType.isSuper()) {
                // Contravariant case: p<? super X>
                return Variance.CONTRAVARIANT;
            } else {
                // Bivariant case: p<?>
                return Variance.BIVARIANT;
            }
        }
        return null;
    }

    private Dvar findSuitableDvar(PsiClass psiClass) {
            for (Dvar dvar : dvars_list) {
                if (dvar.ownerClass.equals(psiClass)) {
                    return dvar;
                }
            }
            return null;
        }

    private void processExternalClassParameter(PsiParameter parameter) {
        PsiType paramType = parameter.getType();
        if (paramType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) paramType;
            PsiClass paramClass = classType.resolve();
            if (paramClass != null && !(paramClass instanceof PsiTypeParameter)) {
                boolean isExternal = isExternalLibraryClass(paramClass);
                if (isExternal) {
                    boolean alreadyExists = false;
                    for (Dvar dvar : dvars_list) {
                        if (dvar.ownerClass != null && dvar.ownerClass.equals(paramClass)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        Dvar newDvar = new Dvar(null, parameter, paramClass, Variance.INVARIANT);
                        dvars_list.add(newDvar);
                    }
                }
            }
        }
    }

    private boolean isExternalLibraryClass(PsiClass psiClass) {
            if (psiClass != null) {
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null) {
                    return qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.");
                }
            }
            return false;
        }

    private void processField(PsiField field) {
        PsiType fieldType = field.getType();
        if (fieldType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) fieldType;
            PsiClass resolvedClass = classType.resolve();
            if (resolvedClass != null) {
                PsiClass containingClass = field.getContainingClass();
                Dvar ownerClassDvar = findSuitableDvar(containingClass);
                Constraint newConstraint = new Constraint(ownerClassDvar, Variance.BIVARIANT, null, Variance.NONE, Variance.NONE);
                constraints_list.add(newConstraint);
                fields.put(field, newConstraint);
            }
        }
    }

    private void processReturnType(PsiMethod method) {
        PsiType returnType = method.getReturnType();
        if (returnType instanceof PsiClassType) {
            PsiClassType returnClassType = (PsiClassType) returnType;
            PsiClass returnClass = returnClassType.resolve();
            if (returnClass instanceof PsiTypeParameter) {
                PsiClass declaringClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
                Dvar ownerClassDvar = findSuitableDvar(declaringClass);
                Constraint newConstraint = new Constraint(ownerClassDvar, Variance.COVARIANT, null,  Variance.NONE, Variance.NONE);
                constraints_list.add(newConstraint);
            }
        }
    }

    public void printMap() {
        System.out.println("****** Definition-site variances ******");
        for (Dvar dvar : dvars_list) {
            System.out.print("Class: " + dvar.ownerClass.getQualifiedName() + ", ");
            System.out.print("Type parameter: " + dvar.typeParameter + ", ");
            System.out.print("Variance: " + dvar.var.toString() + ", ");
            System.out.printf("%n");
        }
        System.out.println("****** Use-site variances ******");
        for (Uvar uvar : uvars_list) {
            System.out.print("Parameter name: " + uvar.element.getText()+ ", ");
            System.out.print("Class: " + uvar.ownerClass.getQualifiedName() + ", ");
            System.out.print("Type parameter: " + uvar.typeExpression + ", ");
            System.out.print("Variance: " + uvar.var.toString() + ", ");
            System.out.printf("%n");
        }

        System.out.println("****** Constraints ******");
        for (Constraint constraint : constraints_list) {
            System.out.print(constraint.dvar + " <= " + constraint.var_type + " *transform* (" + constraint.dependant_var + " *join* " + constraint.var_wilcard + ")");
            System.out.printf("%n");
        }
        System.out.printf("%n");
        System.out.printf("%n");
    }

    public Variance transform (Variance v1, Variance v2) {
        if (v1 == Variance.BIVARIANT || v2 == Variance.BIVARIANT) {
            return Variance.BIVARIANT;
        }
        if (v1 == Variance.INVARIANT || v2 == Variance.INVARIANT) {
            return Variance.INVARIANT;
        }
        if (v1 == v2) {
            return Variance.COVARIANT;
        }
        return Variance.CONTRAVARIANT;
    }

    public Variance join (Variance v1, Variance v2) {
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
        if (v1 == v2)
        {
            return v1;
        }

        return Variance.BIVARIANT;
    }

    public Variance meet(Variance v1, Variance v2) {
        if (v1 == Variance.BIVARIANT && v2 == Variance.BIVARIANT) {
            return Variance.BIVARIANT;
        }
        if (v1 == Variance.INVARIANT || v2 == Variance.INVARIANT) {
            return Variance.INVARIANT;
        }
        if (v1 == Variance.BIVARIANT) {
            return v2;
        }
        if (v2 == Variance.BIVARIANT) {
            return v1;
        }
        if (v1 == v2)
        {
            return v1;
        }
        return Variance.INVARIANT;
    }

    private void calculateDvarVariances() {
        // Recursively calculate variances until all stop changing
        boolean anyChanged;
        do {
            anyChanged = false;
            for (Dvar dvar : dvars_list) {
                if(isExternalLibraryClass(dvar.ownerClass)==false) {
                    Variance oldVariance = dvar.var;
                    Variance newVariance = calculateVariance(dvar);
                    if (newVariance != oldVariance) {
                        dvar.var = newVariance;
                        anyChanged = true;
                    }
                }
            }
        } while (anyChanged);
    }

    private Variance calculateVariance(Dvar dvar) {
        Variance result = Variance.BIVARIANT;
        for (Constraint constraint : constraints_list) {
            if (constraint.dvar.equals(dvar)) {
                Variance join_dvar_wildCard;
                Variance join_uvar;
                Variance transfrom_result;
                if(constraint.dependant_var!=null)
                {
                    Variance var=constraint.dependant_var.var;
                    join_dvar_wildCard = join (var, constraint.var_wilcard);
                    if (constraint.uvar != Variance.NONE) {
                        join_uvar = join (join_dvar_wildCard, constraint.uvar);
                        transfrom_result = transform(constraint.var_type, join_uvar);
                    }
                    else
                    {
                        transfrom_result=transform(constraint.var_type, join_dvar_wildCard);
                    }
                }
                else {
                    transfrom_result = constraint.var_type;
                }
                result = meet (result, transfrom_result);
            }
        }
        return result;
    }
}
