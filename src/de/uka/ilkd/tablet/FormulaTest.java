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

import java.io.StringReader;

import junit.framework.TestCase;

public class FormulaTest extends TestCase {

    private Substitution map;
    
    private boolean unify(String string) throws ParseException {
    	FormulaParser p = new FormulaParser(new StringReader(string));
        p.fullMode = true;
        Formula[] f = p.Formulae();
        map = new Substitution();
        boolean result = Formula.unify(f[0], f[1], map);
        System.out.println(map);
		return result;
    }

	public void testUnify1() throws ParseException {
        
        assertTrue(unify("p(d,X9); p(X10, c)"));
        
        assertEquals("c", map.get("X9").toString());
        assertEquals("d", map.get("X10").toString());
    }

    public void testUnify2() throws ParseException {

        assertFalse(unify("p(X1); p(f(X1))"));
        
    }
    
    public void testUnify3() throws ParseException {
        
    	assertFalse(unify("p(f(X1)); p(X1)"));
    	
    }
    
    public void testUnify4() throws ParseException {

        assertTrue(unify("r(X3,sk4(X3)); r(X19,X20)"));
        
        assertEquals("X19", map.get("X3").toString());
        assertEquals("sk4(X19)", map.get("X20").toString());
    }
    
    public void testUnify5() throws ParseException {

        assertTrue(unify("r(X122,sk35(X122)); r(X139,sk35(X139))"));
        
    }
    
    public void testUninstantiate() throws ParseException {
    	Formula f = new Instantiation("X1", new IdFormula("X2"));
    	Formula g = f.uninstantiate("X1");
    	assertEquals(new IdFormula("X1"), g);
    }

    public void testClose() {
    	Formula f = new NotFormula(new NotFormula(new Application("f", new Application("g", new IdFormula("c")))));
    	Formula g = new NotFormula(new Application("f", new Application("g", new IdFormula("c"))));
    	
    	assertTrue(g.closesWith(f));
    	assertTrue(f.closesWith(g));
    }
    
}
