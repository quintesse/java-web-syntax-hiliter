/*
 * PropsTokenMarker.java - Java props/DOS INI token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

/**
 * Java properties/DOS INI token marker.
 * 
 * @author Slava Pestov
 * @version $Id: PropsTokenMarker.java,v 1.9 1999/12/13 03:40:30 sp Exp $
 */
public class PropsTokenMarker extends TokenMarker {
	public static final byte VALUE = Token.INTERNAL_FIRST;

	@Override
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int lineLength = line.count + offset;
		loop: for (int i = offset; i < lineLength; i++) {
			int i1 = (i + 1);

			switch (token) {
			case Token.NULL:
				switch (array[i]) {
				case '#':
				case ';':
					if (i == offset) {
						addToken(line, offset, line.count, Token.COMMENT1);
						lastOffset = lineLength;
						break loop;
					}
					break;
				case '[':
					if (i == offset) {
						addToken(line, lastOffset, i - lastOffset, token);
						token = Token.KEYWORD2;
						lastOffset = i;
					}
					break;
				case '=':
					addToken(line, lastOffset, i - lastOffset, Token.KEYWORD1);
					token = VALUE;
					lastOffset = i;
					break;
				}
				break;
			case Token.KEYWORD2:
				if (array[i] == ']') {
					addToken(line, lastOffset, i1 - lastOffset, token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			case VALUE:
				break;
			default:
				throw new InternalError("Invalid state: " + token);
			}
		}
		if (lastOffset != lineLength)
			addToken(line, lastOffset, lineLength - lastOffset, Token.NULL);
		return Token.NULL;
	}

	@Override
	public boolean supportsMultilineTokens() {
		return false;
	}
}
