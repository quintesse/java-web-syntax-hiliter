/*
 * ShellScriptTokenMarker.java - Shell script token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.*;
import javax.swing.text.Segment;

/**
 * Shell script token marker.
 * 
 * @author Slava Pestov
 * @version $Id: ShellScriptTokenMarker.java,v 1.18 1999/12/13 03:40:30 sp Exp $
 */
public class ShellScriptTokenMarker extends TokenMarker {
	// public members
	public static final byte LVARIABLE = Token.INTERNAL_FIRST;

	@Override
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		char[] array = line.array;
		byte cmdState = 0; // 0 = space before command, 1 = inside
		// command, 2 = after command
		int offset = line.offset;
		int lastOffset = offset;
		int lineLength = line.count + offset;

		if (token == Token.LITERAL1 && lineIndex != 0 && lineInfo[lineIndex - 1].obj != null) {
			String str = (String) lineInfo[lineIndex - 1].obj;
			if (str != null && str.length() == line.count && SyntaxUtilities.regionMatches(false, line, offset, str)) {
				addToken(line, offset, line.count, Token.LITERAL1);
				token = Token.NULL;
			} else {
				addToken(line, offset, line.count, Token.LITERAL1);
				lineInfo[lineIndex].obj = str;
				token = Token.LITERAL1;
			}
			return token;
		}

		boolean backslash = false;
		loop: for (int i = offset; i < lineLength; i++) {
			int i1 = (i + 1);

			char c = array[i];

			if (c == '\\') {
				backslash = !backslash;
				continue;
			}

			switch (token) {
			case Token.NULL:
				switch (c) {
				case ' ':
				case '\t':
				case '(':
				case ')':
					backslash = false;
					if (cmdState == 1/* insideCmd */) {
						addToken(line, lastOffset, i - lastOffset, Token.KEYWORD1);
						lastOffset = i;
						cmdState = 2; /* afterCmd */
					}
					break;
				case '=':
					backslash = false;
					if (cmdState == 1/* insideCmd */) {
						addToken(line, lastOffset, i - lastOffset, token);
						lastOffset = i;
						cmdState = 2; /* afterCmd */
					}
					break;
				case '&':
				case '|':
				case ';':
					if (backslash)
						backslash = false;
					else
						cmdState = 0; /* beforeCmd */
					break;
				case '#':
					if (backslash)
						backslash = false;
					else {
						addToken(line, lastOffset, i - lastOffset, token);
						addToken(line, i, lineLength - i, Token.COMMENT1);
						lastOffset = lineLength;
						break loop;
					}
					break;
				case '$':
					if (backslash)
						backslash = false;
					else {
						addToken(line, lastOffset, i - lastOffset, token);
						cmdState = 2; /* afterCmd */
						lastOffset = i;
						if (lineLength - i >= 2) {
							switch (array[i1]) {
							case '(':
								continue;
							case '{':
								token = LVARIABLE;
								break;
							default:
								token = Token.KEYWORD2;
							break;
							}
						} else
							token = Token.KEYWORD2;
					}
					break;
				case '"':
					if (backslash)
						backslash = false;
					else {
						addToken(line, lastOffset, i - lastOffset, token);
						token = Token.LITERAL1;
						lineInfo[lineIndex].obj = null;
						cmdState = 2; /* afterCmd */
						lastOffset = i;
					}
					break;
				case '\'':
					if (backslash)
						backslash = false;
					else {
						addToken(line, lastOffset, i - lastOffset, token);
						token = Token.LITERAL2;
						cmdState = 2; /* afterCmd */
						lastOffset = i;
					}
					break;
				case '<':
					if (backslash)
						backslash = false;
					else {
						if (lineLength - i > 1 && array[i1] == '<') {
							addToken(line, lastOffset, i - lastOffset, token);
							token = Token.LITERAL1;
							lastOffset = i;
							lineInfo[lineIndex].obj = new String(array, i + 2, lineLength - (i + 2));
						}
					}
					break;
				default:
					backslash = false;
				if (Character.isLetter(c)) {
					if (cmdState == 0 /* beforeCmd */) {
						addToken(line, lastOffset, i - lastOffset, token);
						lastOffset = i;
						cmdState++; /* insideCmd */
					}
				}
				break;
				}
				break;
			case Token.KEYWORD2:
				backslash = false;
				if (!Character.isLetterOrDigit(c) && c != '_') {
					if (i != offset && array[i - 1] == '$') {
						addToken(line, lastOffset, i1 - lastOffset, token);
						lastOffset = i1;
						token = Token.NULL;
						continue;
					}
					addToken(line, lastOffset, i - lastOffset, token);
					lastOffset = i;
					token = Token.NULL;
				}
				break;
			case Token.LITERAL1:
				if (backslash)
					backslash = false;
				else if (c == '"') {
					addToken(line, lastOffset, i1 - lastOffset, token);
					cmdState = 2; /* afterCmd */
					lastOffset = i1;
					token = Token.NULL;
				} else
					backslash = false;
				break;
			case Token.LITERAL2:
				if (backslash)
					backslash = false;
				else if (c == '\'') {
					addToken(line, lastOffset, i1 - lastOffset, Token.LITERAL1);
					cmdState = 2; /* afterCmd */
					lastOffset = i1;
					token = Token.NULL;
				} else
					backslash = false;
				break;
			case LVARIABLE:
				backslash = false;
				if (c == '}') {
					addToken(line, lastOffset, i1 - lastOffset, Token.KEYWORD2);
					lastOffset = i1;
					token = Token.NULL;
				}
				break;
			default:
				throw new InternalError("Invalid state: " + token);
			}
		}

		switch (token) {
		case Token.NULL:
			if (cmdState == 1)
				addToken(line, lastOffset, lineLength - lastOffset, Token.KEYWORD1);
			else
				addToken(line, lastOffset, lineLength - lastOffset, token);
			break;
		case Token.LITERAL2:
			addToken(line, lastOffset, lineLength - lastOffset, Token.LITERAL1);
			break;
		case Token.KEYWORD2:
			addToken(line, lastOffset, lineLength - lastOffset, token);
			token = Token.NULL;
			break;
		case LVARIABLE:
			addToken(line, lastOffset, lineLength - lastOffset, Token.INVALID);
			token = Token.NULL;
			break;
		default:
			addToken(line, lastOffset, lineLength - lastOffset, token);
		break;
		}
		return token;
	}
}
