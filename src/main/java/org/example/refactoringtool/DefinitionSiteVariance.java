package org.example.refactoringtool;

import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;

import java.util.*;

public class DefinitionSiteVariance {

    static class Dvar {
        PsiTypeParameter typeParameter;
        PsiElement element;
        PsiClass ownerClass;
        Boolean is_generic;
        Variance var;

        public Dvar(PsiTypeParameter typeParameter, PsiElement element, PsiClass ownerClass, Boolean is_generic, Variance var) {
            this.typeParameter = typeParameter;
            this.element = element;
            this.ownerClass = ownerClass;
            this.is_generic = is_generic;
            this.var = var;
        }
        public Dvar copyDvar(boolean is_generic, Variance var) {
            return new Dvar(this.typeParameter, this.element, this.ownerClass, is_generic, var);
        }
    }

    public static class Constraint {
        Dvar dvar; // our dvar that is in the left side of the equation
        Variance var_type; // arg type or return type
        Dvar dependant_var; // the var we are dependent on
        Variance var_wilcard;  // variance to join with because of wild card
        Variance uvar;
        PsiParameter parameter; // The actual parameter name
        //dvar <= var_type *transform* (dependant_var *join* var_wildcard)

        public Constraint (Dvar dvar, Variance var_type, Dvar dependant_var, Variance var_wilcard, Variance uvar, PsiParameter parameter) {
            this.dvar = dvar;
            this.var_type = var_type;
            this.dependant_var = dependant_var;
            this.var_wilcard = var_wilcard;
            this.uvar = uvar;
            this.parameter = parameter;
        }
    }
    private List<Dvar> dvars_list;
    private List<Dvar> dvars_external_list;
    private List<Constraint> constraints_list;
    private List<PsiMethod> methods_list;
    public DefinitionSiteVariance() {
        this.dvars_list = new ArrayList<>();
        this.dvars_external_list = new ArrayList<>();
        this.constraints_list = new ArrayList<>();
        this.methods_list = new ArrayList<>();
    }



    public enum Variance {
        COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT, NONE
    }

    public void analyze(PsiElement root) {
        // Initialize with invariance
        root.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitTypeParameter(PsiTypeParameter typeParameter) {
                super.visitTypeParameter(typeParameter);
                PsiElement owner = typeParameter.getOwner();
                if (owner instanceof PsiClass) {
                    PsiClass ownerClass = (PsiClass) owner;
                    dvars_list.add(new Dvar(typeParameter, owner, ownerClass, true, Variance.NONE)); // first put none
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                methods_list.add(method);

                // Check method return type and parameters for external generics
                checkMethodForExternalGenerics (method);
            }
        });

        MethodBodyAnalysis methodBodyAnalysis = new MethodBodyAnalysis(constraints_list, dvars_list, this);

