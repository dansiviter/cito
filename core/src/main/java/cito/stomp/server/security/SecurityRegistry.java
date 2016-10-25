package cito.stomp.server.security;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import cito.stomp.Frame;
import cito.stomp.server.SecurityContext;
import cito.stomp.server.security.Builder.Limitation;

/**
 * Registry should always allow {@code null} destinations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
@ApplicationScoped
public class SecurityRegistry {
	private final Set<Limitation> limitations = new LinkedHashSet<>();

	@Inject @Any
	private Instance<SecurityConfigurer> configurers;

	/**
	 * 
	 * @param limitation
	 */
	public synchronized void register(Limitation limitation) {
		this.limitations.add(limitation);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public synchronized List<Limitation> getMatching(Frame frame) {
		return this.limitations.stream().filter(e -> e.matches(frame)).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param frame
	 * @param ctx
	 * @return
	 */
	public boolean isPermitted(Frame frame, SecurityContext ctx) {
		for (Limitation limitation : getMatching(frame)) {
			if (!limitation.isPermitted(ctx)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public Builder builder() {
		return builder(this);
	}

	/**
	 * 
	 */
	@PostConstruct
	public void init() {
		this.configurers.forEach(c -> { 
			c.configure(this);
			this.configurers.destroy(c);
		});
	}



	// --- Static Methods ---

	/**
	 * 
	 * @return
	 */
	public static Builder builder(SecurityRegistry registry) {
		return new Builder(registry);
	}
}
