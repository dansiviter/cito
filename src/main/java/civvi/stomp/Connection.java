package civvi.stomp;

import java.io.Closeable;

public interface Connection extends Closeable {
	void open();
}
