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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO: Auto-generated Javadoc
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
public class TableauApplet extends javax.swing.JApplet {


    /**
	 * Version constant
	 */
    private static final String BUILD = "18";

    /*
	 * UI Elements
	 */
    private JToolBar southPanel;
    private JButton jNew;
    private JButton jSample;
    private JLabel jComment;
    private TableauPane tableauComponent;
    private JScrollPane jScrollPane;
    private JToggleButton jUnicode;
    private JButton jInstance;
    private JButton jAuto;
    private JButton jPrint;
    private JButton jLatex;
    private JButton jExport;
    private JButton jUndo;
    private JButton jModelSearch;

    private String lastInput = "";

//    private ModelSearch modelSearchThread;

    private final JComponent guiPanel;

    /**
     * static flag whether to indicate ancestors in nodes
     */
    public static boolean SHOW_ANCESTORS = true;

    /**
	 * static flag whether to allow automatic proofs
	 */
    public static boolean ALLOW_AUTORUN = false;


    /**
     * static flag whether to allow counter example search
     */
    public static final boolean ALLOW_MODELSEARCH = false;


    /**
	 * static flag whether whether to place tex elements
	 * absolute rather than relative
	 */
    public static boolean ABSOLUTE_TEX_EXPORT = true;

    /**
     * Main method to display this JApplet inside a new JFrame.
     *
     * First read and set system properties instead of applet parameters.
     */
    public static void main(String[] args) {

        // set system properties
        SHOW_ANCESTORS = Boolean.parseBoolean(System.getProperty("tablet.showancestor", Boolean.toString(SHOW_ANCESTORS)));
        ALLOW_AUTORUN = Boolean.parseBoolean(System.getProperty("tablet.allowautorun", Boolean.toString(ALLOW_AUTORUN)));
        ABSOLUTE_TEX_EXPORT = Boolean.parseBoolean(System.getProperty("tablet.absolutetex", Boolean.toString(ABSOLUTE_TEX_EXPORT)));
        TableauPane.FONT_SIZE = Integer.getInteger("tablet.fontsize", TableauPane.FONT_SIZE);
        TableauPane.ALLOW_UNIFICATION = Boolean.parseBoolean(System.getProperty("tablet.allowunification", Boolean.toString(TableauPane.ALLOW_UNIFICATION)));

        JFrame frame = new JFrame("Tableau Proof");
        TableauApplet inst = new TableauApplet();
        frame.getContentPane().add(inst.guiPanel);

        frame.setSize(600,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
	 * Instantiates a new tableau applet. Initialise the gui.
	 */
    public TableauApplet() {
        super();
        guiPanel = makeGUIPanel();
    }

    /*
     * read and set applet parameters
     */
    @Override
    public void init() {
        String showAnces = getParameter("showancestor");
        if("false".equals(showAnces)) {
            SHOW_ANCESTORS = false;
        }

        String autorun = getParameter("allowautorun");
        if("true".equals(autorun)) {
            ALLOW_AUTORUN = true;
            jAuto.setEnabled(ALLOW_AUTORUN);
        }

        String allowunification = getParameter("allowunification");
        if("true".equals(allowunification)) {
            TableauPane.ALLOW_UNIFICATION = true;
        }

        String absolutetx = getParameter("absolutetex");
        if("false".equals(absolutetx)) {
        	ABSOLUTE_TEX_EXPORT = false;
        }

        try {
            TableauPane.FONT_SIZE = Integer.parseInt(getParameter("fontsize"));
        } catch(Exception ex) {
            // okay, just don't set it
        }

        getContentPane().add(guiPanel);
    }

    /**
     * Inits the gui.
     * @return the freshly created component to represent this gui
     */
    private JComponent makeGUIPanel() {
        try {
            JPanel result = new JPanel();
            BorderLayout thisLayout = new BorderLayout();
            result.setLayout(thisLayout);
            {
                southPanel = new JToolBar();
                result.add(southPanel, BorderLayout.NORTH);
                {
                    jSample = new JButton();
                    southPanel.add(jSample);
                    jSample.setIcon(mkImg("sample"));
                    jSample.setToolTipText("Load a sample");
                    jSample.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            jSampleActionPerformed(evt);
                        }
                    });
                }
                {
                    jNew = new JButton();
                    southPanel.add(jNew);
                    jNew.setIcon(mkImg("new"));
                    jNew.setToolTipText("New Tableau");
                    jNew.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            jNewActionPerformed(evt);
                        }
                    });
                }
                {
                    jInstance = new JButton();
                    southPanel.add(jInstance);
                    jInstance.setIcon(mkImg("instance"));
                    jInstance.setToolTipText("Instantiate free variable");
                    jInstance.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            jInstanceActionPerformed(evt);
                        }
                    });
                }
                {
                    jUnicode = new JToggleButton();
                    jUnicode.setIcon(mkImg("unicode"));
                    jUnicode.setToolTipText("Use unicode characters");
                    jUnicode.setSelected(true);
                    southPanel.add(jUnicode);
                    jUnicode.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            unicodeChanged(evt);
                        }
                    });

                }
                {
                    jUndo = new JButton();
                    southPanel.add(jUndo);
                    jUndo.setIcon(mkImg("undo"));
                    jUndo.setToolTipText("Undo the last instantiation or rule application");
                    jUndo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            tableauComponent.undo();
                        }
                    });
                }
                {
                    jExport = new JButton();
                    southPanel.add(jExport);
                    jExport.setIcon(mkImg("export"));
                    jExport.setToolTipText("Export to PNG");
                    jExport.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            jExportActionPerformed(evt);
                        }
                    });
                }
                {
                    jLatex = new JButton();
                    southPanel.add(jLatex);
                    jLatex.setIcon(mkImg("tex"));
                    jLatex.setToolTipText("Export to LaTeX");
                    jLatex.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            jLatexActionPerformed(evt);
                        }
                    });
                }
                {
                    jPrint = new JButton();
                    southPanel.add(jPrint);
                    jPrint.setIcon(mkImg("print"));
                    jPrint.setToolTipText("Print");
                    //jPrint.setEnabled(false);
                    jPrint.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            jPrintActionPerformed(evt);
                        }
                    });
                }
                {
                    jAuto = new JButton();
                    southPanel.add(jAuto);
                    jAuto.setIcon(mkImg("go"));
                    jAuto.setEnabled(ALLOW_AUTORUN);
                    jAuto.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            jAutoActionPerformed(evt);
                        }
                    });
                }
