/*
 * BatchFileTokenMarker.java - Batch file token marker
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
 * Batch file token marker.
 * 
 * @author Slava Pestov
 * @version $Id: BatchFileTokenMarker.java,v 1.20 1999/12/13 03:40:29 sp Exp $
 */
public class BatchFileTokenMarker extends TokenMarker {
	@Override
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int lineLength = line.count + offset;

		if (SyntaxUtilities.regionMatches(true, line, offset, "rem")) {
			addToken(line, lastOffset, line.count, Token.COMMENT1);
			return Token.NULL;
		}

		loop: for (int i = offset; i < lineLength; i++) {
			int i1 = (i + 1);

			switch (token) {
			case Token.NULL:
				switch (array[i]) {
				case '%':
					addToken(line, lastOffset, i - lastOffset, token);
					lastOffset = i;
					if (lineLength - i <= 3 || array[i + 2] == ' ') {
						addToken(line, i, 2, Token.KEYWORD2);
						i += 2;
						lastOffset = i;
					} else
						token = Token.KEYWORD2;
					break;
				case '"':
					addToken(line, lastOffset, i - lastOffset, token);
					token = Token.LITERAL1;
					lastOffset = i;
					break;
				case ':':
					if (i == offset) {
						addToken(line, lastOffset, line.count, Token.LABEL);
						lastOffset = lineLength;
						break loop;
					}
					break;
				case ' ':
					if (lastOffset == offset) {
						addToken(line, lastOffset, i - lastOffset, Token.KEYWORD1);
						lastOffset = i;
					}
					break;
				}
				break;
			case Token.KEYWORD2:
				if (array[i] == '%') {
					addToken(line, lastOffset, i1 - lastOffset, token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			case Token.LITERAL1:
				if (array[i] == '"') {
					addToken(line, lastOffset, i1 - lastOffset, token);
					token = Token.NULL;
					lastOffset = i1;
				}
				break;
			default:
				throw new InternalError("Invalid state: " + token);
			}
		}

		if (lastOffset != lineLength) {
			if (token != Token.NULL)
				token = Token.INVALID;
			else if (lastOffset == offset)
				token = Token.KEYWORD1;
			addToken(line, lastOffset, lineLength - lastOffset, token);
		}
		return Token.NULL;
	}

	@Override
	public boolean supportsMultilineTokens() {
		return false;
	}
}
