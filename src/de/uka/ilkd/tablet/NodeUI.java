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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

public class NodeUI {
	
	/* ----- colours for painting */
    private static final Color alphaColor = new Color(128, 255, 128);
    private static final Color betaColor = new Color(128, 128, 255);
    private static final Color gammaColor = new Color(128, 255, 255);
    private static final Color deltaColor = new Color(255, 255, 128);
    private static final Color negnegColor = new Color(255, 128, 255);

	private static final int MARGIN = 10;
	private static final int DOUBLED_MARGIN = 2 * MARGIN;
	private static final int LEVEL_DISTANCE = DOUBLED_MARGIN;
	
    /** height of one level, -1 means not yet calculated */
    private int height = -1;
    
    /** to measure the size of the label */
    private FontMeasurer fontMetrics;

	private Node root;

    enum DragMode {
        NONE, EXPAND, CLOSE
    };

    /** Am I dragged, and if so: how am i dragged */
    private DragMode dragMode = DragMode.NONE;

	private Node draggedNode;
    
    public NodeUI(FontMeasurer fontMetrics, Node root) {
		super();
		this.fontMetrics = fontMetrics;
		this.height = fontMetrics.getBounds("X").height + DOUBLED_MARGIN + LEVEL_DISTANCE;
		this.root = root;
	}

	/**
     * return the height of this node.
     * 
     * inherited from parent, -1 if not yet calculated
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }
    
    private void translate(Node node, final int deltax) {
    	for (Node n : node) {
    		n.bound.x += deltax;
    	}
    }

    public int layout() {
    	calcSize();
    	return layout(root, 0);
    }
    
    private int layout(Node node, int xoffset) {

        int mywidth = node.bound.width + MARGIN;

        int growOffset = xoffset;
        for (Node n : node.getSuccs()) {
            growOffset = layout(n, growOffset);
        }

        int theirWidth = growOffset - xoffset;
        int maxWidth;
        if (theirWidth < mywidth) {
            int trans = (mywidth - theirWidth) / 2;
            for (Node n : node.getSuccs()) {
                translate(n, trans);
            }
            maxWidth = mywidth;
        } else {
            maxWidth = theirWidth;
        }

        node.bound.y = height * node.getDepth();
        node.bound.x = xoffset + (maxWidth - mywidth) / 2;

        return xoffset + maxWidth;
    }
    
    /**
     * does this node contain a point?
     * 
     * @param p
     *                point to check
     * @return true iff the point lays within the boundaries.
     */
    public Node contains(Point p) {
    	return contains(root, p);
    }
    
    private Node contains(Node node, Point p) {
    	for (Node n : node) {
			if(n.bound.contains(p))
				return n;
		}
    	return null;
    }
    
	public void paint(Graphics2D g) {
		paint(root, g);
	}
	
    private void paint(Node node, Graphics2D g) {

        g.setColor(Color.black);

        Point center = getCenter(node);
        for (Node n : node.getSuccs()) {
        	if(isLaidOut(n)) {
        		Point nc = getCenter(n);
        		g.drawLine(center.x, center.y, nc.x, nc.y);
        	}
        }
        
        if(!isLaidOut(node))
        	return;

        if (node.isClosed()) {
            g.drawLine(center.x, center.y, center.x, center.y + height);
            g.fillOval(center.x - MARGIN, center.y + height - MARGIN, DOUBLED_MARGIN, DOUBLED_MARGIN);
        }

        switch (node.getFormula().getType()) {
        case ALPHA:
            g.setColor(alphaColor);
            break;
        case BETA:
            g.setColor(betaColor);
            break;
        case GAMMA:
            g.setColor(gammaColor);
            break;
        case DELTA:
            g.setColor(deltaColor);
            break;
        case NEGNEG:
            g.setColor(negnegColor);
            break;
        default:
            g.setColor(Color.white);
            break;
        }

        g.fillRect(node.bound.x, node.bound.y, node.bound.width, node.bound.height);
		g.setColor(Color.black);

        if(draggedNode == node) {
        	switch (getDragMode()) {
        	case EXPAND:
        		g.setColor(Color.red);
        		break;
        	case CLOSE:
        		g.setColor(Color.blue);
        		break;
        	}
        }

        g.drawRect(node.bound.x, node.bound.y, node.bound.width, node.bound.height);

        int descent = (int) fontMetrics.getDescent("p");
        g.drawString(node.getText(), node.bound.x + MARGIN, 
        		node.bound.y + node.bound.height - MARGIN
                - descent);

        for (Node n : node.getSuccs()) {
            paint(n, g);
        }
    }
    
    private boolean isLaidOut(Node n) {
		return n.bound.width != 0;
	}

	public int getWidth() {
    	return getWidth(root);
    }

    /**
     * get the width over all nodes
     * 
     * @return the width of the tree beginning here
     */
    private int getWidth(Node node) {
        int width = node.bound.width + MARGIN;

        int theirWidth = 0;
        for (Node n : node.getSuccs()) {
            theirWidth += getWidth(n);
        }
        width = Math.max(width, theirWidth);

        return width;
    }
    
    public void calcSize() {
    	for (Node node : root) {
    		String formString = node.getText();
            Dimension b = fontMetrics.getBounds(formString);
            node.bound.width = b.width + DOUBLED_MARGIN;
            node.bound.height = b.height + DOUBLED_MARGIN;
		}
    }

    /**
     * calculate the dimensions (not the position)
     */
    
    /**
     * get the center of the bounding box
     * 
     * @return a newly created Point.
     */
    private Point getCenter(Node node) {
        return new Point(node.bound.x + node.bound.width / 2, 
        		node.bound.y + node.bound.height / 2);
    }

	public void setDragged(Node node, DragMode dragMode) {
		this.draggedNode = node;
		this.dragMode = dragMode;
	}

	public DragMode getDragMode() {
		return dragMode;
	}    
}
