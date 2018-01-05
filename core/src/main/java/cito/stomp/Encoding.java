/*

 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.stomp;

import static java.nio.ByteBuffer.allocate;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map.Entry;

/**
 * Performing encoding and decoding of {@link Frame}s.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Nov 2017]
 */
public enum Encoding { ;
	static final byte COLON = ':';
	public static final byte LF = '\n';
	public static final byte CR = '\r';
	public static final byte NULL = '\u0000';

	/**
	 * 
	 * @param frame
	 * @param direct
	 * @param capacity
	 * @return
	 * @throws BufferOverflowException potentially thrown if there is insufficient space.
	 * @see #DEFAULT_BUFFER_CAPACITY
	 * @see ByteBuffer#put(byte)
	 */
	public static ByteBuffer from(Frame frame, boolean direct, int capacity) {
		final ByteBuffer buf = direct ? allocateDirect(capacity) : allocate(capacity);
		write(frame, buf);
		buf.flip();
		return buf;
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public static void write(Frame frame, ByteBuffer buf) {
		if (frame.isHeartBeat()) {
			buf.put(LF);
			return;
		}

		buf.put(UTF_8.encode(frame.getCommand().name())).put(LF);

		for (Entry<Header, List<String>> e : frame.getHeaders().entrySet()) {
			for (String value : e.getValue()) {
				buf.put(UTF_8.encode(e.getKey().value())).put(COLON).put(UTF_8.encode(value)).put(LF);
			}
		}

		buf.put(LF);

		final ByteBuffer body = frame.getBody();
		if (body != null) {
			buf.put(body);
		}

		buf.put(NULL);
	}

	/**
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws AssertionError if the {@link Builder} does not have required values.
	 */
	public static Frame from(ByteBuffer buf) throws IOException, AssertionError {
		skipEoL(buf);

		final CharBuffer command = readLine(buf);
		if (command.length() == 0) {
			return Frame.HEART_BEAT;
		}
		Frame.Builder builder = Frame.builder(Command.valueOf(command.toString()));
		int contentLength = readHeaders(buf, builder);

		if (contentLength > 0) {
			if (contentLength != buf.remaining() - 1) { // ignoring last octet
				throw new IOException("Content-Length doesn't match remaining bytes!");
			}
			buf.limit(buf.limit() - 1);
			final ByteBuffer body = ByteBuffer.allocate(contentLength);
			body.put(buf);
			body.flip();
			builder.body(null, body);
		}

		return builder.build();
	}

	/**
	 * 
	 * @param buf
	 * @param builder
	 * @return
	 * @throws IOException
	 */
	private static int readHeaders(ByteBuffer buf, Frame.Builder builder) throws IOException {
		int contentLength = -1;
		while (true) {
			final CharBuffer cBuf = readLine(buf);
			if (cBuf == null || !cBuf.hasRemaining()) {
				break;
			}
			if (!indexOf(':', cBuf)) {
				throw new IOException("A header must be of the form '<name>:<value>[,<value>]'! [" + cBuf.rewind() + "]");
			} else {
				final CharBuffer valueBuf = cBuf.slice();
				final Header header = Header.valueOf(cBuf.flip().toString());
				final String value = valueBuf.position(valueBuf.position() + 1).toString();
				if (Header.Standard.CONTENT_LENGTH.equals(header)) {
					// this will get set by Frame#body
					contentLength = Integer.valueOf(value);
				} else {
					builder.header(header, value.split(","));
				}
			}
		}
		return contentLength;
	}

	/**
	 * 
	 * @param c
	 * @param cBuf
	 * @return
	 */
	private static boolean indexOf(char c, CharBuffer cBuf) {
		while (cBuf.hasRemaining()) {
			if (cBuf.get() == ':') {
				cBuf.position(cBuf.position() - 1);
				return true;
			}
		}
		return false;

	}

	/**
	 * 
	 * @param buf
	 * @return
	 */
	private static boolean isEol(ByteBuffer buf) {
		if (!buf.hasRemaining()) {
			return false;
		}
		if (buf.get(buf.position()) == LF) {
			return true;
		}
		return buf.get(buf.position()) == CR && buf.hasRemaining() && buf.get(buf.position() + 1) == LF;
	}

	/**
	 * 
	 * @param buf
	 * @return {@code true} if a End of Line was encountered.
	 */
	private static boolean skipEoL(ByteBuffer buf) {
		if (isEol(buf)) {
			if (buf.get() == CR) {
				buf.get();
			}
			buf.compact();
			buf.flip();
			return true;
		}
		return false;
	}

	/**
	 * Reads a line in and prepares the buffer for next line.
	 * 
	 * @param buf
	 * @return
	 */
	private static CharBuffer readLine(ByteBuffer buf) {
		while (buf.hasRemaining() && !isEol(buf)) {
			buf.get();
		}
		final ByteBuffer tmp = buf.duplicate();
		tmp.flip();
		try {
			return UTF_8.decode(tmp);
		} finally {
			skipEoL(buf);
		}
	}
}
