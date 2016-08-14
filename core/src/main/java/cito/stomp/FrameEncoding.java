package cito.stomp;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class FrameEncoding implements Encoder.TextStream<Frame>, Decoder.TextStream<Frame> {
	@Override
	public void init(EndpointConfig config) { }

	@Override
	public void destroy() { }

	@Override
	public void encode(Frame object, Writer writer) throws EncodeException, IOException {
		object.to(writer);
	}

	@Override
	public Frame decode(Reader reader) throws DecodeException, IOException {
		return Frame.from(reader);
	}
}
