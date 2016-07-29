/* This file is part of TableauApplet.
 *
 * It has been written by Mattias Ulbrich <ulbrich@kit.edu>, 
 * Karlsruhe Institute of Technology, Germany.
 *
 * TableauApplet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TableauApplet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TableauApplet.  If not, see <http://www.gnu.org/licenses/>.
 */

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
