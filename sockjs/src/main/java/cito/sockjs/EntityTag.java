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
package cito.sockjs;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Daniel Siviter
 * @since v1.0 [18 Feb 2017]
 */
public class EntityTag {
	private final String value;
	private final boolean weak;

	private EntityTag(String value, boolean weak) {
		this.value = value;
		this.weak = weak;
	}

	/**
	 * @return the value
	 */
	public String value() {
		return value;
	}

	/**
	 * @return the weak
	 */
	public boolean weak() {
		return weak;
	}


	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EntityTag other = (EntityTag) obj;
		return Objects.equals(this.value, other.value) && this.weak == other.weak;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.value, this.weak);
	}
	@Override
	public String toString() {
		return new StringBuilder(weak() ? "W/" : "").append('"').append(value()).append('"').toString();
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static EntityTag from(String value) {
		if (value == null) return null;
		boolean weakTag = false;
		if (value.startsWith("W/")) {
			weakTag = true;
			value = value.substring(2);
		}
		if (value.startsWith("\"")) {
			value = value.substring(1);
		}
		if (value.endsWith("\"")) {
			value = value.substring(0, value.length() - 1);
		}
		return new EntityTag(value, weakTag);
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	public static EntityTag ifNoneMatch(HttpServletRequest req) {
		return from(req.getHeader("If-None-Match"));
	}
}
