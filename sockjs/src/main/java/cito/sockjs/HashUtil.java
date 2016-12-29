package cito.sockjs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public enum HashUtil { ;
	/**
	 * 
	 * @param in
	 * @return
	 */
	public static String md5(String in) {
		return hash(in, "MD5");
	}

	/**
	 * 
	 * @param in
	 * @param algorithm
	 * @return
	 */
	public static String hash(String in, String algorithm) {
		try {
			final MessageDigest md = MessageDigest.getInstance(algorithm);
			final byte[] array = md.digest(in.getBytes());
			final StringBuilder sb = new StringBuilder();
			for (byte b : array) {
				sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
