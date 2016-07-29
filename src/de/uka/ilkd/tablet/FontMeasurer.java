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
