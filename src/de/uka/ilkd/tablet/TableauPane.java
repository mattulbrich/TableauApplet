package de.uka.ilkd.tablet;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

import de.uka.ilkd.tablet.HistoryItem.NewNodes;
import de.uka.ilkd.tablet.Node.ChildMode;
import de.uka.ilkd.tablet.NodeUI.DragMode;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
@SuppressWarnings("serial")
public class TableauPane extends JComponent implements MouseInputListener,
Printable {

	public static int FONT_SIZE = 12;

	public static boolean ALLOW_UNIFICATION = false; 

	private Node root;

	private JLabel comments;

	private Node draggedNode;

	private History history = new History();

	private NodeUI nodeUI;

	private Thread automaticProveThread;

	private AutomaticProving automaticProve;

	public TableauPane(JLabel comments) {
		this.comments = comments;
		addMouseListener(this);
		addMouseMotionListener(this);
		setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
	}

	/**
	 * (re)initialise the applet with an array of formulas
	 * 
	 * @param forms
	 *                array to start with
	 */
	public void init(Formula forms[]) {
		root = new Node(0, forms[0], null, null);
		nodeUI = new NodeUI(new FontMeasurer((Graphics2D) getGraphics()), root);

		Node last = root;
		for (int i = 1; i < forms.length; i++) {
			last = last.addSucc(forms[i], null);
		}

		invalidate();
		lay();
		comments.setText("Choose the open leaf to extend");
		history = new History(root);
	}

	/**
	 * lay out the component. Put each node to its place. And set the size of
	 * the component.
	 */
	public void lay() {
		if (root != null) {
			nodeUI.layout();
			setSize(getOptimalSize());
			//System.out.println("getSize()=" + getSize());
			setMinimumSize(getSize());
			setPreferredSize(getSize());
			getParent().invalidate();
		}
	}

	/**
	 * get the size which would be ideal for this component, i.e. the space
	 * needed by the tree itself
	 * 
	 * @return the ideal dimensions
	 */
	public Dimension getOptimalSize() {
		if(root == null)
			return null;

		int width = nodeUI.getWidth() + 40;        
		int height = (root.getMaxDepth() + 2) * nodeUI.getHeight();
		return new Dimension(width, height);
	}

	/**
	 * paint it by painting the nodes
	 */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(20, 20);
		if (root != null)
			nodeUI.paint(g2);
	}

	public void refresh() {
		if (root != null) {
			lay();
			repaint();
		}
	}

	public void instantiate(String var, Formula f) throws InstantiationClashException {
		if (root != null) {
			root.instantiate(var, f);
			HistoryItem.Instantiation histItem = new HistoryItem.Instantiation(var, f);
			//            histItem.tree = root.toTree();
			history.add(histItem);
			comments.setText(histItem.toString());
		}
	}

	public void undo() {
		HistoryItem item = history.undo();
		if(item != null)
			comments.setText("Undone: " + item);
		else 
			comments.setText("Nothing to undo");
		refresh();
	}

	private void nodeDraggedExpand(Node from, Node to) {
		Formula expandedFormula = from.getFormula();
		Type type = expandedFormula.getType();

		if (!to.isLeaf()) {
			comments.setText("The target node is not a leaf");
			return;
		}

		if (type == Type.NONE) {
			comments.setText("This formula cannot be expanded any more");
			return;
		}

		NewNodes newnodes = to.expandAsSucc(from);

		comments.setText("A " + type + "-Formula has been expanded. " + newnodes);
		history.add(newnodes);
		lay();
	}

	private void nodeDraggedClose(Node n1, Node n2) {

		if (n1.getFormula().closesWith(n2.getFormula())) {
			HistoryItem.Close histItem = new HistoryItem.Close(n1, n2);

			if (n1.hasAsAncestor(n2)) {
				n1.setClosed(histItem.getNumber());
			} else if (n2.hasAsAncestor(n1)) {
				n2.setClosed(histItem.getNumber());
			} else {
				comments.setText("The two nodes are not on one branch!");
				return;
			}

			comments.setText(histItem.toString());
			history.add(histItem);
		} else {
			if (ALLOW_UNIFICATION) {
				if (tryUnification(n1.getFormula(), n2.getFormula())) {
					comments
					.setText("Formulas have been unified and can now be closed");
					refresh();
					return;
				}
			}
			comments.setText("These formulas do not match to close");
		}
		lay();
	}

	private boolean tryUnification(Formula f1, Formula f2) {
		Substitution inst = new Substitution();
		if (Formula.unify(f1, new NotFormula(f2), inst )
				|| Formula.unify(new NotFormula(f1), f2, inst)) {
			System.out.println("Unification by " + inst);
			for (String var : inst.getDomain()) {
				try {
					instantiate(var, new Instantiation(var, inst.get(var)));
					history.add(new HistoryItem.Instantiation(var, inst.get(var)));
				} catch (InstantiationClashException e) {
					// if this instantiation results in a collision, unification
					// is impossible
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)
				&& e.isControlDown()) {
			Point p = e.getPoint();
			p.translate(-20, -20);
			Node n = nodeUI.contains(p);
			System.out.println(n);
			if (n != null) {
				try {
					int number = Integer.parseInt(JOptionPane
							.showInputDialog("Number of the node to expand: "));
					Node m = root.getNode(number);
					if (m != null)
						nodeDraggedExpand(m, n);
				} catch (Exception ex) {
					comments.setText(ex.toString());
				}
			}
		} else if (e.getClickCount() >= 2
				&& SwingUtilities.isRightMouseButton(e) && e.isControlDown()) {
			Point p = e.getPoint();
			p.translate(-20, -20);
			Node n = nodeUI.contains(p);
			System.out.println(n);
			if (n != null) {
				try {
					int number = Integer
							.parseInt(JOptionPane
									.showInputDialog("Number of the node to close/unify: "));
					Node m = root.getNode(number);
					if (m != null)
						nodeDraggedClose(m, n);
				} catch (Exception ex) {
					comments.setText(ex.toString());
				}
			}
		} else if (e.getClickCount() >= 2) {
			Point p = e.getPoint();
			p.translate(-20, -20);
			Node n = nodeUI.contains(p);
			System.out.println(n);
			if (n != null)
				nodeDraggedExpand(n, n);
		}
		refresh();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

		if (root == null)
			return;

		Point p = e.getPoint();
		p.translate(-20, -20);

		Node n = nodeUI.contains(p);
		System.out.println(n);
		draggedNode = n;
		if (n != null)
			if (SwingUtilities.isLeftMouseButton(e))
				nodeUI.setDragged(n, DragMode.EXPAND);
			else
				nodeUI.setDragged(n, DragMode.CLOSE);
		repaint();
	}

	public void mouseReleased(MouseEvent e) {

		if (root == null)
			return;

		Point p = e.getPoint();
		p.translate(-20, -20);
		Node n = nodeUI.contains(p);
		// System.out.println(n);

		if (draggedNode != null) {
			if (n != null && draggedNode != n) {
				if (nodeUI.getDragMode() == DragMode.EXPAND)
					nodeDraggedExpand(draggedNode, n);
				else
					nodeDraggedClose(draggedNode, n);
			}
			nodeUI.setDragged(null, DragMode.NONE);
		}
		draggedNode = null;

		repaint();
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		p.translate(-20, -20);
		if (root == null)
			return;
		Node n = nodeUI.contains(p);
		if (n != null) {
			setToolTipText(n.getToolTipText());
		} else {
			setToolTipText(null);
		}
	}

	/**
	 * do all Alpha, delta, negneg and beta steps possible automatically (Only
	 * available if not an applet)
	 */
	public void automaticProve() {

		if(root == null)
			return;

		if(automaticProveThread == null) {
			// currently no proof: start one
			// XXX
			int depth = Integer.parseInt(JOptionPane.showInputDialog("How many Gamma-Instances are allowed on a branch?"));
			automaticProve = new AutomaticProving(depth, root, false);
			automaticProveThread = new Thread(automaticProve, "Automatic Proving");
			automaticProveThread.start();

			// TODO Make other buttons unavailable and mouse listening
			// XXX TMP:
			ActionListener action = new ActionListener() {
				private char[] leader = {'·', 'o', 'O', 'o'};
				private int cnt = 0;
				public void actionPerformed(ActionEvent e) {
					nodeUI.calcSize();
					refresh();
					comments.setText(leader[cnt++%4] + " Hit AP-Button again to stop prove");
					automaticProve.trigger();
					if(!automaticProveThread.isAlive()) {
						((Timer)e.getSource()).stop();
						comments.setText("AP finished");
						automaticProve.addHistoryTo(history);
						automaticProve = null;
					}
				}};
				new Timer(1000, action).start();

		} else {
			// interrupt existing proof
			automaticProveThread.interrupt();
			comments.setText("AP interrupted");
		}
	}

	/*
	 * print this component.
	 * 
	 * centre graphic on page vertically and horizontally. 
	 * 
	 * It is scaled down, if it does not fit onto the page.
	 * 
	 * The scale is reduced because 72 dpi is far too much!
	 * 
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		// TODO a little more sophisticated please

		if (pageIndex == 0) {
			// centre on page
			double ph = pageFormat.getImageableHeight();
			double pw = pageFormat.getImageableWidth();
			Dimension gdim = getOptimalSize();
			double scalefactor = Math.min(.7, ph / gdim.getHeight());
			scalefactor = Math.min(scalefactor, pw / gdim.getWidth());

			int x = (int) (pageFormat.getImageableX() + (pw - gdim.width*scalefactor) / 2);
			int y = (int) (pageFormat.getImageableY() + (ph - gdim.height*scalefactor) / 2);

			graphics.translate(x, y);
			((Graphics2D)graphics).scale(scalefactor,scalefactor);

			paint(graphics);
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/**
	 * generate latex code for this tableau.
	 * make sourrounding begin and end tikzpicture and delegate to root
	 * @param sb string builder to write to
	 */
	public void toLatex(StringBuilder sb, boolean absolute) {
		if(absolute) {
			sb.append("\\begin{tikzpicture}[x=1pt, y=1pt]\n");
			root.toLatex(sb, ChildMode.ABSOLUTE);
		} else {
			sb.append("\\begin{tikzpicture}\n");
			root.toLatex(sb, ChildMode.NONE);
		}
		sb.append(";\n\\end{tikzpicture}\n");
	}
}