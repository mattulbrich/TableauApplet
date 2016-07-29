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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import de.uka.ilkd.tablet.HistoryItem.ChoicePoint;

/**
 * A proof history.
 * 
 * It records all steps that have been made and 
 * allows manual and automatic rollback.
 * 
 * Instantiations are also recorded to allow for ATP.
 * 
 * @author mattias ulbrich
 */

public class History implements Iterable<HistoryItem> {

	private LinkedList<HistoryItem> historyStack = new LinkedList<HistoryItem>();
	private Node root;
	
	public History(Node root) {
		this.root = root;
	}

	public History() {
		this.root = null;
	}

	public HistoryItem undo() {
		
		HistoryItem historyItem = null;
		while(!historyStack.isEmpty() && historyItem == null) {
			historyItem = take();
			if(historyItem instanceof ChoicePoint)
				historyItem = null;
		}
		
		if(historyItem == null)
			return null;
		
		historyItem.undo(root);
//		System.out.println("-- UNDO -- " + historyItem);
		return historyItem;
	}
	
	public void undoAll() {
		while(undo() != null);
	}

	public void add(HistoryItem item) {
		assert root != null;
		if(item.isNotEmpty()) {
//			System.out.println("-- DO -- " + item);
			historyStack.add(item);
		}
	}

	public void rollBack() {
		while(!historyStack.isEmpty()) {
			if(peek() instanceof ChoicePoint)
				break;
			
			HistoryItem item = take();
			item.undo(root);
//			System.out.println("-- UNDO --" + item);
//			System.out.println(root.toTree());
		}
	}

	public HistoryItem peek() {
		return historyStack.size() > 0 ? historyStack.getLast() : null;
	}
	
	public HistoryItem take() {
		return historyStack.removeLast();
	}

	public Iterator<HistoryItem> iterator() {
		return Collections.unmodifiableCollection(historyStack).iterator();
	}
	
	@Override
	public String toString() {
	    return historyStack.toString();
	}
}