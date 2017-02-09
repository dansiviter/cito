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
package cito.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Nov 2016]
 */
public class ByteBufferInputStream extends InputStream {

	private int bbisInitPos;
	private int bbisLimit;
	private ByteBuffer bbisBuffer;

	public ByteBufferInputStream(ByteBuffer buffer) {
		this(buffer, buffer.limit() - buffer.position());
	}

	public ByteBufferInputStream(ByteBuffer buffer, int limit) {
		bbisBuffer = buffer;
		bbisLimit = limit;
		bbisInitPos = bbisBuffer.position();
	}

	@Override
	public int read() throws IOException {
		if (bbisBuffer.position() - bbisInitPos > bbisLimit)
			return -1;
		return bbisBuffer.get();
	}
}