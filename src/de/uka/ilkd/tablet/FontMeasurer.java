package de.uka.ilkd.tablet;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


/**
 * use instances of this little class to measure the size of strings.
 * 
 * @author MU
 */

public class FontMeasurer {
    
    private Graphics2D g2d;
    
    public FontMeasurer(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public Dimension getBounds(String text) {
        FontRenderContext frc = g2d.getFontRenderContext();
        Font font = g2d.getFont();
        Rectangle2D r = font.getStringBounds(text, frc);
        return new Dimension((int)r.getWidth(), (int)r.getHeight());
    }
    
    public float getDescent(String text) {
        FontRenderContext frc = g2d.getFontRenderContext();
        Font font = g2d.getFont();
        return font.getLineMetrics(text, frc).getDescent();
    }
    
}
