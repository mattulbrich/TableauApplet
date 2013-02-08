package de.uka.ilkd.tablet;

import java.util.*;

/**
 * sth that can be put on the history stack
 * 
 * @author mattias
 * @see History
 */
public interface HistoryItem {

	public void undo(Node root);
	
	public boolean isNotEmpty();

	public static class Instantiation implements HistoryItem {

		private String variable;
		private Formula instantiation;
		
		public Instantiation(String variable, Formula instantiation) {
			super();
			this.variable = variable;
			this.instantiation = instantiation;
		}

		public void undo(Node root) {
			root.uninstantiate(variable);
		}
		
		@Override
		public String toString() {
			return "Instantiate " + variable + " = " + instantiation;
		}

		public boolean isNotEmpty() {
			return true;
		}
	}
	
	public static class Close implements HistoryItem {
		private Node node1;
		private Node node2;
		
		// internal number to identify nodes closed by this item.
		private int number;
		// the counter of Objects of this type
		private static int counter = 1;
		
		public Close(Node positiveNode, Node negativeNode) {
			super();
			this.node1 = positiveNode;
			this.node2 = negativeNode;
			this.number = ++counter ;
		}

		public void undo(Node root) {
			// only run it once -- on the one higher in the tree.
			if(node1.getNumber() < node2.getNumber()) {
				node1.unsetClosed(number);
			} else {
				node2.unsetClosed(number);
			}
		}
		
		@Override
		public String toString() {
			return "Branch(es) closed with #" + node1.getNumber() + " and #" + node2.getNumber();
		}

		public int getNumber() {
			return number;
		}

		public boolean isNotEmpty() {
			return true;
		}
	}
	
	@SuppressWarnings("serial")
	public static class NewNodes extends ArrayList<Node> implements HistoryItem{
		
		public NewNodes() {
			super();
		}

		public void undo(Node root) {
			for (Node node : this) {
				node.remove();
			}
		}
		
		@Override
		public String toString() {
			String s = "Add nodes:";
			for (Node n : this) {
				s += n.getNumber() + " ";
			}
			return s;
		}

		public boolean isNotEmpty() {
			return size() > 0;
		}
	}
	
	public static class ChoicePoint implements HistoryItem {
		
		private List<ChoiceItem> choices = new LinkedList<ChoiceItem>();
		private Node goal;
		
		public String tree;

		public ChoicePoint(Node goal) {
			super();
			this.goal = goal;
		}

		public ChoiceItem take() {
			if(choices.isEmpty())
				return null;
			else {
				ChoiceItem first = choices.remove(0);
				choices.remove(first);
				return first;
			}
		}

		public void add(ChoiceItem e) {
			choices.add(e);
		}

		public void undo(Node root) {
			throw new UnsupportedOperationException("This may not be called on this pseudo element");
		}
		
		@Override
		public String toString() {
			return "ChoicePoint[" + choices + "]";
		}

		public Node getGoal() {
			return goal;
		}
		
		public void sort() {
			Collections.sort(choices);
		}

		public boolean isNotEmpty() {
			return true;
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class CompoundItem extends ArrayList<HistoryItem> implements HistoryItem  {

		public CompoundItem(HistoryItem h1, HistoryItem h2) {
			super(2);
			add(h1);
			add(h2);
		}
		
		public CompoundItem() {
			super(2);
		}
		
		public void undo(Node root) {
			for (int i = size()-1; i >= 0; i--) {
				get(i).undo(root);
				System.out.println(" +UNDO " + get(i));
			}
		} 
		
		public boolean isNotEmpty() {
			return size() > 0;
		}
	}

}
