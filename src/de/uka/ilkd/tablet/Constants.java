package de.uka.ilkd.tablet;

import java.util.Set;

import de.uka.ilkd.tablet.BinopFormula.Operator;
import de.uka.ilkd.tablet.QuantorFormula.Quantor;

/**
 * This factory / formatter creates skolem and free variable symbols and
 * provides the characters for printing formulas. There is the switch
 * {@link #USE_UNICODE} which decides which characters are taken.
 * 
 * @author MU
 * 
 */
public class Constants {

    static boolean USE_UNICODE = true;

    private static int skolemCounter = 0;

    private static int freeVarCounter = 0;

    /**
     * make a new unique skolem symbol
     * @param set a set of parameter formulas.
     * @return an IdFormula iff list empty, otherwise an application.
     */
    public static Formula mkSkolem(Set<IdFormula> set) {
        String skolem = "sk" + (++skolemCounter);
        if (set.isEmpty()) {
            return new IdFormula(skolem);
        } else {
            Formula[] forms = set.toArray(new Formula[0]);
            return new Application(skolem, forms);
        }
    }

    /** 
     * create a new unique free variable
     * @return a newly created IdFormula X...
     */
    public static Formula mkFreeVar() {
        return new IdFormula("X" + (++freeVarCounter));
    }

    public static String getNot() {
        if (USE_UNICODE)
            return "" + (char) 172;
        else
            return "~";
    }

    public static String getOp(Operator op) {
        if (USE_UNICODE)
            return getOpUnicode(op);
        else
            return getOpASCII(op);
    }

    public static String getQuantor(Quantor q) {
        if (USE_UNICODE)
            return getQuantorUnicode(q);
        else
            return getQuantorASCII(q);
    }

    public static String getQuantorUnicode(Quantor q) {
        switch (q) {
        case EXISTS:
            return "" + (char) 8707;
        case FORALL:
            return "" + (char) 8704;
        }
        throw new Error();
    }

    public static String getQuantorASCII(Quantor q) {
        switch (q) {
        case EXISTS:
            return "E ";
        case FORALL:
            return "A ";
        }
        throw new Error();
    }
    
    public static String getQuantorLatex(Quantor q) {
        switch (q) {
        case EXISTS:
            return "\\exists ";
        case FORALL:
            return "\\forall ";
        }
        throw new Error();
    }

    public static String getOpASCII(Operator op) {
        switch (op) {
        case AND:
            return "&";
        case OR:
            return "|";
        case IMPL:
            return "->";
        case EQUIV:
            return "<->";
        }
        throw new Error();
    }
    
    public static String getOpLatex(Operator op) {
    	switch (op) {
        case AND:
            return " \\wedge ";
        case OR:
            return " \\vee ";
        case IMPL:
            return " \\rightarrow ";
        case EQUIV:
            return " \\leftrightarrow ";
        }
        throw new Error();
    }

    private static String getOpUnicode(Operator op) {
        switch (op) {
        case AND:
            return "" + (char) 8743;
        case OR:
            return "" + (char) 8744;
        case IMPL:
            return "" + (char) 8594;
        case EQUIV:
            return "" + (char) 8596;
        }
        throw new Error();
    }

    public static void resetCounters() {
    	skolemCounter = 0;
    	freeVarCounter = 0;
    }

}
