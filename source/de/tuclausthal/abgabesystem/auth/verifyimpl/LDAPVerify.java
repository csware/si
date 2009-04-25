package de.tuclausthal.abgabesystem.auth.verifyimpl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import de.tuclausthal.abgabesystem.auth.LoginData;
import de.tuclausthal.abgabesystem.auth.VerifyIf;
import de.tuclausthal.abgabesystem.persistence.dao.impl.UserDAO;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class LDAPVerify implements VerifyIf {
	@Override
	public User check_credentials(LoginData logindata) {
		User user = null;
		String username = logindata.getUsername();
		String password = logindata.getPassword();
		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<String, String>(5);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldaps://beelzebub.rz.tu-clausthal.de/ou=people,dc=tu-clausthal,dc=de");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		if (username.contains(",")) {
			// die here!
			return null;
		}

		env.put(Context.SECURITY_PRINCIPAL, "uid=" + username + ",ou=people,dc=tu-clausthal,dc=de");
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			// Create initial context
			LdapContext ctx = new InitialLdapContext(env, null);

			//System.out.println(ctx.getAttributes("uid=" + username));

			// wenn gefunden, dann lokalen user suchen bzw. erstellen

			//System.out.println(ctx.getAttributes("uid=" + username).get("tucmatrikelNr"));
			UserDAO userdao = new UserDAO();
			user = userdao.getUser((String) ctx.getAttributes("uid=" + username).get("uid").get());
			System.out.println(ctx.getAttributes("uid=" + username).get("tucmatrikelNro"));
			if (user == null) {
				if (ctx.getAttributes("uid=" + username).get("tucmatrikelNr") != null) {
					userdao.createUser((String) ctx.getAttributes("uid=" + username).get("uid").get());
				}
			}

			// Close the context when we're done
			ctx.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return user;
	}
}
