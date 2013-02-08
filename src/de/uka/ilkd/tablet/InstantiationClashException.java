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
