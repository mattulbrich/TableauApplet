package de.uka.ilkd.tablet;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.uka.ilkd.tablet.HistoryItem.NewNodes;

/**
 * Nodes the vertices within the tableau tree. They are used for graphics
 * (layout, painting) and for algorithms like closing ...
 * 
 * @author MU
 * 
 */
public class Node implements Iterable<Node>, Comparable<Node> {

	/** this information is not used in this class but stored for NodeUI */
    Rectangle bound = new Rectangle();

    /** depth in the tree, root has 0. */
    private int depth;

    /** the contained formula */
    private Formula formula;

    /** a list of direct successors */
    private List<Node> succs = new ArrayList<Node>(2);

    /** the node directly above, null for the first node */
    private Node parent;

    /** the node that causes my existence (or null if initially there) */
    private Node reason;

    /** "serial number" */
    private int number;
    
    /** This node has been closed if this is positive.
     * If it is 0, it is open.
     * The number (other than 0) refers to the number of the close operation
     * We store it to be able to undo it if needed. */
    private int closedBy;

    /** to create serial numbers */
    private static int overAllCounter = 0;

    public Node(int depth, Formula formula, Node parent, Node reason) {
        super();
        this.depth = depth;
        this.formula = formula;
        this.parent = parent;
        this.reason = reason;
        this.number = ++overAllCounter;
    }

    /**
     * expand the given Node according to the rules and 
     * place the children underneath me. I must be a leaf for that.
     * 
     * @return a HistoryItem that contains the newly created nodes.
     */
    public HistoryItem.NewNodes expandAsSucc(Node node) {
    	
    	assert isLeaf();
    	assert node == this || hasAsAncestor(node);
    	assert !isClosed();
    	
        Formula expandedFormula = node.getFormula();
        HistoryItem.NewNodes newnodes = new HistoryItem.NewNodes();

        Formula[][] expansion = expandedFormula.applyRule(false);
        if (expansion != null) {
            for (Formula[] branch : expansion) {
                Node target = this;
                for (Formula f : branch) {
                    target = target.addSucc(f, node);
                    newnodes.add(target);
                }
            }
        }
        
        return newnodes;
    }

    /**
     * add a new successor. It has my node as parent.
     * 
     * @param f
     *                formula to embed
     * @param reason
     *                reason to set in the new node
     * @return newly created node
     */
    public Node addSucc(Formula f, Node reason) {
        Node n = new Node(depth + 1, f, this, reason);
        succs.add(n);
        return n;
    }



    /**
     * get the label.
     * 
     * This depends on the setting of "tablet.ancestor". If this is set to true
     * nodes will be labelled
     * 
     * "Number[Ancestornumber] : formula"
     * 
     * but otherwise only
     * 
     * "Number : formula"
     * 
     * @return new string
     */
    public String getText() {
        if (TableauApplet.SHOW_ANCESTORS && reason != null)
            return getNumber() + "[" + reason.getNumber() + "]" + ": " + formula;
        else
            return getNumber() + ": " + formula;
    }

 
    @Override
    public String toString() {
        return "Node[#" + getNumber() + 
        	", parent=" + (parent == null ? "null" : "#"+parent.getNumber()) + 
        	", depth=" + depth + ", form=" + formula +
       		(closedBy > 0 ? ", closedby=" + closedBy : "") +
       		", #succ=" + succs.size() + "]";
    }

