package cito.servlet;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Jul 2016]
 */
public abstract class AsyncHandler implements AsyncListener {
	@Override
	public void onComplete(AsyncEvent event) throws IOException { }

	@Override
	public void onTimeout(AsyncEvent event) throws IOException { }

	@Override
	public void onError(AsyncEvent event) throws IOException { }

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException { }
}
