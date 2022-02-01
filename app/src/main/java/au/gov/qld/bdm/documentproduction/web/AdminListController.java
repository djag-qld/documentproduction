package au.gov.qld.bdm.documentproduction.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.RedirectView;

public abstract class AdminListController {

	private final boolean secure;

	public AdminListController(boolean secure) {
		this.secure = secure;
	}
	
	protected RedirectView toggleAndRedirect(boolean hideInactive, HttpServletResponse response) {
		response.addCookie(newCookie("hideInactive", String.valueOf(!hideInactive)));
		return redirectToList();
	}

	protected RedirectView redirectToList() {
		RedirectView redirectView = new RedirectView(getBase());
		redirectView.setExposeModelAttributes(false);
		redirectView.setExposePathVariables(false);
		return redirectView;
	}

	protected abstract String getBase();

	private Cookie newCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(secure);
		return cookie;
	}

}
