package civvi.jaxrs;

import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
@Path("/")
public class IndexResource {
	@Inject
	private ServletContext ctx;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public InputStream get() {
		return this.ctx.getResourceAsStream("/index.html");
	}
}