    // debug
    public String toTree() {
    	StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sb.append(" ");
        }
        sb.append(this).append("\n");
        for (Node n : succs) {
            sb.append(n.toTree());
        }
        return sb.toString();
    }

    /**
     * mark all leafs below this node as closed
     * @param by this is the internal number of the close operatio, > 0
     * @return true iff at least one node has not been closed before and is closed now
     */
    boolean setClosed(int by) {
    	assert by > 0;
        if (isLeaf()) {
        	if(!isClosed()) {
        		closedBy = by;
        		return true;
        	} else {
        		return false;
        	}
        }
        else {
        	boolean oneClosed = false;
            for (Node n : succs) {
                oneClosed |= n.setClosed(by);
            }
            return oneClosed;
        }
    }

    /**
	 * mark all leafs below this node as open if they are closed by a particular
	 * closing operation. may happen during rollbacks
	 * 
	 * @param by
	 *            this is the internal number of the close operatio, > 0
	 */
    void unsetClosed(int by) {
    	if(closedBy == by)
    		closedBy = 0;
    	for (Node n : succs) {
    		n.unsetClosed(by);
        }
    }

    /**
     * get the maximum depth from this node on starting w/ the stored depth.
     */
    public int getMaxDepth() {
        int d = isClosed() ? depth + 1 : depth;
        for (Node n : succs) {
            d = Math.max(d, n.getMaxDepth());
        }
        return d;
    }

    /**
     * tool tip tells about the formula
     * 
     * @return
     */
    public String getToolTipText() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>This is a " + formula.getType() + "-formula<br>");
        if (reason != null)
            sb.append("resulting from #" + reason.getNumber() + " ("
                    + reason.formula.getType() + ")");
        else
            sb.append("and an initial formula");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * per def: a node is a leaf if it has no children and is not closed
     */
    public boolean isLeaf() {
        return succs.isEmpty() && !isClosed();
    }

    public void instantiate(String var, Formula f) throws InstantiationClashException {
        formula = formula.instantiate(var, f);
        for (Node n : succs) {
            n.instantiate(var, f);
        }
    }

    public void uninstantiate(String freevar) {
    	// no longer needed?
//        closed = false;
        formula = formula.uninstantiate(freevar);
        for (Node n : succs) {
            n.uninstantiate(freevar);
        }
    }

    /**
     * remove myself from my parent if I have one!
     * and remove my link to parent too
     */
    public void remove() {
        if (parent != null)
            parent.succs.remove(this);
        parent = null;
    }

    public Formula getFormula() {
        return formula;
    }
    
    public Node getParent() {
		return parent;
	}

    /**
     * is n an ancestor for me? is it a parent, or parent's parent ...
     */
    public boolean hasAsAncestor(Node n) {
        return parent == n || (parent != null && parent.hasAsAncestor(n));
    }

    /**
     * try automatic proving, i.e. saturated application of a certain type.
     * @return 
     * 
     */
    public NewNodes automaticApplication(Type type) {
    	NewNodes newNodes = new NewNodes();
        if (formula.getType() == type) {
            expandIfNotAlreadyExpanded(this, newNodes);
        }
        for (Node n : succs) {
            newNodes.addAll(n.automaticApplication(type));
        }
        return newNodes;
    }

    /**
     * check whether a node has already been expanded (using the reason field).
     * If not and it is a leaf: Expand here. If expanded: return. Else: descend
     * 
     * @param node
     *                Node to expand
     * @param newNodes storage for newly created nodes
     * @return the newly created nodes by this step
     */
    private void expandIfNotAlreadyExpanded(Node node, NewNodes newNodes) {
        if (reason != node) {
            if (isLeaf()) {
            	NewNodes expanded = expandAsSucc(node);
				newNodes.addAll(expanded);
            } else {
                for (Node n : succs) {
                    n.expandIfNotAlreadyExpanded(node, newNodes);
                }
            }
        } else {
        	// node has already been expanded
        }
    }

    /**
     * get the number of created nodes
     * 
     * @return the static value
     */
    public static int getCounter() {
        return overAllCounter;
    }

	public int getNumber() {
		return number;
	}
	
	public boolean isClosed() {
		return closedBy != 0;
	}

	/**
     * find a node with a given number
     * 
     * @param n
     *                number to look
     * @return null if not found in this tree, otherwise a node with number n
     */
    public Node getNode(int n) {
        if (getNumber() == n)
            return this;
        for (Node s : succs) {
            Node ret = s.getNode(n);
            if (ret != null)
                return ret;
        }
        return null;
    }

    private static void indent(StringBuilder sb, int depth) {
    	for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
    }
    
    // TODO ... put into a visitor or somewhere else ...
    
    public enum ChildMode { LEFT, RIGHT, SINGLE, NONE, ABSOLUTE; };
    
    public void toLatex(StringBuilder sb, ChildMode childMode) {
    	indent(sb, 1);
    	sb.append("\\node (n").
    	append(getNumber()).
    	append(") ");
    	switch(childMode) {
    	case LEFT:
    		sb.append("[below left of=n").append(parent.getNumber()).append("] ");
    		break;
    	case RIGHT:
    		sb.append("[below right of=n").append(parent.getNumber()).append("] ");
    		break;
    	case SINGLE:
    		sb.append("[below of=n").append(parent.getNumber()).append("] ");
    		break;
    	case ABSOLUTE:
    		sb.append(String.format((Locale)null, " at (%.1f,-%.1f) ", bound.x+bound.width/2., bound.y+bound.height/2.));
    		break;
    	}
    	sb.append("{ $").
    	append(formula.toLatex()).
    	append("$ \\tableauLabel{").
    	append(getNumber());
    	if(reason != null) {
    		sb.append("[$" + reason.formula.getType().getLatex() + "$(" + reason.getNumber() + ")]"); 
    	}
    	sb.append("}}");
    	if(parent != null) {
    		sb.append(" edge (n").append(parent.getNumber()).append(")");
    	}
    	sb.append(";\n");

    	if(childMode == ChildMode.ABSOLUTE) {
    		for(Node n : succs) {
    			n.toLatex(sb, ChildMode.ABSOLUTE);
    		}
    	} else {
			switch (succs.size()) {
			case 1:
				succs.get(0).toLatex(sb, ChildMode.SINGLE);
				break;
			case 2:
				succs.get(0).toLatex(sb, ChildMode.LEFT);
				succs.get(1).toLatex(sb, ChildMode.RIGHT);
				break;
			}
		}
    	
    	if(isClosed()) {
    		indent(sb, 1);
    		sb.append(String.format("\\draw (n%d)+(0,-%d) node (c%d) {\\tableauClose};\n", getNumber(), 20/*height*/, getNumber()));
    	}
    	
    }

	public List<Node> getSuccs() {
		return Collections.unmodifiableList(succs);
	}

	public int getDepth() {
		return depth;
	}
	
	public void visit(NodeVisitor visitor) {
		boolean visitKids = visitor.visit(this);
		if(visitKids) {
			for (Node node : succs) {
				node.visit(visitor);
			}
		}
	}

 	private static class Itr implements Iterator<Node> {
 		private LinkedList<Node> stack = new LinkedList<Node>();
 		
 		private Itr(Node start) {
 			stack.add(start);
 		}
 		
		public boolean hasNext() {
			return !stack.isEmpty();
		}

		public Node next() {
			Node first = stack.removeFirst();
			stack.addAll(0,first.succs);
			return first;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	public Iterator<Node> iterator() {
		return new Itr(this);
	}

	/**
	 * If nodes need to be compared, they are ordered by their number
	 * @param o node to compare to, not null
	 * @return equal to <code>o.number - this.number</code>
	 */
	public int compareTo(Node o) {
		return o.getNumber() - getNumber();
	}

	public Node getReason() {
		return reason;
	}

	public static void resetCounter() {
		overAllCounter = 0;
    }
	


}