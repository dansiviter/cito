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
 * Simple wrapper to use a {@link ByteBuffer} as an {@link InputStream}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Nov 2016]
 */
public class ByteBufferInputStream extends InputStream {
	private final ByteBuffer buf;

	public ByteBufferInputStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public int read() throws IOException {
		return this.buf.hasRemaining() ? this.buf.get() : -1;
	}
}