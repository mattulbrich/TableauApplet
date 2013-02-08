package de.uka.ilkd.tablet;

/**
 * applies an actionn to a node and to all its children
 *  
 * @author mattias
 */
public interface NodeVisitor {
	/**
	 * 
	 * @param node
	 * @return true if children are to be visited, false if not
	 */
	public boolean visit(Node node);
}
