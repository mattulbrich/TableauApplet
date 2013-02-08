package de.uka.ilkd.tablet;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import de.uka.ilkd.tablet.BinopFormula.Operator;
import de.uka.ilkd.tablet.QuantorFormula.Quantor;

public class ModelSearch extends Thread {

    private String input;
    private ISolver solver;
    private int nextVar;
    private Map<String, Integer> translations;
    private int bound;

    public ModelSearch(String input, int bound) {
        this.input = input;
        this.bound = bound;
    }

    @Override
    public void run() {
        // tableauApplet.setComment("Running SAT solver to find finite model. Press button again to stop");
        try {
            
            
            FormulaParser parser = new FormulaParser(new StringReader(input));
            Formula[] formulas = parser.Formulae();

            solver = SolverFactory.newDefault();
            nextVar = 1;
            translations = new HashMap<String, Integer>();

            for (Formula f : formulas) {
                f = expandFormula(f, bound, new StackMap<String, String>());
                int id = toClauses(f);
                addClause(id);
            }
            
            if(solver.isSatisfiable()) {
                System.out.println("Satisfiable");
                int[] model = solver.model();
//                for (int i : model) {
//                    System.out.println(i);
//                }
                for (Entry<String, Integer> entry : translations.entrySet()) {
                    Integer index = entry.getValue();
                    System.out.println(entry.getKey() + " (" + index + ") => " + (model[index-1] > 0));
                }
            } else {
                System.out.println("Not satisfiable");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // tableauApplet.setComment("");
        }
    }

    private int toClauses(Formula f) throws ContradictionException {
        
        if (f instanceof NotFormula) {
            return -toClauses(f.getSubFormula(0));
        }
        
        if (f instanceof IdFormula || f instanceof Application) {
            String str = f.toString();
            if(translations.containsKey(str)) {
                return translations.get(str);
            } else {
                int v = nextVar;
                nextVar++;
                translations.put(str, v);
                return v;
            }
        }
        
        if (f instanceof BinopFormula) {
            BinopFormula binop = (BinopFormula) f;
            int f1 = toClauses(f.getSubFormula(0));
            int f2 = toClauses(f.getSubFormula(1));
            switch(binop.getOp()) {
            case AND:
                addClause(-nextVar, f1);
                addClause(-nextVar, f2);
                addClause(-f1, -f2, nextVar);
                break;
            case OR:
                addClause(-nextVar, f1, f2);
                addClause(-f1, nextVar);
                addClause(-f2, nextVar);
                break;
            case IMPL:
                addClause(-nextVar, -f1, f2);
                addClause(f1, nextVar);
                addClause(-f2, nextVar);
                break;
            case EQUIV:
                addClause(-nextVar, -f1,  f2);
                addClause(-nextVar,  f1, -f2);
                addClause( nextVar,  f1,  f2);
                addClause( nextVar, -f1, -f2);
                break;
            default: throw new Error("Cannot happen");
            }
            return nextVar++;
        }
        
        throw new IllegalStateException("Unsipported formual: " + f);
    }

    private void addClause(int... vars) throws ContradictionException {
        for (int i : vars) {
            System.out.print(i + " ");
        }
        System.out.println();
        solver.addClause(new VecInt(vars));
    }

    private Formula expandFormula(Formula f, int bound, StackMap<String,String> varMap) {
        
        if (f instanceof BinopFormula) {
            BinopFormula binop = (BinopFormula) f;
            Formula f1 = expandFormula(binop.getSubFormula(0), bound, varMap);
            Formula f2 = expandFormula(binop.getSubFormula(1), bound, varMap);
            return new BinopFormula(binop.getOp(), f1, f2);
        }
        
        if (f instanceof NotFormula) {
            Formula f1 = expandFormula(f.getSubFormula(0), bound, varMap);
            return new NotFormula(f1);
        }
        
        if (f instanceof IdFormula) {
            IdFormula id = (IdFormula) f;
            if(varMap.containsKey(id.identifier)) {
                return new IdFormula(varMap.get(id.identifier));
            } else {
                return f;
            }
        }
        
        if (f instanceof Application) {
            Application app = (Application) f;
            Formula arg[] = new Formula[app.countSubFormulae()];
            for (int i = 0; i < arg.length; i++) {
                arg[i] = expandFormula(f.getSubFormula(i), bound, varMap);
            }
            return new Application(app.identifier, arg);
        }
        
        if (f instanceof QuantorFormula) {
            QuantorFormula quant = (QuantorFormula) f;
            Formula quanted = f.getSubFormula(0);
            String var = quant.variable;
            Formula result = null;
            for (int i = 0; i < bound; i++) {
                varMap.push(var, "$" + i);
                f = expandFormula(quanted, bound, varMap);
                if(result == null) {
                    result = f;
                } else {
                    if(quant.quantor == Quantor.EXISTS) {
                        result = new BinopFormula(Operator.OR, result, f);
                    } else {
                        result = new BinopFormula(Operator.AND, result, f);
                    }
                }
                varMap.pop();
            }
            return result;
        }
        
        throw new IllegalArgumentException("Unsupported type of formula: " + f.getClass() + "; " + f);
    }
    
    public static void main(String[] args) {
        ModelSearch m = new ModelSearch("(E x. p(x)) & ~(A y. p(y))",1);
        m.run();
        
        m = new ModelSearch("(E x. p(x)) & ~(A y. p(y))",2);
        m.run();
        
        m = new ModelSearch("A x. A y. (p(x,y) -> p(x,y)) ; p(a,b) ; ~p(b,a)",4);
        m.run();
    }
    
}