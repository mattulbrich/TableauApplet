package de.uka.ilkd.tablet;

import de.uka.ilkd.tablet.ChoiceItem.Close;
import de.uka.ilkd.tablet.ChoiceItem.Gamma;
import de.uka.ilkd.tablet.HistoryItem.ChoicePoint;
import de.uka.ilkd.tablet.HistoryItem.CompoundItem;
import de.uka.ilkd.tablet.HistoryItem.NewNodes;

public class AutomaticProving implements Runnable {
	
	private int maxChoiceDepth;
	private Node root;
	private History history;
	private Object triggerLock;
	private boolean trigger;
	
	public AutomaticProving(int maxChoiceDepth, Node root) {
		this(maxChoiceDepth, root, false);
	}
	
	public AutomaticProving(int maxChoiceDepth, Node root, boolean useTrigger) {
		this.maxChoiceDepth = maxChoiceDepth;
		this.root = root;
		if(useTrigger)
			this.triggerLock = new Object();
	}

	@Override
	public void run() {
		
		history = new History(root);
		
		history.add(runBasicSteps(root));
		history.add(runAutoClose(root));
		
		try {
			while(true) {
				
				waitTrigger();
				
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				
				Node goal = nextOpenGoal();
				
				if(goal == null) {
					// mark successful
					return;
				}
				
				int choiceDepth = countGamma(goal);
				
				if(choiceDepth > maxChoiceDepth) {
					history.rollBack();
				} else {			
					ChoicePoint choicePoint = createChoicepoint(goal);
					history.add(choicePoint);
				}
				
				ChoicePoint choicePoint = (ChoicePoint) history.peek();
				if(choicePoint == null) {
					// mark unsuccessfull
					history.undoAll();
					return;
				}
				
				ChoiceItem item = choicePoint.take();
				while(item == null) {
					history.take();
					
					history.rollBack();
					choicePoint = (ChoicePoint) history.peek();
					
					if(choicePoint == null) {
						// mark unsuccessfull
						history.undoAll();
						return;
					}
					
					item = choicePoint.take();
				}
				
				goal = choicePoint.getGoal();
				assert goal.isLeaf();
				assert goal == root || goal.hasAsAncestor(root);
				assert root.toTree().equals(choicePoint.tree) : root.toTree();
				
				history.add(item.apply(root));
				
				history.add(runBasicSteps(goal));
				// TODO reicht hier ggf. goal?
				history.add(runAutoClose(root));
				
			}
		} catch (InterruptedException e) {
			System.err.println("AP has been interrupted");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		history.undoAll();
	}
	
	private int countGamma(Node goal) {
		Node reason = goal.getReason();
		int val;
		if(reason != null && reason.getFormula().getType() == Type.GAMMA)
			val = 1;
		else 
			val = 0;
		
		Node parent = goal.getParent();
		if(parent != null)
			val += countGamma(parent);
		
		return val;
	}

	private Node nextOpenGoal() {
		Node smallestLeaf = null;
		for (Node node : root) {
			if(node.isLeaf() && (smallestLeaf == null || node.getNumber() < smallestLeaf.getNumber()))
				smallestLeaf = node;
		}
		return smallestLeaf;
	}
	
	private ChoicePoint createChoicepoint(Node leaf) {
		ChoicePoint choicePoint = new ChoicePoint(leaf);
		// first close then gamma ...
		addClose(choicePoint, leaf);
		addGamma(choicePoint, leaf);
		choicePoint.tree = root.toTree();
		choicePoint.sort();
		return choicePoint;
	}
	
	// choose those gamma formulas more likely that are less expanded on that tree.
	private void addGamma(ChoicePoint choicePoint, Node leaf) {
		for(Node n = leaf; n != null; n = n.getParent()) {
			Formula f = n.getFormula();
			if(f.getType() == Type.GAMMA) {
				Gamma gamma = new Gamma(leaf, n);
//				if(gamma.instCount == 0) {
					choicePoint.add(gamma);
//			}
			}
		}
			
	}

	private void addClose(ChoicePoint choicePoint, Node leaf) {
		for(Node n = leaf; n != null; n = n.getParent())
			for(Node m = n.getParent(); m != null; m = m.getParent()) {
				if(Close.canUnify(m,n)) {
					choicePoint.add(new Close(m, n));
//					System.out.println(" CLOSE " + m + n);
				}
			}
	}

	public NewNodes runBasicSteps(Node below) {
		int before;
		NewNodes newNodes = new NewNodes();
		do {
            before = Node.getCounter();
            newNodes.addAll(below.automaticApplication(Type.ALPHA));
            newNodes.addAll(below.automaticApplication(Type.DELTA));
            newNodes.addAll(below.automaticApplication(Type.NEGNEG));
            newNodes.addAll(below.automaticApplication(Type.BETA));
        } while (Node.getCounter() > before);
		return newNodes;
	}
	
	private CompoundItem runAutoClose(Node below) {
		CompoundItem coll = new CompoundItem();
		for(Node n : below) {
			for(Node p = n.getParent(); p != null; p = p.getParent()) {
				if(n.getFormula().closesWith(p.getFormula())) {
					HistoryItem.Close histItem = new HistoryItem.Close(n, p);
					if(n.setClosed(histItem.getNumber()));
						coll.add(histItem);
				}
			}
		}
		return coll;
	}
	
	public void trigger() {
		if (triggerLock != null)
			synchronized (triggerLock) {
				trigger = true;
				triggerLock.notify();
			}
	}
	
	private void waitTrigger() throws InterruptedException {
		if(triggerLock != null) {
			synchronized (triggerLock) {
				while(!trigger)
					triggerLock.wait();
				trigger = false;
			}
		}
	}

	public void addHistoryTo(History hist) {
		for (HistoryItem hi : history) {
			hist.add(hi);
		}
	}
	
}
