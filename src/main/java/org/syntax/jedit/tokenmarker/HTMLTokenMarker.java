/*
 * HTMLTokenMarker.java - HTML token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.syntax.jedit.*;
import javax.swing.text.Segment;

/**
 * HTML token marker.
 * 
 * @author Slava Pestov
 * @version $Id: HTMLTokenMarker.java,v 1.34 1999/12/13 03:40:29 sp Exp $
 */
public class HTMLTokenMarker extends TokenMarker {
	// These kind of patterns are just too scary for words!
	// But basically it finds HTML/XML attributes within the string.
	// Allowing single words (eg SELECTED), unquoted values (eg COLS=5)
	// and quoted values (eg WIDTH="10px"). Spaces between elements
	// are allowed.
	private final Pattern attributePattern = Pattern.compile("(\\s*)([^=\\s]*)(?:(\\s*?=\\s*?)(?:([^\\s\"]+)|(\".*?\")))?");
	
	public static final byte JAVASCRIPT = Token.INTERNAL_FIRST;

	public HTMLTokenMarker() {
		this(true);
	}

	public HTMLTokenMarker(boolean js) {
		this.js = js;
		keywords = JavaScriptTokenMarker.getKeywords();
	}

	@Override
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		char[] array = line.array;
		int offset = line.offset;
		lastOffset = offset;
		lastKeyword = offset;
		int lineLength = line.count + offset;
		int spacePos = -1;
		boolean tagFound = false;
		boolean spaceInTag = false;
		boolean moreInTag = false;
		boolean backslash = false;

