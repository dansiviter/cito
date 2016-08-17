package cito;

/**
 * An {@link AutoCloseable} that only throws a {@link RuntimeException}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public interface QuietClosable extends AutoCloseable {
	@Override
	public void close();
}
