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