//                {
//                    jModelSearch = new JButton();
//                    southPanel.add(jModelSearch);
//                    jModelSearch.setText("MS");
//                    jModelSearch.setEnabled(ALLOW_MODELSEARCH);
//                    jModelSearch.addActionListener(new ActionListener() {
//                        public void actionPerformed(ActionEvent e) {
//                            jModelSearchActionPerformed(e);
//                        }
//                    });
//                }
            }
            {
                jComment = new JLabel();
                result.add(jComment, BorderLayout.SOUTH);
                jComment.setText("Visualisation of the tableau calculus - Mattias Ulbrich 2007-2016 - #"+BUILD);
            }
            {
                jScrollPane = new JScrollPane();
                jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
                result.add(jScrollPane, BorderLayout.CENTER);
                {
                    tableauComponent = new TableauPane(jComment);
                    jScrollPane.setViewportView(tableauComponent);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new JLabel("An exception occurred (s. console): " + e.getMessage());
        }
    }

    /**
	 * Open a formula dialog and let the user enter a new formula.
	 *
	 * @param evt
	 *            the event
	 */
    private void jNewActionPerformed(ActionEvent evt) {
        FormulaDialog fd = new FormulaDialog(new JFrame());
        fd.setInput(lastInput);
        fd.setVisible(true);
        while (fd.isOK()) {
            fd.unsetOK();
            lastInput = fd.getInput();
            Reader r = new StringReader(lastInput);
            FormulaParser parser = new FormulaParser(r);
            try {
                Formula f[] = parser.Formulae();
                tableauComponent.init(f);
            } catch(ParseException ex) {
                fd.go(ex.currentToken.beginLine, ex.currentToken.beginColumn);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                       JOptionPane.ERROR_MESSAGE);
                fd.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                fd.setVisible(true);
            }
        }
    }

    /**
	 * Act if the unicode button has been pressed.
	 *
	 * @param evt
	 *            the evt, not needed
	 */
    private void unicodeChanged(ActionEvent evt) {
        Constants.USE_UNICODE = jUnicode.isSelected();
        tableauComponent.refresh();
    }

    /**
	 * The SAMPLE formula.
	 */
    private static final String SAMPLE = "~(A x. (p(x)->q(x)) -> (A x. p(x) -> A x. q(x)))";

    /**
	 * Restart applet with an instance of the sample formula.
	 *
	 * @param evt
	 *            the evt, not needed
	 */
    private void jSampleActionPerformed(ActionEvent evt) {
        FormulaParser parser = new FormulaParser(new StringReader(SAMPLE));
        try {
            Formula[] f = parser.Formulae();
            tableauComponent.init(f);
        } catch (ParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Open the variabel instantiation dialog
	 *
	 * @param evt
	 *            the evt, not needed
	 */
    private void jInstanceActionPerformed(ActionEvent evt) {
        String assign = JOptionPane.showInputDialog("Enter the instantiation similar to X1 = f(g(X2),c)");
        if(assign == null) {
	        return;
        }

        FormulaParser parser = new FormulaParser(new StringReader(assign));
        try {
            Instantiation inst = parser.Instantiation();
            String var = inst.getInstantiatedVariable();
            tableauComponent.instantiate(var,inst);
            tableauComponent.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * initiate automatic proofing. delegated to the {@link #tableauComponent}.
	 *
	 * @param evt
	 *            the evt
	 * @see TableauPane#automaticProve()
	 */
    private void jAutoActionPerformed(ActionEvent evt) {
        tableauComponent.automaticProve();
    }

    protected void jModelSearchActionPerformed(ActionEvent e) {
//        if(modelSearchThread == null || !modelSearchThread.isAlive()) {
//            int bound = Integer.parseInt(JOptionPane.showInputDialog(
//            "Bound for the size of models to consider?"));
//            modelSearchThread = new ModelSearch(lastInput, bound);
//            modelSearchThread.run();
//        } else {
//            modelSearchThread.interrupt();
//        }
    }

    /**
	 * open the export file chooser and save current view as PNG, GIF or JPG.
	 *
	 * @param evt
	 *            the evt
	 */
    private void jExportActionPerformed(ActionEvent evt) {
        try {
            JFileChooser jfc = new JFileChooser(".");
            jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
            jfc.addChoosableFileFilter(new FileNameExtensionFilter("GIF image file", "GIF"));
            jfc.addChoosableFileFilter(new FileNameExtensionFilter("JPEG image file", "JPG"));
            jfc.addChoosableFileFilter(new FileNameExtensionFilter("PNG image file", "PNG"));
            if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File outFile = jfc.getSelectedFile();
                Dimension d = tableauComponent.getOptimalSize();
                if(d == null) {
	                throw new IllegalStateException("The tableau is empty - cannot export");
                }
                BufferedImage im = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
                Graphics g = im.getGraphics();
                g.setColor(Color.white);
                g.fillRect(0,0, d.width, d.height);
                tableauComponent.paint(im.getGraphics());
                if (jfc.getFileFilter() instanceof FileNameExtensionFilter) {
					FileNameExtensionFilter extFilter = (FileNameExtensionFilter) jfc.getFileFilter();
					ImageIO.write(im, extFilter.getExtensions()[0], outFile);
                } else {
                	// should not happen actually
                	throw new IllegalStateException("You need to select a file format");
                }
            }
        } catch (Exception e) {
        	e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    e.toString(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Export a latex .tex file.
	 *
	 * @param evt
	 *            the evt, ignored
	 */
    private void jLatexActionPerformed(ActionEvent evt) {
    	try {
            JFileChooser jfc = new JFileChooser(".");
            if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File outFile = jfc.getSelectedFile();
                FileWriter fw = new FileWriter(outFile);
                catResource(fw, "latex/header.latex");
                StringBuilder sb = new StringBuilder();
                tableauComponent.toLatex(sb, ABSOLUTE_TEX_EXPORT);
                fw.write(sb.toString());
                catResource(fw, "latex/footer.latex");
                fw.close();
            }
    	} catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    e.toString(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * open a resource and paste its text to a writer.
     */
    private void catResource(Writer w, String resourceName) throws IOException {
		URL url = getClass().getResource(resourceName);
		if(url == null) {
	        throw new FileNotFoundException("Resource not found: " + resourceName);
        }
		char buf[] = new char[1024];
		Reader r = new InputStreamReader(url.openStream());
		int read = r.read(buf, 0, 1024);
		while(read != -1) {
			w.write(buf, 0, read);
			read = r.read(buf, 0, 1024);
		}
		r.close();
	}

	/**
	 * print to printer
	 *
	 * @param evt
	 *            the evt
	 */
	private void jPrintActionPerformed(ActionEvent evt) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(tableauComponent);
        if (printJob.printDialog())
        {
           try
           {
             printJob.print();
           }
           catch (Exception ex)
           {
             ex.printStackTrace();
           }
        }
    }

    /*
     * look up an img. return an error icon on other cases.
     */
    public Icon mkImg(String name) {
        try {
            return new ImageIcon(getClass().getResource("img/" + name + ".gif"));
        } catch(Exception ex) {
            System.err.println("Missing Icon: " +name);
            return new Icon() {
                public int getIconHeight() {
                    return 24;
                }

                public int getIconWidth() {
                    return 24;
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                	g.setColor(Color.black);
                    g.drawString("?", x, y+20);
                }

            };
        }
    }

    public void setComment(String string) {
        jComment.setText(string);
    }
}
