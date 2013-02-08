package de.uka.ilkd.tablet;

/**
 * The type of a formula.
 * 
 * Every type has an antitype. That is the type of a negated instance. Alpha<->Beta,
 * Gamma<->Delta, Negneg<->Negneg, and None<->None. The latter is not correct
 * since ~A-> NONE and ~~A->NEGNEG, but this does not matter.
 * 
 * <ul>
 * <li>alpha are "conjunctive"
 * <li>beta are "disjunctive"
 * <li>gamma are "universal"
 * <li>delta are "existential"
 * <li>negneg are technical
 * </ul>
 * 
 * @author MU
 * 
 */
enum Type {
    ALPHA("\\alpha"), BETA(ALPHA, "\\beta"), GAMMA("\\gamma"), DELTA(GAMMA, "\\delta"), NONE("-"), NEGNEG("\\lnot\\lnot");

    Type(String latex) {
    	this.latex = latex; 
        opposite = this;
    }

    Type(Type inv, String latex) {
        opposite = inv;
        this.latex = latex;
        inv.opposite = this;
    }

    private Type opposite;
    
    // their latex counterpart. used when exporting latex
    private String latex;

    public Type getOpposite() {
        return opposite;
    }

	public String getLatex() {
		return latex;
	}
    
};
