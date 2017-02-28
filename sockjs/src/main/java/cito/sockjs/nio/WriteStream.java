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
package cito.sockjs.nio;

import static java.nio.channels.Channels.newChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import cito.sockjs.HttpAsyncContext;

/**
 * @author Daniel Siviter
 * @since v1.0 [18 Feb 2017]
 */
public class WriteStream implements WriteListener {
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	private final ReadableByteChannel src;
	private final WritableByteChannel dest;
	private final HttpAsyncContext async;
	private final ServletOutputStream out;
	private final Complete complete;

	/**
	 * 
	 * @param async
	 * @param src
	 * @throws IOException
	 */
	public WriteStream(HttpAsyncContext async, ReadableByteChannel src) throws IOException {
		this(async, src, () -> async.complete());
	}

	/**
	 * 
	 * @param async
	 * @param src
	 * @param complete
	 * @throws IOException
	 */
	public WriteStream(HttpAsyncContext async, ReadableByteChannel src, Complete complete) throws IOException {
		this.async = async;
		this.src = src;
		this.out = async.getResponse().getOutputStream();
		this.dest = newChannel(this.out);
		this.complete = complete;
	}

	@Override
	public void onWritePossible() throws IOException {
		this.buffer.clear();
		while (this.out.isReady()) {
			final int len = this.src.read(this.buffer);
			if (len < 0) {
				this.complete.onComplete();
				return;
			}
			this.buffer.flip();
			this.dest.write(this.buffer);
			this.buffer.compact();
			this.out.flush();
		}
	}

	@Override
	public void onError(Throwable t) {
		this.async.getRequest().getServletContext().log("Unable to write entity!", t);
		try {
			this.complete.onComplete();
		} catch (IOException e) {
			this.async.getRequest().getServletContext().log("Unable to complete!", e);
		}
	}
}
