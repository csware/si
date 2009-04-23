package de.tuclausthal.abgabesystem.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.auth.loginimpl.Form;
import de.tuclausthal.abgabesystem.auth.verifyimpl.FakeVerify;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class Auth {
	public void login() throws IOException {
		SessionAdapter sa = new SessionAdapter(MainBetterNameHereRequired.getServletRequest());
		if (sa.getUser() != null) {
			MainBetterNameHereRequired.getServletRequest().setAttribute("user", sa.getUser());
			return;
		}
		LoginIf login = new Form();
		LoginData logindata = login.get_login_info();
		if (logindata == null) {
			login.fail_nodata();
			// here is no comeback ;)
		} else {
			VerifyIf verify = new FakeVerify();
			User user = verify.check_credentials(logindata);
			if (user == null) {
				login.fail_nodata("Username or password wrong.");
				// here is no comeback ;)
			} else {
				sa.setUser(user);
				MainBetterNameHereRequired.getServletRequest().setAttribute("user", sa.getUser());
				if (login.redirectAfterLogin() == true) {
					// TODO: is this safe?
					HttpServletResponse response= MainBetterNameHereRequired.getServletResponse();
					response.sendRedirect(response.encodeRedirectURL(MainBetterNameHereRequired.getServletRequest().getRequestURL().toString()+"?"+MainBetterNameHereRequired.getServletRequest().getQueryString()));
				}
				return;
			}
		}
		MainBetterNameHereRequired.getServletRequest().removeAttribute("user");
	}
}
