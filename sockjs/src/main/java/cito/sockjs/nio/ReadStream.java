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

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import cito.sockjs.HttpAsyncContext;

/**
 * 
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Feb 2017]
 */
public class ReadStream implements ReadListener {
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);

	private final ReadableByteChannel src;
	private final WritableByteChannel dest;
	private final HttpAsyncContext async;
	private final ServletInputStream in;
	private final Complete complete;

	public ReadStream(HttpAsyncContext async, WritableByteChannel dest, Complete complete) throws IOException {
		this.async = async;
		this.dest = dest;
		this.in = async.getRequest().getInputStream();
		this.src = newChannel(this.in);
		this.complete = complete;
	}

	@Override
	public void onDataAvailable() throws IOException {
		this.buffer.clear();
		while (this.in.isReady() && !this.in.isFinished() && this.src.read(this.buffer) != -1) {
			this.buffer.flip();
			dest.write(this.buffer);
			this.buffer.compact();
		}
	}

	@Override
	public void onAllDataRead() throws IOException {
		this.complete.onComplete(null);
	}

	@Override
	public void onError(final Throwable t) { 
		try {
			this.complete.onComplete(t);
		} catch (IOException e) {
			this.async.getRequest().getServletContext().log("Unable to complete!", e);
		}
	}
}
