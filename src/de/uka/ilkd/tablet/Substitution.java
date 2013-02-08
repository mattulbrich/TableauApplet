package de.uka.ilkd.tablet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Substitution {

	private Map<String, Formula> map = new HashMap<String, Formula>();

	public Set<String> getDomain() {
		return map.keySet();
	}

	public Formula get(String var) {
		return map.get(var);
	}

	public void add(String var, Formula term) {
		try {
			for(String v : getDomain()) {
				Formula org = get(v);
				Formula newTerm = org.instantiate(var, term);
				map.put(v, newTerm);
			}
			map.put(var, term);
		} catch (InstantiationClashException e) {
			// will not happen: only terms
			throw new Error(e);
		}
	}

	public Formula applyTo(Formula f) {
		try {
			for (String v : getDomain()) {
				f = f.instantiate(v, get(v));
			}
			return f;
		} catch (InstantiationClashException e) {
			// will not happen: only terms
			throw new Error(e);
		}
	}
	
	@Override
	public String toString() {
		return "Subst" + map;
	}

}
