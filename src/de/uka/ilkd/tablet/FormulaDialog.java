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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;

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

/**
 * Little dialog to allow to enter formulas
 */

@SuppressWarnings("serial")
public class FormulaDialog extends javax.swing.JDialog {

    private JLabel jLabel1;

    private JButton jOK;

    private JButton jCancel;

    private JPanel jPanel;

    private JTextArea jArea;

    private boolean okPressed = false;

    /**
     * Auto-generated main method to display this JDialog
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        FormulaDialog inst = new FormulaDialog(frame);
        inst.setVisible(true);
    }

    public FormulaDialog(JFrame frame) {
        super(frame);
        initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            getContentPane().setLayout(thisLayout);
            this.setResizable(false);
            this.setModal(true);
            this.setTitle("Enter formulas");
            {
                jLabel1 = new JLabel();
                getContentPane().add(jLabel1, BorderLayout.NORTH);
                BorderLayout jLabel1Layout = new BorderLayout();
                jLabel1.setLayout(jLabel1Layout);
                jLabel1
                        .setText("<html>Enter the formulas to start the tableau with.<br> Separate them by ';'</html>");
                jLabel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
                        10));
            }
            {
                jArea = new JTextArea();
                getContentPane().add(jArea, BorderLayout.CENTER);
                jArea.setBorder(BorderFactory
                        .createBevelBorder(BevelBorder.LOWERED));
            }
            {
                jPanel = new JPanel();
                getContentPane().add(jPanel, BorderLayout.SOUTH);
                {
                    jOK = new JButton();
                    jPanel.add(jOK);
                    jOK.setText("OK");
                    jOK.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            okPressed = true;
                            setVisible(false);
                        }
                    });
                }
                {
                    jCancel = new JButton();
                    jPanel.add(jCancel);
                    jCancel.setText("Cancel");
                    jCancel.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            setVisible(false);
                        }
                    });
                }
            }
            this.setSize(377, 300);
            getRootPane().setDefaultButton(jOK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getInput() {
        return jArea.getText();
    }

    public void setInput(String string) {
        jArea.setText(string);
    }
    
    public boolean isOK() {
        return okPressed;
    }

    public void unsetOK() {
        okPressed = false;
    }

	public void go(int line, int col) {
		try {
			int linestart = jArea.getLineStartOffset(line - 1);
			jArea.setCaretPosition(linestart + col - 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
			// just ignore it
		}
	}

    

}
