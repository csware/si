package de.tuclausthal.abgabesystem.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.auth.loginimpl.Form;
import de.tuclausthal.abgabesystem.auth.verifyimpl.FakeVerify;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class AuthFilter implements Filter {
	@Override
	public void destroy() {
	// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		SessionAdapter sa = new SessionAdapter((HttpServletRequest) request);
		if (sa.getUser() != null) {
			request.setAttribute("user", sa.getUser());
		} else {
			LoginIf login = new Form();
			// optimize, make context sensitive
			LoginData logindata = login.get_login_info((HttpServletRequest) request);
			if (logindata == null) {
				login.fail_nodata((HttpServletRequest) request, (HttpServletResponse) response);
				return;
			} else {
				VerifyIf verify = new FakeVerify();
				User user = verify.check_credentials(logindata);
				if (user == null) {
					login.fail_nodata("Username or password wrong.", (HttpServletRequest) request, (HttpServletResponse) response);
					return;
				} else {
					sa.setUser(user);
					request.setAttribute("user", sa.getUser());
					if (login.redirectAfterLogin() == true) {
						// TODO: is this safe?
						((HttpServletResponse) response).sendRedirect(((HttpServletResponse) response).encodeRedirectURL(MainBetterNameHereRequired.getServletRequest().getRequestURL().toString() + "?" + MainBetterNameHereRequired.getServletRequest().getQueryString()));
					}
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	// TODO Auto-generated method stub
	}
}
