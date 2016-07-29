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


public interface ChoiceItem extends Comparable<ChoiceItem> {
	
	public HistoryItem apply(Node root) throws IllegalStateException;
	
//	public Node getGoal();
	
	public static class Gamma implements ChoiceItem {
		Node target;
		Node gamma;
		// how often has this gamma formula been instantiated on the branch?
		int instCount;
		
		public Gamma(Node target, Node gamma) {
			super();
			this.target = target;
			this.gamma = gamma;
			for(Node n = target; n != null; n = n.getParent()) {
				if(n.getReason() == gamma)
					instCount++;
			}
		}

		public HistoryItem apply(Node root) {
			return target.expandAsSucc(gamma);
		}
		
		@Override
		public String toString() {
			return "Gamma[" + gamma.getNumber() +" on " + target.getNumber()+"]";
		}

		public int compareTo(ChoiceItem o) {
			if (o instanceof Gamma) {
				Gamma g = (Gamma) o;
				if(instCount == g.instCount) {
					return g.gamma.getNumber() - gamma.getNumber(); 
				} else {
					return instCount - g.instCount;
				}
			}
			// first close than gamma
			return 1;
		}

	}
	
	public static class Close implements ChoiceItem {
		
		private Node node1;
		private Node node2;
		private int minIndex;
		
		public Close(Node node1, Node node2) {
			super();
			assert canUnify(node1, node2);
			this.node1 = node1;
			this.node2 = node2;
			this.minIndex = Math.min(node1.getNumber(), node2.getNumber());
		}

		public static boolean canUnify(Node node1, Node node2) {
			Formula f1 = node1.getFormula();
			Formula f2 = node2.getFormula();
			Substitution inst = new Substitution();
			return Formula.unify(f1, new NotFormula(f2), inst)
				|| Formula.unify(new NotFormula(f1), f2, inst);
		}

		public HistoryItem apply(Node root) throws IllegalStateException {
			
			assert node1.hasAsAncestor(node2) || node2.hasAsAncestor(node1);
			
			Formula f1 = node1.getFormula();
			Formula f2 = node2.getFormula();
			HistoryItem.CompoundItem ret = new HistoryItem.CompoundItem();
			Substitution inst = new Substitution();
			
			boolean canUnify = Formula.unify(f1, new NotFormula(f2), inst)
	        		|| Formula.unify(new NotFormula(f1), f2, inst);
	        
			if (!canUnify) 
				throw new IllegalStateException("Unification impossible");
			
			System.out.println("Unification by " + inst);
			for (String var : inst.getDomain()) {
				try {
					HistoryItem.Instantiation instantiation = 
						new HistoryItem.Instantiation(var, inst.get(var));
//					instantiation.tree = root.toTree();
					ret.add(instantiation);
					
					root.instantiate(var, new Instantiation(var, inst.get(var)));
				} catch (InstantiationClashException e) {
					throw new IllegalStateException("Unification impossible");
				}
			}
			
			assert node1.getFormula().closesWith(node2.getFormula());

			HistoryItem.Close closeItem = new HistoryItem.Close(node1, node2);
			
			// Now, after the instantiation;
			if (node1.hasAsAncestor(node2)) {
				node1.setClosed(closeItem.getNumber());
			} else if (node2.hasAsAncestor(node1)) {
				node2.setClosed(closeItem.getNumber());
			} else {
				throw new IllegalStateException("Two nodes are not on one branch");
			}

			ret.add(closeItem);
			
			return ret;
		}

		@Override
		public String toString() {
			return "CloseItem["+node1.getNumber()+" and "+node2.getNumber()+"]";
		}

		public int compareTo(ChoiceItem o) {
			if (o instanceof Close) {
				Close cl = (Close) o;
				return minIndex - cl.minIndex;
			}
			// first close than gamma
			return -1;
		}
		
	}
}