		loop: for (int i = offset; i < lineLength; i++) {
			int i1 = (i + 1);

			char c = array[i];
			if (c == '\\') {
				backslash = !backslash;
				continue;
			}

			switch (token) {
			case Token.NULL: // HTML text
				backslash = false;
				switch (c) {
				case '<':
					addToken(line, lastOffset, i - lastOffset, token);
					lastOffset = lastKeyword = i;
					if (SyntaxUtilities.regionMatches(false, line, i1, "!--")) {
						i += 3;
						token = Token.COMMENT1;
					} else if (js && SyntaxUtilities.regionMatches(true, line, i1, "script>")) {
						addToken(line, lastOffset, 8, Token.KEYWORD1);
						lastOffset = lastKeyword = (i += 8);
						token = JAVASCRIPT;
					} else {
						token = Token.KEYWORD1;
					}
					tagFound = spaceInTag = moreInTag = false;
					break;
				case '&':
					addToken(line, lastOffset, i - lastOffset, token);
					lastOffset = lastKeyword = i;
					token = Token.KEYWORD2;
					break;
				}
				break;
			case Token.KEYWORD1: // Inside a tag
				backslash = false;
				if (!tagFound && Character.isLetter(c)) {
					tagFound = true;
				} else if (tagFound && !spaceInTag && (c == ' ')) {
					spaceInTag = true;
					spacePos = i;
				} else if (spaceInTag && Character.isLetter(c)) {
					moreInTag = true;
				} else if (c == '>') {
					if (moreInTag) {
						addToken(line, lastOffset, 1, Token.NULL);
						addToken(line, lastOffset + 1, spacePos - lastOffset - 1, token);
						
						String ln = new String(line.array, spacePos, i1 - spacePos - 1);
						Matcher m = attributePattern.matcher(ln);
						while (m.find()) {
							addToken(line, m.start(1) + spacePos, m.end(1) - m.start(1), Token.NULL);
							addToken(line, m.start(2) + spacePos, m.end(2) - m.start(2), Token.KEYWORD3);
							addToken(line, m.start(3) + spacePos, m.end(3) - m.start(3), Token.NULL);
							addToken(line, m.start(4) + spacePos, m.end(4) - m.start(4), Token.LITERAL1);
							addToken(line, m.start(5) + spacePos, m.end(5) - m.start(5), Token.LITERAL1);
						}
						
						addToken(line, i1 - 1, 1, Token.NULL);
					} else {
						addToken(line, lastOffset, 1, Token.NULL);
						addToken(line, lastOffset + 1, i1 - lastOffset - 2, token);
						addToken(line, i1 - 1, 1, Token.NULL);
					}
					lastOffset = lastKeyword = i1;
					token = Token.NULL;
				}
				break;
			case Token.KEYWORD2: // Inside an entity
				backslash = false;
				if (c == ';') {
					addToken(line, lastOffset, i1 - lastOffset, token);
					lastOffset = lastKeyword = i1;
					token = Token.NULL;
					break;
				}
				break;
			case Token.COMMENT1: // Inside a comment
				backslash = false;
				if (SyntaxUtilities.regionMatches(false, line, i, "-->")) {
					addToken(line, lastOffset, (i + 3) - lastOffset, token);
					lastOffset = lastKeyword = i + 3;
					token = Token.NULL;
				}
				break;
			case JAVASCRIPT: // Inside a JavaScript
				switch (c) {
				case '<':
					backslash = false;
					doKeyword(line, i, c);
					if (SyntaxUtilities.regionMatches(true, line, i1, "/script>")) {
						addToken(line, lastOffset, i - lastOffset, Token.NULL);
						addToken(line, i, 9, Token.KEYWORD1);
						lastOffset = lastKeyword = (i += 9);
						token = Token.NULL;
					}
					break;
				case '"':
					if (backslash)
						backslash = false;
					else {
						doKeyword(line, i, c);
						addToken(line, lastOffset, i - lastOffset, Token.NULL);
						lastOffset = lastKeyword = i;
						token = Token.LITERAL1;
					}
					break;
				case '\'':
					if (backslash)
						backslash = false;
					else {
						doKeyword(line, i, c);
						addToken(line, lastOffset, i - lastOffset, Token.NULL);
						lastOffset = lastKeyword = i;
						token = Token.LITERAL2;
					}
					break;
				case '/':
					backslash = false;
					doKeyword(line, i, c);
					if (lineLength - i > 1) {
						addToken(line, lastOffset, i - lastOffset, Token.NULL);
						lastOffset = lastKeyword = i;
						if (array[i1] == '/') {
							addToken(line, lastOffset, lineLength - i, Token.COMMENT2);
							lastOffset = lastKeyword = lineLength;
							break loop;
						} else if (array[i1] == '*') {
							token = Token.COMMENT2;
						}
					}
					break;
				default:
					backslash = false;
				if (!Character.isLetterOrDigit(c) && c != '_')
					doKeyword(line, i, c);
				break;
				}
				break;
			case Token.LITERAL1: // JavaScript "..."
				if (backslash)
					backslash = false;
				else if (c == '"') {
					addToken(line, lastOffset, i1 - lastOffset, Token.LITERAL1);
					lastOffset = lastKeyword = i1;
					token = JAVASCRIPT;
				}
				break;
			case Token.LITERAL2: // JavaScript '...'
				if (backslash)
					backslash = false;
				else if (c == '\'') {
					addToken(line, lastOffset, i1 - lastOffset, Token.LITERAL1);
					lastOffset = lastKeyword = i1;
					token = JAVASCRIPT;
				}
				break;
			case Token.COMMENT2: // Inside a JavaScript comment
				backslash = false;
				if (c == '*' && lineLength - i > 1 && array[i1] == '/') {
					addToken(line, lastOffset, (i += 2) - lastOffset, Token.COMMENT2);
					lastOffset = lastKeyword = i;
					token = JAVASCRIPT;
				}
				break;
			default:
				throw new InternalError("Invalid state: " + token);
			}
		}

		switch (token) {
		case Token.LITERAL1:
		case Token.LITERAL2:
			addToken(line, lastOffset, lineLength - lastOffset, Token.INVALID);
			token = JAVASCRIPT;
			break;
		case Token.KEYWORD2:
			addToken(line, lastOffset, lineLength - lastOffset, Token.INVALID);
			token = Token.NULL;
			break;
		case JAVASCRIPT:
			doKeyword(line, lineLength, '\0');
			addToken(line, lastOffset, lineLength - lastOffset, Token.NULL);
			break;
		default:
			addToken(line, lastOffset, lineLength - lastOffset, token);
		break;
		}

		return token;
	}

	// private members
	private KeywordMap keywords;

	private boolean js;

	private int lastOffset;

	private int lastKeyword;

	private boolean doKeyword(Segment line, int i, char c) {
		int i1 = i + 1;

		int len = i - lastKeyword;
		byte id = keywords.lookup(line, lastKeyword, len);
		if (id != Token.NULL) {
			if (lastKeyword != lastOffset)
				addToken(line, lastOffset, lastKeyword - lastOffset, Token.NULL);
			addToken(line, lastKeyword, len, id);
			lastOffset = i;
		}
		lastKeyword = i1;
		return false;
	}
}
