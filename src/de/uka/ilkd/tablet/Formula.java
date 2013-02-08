package de.uka.ilkd.tablet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Formula implements Cloneable {

    protected Formula subFormula[];

    public Formula(Formula... formulae) {
        subFormula = formulae;
    }

    /**
     * apply the tableau rules to this formula.
     * 
     * There is only one rule per formula (here). The result is returned in a
     * matrix of formulas: It is an array of formula array, i.e. an array over
     * branches.
     * 
     * If the formula is encountered in a negated context the argument is set to
     * true, and the corresponding rule must be applied
     * 
     * @param negated
     *                true iff formula in negated context
     * @return a list of branches, null if nothing can be extended.
     */
    public abstract Formula[][] applyRule(boolean negated);

    public abstract Type getType();

    /**
     * to check if two formulas are compatible (i.e. can be unified) this string
     * describes the top-level without telling about the embedded formulas.
     * 
     * @return a string characterising the formula
     */
    public abstract String getPrefix();

    public Formula instantiate(String var, Formula f) throws InstantiationClashException {
        Formula fnew;
        try {
            fnew = (Formula) clone();
            fnew.subFormula = new Formula[subFormula.length];
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }

        for (int i = 0; i < fnew.subFormula.length; i++) {
            fnew.subFormula[i] = subFormula[i].instantiate(var, f);
        }
        return fnew;
    }

    public Formula uninstantiate(String freevar) {
        Formula fnew;
        try {
            fnew = (Formula) clone();
            fnew.subFormula = new Formula[subFormula.length];
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }

        for (int i = 0; i < fnew.subFormula.length; i++) {
            fnew.subFormula[i] = subFormula[i].uninstantiate(freevar);
        }
        return fnew;
    }

    protected void collectFreeVariables(Collection<IdFormula> list) {
        for (Formula form : subFormula) {
            form.collectFreeVariables(list);
        }
    }

    // that's terribly simple!
    @Override
	public boolean equals(Object o) {
    	// TODO think of something more elaborate
        return toString().equals(o.toString());
    }
    
    // this is needed for insertion into hashtables:
    @Override
    public int hashCode() {
    	// TODO think of something more elaborate
    	return toString().hashCode();
    }
    
    public Formula getSubFormula(int i) {
        return subFormula[i];
    }
    
    public int countSubFormulae() {
        return subFormula.length;
    }

    /**
     * is <code>f</code> the complement to this formula.
     * Note: doesnt matter which is negated
     * 
     * @param f a formula, not null
     * @return <code>f</code> and <code>this</code> are complementary
     */
    public boolean closesWith(Formula f) {
        return (f instanceof NotFormula && equals(f.subFormula[0])) 
        	|| (this instanceof NotFormula && f.equals(subFormula[0]));
    }

    /**
     * unify two formulas.
     * 
     * store all instantiations that are needed in a mapping.
     * 
     * @param f1
     *                formula 1 to unify
     * @param f2
     *                formula 2 to unify
     * @param instantiations
     *                an existing map to store instantiations to
     * @return true iff the formulas are unifiable.
     */
    public static boolean unify(Formula f1, Formula f2,
            Substitution instantiations) {

        if (f1 instanceof Instantiation)
            return unify(f1.subFormula[0], f2, instantiations);
        if (f2 instanceof Instantiation)
            return unify(f1, f2.subFormula[0], instantiations);

        if (f1.equals(f2))
            return true;

        if (f1.isFreeVariable()) {
            String identifier = ((IdFormula) f1).identifier;
            Formula formula = instantiations.get(identifier);
            f2 = instantiations.applyTo(f2);
            
            if (formula != null) {
                return unify(formula, f2, instantiations);
            }

            Set<IdFormula> list = new HashSet<IdFormula>();
            f2.collectFreeVariables(list);
            // X1 and f(X1) cannot be unified!
            if (list.contains(f1))
                return false;
            
            
            instantiations.add(identifier, f2);
            return true;
        }

        if (f2.isFreeVariable()) {
            // write this code only once ...
            return unify(f2, f1, instantiations);
        }

        if (!f1.getPrefix().equals(f2.getPrefix()))
            return false;

        for (int i = 0; i < f1.subFormula.length; i++) {
            if (!unify(f1.subFormula[i], f2.subFormula[i], instantiations))
                return false;
        }

        return true;
    }

    /**
     * test whether an identifier (used as bound variable) appears within this formula
     * not within the scope of a quantifier. needed for collision detection.
     * 
     * @param id identifier to check
     * @return true if there is an identifier referencing to id that is not in the 
     * scope of a quantifier for id.
     */
    protected boolean containsFreeIdentifier(String id) {
        for (Formula f : subFormula) {
            if(f.containsFreeIdentifier(id)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isFreeVariable() {
        return false;
    }

	public abstract String toLatex();

}

class Instantiation extends Formula {

    private String instantiatedVar;

    public Instantiation(String instantiatedVar, Formula instantiates) {
        super(instantiates);
        this.instantiatedVar = instantiatedVar;
    }

    @Override
    public Formula[][] applyRule(boolean negated) {
        return subFormula[0].applyRule(negated);
    }

    @Override
    public Type getType() {
        return subFormula[0].getType();
    }

    @Override
    public String toString() {
        return subFormula[0].toString();
    }
    
	@Override
	public String toLatex() {
        // return subFormula[0].toLatex();
		return IdFormula.identifierToLatex(instantiatedVar) + "^*";
	}

    @Override
    public Formula uninstantiate(String freevar) {
        if (freevar.equals(instantiatedVar)) {
            return new IdFormula(instantiatedVar);
        } else {
            return new Instantiation(instantiatedVar, 
            		subFormula[0].uninstantiate(freevar));
        }
    }

    public String getInstantiatedVariable() {
        return instantiatedVar;
    }

    @Override
    public String getPrefix() {
        return subFormula[0].getPrefix();
    }

}

class NotFormula extends Formula {

    public NotFormula(Formula f) {
        super(f);
    }

    @Override
    public Formula[][] applyRule(boolean negated) {

        // strip two negations
        if (negated) {
            return new Formula[][] { { subFormula[0] } };
        } else {
            return subFormula[0].applyRule(true);
        }
    }

    @Override
    public Type getType() {
        if (subFormula[0] instanceof NotFormula)
            return Type.NEGNEG;

        Type type = subFormula[0].getType();
        return type.getOpposite();
    }

    @Override
    public String toString() {
        return Constants.getNot() + subFormula[0];
    }

//    @Override
//    public boolean closesWith(Formula f) {
//        // bugfix: if this is left out, two negated formulae will run into an
//        // endless loop, two negated formulas cannot be complementary
//        if (f instanceof NotFormula) {
//            return false;
//        }
//
//        return f.closesWith(this);
//    }

    @Override
    public String getPrefix() {
        return "~";
    }

	@Override
	public String toLatex() {
		return "\\lnot " + subFormula[0].toLatex();
	}

}

class BinopFormula extends Formula {

    enum Operator {
        AND, OR, IMPL, EQUIV;
    }

    private Operator op;

    BinopFormula(Operator op, Formula f1, Formula f2) {
        super(f1, f2);
        this.op = op;
    }

    @Override
    public Type getType() {
        Type t;
        switch (op) {
        case EQUIV:
        case AND:
            t = Type.ALPHA;
            break;
        default:
            t = Type.BETA;
            break;
        }
        return t;
    }

    @Override
    public String toString() {
        return "(" + subFormula[0] + Constants.getOp(op) + subFormula[1] + ")";
    }
    
	@Override
	public String toLatex() {
		return "(" + subFormula[0].toLatex() + Constants.getOpLatex(op) + subFormula[1].toLatex() + ")";
	}

    @Override
    public Formula[][] applyRule(boolean negated) {
        Formula s1;
        Formula s2;
        s1 = subFormula[0];
        s2 = subFormula[1];

        if (negated) {
            switch (op) {
            case IMPL:
                return new Formula[][] { { s1, not(s2) } };
            case AND:
                return new Formula[][] { { not(s1) }, { not(s2) } };
            case OR:
                return new Formula[][] { { not(s1), not(s2) } };
            case EQUIV:
                return new Formula[][] { { not(s1), s2 }, { s1, not(s2) } };
            default:
                throw new Error(op.name());
            }
        } else {
            switch (op) {
            case IMPL:
                return new Formula[][] { { not(s1) }, { s2 } };
            case AND:
                return new Formula[][] { { s1, s2 } };
            case OR:
                return new Formula[][] { { s1 }, { s2 } };
            case EQUIV:
                return new Formula[][] { { s1, s2 }, { not(s1), not(s2) } };
            default:
                throw new Error(op.name());
            }
        }
    }

    /*
     * little helper to make things easier to read.
     */
    private Formula not(Formula f) {
        return new NotFormula(f);
    }

    @Override
    public String getPrefix() {
        return op.toString();
    }
    
    public Operator getOp() {
        return op;
    }



}

class IdFormula extends Formula {

    String identifier;

    public IdFormula(String identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Formula[][] applyRule(boolean negated) {
        return null;
    }

    @Override
    public Type getType() {
        return Type.NONE;
    }

    @Override
    public Formula instantiate(String var, Formula f) {
        if (identifier.equals(var))
            return f;
        else
            return this;
    }

    @Override
    public String toString() {
        return identifier;
    }
    
	@Override
	public String toLatex() {
		return identifierToLatex(identifier);
	}

	static String identifierToLatex(String identifier) {
		if(identifier.startsWith("X"))
			return "X_{" + identifier.substring(1) +"}";
		else if(identifier.startsWith("sk"))
			return "sk_{" + identifier.substring(2)+"}";
		else
			return identifier;
	}

    @Override
    public void collectFreeVariables(Collection<IdFormula> set) {
        if (isFreeVariable())
            set.add(this);
    }

    @Override
	protected boolean isFreeVariable() {
        return identifier.matches("X[0-9]+");
    }

    @Override
    public String getPrefix() {
        return identifier;
    }
    
    @Override
    protected boolean containsFreeIdentifier(String id) {
        return identifier.equals(id);
    }

}

class QuantorFormula extends Formula {

    enum Quantor {
        FORALL, EXISTS
    }

    Quantor quantor;

    public QuantorFormula(Quantor q, String var, Formula formula) {
        super(formula);
        quantor = q;
        variable = var;
    }

    String variable;

    @Override
    public Type getType() {
        if (quantor == Quantor.EXISTS)
            return Type.DELTA;
        else
            return Type.GAMMA;
    }

    @Override
    public String toString() {
        return Constants.getQuantor(quantor) + variable + "." + subFormula[0];
    }
    
	@Override
	public String toLatex() {
		return Constants.getQuantorLatex(quantor) + variable + "." + subFormula[0].toLatex();
	}


    @Override
    public Formula[][] applyRule(boolean negated) {
        Formula inst;
        // cryptic for gamma&normal | delta&negated
        if ((getType() == Type.GAMMA) != negated)
            inst = Constants.mkFreeVar();
        else {
            Set<IdFormula> set = new HashSet<IdFormula>();
            subFormula[0].collectFreeVariables(set);
            inst = Constants.mkSkolem(set);
        }
        
        Formula formula;

        try {
            formula = subFormula[0].instantiate(variable, inst);
        } catch (InstantiationClashException e) {
            // The instantiation is either a free var or a skolem and should NOT provide such difficulties
            throw new Error(e);
        }
        
        if(negated)
            formula = new NotFormula(formula);

        return new Formula[][] { { formula } };
    }
    
    /*
     * Instantiation may only happen if the quantified variable does not
     * appear in the formula to be introduced.
     * This is a little over-restrictive, because it fails also if  
     * var does not even appear in the quantified body.
     */
    @Override
    public Formula instantiate(String var, Formula f) throws InstantiationClashException {        
        if(f.containsFreeIdentifier(variable))
            throw new InstantiationClashException(variable, var, f);
        return super.instantiate(var, f);
    }
    
    /*
     * if id is the bound variable, it does not appear free in the body.
     */
    @Override
    protected boolean containsFreeIdentifier(String id) {
        if(variable.equals(id))
            return false;
        else
            return super.containsFreeIdentifier(id);
    }

    @Override
    public String getPrefix() {
        return quantor + " " + variable;
    }

}

class Application extends Formula {

    String identifier;

    public Application(String identifier, Formula... formulae) {
        super(formulae);
        this.identifier = identifier;
    }

    @Override
    public Formula[][] applyRule(boolean negated) {
        return null;
    }

    @Override
    public Type getType() {
        return Type.NONE;
    }

    @Override
    public String toString() {
        String ret = identifier + "(";
        for (int i = 0; i < subFormula.length; i++) {
            ret += subFormula[i];
            if (i != subFormula.length - 1)
                ret += ",";
        }
        return ret + ")";
    }
    
	@Override
	public String toLatex() {
        String ret = IdFormula.identifierToLatex(identifier) + "(";
        for (int i = 0; i < subFormula.length; i++) {
            ret += subFormula[i].toLatex();
            if (i != subFormula.length - 1)
                ret += ",";
        }
        return ret + ")";
	}


    @Override
    public String getPrefix() {
        return identifier + "(" + subFormula.length + ")";
    }

}