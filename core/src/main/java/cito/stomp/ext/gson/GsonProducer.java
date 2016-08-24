package cito.stomp.ext.gson;

import javax.enterprise.inject.Produces;

import com.google.gson.Gson;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Aug 2016]
 */
public class GsonProducer {
	@Produces
	public static Gson gson() {
		return new Gson();
	}
}
