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

@SuppressWarnings("serial")
public class InstantiationClashException extends Exception {

    private Formula formula;
    private String varToInst;
    private String problemVar;

    public InstantiationClashException(String problemVar, String varToInst, Formula formula) {
        this.formula = formula;
        this.varToInst = varToInst;
        this.problemVar = problemVar;
    }
    
    @Override
    public String getMessage() {
        return "Collision during instantiation because " + problemVar +" appears in " + 
        formula + " (to be instantiated for " + varToInst +")."; 
    }

}
