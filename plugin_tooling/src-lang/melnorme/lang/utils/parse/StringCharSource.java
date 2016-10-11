/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.utils.parse;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.io.IOException;
import java.io.Reader;

public class StringCharSource extends OffsetBasedCharacterReader<RuntimeException> implements ICharacterReader {
	
	protected final String source;
	
	public StringCharSource(String source) {
		this.source = assertNotNull(source);
	}
	
	public String getSource() {
		return source;
	}
	
	/**
	 * @return character from current read position, plus given offset (offset can be negative). 
	 * EOF if resulting position is out of bounds.
	 */
	@Override
	public int lookahead(int offset) {
		int index = readPosition + offset;
		
		if(index < 0 || index >= source.length()) {
			return EOS;
		}
		return source.charAt(index);
	}
	
	@Override
	public String lookaheadString(int offset, int length) throws RuntimeException {
		return sourceSubString(offset, offset + length);
	}
	
	@Override
	public int bufferedCharCount() {
		if(readPosition >= source.length()) {
			return 0;
		}
		return source.length() - readPosition;
	}
	
	@Override
	protected void doUnread() {
	}
	
	/**
	 * Copy characters from the current position onwards to a buffer.
	 * 
	 * @param buf Buffer to copy data to
	 * @param off Offset in the buffer of the first position to which data should be copied.
	 * @param len Number of characters to copy.
	 * @return the actual number of characters that has been copied.
	 */
	public int copyToBuffer(char [] buf, int off, int len) {
		int numberOfCharactersToRead = bufferedCharCount();
		if (numberOfCharactersToRead > len) {
			numberOfCharactersToRead = len;
		}
		source.getChars(readPosition, readPosition + numberOfCharactersToRead, buf, off);
		readPosition += numberOfCharactersToRead;
		return numberOfCharactersToRead;
	}
	
	/* -----------------  ----------------- */
	
	protected String sourceSubString(int startPos, int endPos) {
		return source.substring(readPosition + startPos, readPosition + endPos);
	}
	
	public static class StringCharSourceReader extends Reader implements ICharacterReader {
		protected StringCharSource child;
		
		public StringCharSourceReader(StringCharSource child) {
			this.child = child;
		}
		
		@Override
		public void close() throws IOException {
			// No need to close anything
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			int result = this.child.copyToBuffer(cbuf, off, len);
			if (result == 0) {
				return -1; // Indicate that the end of the stream has been reached.
			} else {
				return result;
			}
		}

		@Override
		public int lookahead(int offset) throws RuntimeException {
			return this.child.lookahead(offset);
		}

		@Override
		public int bufferedCharCount() {
			return this.child.bufferedCharCount();
		}

		@Override
		public String lookaheadString(int offset, int length) throws RuntimeException {
			return this.child.lookaheadString(offset, length);
		}

		@Override
		public char consume() throws RuntimeException {
			return this.child.consume();
		}

		@Override
		public void unread() throws RuntimeException {
			this.child.unread();
		}
	}
	
	public StringCharSourceReader toReader() {
		return new StringCharSourceReader(this);
	}
	
}