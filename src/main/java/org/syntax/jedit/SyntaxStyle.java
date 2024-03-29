/*
 * SyntaxStyle.java - A simple text style class
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit;

import java.awt.Color;

/**
 * A simple text style class. It can specify the color, italic flag, and bold
 * flag of a run of text.
 * 
 * @author Slava Pestov
 * @version $Id: SyntaxStyle.java,v 1.6 1999/12/13 03:40:30 sp Exp $
 */
public class SyntaxStyle {
    /**
     * Creates a new SyntaxStyle.
     * 
     * @param color The text color
     * @param italic True if the text should be italics
     * @param bold True if the text should be bold
     */
    public SyntaxStyle(Color color, boolean italic, boolean bold) {
	this.color = color;
	this.italic = italic;
	this.bold = bold;
    }

    /**
     * Returns the color specified in this style.
     */
    public Color getColor() {
	return color;
    }

    /**
     * Returns true if no font styles are enabled.
     */
    public boolean isPlain() {
	return !(bold || italic);
    }

    /**
     * Returns true if italics is enabled for this style.
     */
    public boolean isItalic() {
	return italic;
    }

    /**
     * Returns true if boldface is enabled for this style.
     */
    public boolean isBold() {
	return bold;
    }

    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
	return getClass().getName() + "[color=" + color + (italic ? ",italic" : "") + (bold ? ",bold" : "") + "]";
    }

    // private members
    private Color color;

    private boolean italic;

    private boolean bold;
}
