package civvi;

import javax.inject.Qualifier;

import civvi.DestinationEvent.Type;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@Qualifier
public @interface DestinationChanged {
	/**
	 * 
	 * @return
	 */
	Type type();
}
