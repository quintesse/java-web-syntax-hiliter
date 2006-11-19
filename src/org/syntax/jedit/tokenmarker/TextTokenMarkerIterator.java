package org.syntax.jedit.tokenmarker;

import java.util.Iterator;

import javax.swing.text.Segment;

public class TextTokenMarkerIterator implements Iterator<Token> {
	private TokenMarker marker;
	private String[] lines;
	private int lineIndex;
	private Token nextToken;
	
	public TextTokenMarkerIterator(String _text, TokenMarker _marker) {
		marker = _marker;
		lines = _text.split("\n");
		marker.insertLines(0, lines.length);
		lineIndex = 0;
		prepareLine();
	}
	
	public boolean hasNext() {
		return ((lineIndex < lines.length) || (nextToken != null));
	}

	public Token next() {
		if (nextToken == null) {
			prepareLine();
		}
		Token token = nextToken;
		nextToken = token.next;
		return token;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private void prepareLine() {
		if (lineIndex < lines.length) {
			char[] chars = lines[lineIndex].toCharArray();
			Segment segment = new Segment(chars, 0, chars.length);
			nextToken = marker.markTokens(segment, lineIndex);
			lineIndex++;
		} else {
			nextToken = null;
		}
	}
}
