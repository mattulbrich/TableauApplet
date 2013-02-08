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
