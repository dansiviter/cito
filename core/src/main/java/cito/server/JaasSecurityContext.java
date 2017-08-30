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
package cito.server;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.deltaspike.core.api.config.ConfigProperty;

/**
 * @author Daniel Siviter
 * @since v1.0 [3 Jun 2017]
 */
@Dependent
public class JaasSecurityContext implements SecurityContext {
	@Inject
	@ConfigProperty(name = "jaas.contextName", defaultValue = "AppRealm")
	private String contextName;
	private LoginContext ctx;
	private String login;
	private char[] passcode;

	@PostConstruct
	public void init() {
		try {
			this.ctx = new LoginContext(this.contextName, new CallbackHandler());
		}catch (LoginException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 
	 * @param login
	 * @param passcode
	 * @throws LoginException
	 */
	public void login(String login, char[] passcode) throws LoginException {
		this.login = login;
		this.passcode = passcode;
		this.ctx.login();
	}

	/**
	 * @throws LoginException 
	 */
	public void logout() throws LoginException {
		this.ctx.logout();
	}

	public Subject getSubject() {
		return this.ctx.getSubject();
	}

	@Override
	public Principal getUserPrincipal() {
		return getPrincipalFromSubject(getSubject());
	}

	@Override
	public boolean isUserInRole(String role) {
		final Group roles = getRolesFromSubject(getSubject());
		if (roles == null) {
			return false;
		}
		for (Principal principal : Collections.list(roles.members())) {
			if (role.equals(principal.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a JAAS Subject, will look for {@code Group} principals with name "Roles".
	 * 
	 * @param subject
	 * @return
	 */
	public static Group getRolesFromSubject(Subject subject) {
		Set<Group> groupPrincipals = subject.getPrincipals(Group.class);
		if (groupPrincipals!= null) {
			for (Group groupPrincipal: groupPrincipals) {
				if ("Roles".equals(groupPrincipal.getName())) {
					return groupPrincipal;
				}
			}
		}
		return null;
	}

	/**
	 * Get the first non-group principal
	 * 
	 * @param subject
	 * @return
	 */
	public static Principal getPrincipalFromSubject(Subject subject) {
		Set<Principal> principals = subject.getPrincipals();
		if (principals != null) {
			for (Principal p : principals) {
				if (p instanceof Group != false) {
					return p;
				}
			}
		}
		return null;
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [3 May 2017]
	 */
	private class CallbackHandler implements javax.security.auth.callback.CallbackHandler {
		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (Callback callback : callbacks) {
				if (callback instanceof NameCallback) {
					((NameCallback) callback).setName(JaasSecurityContext.this.login);
				} else if (callback instanceof PasswordCallback) {
					((PasswordCallback) callback).setPassword(JaasSecurityContext.this.passcode);
				} else {
					throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
				}
			}
		}
	}
}