        for (PsiMethod method : methods_list) {
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null) {
                for (PsiTypeParameter typeParameter : containingClass.getTypeParameters()) {
                    Dvar dvar = findDvar(typeParameter);
                    if (dvar != null) {
                        analyzeMethodSignature(method, dvar);
                        // TODO: remove
                        System.out.printf("containingClass");
                        System.out.printf("method name:" + method.getName());
                        System.out.printf("%n");
                    }
                }
            }
        }

        for (PsiMethod method : methods_list) {
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null) {
                for (PsiTypeParameter typeParameter : containingClass.getTypeParameters()) {
                    Dvar dvar = findDvar(typeParameter);
                    if (dvar != null) {
                        //methodBodyAnalysis.analyzeMethodBody(method, dvar); // Analyze method body
                        // TODO: remove
                        System.out.printf("Analyzed method body for method: %s%n", method.getName());
                    }
                }
            }
        }
        // TODO: remove
        printConstraints();
        calculateDvarVariances();
        printConstraints();
    }
    private void checkMethodForExternalGenerics(PsiMethod method) {
        // Check return type
        PsiType returnType = method.getReturnType();
        if (returnType != null && !"void".equals(returnType.getCanonicalText())) {
            addGenericDvars(returnType, method);
        }

        // Check parameter types
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            addGenericDvars(parameter.getType(), parameter);
        }
    }
    private void addGenericDvars(PsiType type, PsiElement context) {
        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiClass psiClass = classType.resolve();

            if (psiClass != null && isExternalClass(psiClass)) {
                // Add Dvar for the generic type itself
                addTypeParameterDvar(classType, context);
            }
        }
    }
    private void addTypeParameterDvar(PsiClassType classType, PsiElement context) {
        PsiClass psiClass = classType.resolve();
        if (psiClass != null) {

            // Assuming classType can only resolve to PsiTypeParameter if it's a generic type
            for (PsiType typeArgument : classType.getParameters()) {
                PsiTypeParameter typeParameter = getTypeParameter(typeArgument);

                if (typeArgument instanceof PsiWildcardType)
                {
                    PsiWildcardType wildcardType = (PsiWildcardType) typeArgument;
                    PsiType bound = wildcardType.getBound();

                    if (bound != null) {
                        if (typeParameter != null) {
                            addTypeParameterDvarLow (psiClass, typeParameter, wildcardType, context);
                        } else {
                            // If it's not a type parameter, handle it as a class type
                            addGenericDvars(bound, context);
                        }
                    }
                }
                else {
                    addTypeParameterDvarLow (psiClass, typeParameter, null, context);
                }

            }
        }
    }

    private void addTypeParameterDvarLow (PsiClass psiClass, PsiTypeParameter typeParameter, PsiWildcardType wildcardType, PsiElement context) {

        Variance variance = Variance.INVARIANT;

        if (wildcardType != null)
        {
            variance = checkWildcards(wildcardType);
        }

        Dvar dvar = findDvarByTypeParameterAndClass(typeParameter, psiClass);
        if (dvar == null) {
            variance = join(variance, Variance.INVARIANT);
            dvar = new Dvar(typeParameter, context, psiClass, true, variance);
            dvars_external_list.add(dvar);
        }
    }


    public Dvar findDvarByTypeParameterAndClass(PsiTypeParameter typeParameter, PsiClass ownerClass) {
        for (Dvar dvar : dvars_external_list) {
            if (dvar.typeParameter.equals(typeParameter) && dvar.ownerClass.equals(ownerClass)) {
                return dvar;
            }
        }
        return null;
    }

    public Dvar findDvar(PsiTypeParameter typeParameter) {
        for (Dvar dvar : dvars_list) {
            if (dvar.typeParameter.equals(typeParameter)) {
                return dvar;
            }
        }
        return null;
    }

    public PsiTypeParameter getTypeParameter(PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass instanceof PsiTypeParameter) {
                return (PsiTypeParameter) psiClass;
            }
            else if (psiClass != null) {
                // Check if any of the type parameters of the class match
                for (PsiType typeArg : ((PsiClassType) type).getParameters()) {
                    PsiTypeParameter param = getTypeParameter(typeArg);
                    if (param != null) {
                        return param;
                    }
                }
            }
        }
        else if (type instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) type;
            PsiType bound = wildcardType.getBound();
            if (bound != null) {
                return getTypeParameter(bound); // Recursively resolve the bound type
            }
        }

        return null;
    }

    // if we can find class  name, it means PsiType is not just simply X (couldn't find better way)
    public Boolean isGeneric (PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass != null) {
                String className = psiClass.getQualifiedName();
                if (className == null)
                    return false;

            }
        }
        return true;
    }

    private boolean isExternalClass(PsiClass psiClass) {
        if (psiClass != null) {
            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName != null) {
                // Check if the class is part of the Java standard library
                return qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.");
            }
        }
        return false;
    }
    private PsiClass getOwnerClassFromType (PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            return psiClass;
        }
        return null;
    }

    private Dvar findDvarWithOwner(PsiClass psiClass, List<Dvar> dvarsList) {
        for (Dvar dvar : dvarsList) {
            if (dvar.ownerClass != null && dvar.ownerClass.equals(psiClass)) {
                return dvar;
            }
        }
        return null;
    }

    public Variance checkWildcards(PsiType type) {
        if (type instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) type;
            if (wildcardType.isExtends()) {
                return Variance.COVARIANT;
            } else if (wildcardType.isSuper()) {
                return Variance.CONTRAVARIANT;
            } else {
                return Variance.BIVARIANT; // General case if neither extends nor super
            }
        }
    else if (type instanceof PsiClassType) {
        // Check if the PsiClassType contains any wildcard parameters
        PsiClassType classType = (PsiClassType) type;
        for (PsiType paramType : classType.getParameters()) {
            Variance variance = checkWildcards(paramType);
            if (variance != Variance.INVARIANT) {
                return variance;
            }
        }
    }
        return Variance.INVARIANT; // Not a wildcard type, we will put invariant so it won't make any changes during join
    }

    private void analyzeMethodSignatureTypes (PsiMethod method, PsiType typeToAnalyze, Dvar dvar, boolean isReturnType, PsiParameter parameter) {

        PsiTypeParameter typeParameter = getTypeParameter(typeToAnalyze);
        Variance variance = isReturnType ? Variance.COVARIANT : Variance.CONTRAVARIANT;
        Variance wild_card_var = checkWildcards (typeToAnalyze);

        if (typeParameter != null && typeParameter.equals(dvar.typeParameter)) {
            if (isGeneric(typeToAnalyze)) {
                // Return type is C<X>
                PsiClass ownerClass = getOwnerClassFromType(typeToAnalyze);
                if (isExternalClass (ownerClass)) { //check if external class

                    // in case of external generic, the var will be always covariant, therfor we put it in that way
                    Dvar external_dvar = findDvarByTypeParameterAndClass (typeParameter, ownerClass);
                    Constraint constraint_internal = new Constraint(dvar, variance, external_dvar, wild_card_var, Variance.NONE, null);
                    constraints_list.add(constraint_internal);
                }
                else {
                    if (ownerClass.equals(dvar.ownerClass)) // means same class
                    {
                        Constraint constraint = new Constraint(dvar, variance, dvar, wild_card_var, Variance.NONE, parameter);
                        constraints_list.add(constraint);
                    }
                    else// go over all the dvars and search for this psiClass
                    {
                        Dvar dependant_dvar = findDvarWithOwner (ownerClass, dvars_list);
                        Constraint constraint = new Constraint(dvar, variance, dependant_dvar, wild_card_var, Variance.NONE, parameter);
                        constraints_list.add(constraint);
                    }
                }
            } else {
                // Return type is exactly X
                Dvar non_generic_dvar = dvar.copyDvar(false, Variance.COVARIANT);
                Constraint constraint = new Constraint(dvar, variance, non_generic_dvar, wild_card_var, Variance.NONE, parameter);
                constraints_list.add(constraint);
            }
        } else {  // TODO: finish if needed or remove
            // not finished - dont know if need. I guess it for case such as:
//                class C<X> {
//                    D<Y> goo (C<X> csx) { }
//                }
//
//                class D<Y> {
//                    D<Y> goo (D<Y> csx) { }
//                }
            Dvar dependantVar = findDvar(typeParameter);
            if (dependantVar != null) {
                Constraint constraint = new Constraint(dvar, variance, dependantVar,wild_card_var, Variance.NONE, parameter);
                constraints_list.add(constraint);
            }
        }
    }

    // this is the var function:
    private void analyzeMethodSignature(PsiMethod method, Dvar dvar) {

        PsiType returnType = method.getReturnType();

        // Analyze return type, but skip void return type
        if (returnType != null && !"void".equals(returnType.getCanonicalText())) {
            analyzeMethodSignatureTypes (method, returnType, dvar, true, null);
        }
        // Analyze arg type
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
                PsiType parameterType = parameter.getType();
            analyzeMethodSignatureTypes (method, parameterType, dvar, false, parameter);
        }


    }

    public void printMap() { //TODO: delete
        System.out.println("****** Definition-site variances ******");
        for (Dvar dvar : dvars_list) {
            System.out.print("Class: " + dvar.ownerClass.getQualifiedName() + ", ");
            System.out.print("Type parameter: " + dvar.typeParameter.getName() + ", ");
            System.out.print("Variance: " + dvar.var.toString() + ", ");
            System.out.printf("%n");
        }
        for (Dvar dvar : dvars_external_list) {
            System.out.print("Class: " + dvar.ownerClass.getQualifiedName() + ", ");
            System.out.print("Type parameter: " + dvar.typeParameter.getName() + ", ");
            System.out.print("Variance: " + dvar.var.toString() + ", ");
            System.out.printf("%n");
        }

        System.out.println("****** Use-site variances ******");
        for (Constraint constraint : constraints_list) {
            System.out.print("Class: " + constraint.dependant_var.ownerClass.getQualifiedName() + ", ");
            System.out.print("Type parameter: " + constraint.dependant_var.typeParameter.getName() + ", ");
            System.out.print("Variance: " + constraint.uvar.toString() + ", ");
            System.out.printf("%n");
        }
    }
    public void printConstraints() {//TODO: delete
         System.out.println("****** CONSTRAINTS ******");
        for (Constraint constraint : constraints_list) {
            System.out.print("The dvar we check : dvar(" + constraint.dvar.typeParameter.getName() +";"
                    + constraint.dvar.ownerClass.getQualifiedName() + ")");
            System.out.print("Current variance: " + constraint.dvar.var.toString() + ", ");
            System.out.printf("%n");
            System.out.print("Position (return type or arg type): " + constraint.var_type.toString() + ", ");
            System.out.printf("%n");
            System.out.print("dependant dvar : dvar(" + constraint.dependant_var.typeParameter.getName() +";"
                            + constraint.dependant_var.ownerClass.getQualifiedName() + ")");
            System.out.print("dependant parameter variance : " + constraint.dependant_var.var.toString() + ", ");
            System.out.printf("%n");
            System.out.print("type of wildcard (INVARIANT) if no wildcards " + constraint.var_wilcard.toString());
            System.out.printf("%n");
            System.out.print("type of uvar" + constraint.uvar.toString());
            System.out.printf("%n");
            System.out.println("****************");
            System.out.printf("%n");
        }
    }

    private Variance transform (Variance v1, Variance v2) {
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
        return Variance.INVARIANT;
    }

    private void calculateDvarVariances() {

        // Initialize all dvar variances to BIVARIANT
        for (Dvar dvar : dvars_list) {
            dvar.var = Variance.BIVARIANT;
        }

        // Recursively calculate variances until all stop changing
        boolean anyChanged;
        do {
            anyChanged = false;
            for (Dvar dvar : dvars_list) {
                Variance oldVariance = dvar.var;
                Variance newVariance = calculateVariance(dvar);
                if (newVariance != oldVariance) {
                    dvar.var = newVariance;
                    anyChanged = true;
                }
            }
        } while (anyChanged);
    }

    private Variance calculateVariance(Dvar dvar) {
        Variance result = Variance.BIVARIANT;
        for (Constraint constraint : constraints_list) {
            if (constraint.dvar.equals(dvar)) {
                Variance join_result = join (constraint.dependant_var.var, constraint.var_wilcard);
                if (constraint.uvar != Variance.NONE) {
                    join_result = join (join_result, constraint.uvar);
                }
                Variance transfrom_result = transform(constraint.var_type, join_result);
                result = meet (result, transfrom_result);
            }
        }
        return result;
    }

}
